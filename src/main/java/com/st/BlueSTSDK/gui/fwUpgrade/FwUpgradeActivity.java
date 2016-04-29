package com.st.BlueSTSDK.gui.fwUpgrade;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.ActivityWithNode;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwUpgradeConsole;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwVersion;

public class FwUpgradeActivity extends ActivityWithNode {

    private static final String FW_FILE=FwUpgradeActivity.class.getCanonicalName()+".FwFile";

    private static final int CHOOSE_FILE_REQUESTCODE=1;


    public static Intent getStartIntent(Context c, Node node, boolean keepTheConnectionOpen) {
        return ActivityWithNode.getStartIntent(c,FwUpgradeActivity.class,node,
                keepTheConnectionOpen);
    }

    private Uri mFwFile;

    private TextView mVersionText;
    private TextView mFwFileText;

    private Node.NodeStateListener mOnConnected = new Node.NodeStateListener() {
        @Override
        public void onStateChange(Node node, Node.State newState, Node.State prevState) {
            if(newState==Node.State.Connected){
                initFwVersion();
            }
        }
    };

    private FwUpgradeConsole mConsole;
    private FwUpgradeConsole.FwUpgradeCallback mConsoleListener = new FwUpgradeConsole.FwUpgradeCallback() {
        @Override
        public void onVersionRead(FwUpgradeConsole console, final FwVersion version) {
            FwUpgradeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVersionText.setText(version.toString());
                }
            });
        }

        @Override
        public void onLoadFwComplete(FwUpgradeConsole console, Uri fwFile, boolean status) {

        }

        @Override
        public void onLoadFwProgresUpdate(FwUpgradeConsole console, Uri fwFile, final long loadBytes) {
            FwUpgradeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mVersionText.setText("load: "+loadBytes);
                }
            });
        }
    };

    private  void initFwVersion(){
        mConsole = FwUpgradeConsole.getFwUpgradeConsole(mNode);
        if(mConsole!=null) {
            mConsole.setLicenseConsoleListener(mConsoleListener);
            mConsole.readVersion();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fw_upgrade);

        mVersionText = (TextView) findViewById(R.id.versionText);
        mFwFileText  = (TextView) findViewById(R.id.fwFileText);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.startUpgradeButton);
        if( fab!=null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startFwUpgrade();
                }
            });
        }

        if(mNode.isConnected()){
            initFwVersion();
        }else{
            mNode.addNodeStateListener(mOnConnected);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mNode.removeNodeStateListener(mOnConnected);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mFwFile = savedInstanceState.getParcelable(FW_FILE);
        if(mFwFile!=null)
            mFwFileText.setText(mFwFile.getPath());
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putParcelable(FW_FILE,mFwFile);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    public void onSelectFileClick(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
        intent.setType("*/*");
        Intent i = Intent.createChooser(intent, "Open firmwere file");

        keepConnectionOpen(true);

        startActivityForResult(i, CHOOSE_FILE_REQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFwFile=data.getData();
        mFwFileText.setText(mFwFile.getPath());
    }

    private void startFwUpgrade(){
        if(mConsole!=null){
            mConsole.loadFw(mFwFile);
        }
    }


}
