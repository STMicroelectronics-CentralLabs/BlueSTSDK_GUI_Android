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
package com.st.STM32WB.fwUpgrade.statOtaConfig;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.NodeConnectionService;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.STM32WB.fwUpgrade.FwUpgradeSTM32WBActivity;
import com.st.BlueSTSDK.gui.fwUpgrade.RequestFileUtil;
import com.st.STM32WB.fwUpgrade.feature.RebootOTAModeFeature;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNumberRange;
import com.st.STM32WB.fwUpgrade.feature.STM32OTASupport;


@DemoDescriptionAnnotation(name="Firmware Upgrade",
        requareAll = {RebootOTAModeFeature.class}
        //inside a lib the R file is not final so you can not set the icon, to do it extend this
        //this class in the main application an set a new annotation
        )
public class StartOtaRebootFragment extends DemoFragment implements StartOtaConfigContract.View {

    private static final String FIRST_SECTOR_KEY = StartOtaRebootFragment.class.getCanonicalName()+".FIRST_SECTOR_KEY";
    private static final String NUMBER_OF_SECTOR_KEY = StartOtaRebootFragment.class.getCanonicalName()+".NUMBER_OF_SECTOR_KEY";
    private static final String FW_URI_KEY = StartOtaRebootFragment.class.getCanonicalName()+".FW_URI_KEY";

    private static final byte MIN_DELETABLE_SECTOR = 7;

    private static final class MemoryLayout{
        final short fistSector;
        final short nSector;

        private MemoryLayout(short fistSector, short nSector) {
            this.fistSector = fistSector;
            this.nSector = nSector;
        }
    }

    private static final MemoryLayout APPLICATION_MEMORY = new MemoryLayout((short)0x07,(short) 0x7F);
    private static final MemoryLayout BLE_MEMORY = new MemoryLayout((short)0x0F,(short) 0x7F);

    private StartOtaConfigContract.Presenter mPresenter;
    private RequestFileUtil mRequestFileUtil;
    private CompoundButton mApplicationMemory;
    private CompoundButton mBleMemory;
    private CompoundButton mCustomMemory;
    private View mCustomAddressView;

    private TextView mSelectedFwName;

    private TextInputLayout mSectorTextLayout;
    private TextInputLayout mLengthTextLayout;

    private Uri mSelectedFw;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_ota_reboot, container, false);

        mApplicationMemory = mRootView.findViewById(R.id.otaReboot_appMemory);
        mBleMemory = mRootView.findViewById(R.id.otaReboot_bleMemory);
        mCustomMemory = mRootView.findViewById(R.id.otaReboot_customMemory);

        mCustomAddressView = mRootView.findViewById(R.id.otaReboot_customAddrView);
        mSectorTextLayout = mRootView.findViewById(R.id.otaReboot_sectorLayout);
        mLengthTextLayout = mRootView.findViewById(R.id.otaReboot_lengthLayout);
        mSelectedFwName = mRootView.findViewById(R.id.otaReboot_fwFileName);

        setUpSectorInputChecker(mSectorTextLayout);
        setUpLengthInputChecker(mLengthTextLayout);
        setUpCustomMemorySelection();
        setUpSelectFileButton(mRootView.findViewById(R.id.otaReboot_selectFileButton));
        setUpFab(mRootView);
        mRequestFileUtil = new RequestFileUtil(this, mRootView);
        return mRootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mSectorTextLayout.getEditText()!=null) {
            String text = mSectorTextLayout.getEditText().getText().toString();
            outState.putString(FIRST_SECTOR_KEY,text);
        }
        if(mLengthTextLayout.getEditText()!=null) {
            String text = mLengthTextLayout.getEditText().getText().toString();
            outState.putString(NUMBER_OF_SECTOR_KEY, text);
        }
        if(mSelectedFw!=null){
            outState.putParcelable(FW_URI_KEY, mSelectedFw);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState == null)
            return;
        if(savedInstanceState.containsKey(FIRST_SECTOR_KEY) &&
            mSectorTextLayout.getEditText()!=null){
            mSectorTextLayout.getEditText().setText(savedInstanceState.getString(FIRST_SECTOR_KEY));
        }
        if(savedInstanceState.containsKey(NUMBER_OF_SECTOR_KEY) &&
                mLengthTextLayout.getEditText()!=null){
            mLengthTextLayout.getEditText().setText(savedInstanceState.getString(NUMBER_OF_SECTOR_KEY));
        }
        if(savedInstanceState.containsKey(FW_URI_KEY)){
            onFileSelected(savedInstanceState.getParcelable(FW_URI_KEY));
        }
    }

    private void setUpLengthInputChecker(TextInputLayout lengthTextLayout) {
        EditText text = lengthTextLayout.getEditText();
        if(text!=null) {
           // text.addTextChangedListener(new CheckHexNumber(lengthTextLayout, R.string.otaReboot_notHexError));
            text.addTextChangedListener(new CheckNumberRange(lengthTextLayout, R.string.otaReboot_lengthOutOfRange,0,0xff));
        }
    }

    private void setUpSelectFileButton(Button selectFileButton){
        selectFileButton.setOnClickListener(v -> mPresenter.onSelectFwFilePressed());
    }

    private void setUpSectorInputChecker(TextInputLayout sectorTextLayout) {
        EditText text = sectorTextLayout.getEditText();
        if(text!=null) {
          //  text.addTextChangedListener(new CheckHexNumber(sectorTextLayout, R.string.otaReboot_notHexError));
            text.addTextChangedListener(new CheckNumberRange(sectorTextLayout, R.string.otaReboot_sectorOutOfRange,MIN_DELETABLE_SECTOR,0xff));
        }

    }

    private void setUpCustomMemorySelection(){
        mCustomMemory.setOnCheckedChangeListener((buttonView, isChecked) ->
                mCustomAddressView.setVisibility(isChecked ? View.VISIBLE : View.GONE));
    }


    private void setUpFab(View root){
        root.findViewById(R.id.otaReboot_fab).setOnClickListener(v -> mPresenter.onRebootPressed());
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {

        RebootOTAModeFeature feature = node.getFeature(RebootOTAModeFeature.class);

        mPresenter = new StartOtaRebootPresenter(this,feature);
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {

    }

    @Override
    public short getSectorToDelete() {
        if(mApplicationMemory.isChecked() || mSectorTextLayout.getEditText() == null)
            return APPLICATION_MEMORY.fistSector;
        if(mBleMemory.isChecked() || mSectorTextLayout.getEditText() == null){
            return BLE_MEMORY.fistSector;
        }
        try {
            return Short.parseShort(mSectorTextLayout.getEditText().getText().toString(), 10);
        }catch (NumberFormatException e){
            return APPLICATION_MEMORY.fistSector;
        }

    }

    @Override
    public short getNSectorToDelete() {
        if(mApplicationMemory.isChecked() || mLengthTextLayout.getEditText() == null)
            return APPLICATION_MEMORY.nSector;
        if(mBleMemory.isChecked() || mLengthTextLayout.getEditText() == null)
            return BLE_MEMORY.nSector;
        try{
            return Short.parseShort(mLengthTextLayout.getEditText().getText().toString(),10);
        }catch (NumberFormatException e){
            return APPLICATION_MEMORY.nSector;
        }

    }

    private @FirmwareType int getSelectedFwType(){
        if(mBleMemory.isChecked()){
            return FirmwareType.BLE_FW;
        }else{
            return FirmwareType.BOARD_FW;
        }
    }

    @Override
    public void openFileSelector() {
        mRequestFileUtil.openFileSelector();
    }

    @Override
    public void performFileUpload() {
        Node n = getNode();
        if(n == null)
            return;

        NodeConnectionService.disconnect(requireContext(),getNode());
        long address = sectorToAddress(getSectorToDelete());
        startActivity(FwUpgradeSTM32WBActivity.getStartIntent(requireContext(),
                STM32OTASupport.getOtaAddressForNode(getNode()),
                mSelectedFw,address,getSelectedFwType()));
    }

    private long sectorToAddress(short sectorToDelete) {
        return sectorToDelete*0x1000;
    }

    private void onFileSelected(@Nullable Uri fwFile){
        mSelectedFw = fwFile;
        mSelectedFwName.setText(RequestFileUtil.getFileName(requireContext(),fwFile));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri selectedFile = mRequestFileUtil.onActivityResult(requestCode,resultCode,data);
        onFileSelected(selectedFile);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mRequestFileUtil.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }//onRequestPermissionsResult

}
