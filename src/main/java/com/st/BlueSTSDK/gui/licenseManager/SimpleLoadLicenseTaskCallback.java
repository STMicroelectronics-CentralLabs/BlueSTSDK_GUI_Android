package com.st.BlueSTSDK.gui.licenseManager;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.StringRes;

import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.NodeListActivity;
import com.st.BlueSTSDK.gui.R;

/**
 * Class that implement the {@link LoadLicenseTask.LoadLicenseTaskCallback} interface.
 * if the upload is finished it send to the {@link NodeListActivity} and show a toast notification
 */
public class SimpleLoadLicenseTaskCallback implements LoadLicenseTask.LoadLicenseTaskCallback {

    private Node mNode;

    public SimpleLoadLicenseTaskCallback(Node node){
        mNode=node;
    }

    /**
     * disconnect the node, show a toast message and start the {@link com.st.BlueSTSDK.gui.NodeListActivity}
     * @param message message string to show in the toast
     */
    private void showToast(final Context c, @StringRes final int message){
        Runnable showToastTask = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(c, message, Toast.LENGTH_LONG).show();
            }
        };

        if(c instanceof Activity){
            ((Activity)c).runOnUiThread(showToastTask);
        }else{
            new Handler(c.getMainLooper()).post(showToastTask);
        }//if
    }//showToast

    @Override
    public void onLicenseLoad(Context c,LoadLicenseTask loader) {
        mNode.disconnect();
        showToast(c,R.string.licenseManager_licenseLoadComplete);
    }//onLicenseLoad

    @Override
    public void onInvalidLicense(Context c,LoadLicenseTask loader) {
        mNode.disconnect();
        showToast(c,R.string.licenseManager_licenseLoadFail);
    }//onInvalidLicense

    /**
     * the license belongs to a different board, show a snack message
     * @param loader object that finish to load the license
     */
    @Override
    public void onWrongBoardId(Context c, LoadLicenseTask loader) {
        if(c instanceof Activity){
            Activity a = (Activity)c;
            View root = a.findViewById(android.R.id.content);
            Snackbar.make(root,R.string.licenseManager_wrongBoardFail,Snackbar.LENGTH_SHORT).show();
        }else{
            showToast(c,R.string.licenseManager_wrongBoardFail);
        }//if

    }//onWrongBoardId
}
