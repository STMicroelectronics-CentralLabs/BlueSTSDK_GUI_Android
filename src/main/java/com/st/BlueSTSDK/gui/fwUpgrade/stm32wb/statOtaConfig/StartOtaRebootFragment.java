package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.statOtaConfig;


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
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.FwUpgradeSTM32WBActivity;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.RequestFileUtil;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature.RebootOTAModeFeature;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNumberRange;
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog;


@DemoDescriptionAnnotation(name="Firmware Upgrade",
        requareAll = {RebootOTAModeFeature.class}
        //inside a lib the R file is not final so you can not set the icon, to do it extend this
        //this class in the main application an set a new annotation
        )
public class StartOtaRebootFragment extends DemoFragment implements StartOtaConfigContract.View {

    private static final byte MIN_DELETABLE_SECTOR = 7;

    private static final String DIALOG_TAG = StartOtaRebootFragment.class.getName()+".rebootDialog";

    private static final class MemoryLayout{
        final short fistSector;
        final short nSector;

        private MemoryLayout(short fistSector, short nSector) {
            this.fistSector = fistSector;
            this.nSector = nSector;
        }
    }

    private static final MemoryLayout APPLICATION_MEMORY = new MemoryLayout((short)0x07,(short) 0xFF);
    private static final MemoryLayout BLE_MEMORY = new MemoryLayout((short)0x07,(short) 0xFF);

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
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
        if(mApplicationMemory.isChecked())
            return APPLICATION_MEMORY.fistSector;
        return Short.parseShort(mSectorTextLayout.getEditText().getText().toString(),16);
    }

    @Override
    public short getNSectorToDelete() {
        if(mApplicationMemory.isChecked())
            return APPLICATION_MEMORY.nSector;
        return Short.parseShort(mLengthTextLayout.getEditText().getText().toString(),16);
    }

    @Override
    public void showConnectionResetWarningDialog() {
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string.otaReboot_rebootDialog);

        dialog.setOnclickListener((dialog1, which) -> mPresenter.onConnectionResetWarningDismiss());

        dialog.show(getChildFragmentManager(),DIALOG_TAG);
    }

    @Override
    public void openFileSelector() {
        mRequestFileUtil.openFileSelector();
    }

    @Override
    public void performFileUpload(@Nullable Uri file) {
        NodeConnectionService.disconnect(requireContext(),getNode());
        long address = sectorToAddress(getSectorToDelete());
        startActivity(FwUpgradeSTM32WBActivity.getStartIntent(requireContext(),null,
                mSelectedFw,address));
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
