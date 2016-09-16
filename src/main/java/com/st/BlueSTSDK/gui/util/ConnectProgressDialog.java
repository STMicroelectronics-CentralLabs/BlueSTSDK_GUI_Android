package com.st.BlueSTSDK.gui.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Toast;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.R;


public class ConnectProgressDialog extends ProgressDialog implements Node.NodeStateListener {

    private Activity mActivity;

    public ConnectProgressDialog(Activity activity, String nodeName) {
        super(activity,ProgressDialog.STYLE_SPINNER);
        setTitle(R.string.progressDialogConnTitle);
        setMessage(String.format(activity.getString(R.string.progressDialogConnMsg), nodeName));
        mActivity=activity;
    }

    @Override
    public void onStateChange(final Node node, Node.State newState, Node.State prevState) {
        //we connect -> hide the dialog
        if(newState == Node.State.Connecting && mActivity!=null)
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    show();
                }
            });
        else if ((newState == Node.State.Connected) && mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            });
            //error state -> show a toast message and start a new connection
        } else if ((newState == Node.State.Unreachable ||
                newState == Node.State.Dead ||
                newState == Node.State.Lost) && mActivity != null) {
            final String msg = getErrorString(newState,node.getName());
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private String getErrorString(Node.State state, String nodeName){
        switch (state) {
            case Dead:
                return String.format(mActivity.getString(R.string.progressDialogConnMsgDeadNodeError),
                        nodeName);
            case Unreachable:
                return String.format(mActivity.getString(R.string.progressDialogConnMsgUnreachableNodeError),
                        nodeName);
            case Lost:
            default:
                return String.format(mActivity.getString(R.string
                                .progressDialogConnMsgLostNodeError),nodeName);
        }//switch
    }

}
