package com.st.BlueSTSDK.gui.fwUpgrade;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FwVersionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FwVersionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public FwVersionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FwVersionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FwVersionFragment newInstance(String param1, String param2) {
        FwVersionFragment fragment = new FwVersionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private TextView mName;
    private TextView mVersion;
    private TextView mMcuType;
    private View mContentView;
    private View mLoadingView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView  = inflater.inflate(R.layout.fragment_fw_version, container, false);

        mName = rootView.findViewById(R.id.fwVersion_nameValue);
        mVersion = rootView.findViewById(R.id.fwVersion_versionValue);
        mMcuType = rootView.findViewById(R.id.fwVersion_mcuTypeValue);
        mContentView = rootView.findViewById(R.id.fwVersion_contentView);
        mLoadingView = rootView.findViewById(R.id.fwVersion_loadingView);

        return  rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FwVersionViewModel viewModel =  ViewModelProviders.of(requireActivity()).get(FwVersionViewModel.class);
        viewModel.isWaitingFwVersion().observe(this, isLoading -> {
            if(isLoading!=null && !isLoading){
                mLoadingView.setVisibility(View.GONE);
                mContentView.setVisibility(View.VISIBLE);
            }else{
                mLoadingView.setVisibility(View.VISIBLE);
                mContentView.setVisibility(View.GONE);
            }
        });

        viewModel.getFwVersion().observe(this, fwVersion -> {
            if(fwVersion==null){
                mName.setText(R.string.fwVersion_unknown);
                mVersion.setText(R.string.fwVersion_unknown);
                mMcuType.setText(R.string.fwVersion_unknown);
            }else{
                mVersion.setText(getString(R.string.fwVersion_versionFormat,
                        fwVersion.getMajorVersion(),fwVersion.getMinorVersion(),fwVersion.getPatchVersion()));
                if(fwVersion instanceof FwVersionBoard) {
                    mName.setText(((FwVersionBoard) fwVersion).getName());
                    mMcuType.setText(((FwVersionBoard) fwVersion).getMcuType());
                }else{
                    mVersion.setText(R.string.fwVersion_unknown);
                    mMcuType.setText(R.string.fwVersion_unknown);
                }
            }
        });
    }
}
