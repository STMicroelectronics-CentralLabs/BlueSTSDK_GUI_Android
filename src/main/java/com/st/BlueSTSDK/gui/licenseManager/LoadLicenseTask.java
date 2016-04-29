package com.st.BlueSTSDK.gui.licenseManager;

import android.content.Context;

import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.LicenseConsole;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseManagerDBContract;

import java.util.List;

/**
 * Helper class that load the license into the board, but check that the uid is equal before do it
 */
public class LoadLicenseTask {

    /**
     * Interface used for communicate the license upload result
     */
    public interface LoadLicenseTaskCallback{
        /**
         * call when the upload correctly finish
         * @param c context where the load is started
         * @param loader object that finish to load the license
         */
        void onLicenseLoad(Context c,LoadLicenseTask loader);

        /**
         * call when the license doesn't work for the board
         * @param c context where the load is started
         * @param loader object that finish to load the license
         */
        void onInvalidLicense(Context c,LoadLicenseTask loader);

        /**
         * call when the boardId is different from the expected
         * @param c context where the load is started
         * @param loader object that finish to load the license
         */
        void onWrongBoardId(Context c,LoadLicenseTask loader);
    }

    /**
     * console used for load the license
     */
    private LicenseConsole mConsole;

    /**
     * board id where load the license
     */
    private String mBoardId;

    /**
     * license name
     */
    private String mLicName;

    /**
     *  license code
     */
    private byte[] mLicCode;


    /**
     * build a set of command for load the license
     * @param console console where load the license
     * @param callback object where notify the load result
     */
    LoadLicenseTask(final Context c, LicenseConsole console, final LoadLicenseTaskCallback callback){
        mConsole =console;
        mConsole.setLicenseConsoleListener(new LicenseConsole.LicenseConsoleCallback() {

            /**
             * if the board is is equal to the one that we are using load the license otherwise
             * call {@link LoadLicenseTaskCallback#onWrongBoardId(Context, LoadLicenseTask)}
             * @param console object that request the board id
             * @param uid     board id
             */
            @Override
            public void onBoardIdRead(LicenseConsole console, String uid) {
                if(mBoardId!=null && uid.equals(mBoardId)){
                    if(mLicName!=null && mLicCode!=null)
                        console.writeLicenseCode(mLicName,mLicCode);
                    else
                        callback.onInvalidLicense(c,LoadLicenseTask.this);
                }else if(callback!=null){
                    callback.onWrongBoardId(c,LoadLicenseTask.this);
                }//if-else
            }//onBoardIdRead

            @Override
            public void onLicenseStatusRead(LicenseConsole console, List<LicenseStatus> licenses) {

            }

            /**
             * call the {@link LoadLicenseTaskCallback#onLicenseLoad(Context, LoadLicenseTask)} if the
             * status is true, or
             * {@link LoadLicenseTaskCallback#onInvalidLicense(Context, LoadLicenseTask)} if the status if
             * false
             * @param console object that load the license
             * @param status  true if the license is valid, false otherwise
             */
            @Override
            public void onLicenseLoad(LicenseConsole console, boolean status) {
                if(status)
                    callback.onLicenseLoad(c,LoadLicenseTask.this);
                else
                    callback.onInvalidLicense(c,LoadLicenseTask.this);
            }//onLicenseLoad
        });
    }

    /**
     * start loading the license
     * @param uid id of the board where load the license
     * @param licName name of the license to load
     * @param licCode license code
     */
    void load(String uid, String licName, byte[] licCode){

        //store the data
        mBoardId=uid;
        mLicCode=licCode;
        mLicName=licName;

        //trigger the process, star reading the uid
        mConsole.readBoardId();
    }//load

    void load(LicenseManagerDBContract.LicenseEntry entry){
        load(entry.getBoardId(),entry.getLicenseType(),entry.getLicenseCode());
    }

}
