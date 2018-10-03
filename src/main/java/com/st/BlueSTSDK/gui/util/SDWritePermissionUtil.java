package com.st.BlueSTSDK.gui.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.st.BlueSTSDK.gui.R;

public class SDWritePermissionUtil {

    public interface OnWritePermissionAcquiredCallback{
        void onWritePermissionAcquired();
    }

    private static final int RESULT_WRITE_REQUEST = 2;


    private final @NonNull Context mCtx;
    private final @Nullable FragmentActivity mActivity;
    private final @Nullable Fragment mFragment;
    private final @NonNull View mRootView;
    private OnWritePermissionAcquiredCallback mCallback;

    /**
     *
     * @param src fragment that will trigger the open of the file selector
     * @param rootView view where show the shankbar with the request/errors
     */
    public SDWritePermissionUtil(@NonNull Fragment src,@NonNull View rootView) {
        this.mFragment = src;
        this.mCtx = src.requireContext();
        this.mRootView = rootView;
        this.mActivity=null;
    }

    public SDWritePermissionUtil(@NonNull FragmentActivity src,@NonNull View rootView) {
        this.mFragment = null;
        mActivity = src;
        this.mRootView = rootView;
        mCtx = mActivity;
    }


    private Activity requireActivity(){
        if(mFragment!=null)
            return mFragment.requireActivity();
        if(mActivity!=null)
            return mActivity;
        throw new IllegalStateException("Fragment or activity must be != null");
    }

    private void requestPermissions(String permission[],int requestCode){
        if(mFragment!=null)
            mFragment.requestPermissions(permission,requestCode);
        else if (mActivity!=null)
            ActivityCompat.requestPermissions(mActivity,permission,requestCode);
        else {
            throw new IllegalStateException("Fragment or activity must be != null");
        }
    }

    public void acquireWritePermission(OnWritePermissionAcquiredCallback callback){
        mCallback = callback;
        if(checkWriteSDPermission()){
            mCallback.onWritePermissionAcquired();
        }
    }

    /**
     * check it we have the permission to write data on the sd
     * @return true if we have it, false if we ask for it
     */
    public boolean checkWriteSDPermission(){
        if (ContextCompat.checkSelfPermission(mCtx,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //onClick
                Snackbar.make(mRootView, R.string.SDWritePermission_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, view -> requestPermissions(
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                RESULT_WRITE_REQUEST)).show();
            } else {
                // No explanation needed, we can request the permission.
                requestPermissions( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        RESULT_WRITE_REQUEST);
            }//if-else
            return false;
        }else
            return  true;
    }


    /**
     * function to call in the fragment onRequestPermissionsResult if the permission is grantend open
     *  the file choorser to select the file
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case RESULT_WRITE_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCallback.onWritePermissionAcquired();
                } else {
                    Snackbar.make(mRootView, R.string.SDWritePermission_notGranted,
                            Snackbar.LENGTH_SHORT).show();

                }//if-else
                break;
            }//REQUEST_LOCATION_ACCESS
        }//switch
    }//onRequestPermissionsResult


}

