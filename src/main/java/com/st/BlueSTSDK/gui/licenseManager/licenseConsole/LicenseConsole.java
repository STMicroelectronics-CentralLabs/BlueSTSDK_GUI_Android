package com.st.BlueSTSDK.gui.licenseManager.licenseConsole;

import android.app.Activity;
import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.licenseManager.LicenseStatus;
import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.nucleo.LicenseConsoleNucleo;

import java.util.List;

/**
 * Class that use the debug console for send/receive information about the license status
 */
public abstract class LicenseConsole {

    static public @Nullable LicenseConsole getLicenseConsole(Node node){
        Debug debug = node.getDebug();
        if(debug==null)
            return null;
        //else
        switch (node.getType()) {
            case STEVAL_WESU1:
                return new LicenseConsoleWesu(debug);
            case NUCLEO:
            case SENSOR_TILE:
            case BLUE_COIN:
                return new LicenseConsoleNucleo(debug);
        }
        return  null;
    }

    /**
     * console where send the command
     */
    protected Debug mConsole;

    /**
     *
     * @param console console where send the command
     */
    protected LicenseConsole(Debug console) {
        mConsole = console;
    }

    /**
     * true if the object is waiting an answer from the console
     *
     * @return true if the object is busy
     */
    public abstract boolean isWaitingAnswer();

    /**
     * request to read the board id
     * the board id is notify using the method
     * {@link ReadBoardIdCallback#onBoardIdRead(LicenseConsole, String)}
     * @return true if the command is correctly send
     */
    public abstract boolean readBoardId(ReadBoardIdCallback callback);

    public interface ReadBoardIdCallback {
        /**
         * function called when we receive the board id.
         *
         * @param console object that request the board id
         * @param uid     board id
         */
        void onBoardIdRead(LicenseConsole console, String uid);
    }

    public ReadBoardIdCallback getDefaultReadBoardId(Activity a){
        return new ReadBoardIdCallback() {
            @Override
            public void onBoardIdRead(LicenseConsole console, String uid) {

            }
        };
    }

    /**
     * request to the node the license status.
     * the response is notify using the method
     * {@link ReadLicenseStatusCallback#onLicenseStatusRead(LicenseConsole , List)}
     * @return true if the request is correctly send, false otherwise.
     */
    public abstract boolean readLicenseStatus(ReadLicenseStatusCallback callback);

    public  interface ReadLicenseStatusCallback {
        /**
         * function called when we receive the license status from the board
         *
         * @param console  object that request the board status
         * @param licenses list of license available on the node
         */
        void onLicenseStatusRead(LicenseConsole console, List<LicenseStatus> licenses);
    }

    public ReadLicenseStatusCallback getDefaultReadLicenseStatusCallback(Activity a){
        return new ReadLicenseStatusCallback() {
            @Override
            public void onLicenseStatusRead(LicenseConsole console, List<LicenseStatus> licenses) {
            }
        };
    }

    /**
     * upload the license to the node, the success fill be notify using the method
     * {@link WriteLicenseCallback#onLicenseLoadSuccess(LicenseConsole, String, byte[])} or
     * {@link WriteLicenseCallback#onLicenseLoadFail(LicenseConsole, String, byte[])}
     * @param licName license name
     * @param licCode license code
     * @return true if the message is correctly send, false otherwise
     */
    public abstract boolean writeLicenseCode(String licName, byte[] licCode, WriteLicenseCallback
            callback);

    public interface WriteLicenseCallback{
        void onLicenseLoadSuccess(LicenseConsole console,String licName,byte[] licCode);
        void onLicenseLoadFail(LicenseConsole console,String licName,byte[] licCode);
    }

    public WriteLicenseCallback getDefaultWriteLicenseCallback(Activity a){
        return new WriteLicenseCallback() {
            @Override
            public void onLicenseLoadSuccess(LicenseConsole console, String licName, byte[] licCode) {
            }

            @Override
            public void onLicenseLoadFail(LicenseConsole console, String licName, byte[] licCode) {
            }
        };
    }

    /**
     * remove all the license into the node, when the process end the method
     * {@link CleanLicenseCallback#onLicenseClearedSuccess(LicenseConsole)} or
     * {@link CleanLicenseCallback#onLicenseClearedFail(LicenseConsole)}
     * @return true if the message is correctly send, false otherwise
     */
    public abstract boolean cleanAllLicense(CleanLicenseCallback callback);

    public  interface CleanLicenseCallback{
        void onLicenseClearedSuccess(LicenseConsole console);
        void onLicenseClearedFail(LicenseConsole console);
    }

    public  CleanLicenseCallback getDefaultCleanLicense(Activity a){
        return new CleanLicenseCallback() {
            @Override
            public void onLicenseClearedSuccess(LicenseConsole console) {
            }

            @Override
            public void onLicenseClearedFail(LicenseConsole console) {
            }
        };
    }


    public static class LicenseConsoleCallbackEmpty implements ReadBoardIdCallback,
            ReadLicenseStatusCallback,WriteLicenseCallback,CleanLicenseCallback{
        @Override
        public void onBoardIdRead(LicenseConsole console, String uid) {}

        @Override
        public void onLicenseStatusRead(LicenseConsole console, List<LicenseStatus> licenses) {}

        @Override
        public void onLicenseLoadSuccess(LicenseConsole console,String licName,byte[] licCode) { }

        @Override
        public void onLicenseLoadFail(LicenseConsole console,String licName,byte[] licCode) { }

        @Override
        public void onLicenseClearedSuccess(LicenseConsole console) { }

        @Override
        public void onLicenseClearedFail(LicenseConsole console) { }

    }
}
