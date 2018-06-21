package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.uploadOtaFile;

import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeService;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.RequestFileUtil;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckHexNumber;
import com.st.BlueSTSDK.gui.util.InputChecker.CheckNumberRange;
import com.st.BlueSTSDK.gui.util.SimpleFragmentDialog;


public class UploadOtaFileFragment extends Fragment implements UploadOtaFileActionReceiver.UploadFinishedListener{

    private static final String NODE_PARAM = UploadOtaFileFragment.class.getCanonicalName()+".NODE_PARAM";
    private static final String FILE_PARAM = UploadOtaFileFragment.class.getCanonicalName()+".FILE_PARAM";
    private static final String ADDRESS_PARAM = UploadOtaFileFragment.class.getCanonicalName()+".ADDRESS_PARAM";

    private static final int MIN_MEMORY_ADDRESS = 0x6000;
    private static final int MAX_MEMORY_ADDRESS = 0xFFFF;

    public static UploadOtaFileFragment build(@NonNull Node node, @Nullable Uri file,@Nullable Long address){
        Bundle args = new Bundle();
        args.putString(NODE_PARAM,node.getTag());
        if(file!=null)
            args.putParcelable(FILE_PARAM,file);
        if(address!=null)
            args.putLong(ADDRESS_PARAM,address);

        UploadOtaFileFragment f = new UploadOtaFileFragment();
        f.setArguments(args);
        return f;
    }

    public UploadOtaFileFragment() {
        // Required empty public constructor
    }

    private Node mNode;
    private RequestFileUtil mRequestFile;
    private View mRootView;
    private TextView mFileNameText;
    private ProgressBar mUploadProgress;
    private TextView mUploadMessage;
    private Uri mSelectedFw;
    private TextView mAddressText;
    private View mProgressViewGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mRootView =  inflater.inflate(R.layout.fragment_upload_ota_file, container, false);

        mFileNameText = mRootView.findViewById(R.id.otaUpload_selectFileName);
        mProgressViewGroup = mRootView.findViewById(R.id.otaUpload_uploadProgressGroup);
        mUploadProgress = mRootView.findViewById(R.id.otaUpload_uploadProgress);
        mUploadMessage = mRootView.findViewById(R.id.otaUpload_uploadMessage);
        mAddressText = mRootView.findViewById(R.id.otaUpload_addressText);

        setupSelectFileButton(mRootView.findViewById(R.id.otaUpload_selectFileButton));
        setupStartUploadButton(mRootView.findViewById(R.id.otaUpload_startUploadButton));
        setupAddressText(mAddressText,mRootView.findViewById(R.id.otaUpload_addressTextLayout));
        mRequestFile = new RequestFileUtil(this,mRootView);
        return  mRootView;
    }

    private void setupAddressText(TextView addressText, TextInputLayout addressLayout) {
        addressText.addTextChangedListener(new CheckHexNumber(addressLayout,R.string.otaUpload_invalidHex));
        addressText.addTextChangedListener(new CheckNumberRange(addressLayout,R.string.otaUpload_invalidMemoryAddress,
                MIN_MEMORY_ADDRESS, MAX_MEMORY_ADDRESS));

        Bundle args = getArguments();
        long address= MAX_MEMORY_ADDRESS;
        if(args!=null) {
            address = getArguments().getLong(ADDRESS_PARAM, MIN_MEMORY_ADDRESS);

        }
        addressText.setText("0x"+Long.toHexString(address));

    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        Uri file = args.getParcelable(FILE_PARAM);
        onFileSelected(file);

        mNode = Manager.getSharedInstance().getNodeWithTag(args.getString(NODE_PARAM));

    }

    private BroadcastReceiver mMessageReceiver;

    @Override
    public void onResume() {
        super.onResume();
        mMessageReceiver = new UploadOtaFileActionReceiver(mUploadProgress,mUploadMessage,this);
        // Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver,
                FwUpgradeService.getServiceActionFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(mMessageReceiver);

    }

    private Long getFwAddress(){
        try{
            //clamp
            return Math.max(MIN_MEMORY_ADDRESS,Math.min(Long.decode(mAddressText.getText().toString()),MAX_MEMORY_ADDRESS));
        }catch (NumberFormatException e){
            return null;
        }
    }

    private void setupStartUploadButton(View button) {
        button.setOnClickListener(v -> {
            Long addrres = getFwAddress();
            if(mSelectedFw!=null) {
                if(addrres!=null) {
                    startUploadFile(mSelectedFw, addrres);
                }else{
                    Snackbar.make(mRootView,"Invalid address",Snackbar.LENGTH_SHORT).show();
                }
            }else{
                Snackbar.make(mRootView,"Invalid file",Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void startUploadFile(@NonNull Uri selectedFile,long address) {
        FwUpgradeService.startUploadService(requireContext(),mNode,selectedFile,address);
        mProgressViewGroup.setVisibility(View.VISIBLE);
    }

    private void setupSelectFileButton(View button) {
        button.setOnClickListener(v -> mRequestFile.openFileSelector());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        onFileSelected(mRequestFile.onActivityResult(requestCode,resultCode,data));
    }

    private void onFileSelected(@Nullable Uri fwFile){
        if(fwFile==null)
            return;
        mSelectedFw = fwFile;
        mFileNameText.setText(RequestFileUtil.getFileName(requireContext(),fwFile));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mRequestFile.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }//onRequestPermissionsResult

    @Override
    public void onUploadFinished(float time_s) {
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(getString(R.string.otaUpload_finished,time_s));
        dialog.setOnclickListener((dialog1, which) -> {
            //UploadOtaFileFragment.this
            NavUtils.navigateUpTo(requireActivity(),NavUtils.getParentActivityIntent(requireActivity()));
        });
        dialog.show(getFragmentManager(),"finisDialog");
    }

}
