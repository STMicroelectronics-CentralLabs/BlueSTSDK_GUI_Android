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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
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
public class StartOtaRebootFragment extends DemoFragment implements StartOtaConfigContract.View, AdapterView.OnItemSelectedListener {

    private static final String FIRST_SECTOR_KEY = StartOtaRebootFragment.class.getCanonicalName()+".FIRST_SECTOR_KEY";
    private static final String NUMBER_OF_SECTOR_KEY = StartOtaRebootFragment.class.getCanonicalName()+".NUMBER_OF_SECTOR_KEY";
    private static final String FW_URI_KEY = StartOtaRebootFragment.class.getCanonicalName()+".FW_URI_KEY";

    private int mWB_board = 0; //Default WB board not Defined


    private static final class MemoryLayout{
        final short fistSector;
        final short nSector;
        final short sectorSize;

        private MemoryLayout(short fistSector, short nSector,short sectorSize) {
            this.fistSector = fistSector;
            this.nSector = nSector;
            this.sectorSize = sectorSize;
        }
    }

    // IMPORTANT!!!!!!
    // the code works thinking that the SectorSize of the
    // Application Memory is == to the one for BLE Memory
    // And also that the fistSector is the same for them
    private static final MemoryLayout APPLICATION_MEMORY[] = {
            new MemoryLayout((short)0x00,(short) 0x00,(short) 0x0000 /*   */), //Undef
            new MemoryLayout((short)0x07,(short) 0x7F,(short) 0x1000 /* 4k*/), //WB55
            new MemoryLayout((short)0x0E,(short) 0x24,(short) 0x800  /* 2k*/)  //WB15
    };

    private static final MemoryLayout BLE_MEMORY[] = {
            new MemoryLayout((short)0x000,(short) 0x00,(short) 0x0000 /*   */), //Undef
            new MemoryLayout((short)0x0F,(short) 0x7F,(short) 0x1000  /* 4k*/), //WB55
            new MemoryLayout((short)0x0E,(short) 0x3C,(short) 0x800   /* 2k*/)  //WB15
    };

    private StartOtaConfigContract.Presenter mPresenter;
    private RequestFileUtil mRequestFileUtil;
    private View mCustomAddressView;

    private TextView mSelectedFwName;

    private TextInputLayout mSectorTextLayout;
    private TextInputLayout mLengthTextLayout;
    private TextView mOtaReboot_file;

    private Uri mSelectedFw;

    private TextView mOtaReboot_description;
    private RadioGroup mOtaReboot_fwTypeGroup;
    private Button mSelectFileButton;

    private CompoundButton mApplicationMemory;
    private CompoundButton mBleMemory;


    private FloatingActionButton mOtaRebootFab;

    private TextWatcher watcherSectorTextLayout;
    private TextWatcher watcherLengthTextLayout;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(id!=0) {
            if(mWB_board!=((int) id)) {
                //remove the previous Text watchers
                if(watcherSectorTextLayout!=null) {
                    mSectorTextLayout .getEditText().removeTextChangedListener(watcherSectorTextLayout);
                }
                if(watcherLengthTextLayout!=null) {
                    mLengthTextLayout .getEditText().removeTextChangedListener(watcherLengthTextLayout);
                }
                mSectorTextLayout.getEditText().setText("");
                mLengthTextLayout.getEditText().setText("");

                // Update the board type
                mWB_board=  (int) id;

                //For the moment Disable the wireless binary for WB15
                //Because the firmware could yet not check it
                if(mWB_board==2) {
                    mBleMemory.setEnabled(false);
                    //If it was checked... uncheck it
                    if(mBleMemory.isChecked()) {
                        mBleMemory.setChecked(false);
                    }
                } else {
                    mBleMemory.setEnabled(true);
                }

                //Add the text watcher
                setUpSectorInputChecker(mSectorTextLayout);
                setUpLengthInputChecker(mLengthTextLayout);
            } else {
                mWB_board=  (int) id;
            }

            mOtaReboot_description.setVisibility(View.VISIBLE);
            mOtaReboot_fwTypeGroup.setVisibility(View.VISIBLE);
        } else {
            mWB_board= 0;
            //Toast.makeText(parent.getContext(), "Select a valid WB board", Toast.LENGTH_SHORT).show();
            mOtaReboot_description.setVisibility(View.GONE);
            mOtaReboot_fwTypeGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.fragment_ota_reboot, container, false);

        Spinner mSelectBoardSpinner;
        CompoundButton mCustomMemory;

        mApplicationMemory = mRootView.findViewById(R.id.otaReboot_appMemory);
        mBleMemory = mRootView.findViewById(R.id.otaReboot_bleMemory);
        mCustomMemory = mRootView.findViewById(R.id.otaReboot_customMemory);

        mCustomAddressView = mRootView.findViewById(R.id.otaReboot_customAddrView);
        mSectorTextLayout = mRootView.findViewById(R.id.otaReboot_sectorLayout);
        mLengthTextLayout = mRootView.findViewById(R.id.otaReboot_lengthLayout);
        mSelectedFwName = mRootView.findViewById(R.id.otaReboot_fwFileName);

        mSelectBoardSpinner = mRootView.findViewById(R.id.otaReboot_boardSelection);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.wb_board_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSelectBoardSpinner.setAdapter(adapter);
        mSelectBoardSpinner.setOnItemSelectedListener(this);

        mOtaReboot_description = mRootView.findViewById(R.id.otaReboot_description);
        mOtaReboot_fwTypeGroup = mRootView.findViewById(R.id.otaReboot_fwTypeGroup);

        mSelectFileButton = mRootView.findViewById(R.id.otaReboot_selectFileButton);
        mSelectFileButton.setOnClickListener(v -> mPresenter.onSelectFwFilePressed());

        mRequestFileUtil = new RequestFileUtil(this, mRootView);

        mOtaReboot_file = mRootView.findViewById(R.id.otaReboot_file);

        /* Listener for RadioGroup */
        mApplicationMemory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mSelectFileButton.setVisibility(View.VISIBLE);
        });
        mBleMemory.setOnCheckedChangeListener((buttonView, isChecked) ->{
            mSelectFileButton.setVisibility(View.VISIBLE);
        });
        mCustomMemory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setUpUiForCustomMemory(isChecked);
        });

        mOtaRebootFab =  mRootView.findViewById(R.id.otaReboot_fab);
        mOtaRebootFab.setOnClickListener(v -> mPresenter.onRebootPressed());

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(mSelectedFw!=null){
            UpdateUiForFileSelected(mSelectedFw);
        }
    }

    private void setUpUiForCustomMemory(boolean isChecked) {
        mCustomAddressView.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        mSelectFileButton.setVisibility(View.VISIBLE);
        if(isChecked) {
            setUpSectorInputChecker(mSectorTextLayout);
            setUpLengthInputChecker(mLengthTextLayout);
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        SharedPreferences pref = requireContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        if(mSectorTextLayout.getEditText()!=null) {
            String text = mSectorTextLayout.getEditText().getText().toString();
            editor.putString(FIRST_SECTOR_KEY, text);
        }
        if(mLengthTextLayout.getEditText()!=null) {
            String text = mLengthTextLayout.getEditText().getText().toString();
            editor.putString(NUMBER_OF_SECTOR_KEY, text);
        }
        editor.apply();

//        if(mSectorTextLayout.getEditText()!=null) {
//            String text = mSectorTextLayout.getEditText().getText().toString();
//            outState.putString(FIRST_SECTOR_KEY,text);
//        }
//        if(mLengthTextLayout.getEditText()!=null) {
//            String text = mLengthTextLayout.getEditText().getText().toString();
//            outState.putString(NUMBER_OF_SECTOR_KEY, text);
//        }
        if(mSelectedFw!=null){
            outState.putParcelable(FW_URI_KEY, mSelectedFw);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        SharedPreferences pref = requireContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        String firstSector = pref.getString(FIRST_SECTOR_KEY, null);
        if(firstSector!=null) {
            mSectorTextLayout.getEditText().setText(firstSector);
        }
        String numberSector = pref.getString(NUMBER_OF_SECTOR_KEY, null);
        if(numberSector!=null) {
            mLengthTextLayout.getEditText().setText(numberSector);
        }

//        if(savedInstanceState == null)
//            return;
//        if(savedInstanceState.containsKey(FIRST_SECTOR_KEY) &&
//            mSectorTextLayout.getEditText()!=null){
//            mSectorTextLayout.getEditText().setText(savedInstanceState.getString(FIRST_SECTOR_KEY));
//        }
//        if(savedInstanceState.containsKey(NUMBER_OF_SECTOR_KEY) &&
//                mLengthTextLayout.getEditText()!=null){
//            mLengthTextLayout.getEditText().setText(savedInstanceState.getString(NUMBER_OF_SECTOR_KEY));
//        }
        if(savedInstanceState!=null) {
            if (savedInstanceState.containsKey(FW_URI_KEY)) {
                UpdateUiForFileSelected(savedInstanceState.getParcelable(FW_URI_KEY));
            }
        }
    }

    private void UpdateUiForFileSelected(Uri fileSelected) {
        mSelectedFw = fileSelected;
        String fileName = RequestFileUtil.getFileName(requireContext(),fileSelected);
        mSelectedFwName.setVisibility(View.VISIBLE);
        mOtaReboot_file.setVisibility(View.VISIBLE);
        mSelectedFwName.setText(fileName);
        mOtaRebootFab.setEnabled(true);
    }

    //Checks the Number of sectors delete
    private void setUpLengthInputChecker(TextInputLayout lengthTextLayout) {
        EditText text = lengthTextLayout.getEditText();
        if(text!=null) {
            //we make the test taking the bigger between BLE and Application Memory
            short maxValue = (short) Math.max(BLE_MEMORY[mWB_board].nSector,APPLICATION_MEMORY[mWB_board].nSector);
            watcherSectorTextLayout = new CheckNumberRange(lengthTextLayout, R.string.otaReboot_lengthOutOfRange, 0x00, maxValue);
            text.addTextChangedListener(watcherSectorTextLayout);
        }
    }

    //Checks the first sector to delete
    private void setUpSectorInputChecker(TextInputLayout sectorTextLayout) {
        EditText text = sectorTextLayout.getEditText();
        if(text!=null) {
            //The code works thinking that the first sector is the same for both Application and BLE memory
            short maxValue = (short) Math.max(BLE_MEMORY[mWB_board].nSector,APPLICATION_MEMORY[mWB_board].nSector);
            watcherLengthTextLayout = new CheckNumberRange(sectorTextLayout, R.string.otaReboot_sectorOutOfRange,
                    APPLICATION_MEMORY[mWB_board].fistSector,
                    APPLICATION_MEMORY[mWB_board].fistSector+maxValue);

            text.addTextChangedListener(watcherLengthTextLayout);
        }
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
            return APPLICATION_MEMORY[mWB_board].fistSector;
        if(mBleMemory.isChecked() || mSectorTextLayout.getEditText() == null){
            return BLE_MEMORY[mWB_board].fistSector;
        }
        try {
            return Short.parseShort(mSectorTextLayout.getEditText().getText().toString(), 10);
        }catch (NumberFormatException e){
            return APPLICATION_MEMORY[mWB_board].fistSector;
        }

    }

    @Override
    public short getNSectorToDelete() {

        // Fixed Number
//        if(mApplicationMemory.isChecked() || mLengthTextLayout.getEditText() == null)
//            return APPLICATION_MEMORY[mWB_board].nSector;
//        if(mBleMemory.isChecked() || mLengthTextLayout.getEditText() == null)
//            return BLE_MEMORY[mWB_board].nSector;
//        try{
//            return Short.parseShort(mLengthTextLayout.getEditText().getText().toString(),10);
//        }catch (NumberFormatException e){
//            return APPLICATION_MEMORY[mWB_board].nSector;
//        }

        //Variable Number taking into account also the File size
        //File Size in Bytes
        long FileDimension = RequestFileUtil.getFileSize(requireContext(),mSelectedFw);
        //File Size in Sectors
        //IMPORTANT the code works only if the sector sze of APPLICATION_MEMORY== BLE_MEMORY
        short SectorsForFileDimension = (short) ((FileDimension+APPLICATION_MEMORY[mWB_board].sectorSize-1)/(APPLICATION_MEMORY[mWB_board].sectorSize));

        short MaxSectorSelected;

        //Return the Minimum Number
        if(mApplicationMemory.isChecked() || mLengthTextLayout.getEditText() == null) {
            MaxSectorSelected = APPLICATION_MEMORY[mWB_board].nSector;
            return (SectorsForFileDimension < MaxSectorSelected) ? SectorsForFileDimension : MaxSectorSelected;
        }
        if(mBleMemory.isChecked() || mLengthTextLayout.getEditText() == null) {
            MaxSectorSelected = BLE_MEMORY[mWB_board].nSector;
            return (SectorsForFileDimension < MaxSectorSelected) ? SectorsForFileDimension : MaxSectorSelected;
        }

        try{
            MaxSectorSelected =  Short.parseShort(mLengthTextLayout.getEditText().getText().toString(),10);
        } catch (NumberFormatException e){
            MaxSectorSelected =  APPLICATION_MEMORY[mWB_board].nSector;
        }

        return (SectorsForFileDimension < MaxSectorSelected ) ? SectorsForFileDimension : MaxSectorSelected ;

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
                mSelectedFw,address,getSelectedFwType(),mWB_board));
    }

    private long sectorToAddress(short sectorToDelete) {
        // IMPORTANT!!!!!!
        // the code works thinking that the SectorSize of the
        // Application Memory is == to the one for BLE Memory
        // so it uses the BLE_MEMORY one for both the programs
        return sectorToDelete*BLE_MEMORY[mWB_board].sectorSize;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSelectedFw = mRequestFileUtil.onActivityResult(requestCode,resultCode,data);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mRequestFileUtil.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }//onRequestPermissionsResult

}
