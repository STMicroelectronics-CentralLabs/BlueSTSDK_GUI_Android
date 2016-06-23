package com.st.BlueSTSDK.gui.licenseManager;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.StringRes;

import android.widget.Toast;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.R;

/**
 * Class that implement the {@link LoadLicenseTask.LoadLicenseTaskCallback} interface.
 * it just use show a toast message with the load result.
 */
//move otside lib
public class SimpleLoadLicenseTaskCallback implements LoadLicenseTask.LoadLicenseTaskCallback {

    /**
     * node where we are loading the license
     */
    private Node mNode;

    /**
     * @param node node where we are loading the license
     */
    public SimpleLoadLicenseTaskCallback(Node node){
        mNode=node;
    }

    /**
     * display a toast message
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

    /**
     * disconnect the node and show the message
     * @param c context where the load is started
     * @param loader object that finish to load the license
     */
    @Override
    public void onLicenseLoad(Context c,LoadLicenseTask loader) {
        mNode.disconnect();
        showToast(c,R.string.licenseManager_licenseLoadComplete);
    }//onLicenseLoad

    /**
     * disconnect the node and show a message
     * @param c context where the load is started
     * @param loader object that finish to load the license
     */
    @Override
    public void onInvalidLicense(Context c,LoadLicenseTask loader) {
        mNode.disconnect();
        showToast(c,R.string.licenseManager_licenseLoadFail);
    }//onInvalidLicense

    /**
     * show an error message
     * @param c context where the load is started
     * @param loader object that finish to load the license
     */
    @Override
    public void onWrongBoardId(Context c, LoadLicenseTask loader) {
        showToast(c,R.string.licenseManager_wrongBoardFail);
    }//onWrongBoardId
}
