package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.searchOtaNode;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.util.FragmentUtil;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnOtaNodeSearchCallback} interface
 * to handle interaction events.
 */
public class SearchOtaNodeFragment extends Fragment implements SearchOtaNodeContract.View {

    private OnOtaNodeSearchCallback mListener;

    private static final String SEARCH_ADDRESS_PARAM = SearchOtaNodeFragment.class.getCanonicalName()+".SEARCH_ADDRESS_PARAM";

    public static SearchOtaNodeFragment instanciate(@Nullable String searchAddress){
        SearchOtaNodeFragment f = new SearchOtaNodeFragment();

        if(searchAddress!=null) {
            Bundle args = new Bundle();
            args.putString(SEARCH_ADDRESS_PARAM, searchAddress);
            f.setArguments(args);
        }

        return f;
    }

    public static SearchOtaNodeFragment instanciate(){
        return instanciate(null);
    }

    public SearchOtaNodeFragment() {
        // Required empty public constructor
    }

    private TextView mMessage;
    private SearchOtaNodeContract.Presenter mPresenter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_search_ota_node, container, false);
        mMessage = root.findViewById(R.id.otaSearch_message);
        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnOtaNodeSearchCallback) {
            mListener = (OnOtaNodeSearchCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnOtaNodeSearchCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    private @Nullable String getSearchNodeAddress(){
        Bundle args = getArguments();
        if(args==null)
            return null;
        return args.getString(SEARCH_ADDRESS_PARAM,null);
    }

    @Override
    public void onStart() {
        super.onStart();

        String address = getSearchNodeAddress();

        mPresenter = new SearchOtaNodePresenter(this, Manager.getSharedInstance());
        mPresenter.startScan(address);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.stopScan();
    }


    private void changeMessageText(@StringRes int message){
        FragmentUtil.runOnUiThread(this,()-> mMessage.setText(message));
    }

    @Override
    public void startScan() {
       changeMessageText(R.string.otaSearch_scanStart);
    }

    @Override
    public void foundNode(@NonNull Node node) {
        changeMessageText(R.string.otaSearch_nodeFound);
        mListener.onOtaNodeFound(node);
    }

    @Override
    public void nodeNodeFound() {
        changeMessageText(R.string.otaSearch_nodeNotFound);
    }


    public interface OnOtaNodeSearchCallback {
        void onOtaNodeFound(@NonNull Node node);
    }
}
