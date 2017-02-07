/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.BlueSTSDK.gui.fwUpgrade;

import android.Manifest;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.ActivityWithNode;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwUpgradeConsole;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwVersionBoard;
import com.st.BlueSTSDK.gui.util.AlertAndFinishDialog;

/**
 * Activity where the user can see the current firware name and version and upload a new firmware
 */
public class FwUpgradeActivity extends ActivityWithNode {

    private static final FwVersionBoard MIN_COMPATIBILITY_VERSION[] = new FwVersionBoard[]{
            new FwVersionBoard("BLUEMICROSYSTEM2","",2,0,1)
    };

    private static final int CHOOSE_BOARD_FILE_REQUESTCODE=1;

    private static final String FRAGMENT_DIALOG_TAG = "Dialog";

    private static final String VERSION = FwUpgradeActivity.class.getName()+"FW_VERSION";
    private static final String FINAL_MESSAGE = FwUpgradeActivity.class.getName()+"FINAL_MESSAGE";
    private static final int RESULT_READ_ACCESS = 2;

    public static Intent getStartIntent(Context c, Node node, boolean keepTheConnectionOpen) {
        return ActivityWithNode.getStartIntent(c,FwUpgradeActivity.class,node,
                keepTheConnectionOpen);
    }

    private View mRootView;
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
                    releaseDialog(mLoadVersionProgressDialog);
                    mLoadVersionProgressDialog=null;
                    if(version==null){
                        displayFwUpgradeNotAvailableAndFinish();
                        return;
                    }
                    if(fwType==FwUpgradeConsole.BOARD_FW) {
                        FwVersionBoard boardVersion =(FwVersionBoard) version;
                        if(checkFwIncompatibility(boardVersion))
                            displayVersion(boardVersion);
                    }
                }
            });
        }

    };

    private void displayFwUpgradeNotAvailableAndFinish() {
        DialogFragment newFragment = AlertAndFinishDialog.newInstance(
                getString(R.string.FwUpgrade_dialogTitle),
                getString(R.string.FwUpgrade_notAvailableMsg), true);
        newFragment.show(getFragmentManager(), FRAGMENT_DIALOG_TAG);

    }

    private boolean checkFwIncompatibility(FwVersionBoard version){
        for(FwVersionBoard knowBoard : MIN_COMPATIBILITY_VERSION){
            if(version.getName().equals(knowBoard.getName())){
                if(version.compareTo(knowBoard)<0){
                    displayNeedNewFwAndFinish(knowBoard);
                    return false;
                }//if
            }//if
        }//for
        return true;
    }

    private void displayNeedNewFwAndFinish(FwVersionBoard minVersion) {

        DialogFragment newFragment = AlertAndFinishDialog.newInstance(
                getString(R.string.FwUpgrade_dialogTitle),
                getString(R.string.FwUpgrade_needUpdateMsg, minVersion), true);
        newFragment.show(getFragmentManager(), FRAGMENT_DIALOG_TAG);
    }

    private void displayNotSupportedAndFinish(){
        DialogFragment newFragment = AlertAndFinishDialog.newInstance(
                getString(R.string.FwUpgrade_dialogTitle),
                getString(R.string.fwUpgrade_notSupportedMsg), true);
        newFragment.show(getFragmentManager(), FRAGMENT_DIALOG_TAG);
    }

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
        }else{
            displayNotSupportedAndFinish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fw_upgrade);
        mRootView = findViewById(R.id.activityFwRootView);
        mVersionBoardText = (TextView) findViewById(R.id.fwVersionValue);
        mBoardTypeText = (TextView) findViewById(R.id.boardTypeValue);
        mFwBoardName =(TextView) findViewById(R.id.fwName);
        mFinalMessage = (TextView) findViewById(R.id.upgradeFinishMessage);

        FloatingActionButton uploadButton = (FloatingActionButton) findViewById(R.id.startUpgradeButton);
        if( uploadButton !=null) {
            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if(checkReadSDPermission())
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
            if(mVersion!=null)
                displayVersion(mVersion);
            mFinalMessage.setText(savedInstanceState.getString(FINAL_MESSAGE,""));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(VERSION, mVersion);
        outState.putString(FINAL_MESSAGE, mFinalMessage.getText().toString());
    }

    private static void releaseDialog(@Nullable Dialog d){
        if(d!=null && d.isShowing()) {
            d.dismiss();
        }
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
                releaseDialog(mFormattingProgressDialog);
                mFormattingProgressDialog=null;
            }
            mUploadFileProgressDialog.show();
            mUploadFileProgressDialog.setProgress((int)uploadBytes);
        }

        private void uploadFinished(float timeS){
            releaseDialog(mUploadFileProgressDialog);
            mUploadFileProgressDialog=null;
            mFinalMessage.setText(String.format(getString(R.string.fwUpgrade_upgradeCompleteMessage),timeS));
        }

        private void uploadError(String msg){
            releaseDialog(mUploadFileProgressDialog);
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
        keepConnectionOpen(true,false);
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

    /**
     * check it we have the permission to write data on the sd
     * @return true if we have it, false if we ask for it
     */
    private boolean checkReadSDPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Snackbar.make(mRootView, R.string.FwUpgrade_readSDRationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(FwUpgradeActivity.this,
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                        RESULT_READ_ACCESS);
                            }//onClick
                        }).show();
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        RESULT_READ_ACCESS);
            }//if-else
            return false;
        }else
            return  true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case RESULT_READ_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startFwUpgrade();
                } else {
                    Snackbar.make(mRootView, "Impossible read Firmware files",
                            Snackbar.LENGTH_SHORT).show();

                }//if-else
                break;
            }//REQUEST_LOCATION_ACCESS
        }//switch
    }//onRequestPermissionsResult

}
