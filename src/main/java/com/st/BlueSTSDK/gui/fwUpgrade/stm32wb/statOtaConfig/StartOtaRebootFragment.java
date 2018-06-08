package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.statOtaConfig;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature.RebootOTAModeFeature;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNumberRange;
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog;

import static android.app.Activity.RESULT_OK;

@DemoDescriptionAnnotation(name="Firmware Upgrade", requareAll = {RebootOTAModeFeature.class})
public class StartOtaRebootFragment extends DemoFragment implements StartOtaConfigContract.View {

    private static final int CHOOSE_BOARD_FILE_REQUESTCODE=1;
    private static final byte MIN_DELETABLE_SECTOR = 7;
    private static final int RESULT_READ_ACCESS = 2;

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
    private View mRootView;
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
        mRootView =  inflater.inflate(R.layout.fragment_ota_reboot, container, false);

        mApplicationMemory = mRootView.findViewById(R.id.otaReboot_appMemory);
        mBleMemory = mRootView.findViewById(R.id.otaReboot_bleMemory);
        mCustomMemory = mRootView.findViewById(R.id.otaReboot_customMemory);

        mCustomAddressView = mRootView.findViewById(R.id.otaReboot_customAddrView);
        mSectorTextLayout = mRootView.findViewById(R.id.otaReboot_sectorLayout);
        mLengthTextLayout = mRootView.findViewById(R.id.otaReboot_lengthLayout);
        mSelectedFwName = mRootView.findViewById(R.id.otaReboot_fwFileName);

        setUpSectorInputChecker(mSectorTextLayout);
        setUpLengthInputChecker(mLengthTextLayout);
        setUpCastomMemorySelection();
        setUpSelectFileButton(mRootView.findViewById(R.id.otaReboot_selectFileButton));
        setUpFab(mRootView);
        return  mRootView;
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

    private void setUpCastomMemorySelection(){
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

        dialog.setOnclickLstener((dialog1, which) -> mPresenter.onConnectionResetWarningDismiss());

        dialog.show(getChildFragmentManager(),DIALOG_TAG);
    }

    @Override
    public void openFileSelector() {
        if(checkReadSDPermission()) {
            startActivityForResult(getFileSelectIntent(), CHOOSE_BOARD_FILE_REQUESTCODE);
        }
    }

    @Override
    public void performFileUpload(@Nullable Uri file) {
        NodeConnectionService.disconnect(requireContext(),getNode());

    }

    private Intent getFileSelectIntent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return intent;
        //Intent i = Intent.createChooser(intent, "Open firmwere file");
    }


    private @Nullable String getFileName(@Nullable Uri uri) {
        if(uri ==null)
            return null;
        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            return uri.getLastPathSegment();
        }
        if (scheme.equals("content")) {
            Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
                return fileName;
            }
        }
        return null;
    }

    private void onFileSelected(@Nullable Uri fwFile){
        mSelectedFw = fwFile;
        mSelectedFwName.setText(getFileName(fwFile));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==CHOOSE_BOARD_FILE_REQUESTCODE) {
                onFileSelected(data.getData());
            }
        }
    }

    /**
     * check it we have the permission to write data on the sd
     * @return true if we have it, false if we ask for it
     */
    private boolean checkReadSDPermission(){
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //onClick
                Snackbar.make(mRootView, R.string.FwUpgrade_readSDRationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, view -> requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                RESULT_READ_ACCESS)).show();
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(
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
                    startActivityForResult(getFileSelectIntent(), CHOOSE_BOARD_FILE_REQUESTCODE);
                } else {
                    Snackbar.make(mRootView, R.string.FwUpgrade_permissionDenied,
                            Snackbar.LENGTH_SHORT).show();

                }//if-else
                break;
            }//REQUEST_LOCATION_ACCESS
        }//switch
    }//onRequestPermissionsResult

}
