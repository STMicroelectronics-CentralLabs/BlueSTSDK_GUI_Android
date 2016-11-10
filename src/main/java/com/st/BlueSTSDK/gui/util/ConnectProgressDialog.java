/*******************************************************************************
 * COPYRIGHT(c) 2016 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
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
