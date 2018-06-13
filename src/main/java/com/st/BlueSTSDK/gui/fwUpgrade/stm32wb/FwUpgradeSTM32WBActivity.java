package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.gui.NodeConnectionService;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature.STM32OTASupport;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.searchOtaNode.SearchOtaNodeFragment;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.uploadOtaFile.UploadOtaFileFragment;

public class FwUpgradeSTM32WBActivity extends AppCompatActivity implements SearchOtaNodeFragment.OnOtaNodeSearchCallback {

    private static final String NODE_PARAM = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".NODE_PARAM";
    private static final String FILE_PARAM = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".FILE_PARAM";

    public static Intent getStartIntent(@NonNull Context context, @Nullable Node node, @Nullable Uri file){
        Intent fwUpgradeActivity = new Intent(context, FwUpgradeSTM32WBActivity.class);
        if(node!=null){
            fwUpgradeActivity.putExtra(NODE_PARAM,node.getTag());
        }
        if(file!=null){
            fwUpgradeActivity.putExtra(FILE_PARAM,file);
        }

        return fwUpgradeActivity;
    }

    private Node mNode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fw_upgrade_stm32_wb);

        Intent startIntent = getIntent();
        if(!startIntent.hasExtra(NODE_PARAM)){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.otaSTM32_content,new SearchOtaNodeFragment(),"searchNode")
                    .commit();
        }else{
            Node n = Manager.getSharedInstance().getNodeWithTag(startIntent.getStringExtra(NODE_PARAM));
            onOtaNodeFound(n);
        }

    }

    private void showUploadFileFragment(@NonNull Node node){
        Intent startIntent = getIntent();
        Uri file = startIntent.getParcelableExtra(FILE_PARAM);
        UploadOtaFileFragment fragment = UploadOtaFileFragment.build(node,file);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(getSupportFragmentManager().findFragmentByTag("searchNode")!=null)
            transaction.replace(R.id.otaSTM32_content,fragment);
        else
            transaction.replace(R.id.otaSTM32_content,fragment);
        transaction.commit();
    }

    @Override
    public void onOtaNodeFound(@NonNull Node node) {
        mNode = node;
        ConnectionOption option = ConnectionOption.builder()
                //the node was probably connected with another name and char set so
                // it is better to reset the cache
                .resetCache(true)
                .setFeatureMap(STM32OTASupport.getOTAFeatures())
                .build();
        NodeConnectionService.connect(this,node,option);
        showUploadFileFragment(node);
    }
}
