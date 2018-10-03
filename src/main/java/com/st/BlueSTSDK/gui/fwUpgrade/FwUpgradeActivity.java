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

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.ActivityWithNode;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionConsole;
import com.st.BlueSTSDK.gui.util.AlertAndFinishDialog;
import com.st.BlueSTSDK.gui.util.DialogUtil;

/**
 * Activity where the user can see the current firware name and version and upload a new firmware
 */
public class FwUpgradeActivity extends ActivityWithNode {

    private static final FwVersionBoard MIN_COMPATIBILITY_VERSION[] = new FwVersionBoard[]{
            new FwVersionBoard("BLUEMICROSYSTEM2","",2,0,1)
    };

    private static final String FRAGMENT_DIALOG_TAG = "Dialog";

    private static final String VERSION = FwUpgradeActivity.class.getName()+"FW_VERSION";
    private static final String FINAL_MESSAGE = FwUpgradeActivity.class.getName()+"FINAL_MESSAGE";
    private static final String EXTRA_FW_TO_LOAD = FwUpgradeActivity.class.getName()+"EXTRA_FW_TO_LOAD";

    /**
     * crate the start intent for this activity
     * @param c context to use
     * @param node node where upload the fw
     * @param keepTheConnectionOpen keep the connection open when the activity ends
     * @return intent to start the activity
     */
    public static Intent getStartIntent(Context c, Node node, boolean keepTheConnectionOpen) {
        return ActivityWithNode.getStartIntent(c,FwUpgradeActivity.class,node,
                keepTheConnectionOpen);
    }

    public static Intent getStartIntent(Context c, Node node, boolean keepTheConnectionOpen, @Nullable ConnectionOption option) {
        return ActivityWithNode.getStartIntent(c,FwUpgradeActivity.class,node,
                keepTheConnectionOpen,option);
    }

    /**
     * crate the start intent for this activity
     * @param c context to use
     * @param node node where upload the fw
     * @param keepTheConnectionOpen keep the connection open when the activity ends
     * @param fwLocation local file to upload on the node
     * @return intent to start this activity and automaticaly start uploading the file
     */
    public static Intent getStartIntent(Context c, Node node, boolean keepTheConnectionOpen,
                                        Uri fwLocation) {
        Intent intent = getStartIntent(c,node,keepTheConnectionOpen);
        intent.putExtra(EXTRA_FW_TO_LOAD,fwLocation);
        return intent;
    }

    private TextView mVersionBoardText;
    private TextView mBoardTypeText;
    private TextView mFwBoardName;
    private TextView mFinalMessage;
    private FwVersionBoard mVersion;
    private RequestFileUtil mRequestFile;

    private Node.NodeStateListener mOnConnected = (node, newState, prevState) -> {
        if(newState==Node.State.Connected){
            FwUpgradeActivity.this.runOnUiThread(this::initFwVersion);
        }
    };

    private void displayVersion(FwVersionBoard version){
        mVersion= version;
        mVersionBoardText.setText(version.toString());
        mBoardTypeText.setText(version.getMcuType());
        mFwBoardName.setText(version.getName());
    }

    private ProgressDialog mLoadVersionProgressDialog;


    private FwVersionConsole.FwVersionCallback mVersionConsoleListener = new FwVersionConsole.FwVersionCallback() {
        @Override
        public void onVersionRead(FwVersionConsole console,
                                  @FirmwareType final int fwType,
                                  @Nullable FwVersion version) {
            FwUpgradeActivity.this.runOnUiThread(() -> {
                DialogUtil.releaseDialog(mLoadVersionProgressDialog);
                mLoadVersionProgressDialog=null;
                if(version==null){
                    displayFwUpgradeNotAvailableAndFinish();
                    return;
                }
                if(fwType==FirmwareType.BOARD_FW) {
                    FwVersionBoard boardVersion =(FwVersionBoard) version;
                    if(checkFwIncompatibility(boardVersion)) {
                        displayVersion(boardVersion);
                        startExternalFwUpgrade();
                    }
                }
            });
            console.setLicenseConsoleListener(null);
        }
    };

    /**
     * if the intent contains the fw uri, and the permission are in place start the fw upload
     * @return true if the fw upload starts
     */
    private boolean startExternalFwUpgrade() {
        Intent intent = getIntent();
        Node node = getNode();
        if(intent.hasExtra(EXTRA_FW_TO_LOAD) && mRequestFile.checkReadSDPermission() && node!=null){
            Uri fwLocation = intent.getParcelableExtra(EXTRA_FW_TO_LOAD);
            FwUpgradeService.startUploadService(this,node,fwLocation,null);
            return true;
        }//if
        return false;
    }

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
        FwVersionConsole console = FwVersionConsole.getFwVersionConsole(mNode);
        if(console !=null) {
            mLoadVersionProgressDialog = new ProgressDialog(this);
            mLoadVersionProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mLoadVersionProgressDialog.setTitle(R.string.fwUpgrade_loading);
            mLoadVersionProgressDialog.setMessage(getString(R.string.fwUpgrade_loadFwVersion));

            console.setLicenseConsoleListener(mVersionConsoleListener);
            mLoadVersionProgressDialog.show();
            console.readVersion(FirmwareType.BOARD_FW);
        }else{
            displayNotSupportedAndFinish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fw_upgrade);
        View rootView = findViewById(R.id.activityFwRootView);
        mVersionBoardText = findViewById(R.id.fwVersionValue);
        mBoardTypeText = findViewById(R.id.boardTypeValue);
        mFwBoardName = findViewById(R.id.fwName);
        mFinalMessage =  findViewById(R.id.upgradeFinishMessage);

        FloatingActionButton uploadButton = findViewById(R.id.startUpgradeButton);
        if( uploadButton !=null) {
            uploadButton.setOnClickListener(view -> startFwUpgrade());
        }

        if(savedInstanceState!=null) {
            mVersion= savedInstanceState.getParcelable(VERSION);
            if(mVersion!=null)
                displayVersion(mVersion);
            mFinalMessage.setText(savedInstanceState.getString(FINAL_MESSAGE,""));
        }

        mRequestFile = new RequestFileUtil(this, rootView);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(VERSION, mVersion);
        outState.putString(FINAL_MESSAGE, mFinalMessage.getText().toString());
    }



    @Override
    protected void onPause() {
        super.onPause();
        mNode.removeNodeStateListener(mOnConnected);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        mMessageReceiver.releaseDialog();
        DialogUtil.releaseDialog(mLoadVersionProgressDialog);
    }

    private class FwUpgradeServiceActionReceiver extends com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeServiceActionReceiver{

        FwUpgradeServiceActionReceiver(Context c) {
            super(c);
        }

        @Override
        protected void onUploadError(String msg) {
            super.onUploadError(msg);
            mFinalMessage.setText(msg);
        }

        @Override
        protected void onUploadFinished(float timeS) {
            super.onUploadFinished(timeS);
            mFinalMessage.setText(String.format(getString(R.string.fwUpgrade_upgradeCompleteMessage),timeS));
        }
    }


    private FwUpgradeServiceActionReceiver mMessageReceiver;

    @Override
    public void onResume() {
        super.onResume();
        mMessageReceiver = new FwUpgradeServiceActionReceiver(this);
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                FwUpgradeService.getServiceActionFilter());

        if(mVersion==null){
            if (mNode.isConnected()) {
                initFwVersion();
            } else {
                mNode.addNodeStateListener(mOnConnected);
            }
        }

    }

    private void startFwUpgrade(){
        keepConnectionOpen(true,false);
        mRequestFile.openFileSelector();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedFile = mRequestFile.onActivityResult(requestCode, resultCode, data);
        Node node = getNode();
        if (selectedFile != null && node != null) {
            FwUpgradeService.startUploadService(this, node, selectedFile, null);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mRequestFile.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }//onRequestPermissionsResult

}
