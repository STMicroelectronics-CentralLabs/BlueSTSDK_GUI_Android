package com.st.BlueSTSDK.gui.fwUpgrade;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
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

    private static final String VERSION = FwUpgradeActivity.class.getName()+"FW_VERSION";
    private static final String FINAL_MESSAGE = FwUpgradeActivity.class.getName()+"FINAL_MESSAGE";

    public static Intent getStartIntent(Context c, Node node, boolean keepTheConnectionOpen) {
        return ActivityWithNode.getStartIntent(c,FwUpgradeActivity.class,node,
                keepTheConnectionOpen);
    }

    private TextView mVersionBoardText;
    private TextView mBoardTypeText;
    private TextView mFwBoardName;
    private TextView mFinalMessage;
    private FwVersionBoard mVersion;

    private Node.NodeStateListener mOnConnected = new Node.NodeStateListener() {
        @Override
        public void onStateChange(Node node, Node.State newState, Node.State prevState) {
            if(newState==Node.State.Connected){
                initFwVersion();
            }
        }
    };

    private void displayVersion(FwVersionBoard version){
        mVersion= version;
        mVersionBoardText.setText(version.toString());
        mBoardTypeText.setText(version.getMcuType());
        mFwBoardName.setText(version.getName());
    }

    private ProgressDialog mLoadVersionProgressDialog;
    
    private FwUpgradeConsole.FwUpgradeCallback mConsoleListener = new FwUpgradeConsole.SimpleFwUpgradeCallback() {

        @Override
        public void onVersionRead(final FwUpgradeConsole console,
                                  @FwUpgradeConsole.FirmwareType final int fwType,
                                  final FwVersion version) {
            FwUpgradeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(fwType==FwUpgradeConsole.BOARD_FW) {
                        displayVersion((FwVersionBoard) version);
                    }
                    mLoadVersionProgressDialog.dismiss();
                    mLoadVersionProgressDialog=null;
                }
            });
        }

    };

    
    private  void initFwVersion(){
        FwUpgradeConsole console = FwUpgradeConsole.getFwUpgradeConsole(mNode);
        if(console !=null) {
            mLoadVersionProgressDialog = new ProgressDialog(this);
            mLoadVersionProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mLoadVersionProgressDialog.setTitle(R.string.fwUpgrade_loading);
            mLoadVersionProgressDialog.setMessage(getString(R.string.fwUpgrade_loadFwVersion));

            console.setLicenseConsoleListener(mConsoleListener);
            console.readVersion(FwUpgradeConsole.BOARD_FW);
            mLoadVersionProgressDialog.show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fw_upgrade);

        mVersionBoardText = (TextView) findViewById(R.id.fwVersionValue);
        mBoardTypeText = (TextView) findViewById(R.id.boardTypeValue);
        mFwBoardName =(TextView) findViewById(R.id.fwName);
        mFinalMessage = (TextView) findViewById(R.id.upgradeFinishMessage);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.startUpgradeButton);
        if( fab!=null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startFwUpgrade();
                }
            });
        }

        if(savedInstanceState==null) {
            if (mNode.isConnected()) {
                initFwVersion();
            } else {
                mNode.addNodeStateListener(mOnConnected);
            }
        }else{
            mVersion= savedInstanceState.getParcelable(VERSION);
            displayVersion(mVersion);
            mFinalMessage.setText(savedInstanceState.getString(FINAL_MESSAGE,""));
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(VERSION,mVersion);
        outState.putString(FINAL_MESSAGE,mFinalMessage.getText().toString());
    }

    private static void releaseDialog(@Nullable Dialog d){
        if(d!=null && d.isShowing())
            d.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNode.removeNodeStateListener(mOnConnected);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        releaseDialog(mUploadFileProgressDialog);
        releaseDialog(mFormattingProgressDialog);
        releaseDialog(mLoadVersionProgressDialog);
    }


    private Intent getFileSelectIntent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
        intent.setType("*/*");
        return intent;
        //Intent i = Intent.createChooser(intent, "Open firmwere file");
    }

    private ProgressDialog mUploadFileProgressDialog;
    private ProgressDialog mFormattingProgressDialog;

    private class FwUpgradeServiceActionReceiver extends BroadcastReceiver
    {

        private Context mContext;


        FwUpgradeServiceActionReceiver(Context c){
            mContext=c;
        }

        private ProgressDialog createUpgradeProgressDialog(Context c){
            ProgressDialog dialog = new ProgressDialog(c);
            dialog.setTitle(R.string.fwUpgrade_uploading);
            dialog.setCancelable(false);
            dialog.setProgressNumberFormat(c.getString(R.string.fwUpgrade_upgradeNumberFormat));
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            return dialog;
        }

        private ProgressDialog createFormatProgressDialog(Context c){
            ProgressDialog dialog = new ProgressDialog(c);
            dialog.setTitle(R.string.fwUpgrade_formatting);
            dialog.setCancelable(false);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            return dialog;
        }

        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if(action.equals(FwUpgradeService.FW_UPLOAD_STARTED_ACTION))
                uploadStarted();
            if(action.equals(FwUpgradeService.FW_UPLOAD_STATUS_UPGRADE_ACTION)){
                long total = intent.getLongExtra(FwUpgradeService
                        .FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE_EXTRA, Integer.MAX_VALUE);
                long upload = intent.getLongExtra(FwUpgradeService
                        .FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE_EXTRA,0);
                upgradeUploadStatus(upload,total);
            }else if(action.equals(FwUpgradeService.FW_UPLOAD_FINISHED_ACTION)){
                float time = intent.getFloatExtra(FwUpgradeService
                        .FW_UPLOAD_FINISHED_TIME_S_EXTRA,0.0f);
                uploadFinished(time);
            }else if(action.equals(FwUpgradeService.FW_UPLOAD_ERROR_ACTION)){
                String message = intent.getStringExtra(FwUpgradeService
                        .FW_UPLOAD_ERROR_MESSAGE_EXTRA);
                uploadError(message);

            }
        }

        private void uploadStarted() {
            mFormattingProgressDialog = createFormatProgressDialog(mContext);
            mFormattingProgressDialog.show();
        }

        private void upgradeUploadStatus(long uploadBytes, long totalBytes){
            if(mUploadFileProgressDialog==null){
                mUploadFileProgressDialog= createUpgradeProgressDialog(mContext);
                mUploadFileProgressDialog.setMax((int)totalBytes);
                if(mFormattingProgressDialog!=null){
                    mFormattingProgressDialog.dismiss();
                    mFormattingProgressDialog=null;
                }
                mUploadFileProgressDialog.show();
            }
            mUploadFileProgressDialog.setProgress((int)uploadBytes);
        }

        private void uploadFinished(float timeS){
            mUploadFileProgressDialog.dismiss();
            mUploadFileProgressDialog=null;
            mFinalMessage.setText(String.format("Upload Completed in: %.2f\nReset the board" +
                    " for apply it",timeS));
        }

        private void uploadError(String msg){
            mUploadFileProgressDialog.dismiss();
            mUploadFileProgressDialog=null;
            mFinalMessage.setText(msg);
        }
    }


    private BroadcastReceiver mMessageReceiver;

    @Override
    public void onResume() {
        super.onResume();
        mMessageReceiver = new FwUpgradeServiceActionReceiver(this);
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                FwUpgradeService.getServiceActionFilter());
    }



    private void startFwUpgrade(){
        keepConnectionOpen(true);
        startActivityForResult(getFileSelectIntent(), CHOOSE_BOARD_FILE_REQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==CHOOSE_BOARD_FILE_REQUESTCODE) {
                Uri file = data.getData();
                if(file!=null)
                    FwUpgradeService.startUploadService(this,getNode(),file);
            }
        }

    }

}
