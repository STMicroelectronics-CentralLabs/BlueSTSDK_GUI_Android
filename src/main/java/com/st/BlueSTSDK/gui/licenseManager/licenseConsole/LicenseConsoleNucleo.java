package com.st.BlueSTSDK.gui.licenseManager.licenseConsole;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.gui.licenseManager.LicenseStatus;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseDefines;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicenseConsoleNucleo extends LicenseConsole {

    /**
     * command used for receive the board id
     */
    private static final String GET_UID = "uid\n";

    /**
     * command used for receive the license status
     */
    private static final String GET_LIC = "lic\n";

    /**
     * string present if a license is available in the node
     */
    private static final String LIC_PRESENT = "OK";

    /**
     * number of ms after that we can consider the command answer finished
     */
    private static final int COMMAND_TIMEOUT_MS = 500;

    /**
     * patter used for split/capture the license name and its status, each line has the format:
     * licenseName [OK|--]
     */
    private static final Pattern LICENSE_STATUS_PARSE = Pattern.compile("(\\w+)\\s+(..)");

    /**
     * pattern used for understand if the license is successfully load
     */
    private static final Pattern LICENSE_LOAD_STATUS_PARSE = Pattern.compile(".*Succes.*");

    private static final Pattern LICENSE_CLEAR_STATUS_PARSE = Pattern.compile(".*Reset of All.*");

    private static final String CLEAR_BOARD_LIC_COMMAND="XX0\n";


    /**
     * object that will receive the console data
     */
    private Debug.DebugOutputListener mCurrentListener;

    /**
     * Buffer where store the command response
     */
    private StringBuilder mBuffer;

    /**
     * object used for manage the get board id command
     */
    private Debug.DebugOutputListener mConsoleGetIdListener = new Debug.DebugOutputListener() {

        @Override
        public void onStdOutReceived(Debug debug, String message) {
            mBuffer.append(message);
            if (mBuffer.length()>2 &&
                    mBuffer.substring(mBuffer.length()-2).equals("\r\n")) {
                mBuffer.delete(mBuffer.length()-2,mBuffer.length());
                setConsoleListener(null);
                if (mCallback != null)
                    mCallback.onBoardIdRead(LicenseConsoleNucleo.this, mBuffer.toString());
            }
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {

        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {

        }
    };

    /**
     * handler used for the command timeout
     */
    private Handler mTimeout;

    /**
     * object used to manage the get status command output
     */
    private Debug.DebugOutputListener mConsoleGetStatusListener = new Debug.DebugOutputListener() {

        /**
         *  action to do when the timeout is fired
         */
        private Runnable mOnDataFinish = new Runnable() {
            @Override
            public void run() {
                setConsoleListener(null);
                if (mBuffer.length() != 0)
                    mCallback.onLicenseStatusRead(LicenseConsoleNucleo.this,
                            parseLicStatusResponse(mBuffer.toString()));
            }
        };


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
    };

    /**
     * object used to manage the load license command
     */
    private Debug.DebugOutputListener mConsoleLoadLicenseListener = new Debug.DebugOutputListener() {

        /**
         * remove the listener and notify the failure
         */
        private Runnable onTimeout = new Runnable() {
            @Override
            public void run() {
                setConsoleListener(null);
                mCallback.onLicenseLoad(LicenseConsoleNucleo.this, false);
            }
        };
        /**
         * when the fist message is send, start a timeout, that if we don't receive an answer we
         * notify a failure.
         * we need the boolean since the license command is send in more than one message
         */
        private boolean mFistWrite = true;

        /**
         *
         * @param debug   object that send the message
         * @param message message that someone write in the debug console
         */
        @Override
        public void onStdOutReceived(Debug debug, String message) {
            mBuffer.append(message);
            if (mBuffer.length()>2 &&
                    mBuffer.substring(mBuffer.length()-2).equals("\r\n")) {
                mBuffer.delete(mBuffer.length()-2,mBuffer.length());
                mTimeout.removeCallbacks(onTimeout);
                setConsoleListener(null);
                if (mCallback != null)
                    mCallback.onLicenseLoad(LicenseConsoleNucleo.this,
                            LICENSE_LOAD_STATUS_PARSE.matcher(mBuffer).find());
            }
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {

        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            if (mFistWrite)
                mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
        }
    };

    /**
     * object used to manage the clean console license
     */
    private Debug.DebugOutputListener mConsoleCleanLicenseListener = new Debug.DebugOutputListener() {

        /**
         * remove the listener and notify the failure
         */
        private Runnable onTimeout = new Runnable() {
            @Override
            public void run() {
                setConsoleListener(null);
                mCallback.onLicenseCleared(LicenseConsoleNucleo.this, false);
            }
        };
        /**
         * when the fist message is send, start a timeout, that if we don't receive an answer we
         * notify a failure.
         * we need the boolean since the license command is send in more than one message
         */
        private boolean mFistWrite = true;

        /**
         *
         * @param debug   object that send the message
         * @param message message that someone write in the debug console
         */
        @Override
        public void onStdOutReceived(Debug debug, String message) {
            if (message.endsWith("\r\n")) {
                mTimeout.removeCallbacks(onTimeout);
                mBuffer.append(message, 0, message.length() - 2);
                setConsoleListener(null);
                if (mCallback != null)
                    mCallback.onLicenseCleared(LicenseConsoleNucleo.this,
                            LICENSE_CLEAR_STATUS_PARSE.matcher(mBuffer).find());
            } else {
                mBuffer.append(message);
            }
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {

        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            if (mFistWrite)
                mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
        }
    };

    /**
     * build a debug console without a callback
     * @param console console to use for send the command
     */
    LicenseConsoleNucleo(Debug console){
        this(console,null);
    }

    /**
     *
     * @param console console where send the command
     * @param callback object where notify the command answer
     */
    LicenseConsoleNucleo(Debug console, LicenseConsoleCallback callback) {
        super(console,callback);
        mTimeout = new Handler(Looper.getMainLooper());
        mBuffer = new StringBuilder();

    }

    /**
     * Extract the list of license status object form a string
     *
     * @param resp node response to the command for list the license status
     * @return list of license status
     */
    private static List<LicenseStatus> parseLicStatusResponse(String resp) {
        ArrayList<LicenseStatus> licStatus = new ArrayList<>();
        Matcher parseLine = LICENSE_STATUS_PARSE.matcher(resp);
        while (parseLine.find()) {
            String licName = parseLine.group(1);
            //last 2 char are the license short name/code
            LicenseInfo info = LicenseDefines.getLicenseInfo(licName.substring(licName.length()-2));
            if(info!=null)
                licStatus.add(new LicenseStatus(info,
                        parseLine.group(2).equals(LIC_PRESENT)));
        }//for
        return licStatus;
    }//parseResponse

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

        mBuffer.setLength(0); //reset the buffer
        setConsoleListener(mConsoleLoadLicenseListener);
        mConsole.write(licName);
        mConsole.write(licCode);
        return true;
    }

    @Override
    public boolean cleanAllLicense() {
        if(isWaitingAnswer())
            return false;

        mBuffer.setLength(0);
        setConsoleListener(mConsoleCleanLicenseListener);
        mConsole.write(CLEAR_BOARD_LIC_COMMAND);
        return true;
    }
}
