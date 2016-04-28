package com.st.BlueSTSDK.gui.licenseManager.licenseConsole;

import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.gui.licenseManager.LicenseStatus;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseDefines;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicenseConsoleWesu extends LicenseConsole {

    private static final SparseArray<String> LIC_MAPPING = new SparseArray<>();
    static {
        LIC_MAPPING.append(1,"FX");
        LIC_MAPPING.append(2,"AR");
        LIC_MAPPING.append(3,"CP");
    }

    /**
     * command used for receive the board id
     */
    private static final String GET_UID = "?mcuid\n";

    /**
     * command used for receive the license status
     */
    private static final String GET_LIC = "?algostatus";

    private static final String LOAD_LIC="!lic%d_%s";

    /**
     * number of ms after that we can consider the command answer finished
     */
    private static final int COMMAND_TIMEOUT_MS = 500;

    private static final Pattern BOARD_ID_PARSE = Pattern.compile(".*([0-9A-F]{24}).*");

    private static final Pattern LICENSE_STATUS_PARSE = Pattern.compile(".*Algorithm(\\d*) (not)?\\s*initialized.*");

    /**
     * object that will receive the console data
     */
    private Debug.DebugOutputListener mCurrentListener;

    /**
     * Buffer where store the command response
     */
    private StringBuilder mBuffer;

    /**
     * handler used for the command timeout
     */
    private Handler mTimeout;

    private class TimeOutConsoleListener implements Debug.DebugOutputListener{

        private Runnable mOnDataFinish;

        public TimeOutConsoleListener(Runnable onDataFinish){
            mOnDataFinish = onDataFinish;
        }

        @Override
        public void onStdOutReceived(Debug debug, String message) {
            mTimeout.removeCallbacks(mOnDataFinish); //remove the timeout
            mBuffer.append(message);
            //add a new timeout
            mTimeout.postDelayed(mOnDataFinish, COMMAND_TIMEOUT_MS);
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {
        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            //when the command is send, start the timeout
            mTimeout.postDelayed(mOnDataFinish, COMMAND_TIMEOUT_MS);
        }
    }

    /**
     * object used for manage the get board id command
     */
    private Debug.DebugOutputListener mConsoleGetIdListener;

    /**
     * object used to manage the get status command output
     */
    private Debug.DebugOutputListener mConsoleGetStatusListener;

    /**
     * object used to manage the load license command
     */
    private Debug.DebugOutputListener mConsoleLoadLicenseListener;

    /**
     * build a debug console without a callback
     * @param console console to use for send the command
     */
    LicenseConsoleWesu(Debug console){
        this(console,null);
    }

    /**
     *
     * @param console console where send the command
     * @param callback object where notify the command answer
     */
    LicenseConsoleWesu(Debug console, LicenseConsoleCallback callback) {
        super(console,callback);
        mTimeout = new Handler();
        mBuffer = new StringBuilder();
        mConsoleGetIdListener = new TimeOutConsoleListener(new Runnable() {
            @Override
            public void run() {
                setConsoleListener(null);
                if (mBuffer.length() != 0)
                    mCallback.onBoardIdRead(LicenseConsoleWesu.this,
                            extractBoardUid(mBuffer.toString()));
            }
        });
        mConsoleGetStatusListener = new TimeOutConsoleListener(new Runnable() {
            @Override
            public void run() {
                setConsoleListener(null);
                if (mBuffer.length() != 0)
                    mCallback.onLicenseStatusRead(LicenseConsoleWesu.this,
                            extractLicenseStatus(mBuffer.toString()));
            }
        });
    }

    private static String extractBoardUid(String data){
        Log.d("extractBoardUid",data);
        Matcher matcher = BOARD_ID_PARSE.matcher(data);
        if(matcher.find())
            return matcher.group(1);
        else
            return null;
    }

    private static List<LicenseStatus> extractLicenseStatus(String data){
        Log.d("extractLicenseStatus",data);
        ArrayList<LicenseStatus> licStatus = new ArrayList<>();
        Matcher matcher = LICENSE_STATUS_PARSE.matcher(data);
        while (matcher.find()){
            int licId = Integer.getInteger(matcher.group(1));
            LicenseInfo licCode = LicenseDefines.getLicenseInfo(LIC_MAPPING.get(licId));
            boolean isPresent = matcher.group(2)==null;
            if(licCode!=null)
                licStatus.add(new LicenseStatus(licCode,isPresent));
        }

        return licStatus;
    }

    /**
     * change the listener to use for receive the debug console message, null will close the
     * debug console
     *
     * @param listener object to use for notify the console messages
     */
    private void setConsoleListener(Debug.DebugOutputListener listener) {
        synchronized (this) {
            mCurrentListener = listener;
            mConsole.setDebugOutputListener(listener);
        }//synchronized
    }

    @Override
    public boolean isWaitingAnswer() {
        return mCurrentListener != null;
    }

    @Override
    public boolean readBoardId() {
        if (isWaitingAnswer())
            return false;

        mBuffer.setLength(0); //reset the buffer
        setConsoleListener(mConsoleGetIdListener);
        mConsole.write(GET_UID);
        return true;
    }

    @Override
    public boolean readLicenseStatus() {
        if (isWaitingAnswer())
            return false;

        mBuffer.setLength(0); //reset the buffer
        setConsoleListener(mConsoleGetStatusListener);
        mConsole.write(GET_LIC);
        return true;
    }

    @Override
    public boolean writeLicenseCode(String licName, byte[] licCode) {
        if (isWaitingAnswer())
            return false;

        setConsoleListener(mConsoleLoadLicenseListener);
        mBuffer.setLength(0); //reset the buffer

        BigInteger lic = new BigInteger(1,licCode);
        int licNameCode= LIC_MAPPING.indexOfValue(licName);

        mConsole.write(String.format(LOAD_LIC,licNameCode,lic.toString()));
        return true;
    }

}
