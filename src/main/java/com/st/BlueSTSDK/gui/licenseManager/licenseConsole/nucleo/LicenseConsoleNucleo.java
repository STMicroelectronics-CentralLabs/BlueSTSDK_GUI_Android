/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

package com.st.BlueSTSDK.gui.licenseManager.licenseConsole.nucleo;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.gui.licenseManager.LicenseStatus;
import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.LicenseConsole;
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
    private static final int COMMAND_TIMEOUT_MS = 1000;

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
                if (mReadBoardIdCallback != null)
                    mReadBoardIdCallback.onBoardIdRead(LicenseConsoleNucleo.this, mBuffer.toString());
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
                if (mBuffer.length() != 0 && mReadLicenseStatusCallback!=null)
                    mReadLicenseStatusCallback.onLicenseStatusRead(LicenseConsoleNucleo.this,
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


    private class ConsoleLoadLicenseLister implements Debug.DebugOutputListener {

        private String mLicName;
        private byte[] mLicCode;

        ConsoleLoadLicenseLister(String name,byte[] licCode){
            mLicName=name;
            mLicCode=licCode;
        }

        /**
         * remove the listener and notify the failure
         */
        private Runnable onTimeout = new Runnable() {
            @Override
            public void run() {
                setConsoleListener(null);
                if(mWriteLicenseCallback!=null)
                    mWriteLicenseCallback.onLicenseLoadFail(LicenseConsoleNucleo.this,mLicName,mLicCode);
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
                if (mWriteLicenseCallback != null)
                    if(LICENSE_LOAD_STATUS_PARSE.matcher(mBuffer).find())
                        mWriteLicenseCallback.onLicenseLoadSuccess(LicenseConsoleNucleo.this,mLicName,mLicCode);
                    else
                        mWriteLicenseCallback.onLicenseLoadFail(LicenseConsoleNucleo.this,mLicName,mLicCode);
            }
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {

        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            if (mFistWrite) {
                mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
                mFistWrite=false;
            }
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
                if(mCleanLicenseCallback!=null)
                    mCleanLicenseCallback.onLicenseClearedFail(LicenseConsoleNucleo.this);
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
                mBuffer.append(message, 0, message.length() - 2);
                setConsoleListener(null);
                if (mCleanLicenseCallback != null)
                    if(LICENSE_CLEAR_STATUS_PARSE.matcher(mBuffer).find())
                        mCleanLicenseCallback.onLicenseClearedSuccess(LicenseConsoleNucleo.this);
                    else
                        mCleanLicenseCallback.onLicenseClearedFail(LicenseConsoleNucleo.this);
            } else {
                mBuffer.append(message);
            }
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {

        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            if (mFistWrite) {
                mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
                mFistWrite=false;
            }
        }
    };

    /**
     *
     * @param console console where send the command
     */
    public LicenseConsoleNucleo(Debug console) {
        super(console);
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
            mConsole.removeDebugOutputListener(mCurrentListener);
            mConsole.addDebugOutputListener(listener);
            mCurrentListener = listener;
        }//synchronized
    }

    @Override
    public boolean isWaitingAnswer() {
        return mCurrentListener != null;
    }

    private ReadBoardIdCallback mReadBoardIdCallback=null;

    @Override
    public boolean readBoardId(ReadBoardIdCallback callback) {
        if (isWaitingAnswer())
            return false;

        mReadBoardIdCallback=callback;

        mBuffer.setLength(0); //reset the buffer
        setConsoleListener(mConsoleGetIdListener);
        mConsole.write(GET_UID);
        return true;
    }

    private ReadLicenseStatusCallback mReadLicenseStatusCallback=null;

    @Override
    public boolean readLicenseStatus(ReadLicenseStatusCallback callback) {
        if (isWaitingAnswer())
            return false;

        mReadLicenseStatusCallback=callback;

        mBuffer.setLength(0); //reset the buffer
        setConsoleListener(mConsoleGetStatusListener);
        mConsole.write(GET_LIC);
        return true;
    }

    private WriteLicenseCallback mWriteLicenseCallback=null;

    @Override
    public boolean writeLicenseCode(String licName, byte[] licCode, WriteLicenseCallback callback) {
        if (isWaitingAnswer())
            return false;

        mWriteLicenseCallback=callback;

        mBuffer.setLength(0); //reset the buffer
        setConsoleListener(new ConsoleLoadLicenseLister(licName,licCode));
        mConsole.write(licName);
        mConsole.write(licCode);
        return true;
    }

    private CleanLicenseCallback mCleanLicenseCallback;

    @Override
    public boolean cleanAllLicense(CleanLicenseCallback callback) {
        if(isWaitingAnswer())
            return false;

        mCleanLicenseCallback=callback;

        mBuffer.setLength(0);
        setConsoleListener(mConsoleCleanLicenseListener);
        mConsole.write(CLEAR_BOARD_LIC_COMMAND);
        return true;
    }

    @Override
    public CleanLicenseCallback getDefaultCleanLicense(Activity a) {
        return new DefaultLicenseCleanCallback(a.getFragmentManager());
    }

    @Override
    public WriteLicenseCallback getDefaultWriteLicenseCallback(Activity a) {
        return new DefaultWriteLicenseCallback(a.getFragmentManager());
    }
}
