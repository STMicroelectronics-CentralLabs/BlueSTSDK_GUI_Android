package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.gui.ConnectionStatusView.ConnectionStatusController;
import com.st.BlueSTSDK.gui.ConnectionStatusView.ConnectionStatusView;
import com.st.BlueSTSDK.gui.NodeConnectionService;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature.STM32OTASupport;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.searchOtaNode.SearchOtaNodeFragment;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.uploadOtaFile.UploadOtaFileFragment;

public class FwUpgradeSTM32WBActivity extends AppCompatActivity implements SearchOtaNodeFragment.OnOtaNodeSearchCallback {

    private static final String NODE_PARAM = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".NODE_PARAM";
    private static final String FILE_PARAM = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".FILE_PARAM";
    private static final String ADDRESS_PARAM = FwUpgradeSTM32WBActivity.class.getCanonicalName()+".ADDRESS_PARAM";

    public static Intent getStartIntent(@NonNull Context context, @Nullable Node node, @Nullable Uri file,
                                        @Nullable Long address){
        Intent fwUpgradeActivity = new Intent(context, FwUpgradeSTM32WBActivity.class);
        if(node!=null){
            fwUpgradeActivity.putExtra(NODE_PARAM,node.getTag());
        }
        if(file!=null){
            fwUpgradeActivity.putExtra(FILE_PARAM,file);
        }
        if(address!=null) {
            fwUpgradeActivity.putExtra(ADDRESS_PARAM, address);
        }

        return fwUpgradeActivity;
    }

    private Node mNode;
    private ConnectionStatusView mConnectionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fw_upgrade_stm32_wb);

        mConnectionStatus = findViewById(R.id.otaStm32_connectionStatus);

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
        Long address = startIntent.hasExtra(ADDRESS_PARAM) ?
                startIntent.getLongExtra(ADDRESS_PARAM,0) : null;
        UploadOtaFileFragment fragment = UploadOtaFileFragment.build(node,file,address);

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

        ConnectionStatusController mConnectionStatusContoller = new ConnectionStatusController(mConnectionStatus, mNode);
        getLifecycle().addObserver(mConnectionStatusContoller);

        ConnectionOption option = ConnectionOption.builder()
                //the node was probably connected with another name and char set so
                // it is better to reset the cache
                .resetCache(true)
                .setFeatureMap(STM32OTASupport.getOTAFeatures())
                .build();
        NodeConnectionService.connect(this,node,option);
        showUploadFileFragment(node);
    }

    /**
     * if we have to leave this activity, we force the disconnection of the node
     */
    @Override
    public void onBackPressed() {
        disconnectNode();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button, we go back in the same task
            //for avoid to recreate the DemoActivity
            case android.R.id.home:
                disconnectNode();
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }//switch

        return super.onOptionsItemSelected(item);
    }//onOptionsItemSelected

    private void disconnectNode() {
        if(mNode!=null && mNode.isConnected()){
            NodeConnectionService.disconnect(this,mNode);
        }
    }

}
