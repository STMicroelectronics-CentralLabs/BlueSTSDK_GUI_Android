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
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.gui.ActivityWithNode;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;
import com.st.BlueSTSDK.gui.fwUpgrade.uploadFwFile.UploadOtaFileFragment;
import com.st.BlueSTSDK.gui.util.AlertAndFinishDialog;

/**
 * Activity where the user can see the current firware name and version and upload a new firmware
 */
public class FwUpgradeActivity extends ActivityWithNode {
    private static final String FRAGMENT_DIALOG_TAG = "Dialog";
    private static final String FRAGMENT_FILE_UPLOAD_TAG = "UploadFileFragment";

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

    private Node.NodeStateListener mOnConnected = (node, newState, prevState) -> {
        Log.d("fwUpgrade","State: "+newState);
        if(newState==Node.State.Connected){
            FwUpgradeActivity.this.mViewModel.loadFwVersionFromNode(node);
        }
    };


    /**
     * if the intent contains the fw uri, and the permission are in place start the fw upload
     * @return true if the fw upload starts
     */
    private boolean startExternalFwUpgrade() {
        Intent intent = getIntent();
        Node node = getNode();
        if(intent.hasExtra(EXTRA_FW_TO_LOAD) && node!=null){
            Uri fwLocation = intent.getParcelableExtra(EXTRA_FW_TO_LOAD);
            addFileUploadFragment(node,fwLocation);
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


    private FwVersionViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fw_upgrade);

        mViewModel = ViewModelProviders.of(this).get(FwVersionViewModel.class);

        mViewModel.getFwVersion().observe(this, fwVersion -> {
            if(fwVersion==null){
                displayFwUpgradeNotAvailableAndFinish();
            }else {
                startExternalFwUpgrade();
            }
        });

        mViewModel.supportFwUpgrade().observe(this, isSupported -> {
            if(isSupported == null)
                return;
            if(!isSupported){
                displayNotSupportedAndFinish();
            }
        });

        mViewModel.requireManualUpdateTo().observe(this, minSupportedVersion -> {
            if(minSupportedVersion == null)
                return;
            displayNeedNewFwAndFinish(minSupportedVersion);
        });


        addFileUploadFragment(mNode,null);
    }

    private void addFileUploadFragment(@NonNull Node node, @Nullable Uri file){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        Fragment prevFragment = fm.findFragmentByTag(FRAGMENT_FILE_UPLOAD_TAG);
        if(prevFragment != null){
            transaction.remove(prevFragment);
        }
        Fragment newFragment = UploadOtaFileFragment.build(node, file, null,false);
        transaction.add(R.id.fwUpgrade_uploadFileFragment,newFragment,FRAGMENT_FILE_UPLOAD_TAG);
        transaction.commit();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (mNode.isConnected()) {
            Log.d("fwUpgrade","directly load version");
            mViewModel.loadFwVersionFromNode(mNode);
        } else {
            Log.d("fwUpgrade","wait connection");
            mNode.addNodeStateListener(mOnConnected);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNode.removeNodeStateListener(mOnConnected);
    }

}
