package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.searchOtaNode;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnOtaNodeSearchCallback} interface
 * to handle interaction events.
 */
public class SearchOtaNodeFragment extends Fragment implements SearchOtaNodeContract.View {

    private OnOtaNodeSearchCallback mListener;

    public SearchOtaNodeFragment() {
        // Required empty public constructor
    }

    private ProgressBar mProgress;
    private TextView mMessage;
    private SearchOtaNodeContract.Presenter mPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_search_ota_node, container, false);
        mMessage = root.findViewById(R.id.otaSearch_message);
        mProgress = root.findViewById(R.id.otaSearch_progress);
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


    @Override
    public void onStart() {
        super.onStart();
        mPresenter = new SearchOtaNodePresenter(this, Manager.getSharedInstance());
        mPresenter.startScan();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPresenter.stopScan();
    }

    @Override
    public void startScan() {
        getActivity().runOnUiThread(() -> mMessage.setText("scan started"));
    }

    @Override
    public void foundNode(Node node) {
        getActivity().runOnUiThread(() -> mMessage.setText("node Found"));
        mListener.onOtaNodeFound(node);
    }

    @Override
    public void nodeNodeFound() {
        getActivity().runOnUiThread(() -> mMessage.setText("node not found"));
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnOtaNodeSearchCallback {

        void onOtaNodeFound(@NonNull Node node);
    }
}
