package com.st.BlueSTSDK.gui.fwUpgrade;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.ActivityWithNode;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwUpgradeConsole;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwVersion;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwVersionBoard;

public class FwUpgradeActivity extends ActivityWithNode {

    private static final int CHOOSE_BOARD_FILE_REQUESTCODE=1;

    public static Intent getStartIntent(Context c, Node node, boolean keepTheConnectionOpen) {
        return ActivityWithNode.getStartIntent(c,FwUpgradeActivity.class,node,
                keepTheConnectionOpen);
    }

    private TextView mVersionBoardText;
    private TextView mBoardTypeText;
    private TextView mFwBoardName;

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

        private long startUploadTime=-1;

        @Override
        public void onVersionRead(final FwUpgradeConsole console,
                                  @FwUpgradeConsole.FirmwareType final int fwType,
                                  final FwVersion version) {
            FwUpgradeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(fwType==FwUpgradeConsole.BOARD_FW) {
                        mVersionBoardText.setText(version.toString());
                        mBoardTypeText.setText(((FwVersionBoard) version).getMcuType());
                        mFwBoardName.setText(((FwVersionBoard) version).getName());
                    }
                    mProgressDialog.dismiss();
                }
            });
        }

        @Override
        public void onLoadFwComplete(FwUpgradeConsole console, Uri fwFile, boolean status) {
            long totalTimeMs = System.currentTimeMillis()-startUploadTime;
            float totalTimeS= totalTimeMs/1000.0f;
            final String newStatus;
            if(status)
                newStatus=String.format("Update done: %.2f",totalTimeS);
            else
                newStatus=String.format("Update Fail: %.2f",totalTimeS);
            FwUpgradeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgressDialog.dismiss();

                }
            });
            startUploadTime=-1;
        }

        private long mFileLength=Long.MAX_VALUE;
        @Override
        public void onLoadFwProgresUpdate(FwUpgradeConsole console, Uri fwFile, final long remainingBytes) {
            if(startUploadTime<0) {
                startUploadTime = System.currentTimeMillis();
                mFileLength=remainingBytes;

            }

            //update the gui only after 128bytes are send, for avoid stress the ui thread
            FwUpgradeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mFileLength!=Long.MAX_VALUE){
                        mProgressDialog.setMax((int)mFileLength);
                    }
                    mProgressDialog.setProgress((int)(mFileLength-remainingBytes));
                }
            });
        }
    };

    private ProgressDialog mProgressDialog;

    private  void initFwVersion(){
        mConsole = FwUpgradeConsole.getFwUpgradeConsole(mNode);
        if(mConsole!=null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setTitle("Loading...");
            mProgressDialog.setMessage("Load firmware version");

            mConsole.setLicenseConsoleListener(mConsoleListener);
            mConsole.readVersion(FwUpgradeConsole.BOARD_FW);
            mProgressDialog.show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fw_upgrade);

        mVersionBoardText = (TextView) findViewById(R.id.fwVersionValue);
        mBoardTypeText = (TextView) findViewById(R.id.boardTypeValue);
        mFwBoardName =(TextView) findViewById(R.id.fwName);

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


    private Intent getFileSelectIntent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
        intent.setType("*/*");
        return intent;
        //Intent i = Intent.createChooser(intent, "Open firmwere file");
    }

    void uploadFwFile(@NonNull Uri file){
        if(mConsole!=null){
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setTitle("Uploading");
            mProgressDialog.setMessage("wait");
            mProgressDialog.setCancelable(false);
            mProgressDialog.setProgressNumberFormat("%1d/%2d Bytes");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mConsole.loadFw(FwUpgradeConsole.BOARD_FW, file);
            mProgressDialog.show();
        }//if
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==CHOOSE_BOARD_FILE_REQUESTCODE) {
                Uri file = data.getData();
                if(file!=null)
                    uploadFwFile(file);
            }
        }

    }

    private void startFwUpgrade(){
        keepConnectionOpen(true);
        startActivityForResult(getFileSelectIntent(), CHOOSE_BOARD_FILE_REQUESTCODE);
    }

}
