package com.st.BlueSTSDK.gui.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.R;

/**
 * Progress dialog that is displayed while the mobile is connecting with the node
 * you can directly use this class as nodeStateListener for aromatically display/dismiss the dialog
 * when the node state change.
 */
public class ConnectProgressDialog extends ProgressDialog implements Node.NodeStateListener {

    //main thread wher run the command for change the gui
    private Handler mMainThread;

    /**
     * create the dialog
     * @param ctx context where the dialog is displayed
     * @param nodeName node name
     */
    public ConnectProgressDialog(Context ctx, String nodeName) {
        super(ctx,ProgressDialog.STYLE_SPINNER);
        setTitle(R.string.progressDialogConnTitle);
        setMessage(String.format(ctx.getString(R.string.progressDialogConnMsg), nodeName));
        mMainThread = new Handler(ctx.getMainLooper());
    }

    /**
     * change the dialog state in funciton of the node state:
     * show the dialog when the node is connectiong, dismiss the dialog when the node is connected
     * display a toast message when we lost the connection
     * @param node note that change its status
     * @param newState new node status
     * @param prevState previous node status
     */
    @Override
    public void onStateChange(final Node node, Node.State newState, Node.State prevState) {
        //we connect -> hide the dialog
        if(newState == Node.State.Connecting && mMainThread!=null)
            mMainThread.post(new Runnable() {
                @Override
                public void run() {
                    show();
                }
            });
        else if ((newState == Node.State.Connected) && mMainThread != null) {
            mMainThread.post(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            });
            //error state -> show a toast message and start a new connection
        } else if ((newState == Node.State.Unreachable ||
                newState == Node.State.Dead ||
                newState == Node.State.Lost) && mMainThread != null) {
            final String msg = getErrorString(newState,node.getName());
            mMainThread.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private String getErrorString(Node.State state, String nodeName){
        Context ctx = getContext();
        switch (state) {
            case Dead:
                return String.format(ctx.getString(R.string.progressDialogConnMsgDeadNodeError),
                        nodeName);
            case Unreachable:
                return String.format(ctx.getString(R.string.progressDialogConnMsgUnreachableNodeError),
                        nodeName);
            case Lost:
            default:
                return String.format(ctx.getString(R.string
                                .progressDialogConnMsgLostNodeError),nodeName);
        }//switch
    }

}
