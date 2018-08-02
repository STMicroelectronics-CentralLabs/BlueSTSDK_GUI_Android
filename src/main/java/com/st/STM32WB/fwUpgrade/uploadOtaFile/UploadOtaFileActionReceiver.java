package com.st.STM32WB.fwUpgrade.uploadOtaFile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeService;


public class UploadOtaFileActionReceiver extends BroadcastReceiver {


    public interface UploadFinishedListener{
        void onUploadFinished(float time_s);
    }

    private final ProgressBar mProgressBar;
    private final TextView mTextView;
    private final Resources mRes;
    private final UploadFinishedListener mListener;

    public UploadOtaFileActionReceiver(@NonNull ProgressBar progressBar, @NonNull TextView textView,
                                       @NonNull UploadFinishedListener listener) {
        mProgressBar = progressBar;
        mProgressBar.setMax(0);
        mTextView = textView;
        mRes = mTextView.getContext().getResources();
        mListener =listener;
    }

    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if(action==null)
            return;
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
        mTextView.setText(R.string.otaUpload_start);
    }

    protected void onUpgradeUploadStatus(long uploadBytes, long totalBytes){
        if(mProgressBar.getMax()==0){
            mProgressBar.setMax((int)totalBytes);
        }
        mProgressBar.setProgress((int)uploadBytes);

        mTextView.setText(mRes.getString(R.string.otaUpload_status,uploadBytes));
    }

    protected void onUploadFinished(float timeS) {
        mListener.onUploadFinished(timeS);
    }

    protected void onUploadError(String msg){
        mTextView.setText(mRes.getString(R.string.otaUpload_error,msg));
    }

}
