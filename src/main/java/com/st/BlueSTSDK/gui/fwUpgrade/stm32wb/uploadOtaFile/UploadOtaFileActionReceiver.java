package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.uploadOtaFile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeService;


public class UploadOtaFileActionReceiver extends BroadcastReceiver {

    private final ProgressBar mProgressBar;
    private final TextView mTextView;

    public UploadOtaFileActionReceiver(ProgressBar progressBar, TextView textView) {
        mProgressBar = progressBar;
        mProgressBar.setMax(0);
        mTextView = textView;
    }

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d("UploadOtaFileActionReceiver", "onReceive: "+action);
        if(action.equals(FwUpgradeService.FW_UPLOAD_STARTED_ACTION))
            onUploadStarted();
        if(action.equals(FwUpgradeService.FW_UPLOAD_STATUS_UPGRADE_ACTION)){
            long total = intent.getLongExtra(FwUpgradeService
                    .FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE_EXTRA, Integer.MAX_VALUE);
            long upload = intent.getLongExtra(FwUpgradeService
                    .FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE_EXTRA,0);
            onUpgradeUploadStatus(upload,total);
        }else if(action.equals(FwUpgradeService.FW_UPLOAD_FINISHED_ACTION)){
            float timeS = intent.getFloatExtra(FwUpgradeService
                    .FW_UPLOAD_FINISHED_TIME_S_EXTRA,0.0f);
            onUploadFinished(timeS);
        }else if(action.equals(FwUpgradeService.FW_UPLOAD_ERROR_ACTION)){
            String message = intent.getStringExtra(FwUpgradeService
                    .FW_UPLOAD_ERROR_MESSAGE_EXTRA);
            onUploadError(message);
        }
    }

    protected void onUploadStarted() {
        mTextView.setText("Upload Start");

    }

    protected void onUpgradeUploadStatus(long uploadBytes, long totalBytes){
        if(mProgressBar.getMax()==0){
            mProgressBar.setMax((int)totalBytes);
        }
        mProgressBar.setProgress((int)uploadBytes);
        mTextView.setText("Upload running: "+uploadBytes);
    }

    protected void onUploadFinished(float timeS) {
        mTextView.setText("Upload end");
    }

    protected void onUploadError(String msg){
        mTextView.setText("Upload Error:"+msg);
    }

}
