package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.st.BlueSTSDK.gui.R;

import static android.app.Activity.RESULT_OK;

/**
 * Helper class to open a file selector to select a file
 */
public class RequestFileUtil {

    private static final int CHOOSE_BOARD_FILE_REQUESTCODE=1;
    private static final int RESULT_READ_ACCESS = 2;


    private final Fragment mSrc;
    private final View mRootView;

    /**
     *
     * @param src fragment that will trigger the open of the file selector
     * @param rootView view where show the shankbar with the request/errors
     */
    public RequestFileUtil(Fragment src, View rootView) {
        this.mSrc = src;
        this.mRootView = rootView;
    }


    /**
     * check the permission and open the file selector
     */
    public void openFileSelector(){
        if(checkReadSDPermission()) {
            mSrc.startActivityForResult(getFileSelectIntent(), CHOOSE_BOARD_FILE_REQUESTCODE);
        }
    }

    private Intent getFileSelectIntent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        return intent;
        //Intent i = Intent.createChooser(intent, "Open firmwere file");
    }


    /**
     * extract the file name
     * @param context context to use to retrive the file info
     * @param uri uri with the file name to query
     * @return name o the file inside the uri
     */
    public static @Nullable String getFileName(@NonNull Context context, @Nullable Uri uri) {
        if(uri ==null)
            return null;
        String scheme = uri.getScheme();
        if (scheme.equals("file")) {
            return uri.getLastPathSegment();
        }
        if (scheme.equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                cursor.close();
                return fileName;
            }
        }
        return null;
    }


    /**
     * to call in the fragment onActivityResult if the request is correct will return the uri, null
     *  otherwise
     * @param requestCode
     * @param resultCode
     * @param data
     * @return selected file or null
     */
    public @Nullable Uri onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==CHOOSE_BOARD_FILE_REQUESTCODE) {
                return data.getData();
            }
        }
        return null;
    }

    /**
     * check it we have the permission to write data on the sd
     * @return true if we have it, false if we ask for it
     */
    private boolean checkReadSDPermission(){
        if (ContextCompat.checkSelfPermission(mSrc.requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(mSrc.requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //onClick
                Snackbar.make(mRootView, R.string.FwUpgrade_readSDRationale,
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, view -> mSrc.requestPermissions(
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                RESULT_READ_ACCESS)).show();
            } else {
                // No explanation needed, we can request the permission.
                mSrc.requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        RESULT_READ_ACCESS);
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
            case RESULT_READ_ACCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mSrc.startActivityForResult(getFileSelectIntent(), CHOOSE_BOARD_FILE_REQUESTCODE);
                } else {
                    Snackbar.make(mRootView, R.string.FwUpgrade_permissionDenied,
                            Snackbar.LENGTH_SHORT).show();

                }//if-else
                break;
            }//REQUEST_LOCATION_ACCESS
        }//switch
    }//onRequestPermissionsResult


}
