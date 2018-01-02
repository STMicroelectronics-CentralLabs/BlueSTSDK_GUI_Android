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

package com.st.BlueSTSDK.gui.licenseManager.licenseConsole;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.gui.licenseManager.LicenseStatus;
import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.nucleo.DefaultLicenseCleanCallback;
import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.nucleo.DefaultWriteLicenseCallback;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseDefines;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LicenseConsoleWesu extends LicenseConsole {

    private final static String TAG = LicenseConsoleWesu.class.getCanonicalName();

    /**
     * command used for receive the board id
     */
    private static final String ALG_MOTION_FX = "FX";
    private static final String ALG_MOTION_AR = "AR";
    private static final String ALG_MOTION_CP = "CP";

    private static final SparseArray<String> LIC_MAPPING = new SparseArray<>();
    public static final String EMPTY_LIC = "[0]{96}";

    static {
        LIC_MAPPING.put(1,ALG_MOTION_FX);
        LIC_MAPPING.put(2,ALG_MOTION_AR);
        LIC_MAPPING.put(3,ALG_MOTION_CP);
    }

    private static String getAlgName(String name){
        switch (name) {
            case "FX":
                return ALG_MOTION_FX;
            case "AR":
                return ALG_MOTION_AR;
            case "CP":
                return ALG_MOTION_CP;
            default:
                return "Unknown";
        }
    }

    /**
     * command used for receive the board id
     */
    private static final String GET_UID = "?mcuid\n";

    /**
     * command used for receive the license status
     */
    private static final String GET_LIC = "?algostatus\n";

    private static final String LOAD_LIC="!lic%d_%s\n";


    private static final Pattern BOARD_ID_PARSE = Pattern.compile(".*([0-9A-Fa-f]{24})(_([0-9A-Fa-f]{3,4}))?.*");

    private static final Pattern LICENSE_STATUS_PARSE_OLD = Pattern.compile(".*Algorithm(\\d*)\\s*license\\s*.lic\\d*_([0-9A-Fa-f]{96}).*");

    /**
     * patter used for split/capture the license name and its status, each line has the format:
     * Algoname XX license
     */
    private static final Pattern LICENSE_STATUS_PARSE = Pattern.compile(".*\\s([A-Z]{2})\\s*license\\s*.lic\\d*_([0-9A-Fa-f]{96}).*");

    /**
     * pattern used for understand if the license is successfully load
     */
    private static final Pattern LICENSE_LOAD_STATUS_PARSE = Pattern.compile(".*License write successful.*");

    private static final String ERASE_LICENSES = "!eraselics\n";

    private static final String DEFAULT_MCU_FAMILY_ID = "437";

    private static final Pattern LICENSE_TO_BYTE_CODE = Pattern.compile("([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");
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

    /**
     * object used for manage the get board id command
     */
    private Debug.DebugOutputListener mConsoleGetIdListener = new Debug.DebugOutputListener() {

        private static final int COMMAND_TIMEOUT_MS=1000;

        /**
         *  action to do when the timeout is fired
         */
        private Runnable onTimeout = new Runnable() {
            @Override
            public void run() {
                setConsoleListener(null);
                if (mBuffer.length() != 0 && mReadBoardIdCallback!=null)
                    mReadBoardIdCallback.onBoardIdRead(LicenseConsoleWesu.this,
                            extractBoardUid(mBuffer.toString()));
            }
        };

        @Override
        public void onStdOutReceived(Debug debug, String message) {

            mTimeout.removeCallbacks(onTimeout); //remove the timeout
            mBuffer.append(message);
            String uid =extractBoardUid(mBuffer.toString());
            if(uid!=null){
                onTimeout.run();
            }else {
                mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
            }
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {
        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            //when the command is send, start the timeout
            mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
        }
    };

    /**
     * object used to manage the get status command output
     */
    private Debug.DebugOutputListener mConsoleGetStatusListener = new Debug.DebugOutputListener() {

        private static final int COMMAND_TIMEOUT_MS=1000;

        /**
         *  action to do when the timeout is fired
         */
        private Runnable onTimeout = new Runnable() {
            @Override
            public void run() {
                setConsoleListener(null);
                if (mBuffer.length() != 0 && mReadLicenseStatusCallback!=null)
                    mReadLicenseStatusCallback.onLicenseStatusRead(LicenseConsoleWesu.this,
                            extractLicenseStatus(mBuffer.toString()));
            }
        };

        @Override
        public void onStdOutReceived(Debug debug, String message) {

            mTimeout.removeCallbacks(onTimeout); //remove the timeout
            mBuffer.append(message);
            //add a new timeout
            mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {
        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            //when the command is send, start the timeout
            mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
        }
    };

    private class ConsoleLoadLicenseLister implements Debug.DebugOutputListener {

        private String mLicName;
        private byte[] mLicCode;

        ConsoleLoadLicenseLister(String name,byte[] licCode){
            mLicName=name;
            mLicCode=licCode;
        }

        private static final int COMMAND_TIMEOUT_MS = 1000;
        private static final int MAX_COMMAND_LENGTH=20;

        /**
         * action to do when the timeout is fired
         */
        private Runnable onTimeout = new Runnable() {
            @Override
            public void run() {
                setConsoleListener(null);
                if (mBuffer.length() != 0 && mWriteLicenseCallback != null)
                    if(loadLicenseStatus(mBuffer.toString()))
                        mWriteLicenseCallback.onLicenseLoadSuccess(LicenseConsoleWesu.this,
                                mLicName,mLicCode);
                    else
                        mWriteLicenseCallback.onLicenseLoadFail(LicenseConsoleWesu.this,
                                mLicName,mLicCode);
            }
        };

        @Override
        public void onStdErrReceived(Debug debug, String message) {
        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            //when the command is send, start the timeout
            mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
        }

        private String mStrToSend;
        private int mLastSendSequence;
        private int mSplitSize;

        public void sendMessage(String strToSend, int splitSize){
            if (strToSend != null && strToSend.length() > 0) {
                mStrToSend = strToSend;
                mLastSendSequence = 0;
                mSplitSize = splitSize;
                SendNextMessage();
            }
        }

        private String getSendingSequence(){
            return (mLastSendSequence * mSplitSize <  mStrToSend.length() ) ?
                    mStrToSend.substring(mSplitSize * mLastSendSequence, Math.min(mSplitSize * mLastSendSequence + mSplitSize, mStrToSend.length())) :
                    null;
        }
        private void SendNextMessage() {
            String mexToSend = getSendingSequence();
            if (mexToSend != null)
                mConsole.write(mexToSend);
        }

        @Override
        public void onStdOutReceived(Debug debug, String message) {

            mTimeout.removeCallbacks(onTimeout); //remove the timeout
            mBuffer.append(message);
            //add a new timeout
            Log.d(TAG, message);
            if(loadLicenseStatus(mBuffer.toString()))
                onTimeout.run();
            else {
                if (message.equals(getSendingSequence())) {
                    mLastSendSequence++;
                    SendNextMessage();
                }
                mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
            }

        }

        public void start() {
            sendMessage(getLoadLicCommand(mLicName,mLicCode),MAX_COMMAND_LENGTH);
        }
    };

    /**
     * object used for manage the get board id command
     */
    private Debug.DebugOutputListener mConsoleEraseListener = new Debug.DebugOutputListener() {

        private static final int COMMAND_TIMEOUT_MS = 1000;

        /**
         * action to do when the timeout is fired
         */
        private Runnable onTimeout = new Runnable() {
            @Override
            public void run() {
                setConsoleListener(null);
                if (mBuffer.length() != 0 && mCleanLicenseCallback != null)
                    mCleanLicenseCallback.onLicenseClearedSuccess(LicenseConsoleWesu.this);
            }
        };

        @Override
        public void onStdOutReceived(Debug debug, String message) {

            mTimeout.removeCallbacks(onTimeout); //remove the timeout
            mBuffer.append(message);
            //add a new timeout
            mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) {
        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
            //when the command is send, start the timeout
            mTimeout.postDelayed(onTimeout, COMMAND_TIMEOUT_MS);
        }
    };


    /**
     *
     * @param console console where send the command
     */
    LicenseConsoleWesu(Debug console) {
        super(console);
        mTimeout = new Handler(Looper.getMainLooper());
        mBuffer = new StringBuilder();
    }

    private static boolean loadLicenseStatus(String data){
        return  LICENSE_LOAD_STATUS_PARSE.matcher(data).find();
    }
    private static String extractBoardUid(String data){
        Matcher matcher = BOARD_ID_PARSE.matcher(data);

        if (matcher.find()) {
            String mcu_id = "";
            String mcu_id_temp = matcher.group(1);
            Matcher matcher1 = LICENSE_TO_BYTE_CODE.matcher(mcu_id_temp);
            while (matcher1.find())
                mcu_id += matcher1.group(4) + matcher1.group(3) +
                          matcher1.group(2) +  matcher1.group(1); //to invert in little endian

            String mcu_fam = matcher.group(3);
            if (mcu_fam == null) mcu_fam = DEFAULT_MCU_FAMILY_ID;
            return mcu_id + "_" + mcu_fam;
        }

        return null; //not valid board id
    }

    private static List<LicenseStatus> extractLicenseStatus(String data){
        ArrayList<LicenseStatus> licStatus = new ArrayList<>();
        Matcher matcher = LICENSE_STATUS_PARSE_OLD.matcher(data);
        while (matcher.find()) {
            int licId = Integer.parseInt(matcher.group(1));   //old style Alg 1 ==> lic2 ...
            LicenseInfo licCode = LicenseDefines.getLicenseInfo(LIC_MAPPING.get(licId + 1));
            String lic = matcher.group(2);
            boolean isPresent = lic != null && (!Pattern.matches(EMPTY_LIC, lic)); //96 0 (zeros) means not valid license
            if (licCode != null) {
                licStatus.add(new LicenseStatus(licCode,isPresent));
            }
        }
        if (licStatus.size() == 0) // no matching found new license style
        {
            Matcher matcher_new = LICENSE_STATUS_PARSE.matcher(data);
            while (matcher_new.find()) {
                //String licAlg0 = matcher_new.group(0);
                //String licAlg1 = matcher_new.group(1);
                String licName = matcher_new.group(1);
                LicenseInfo licCode = LicenseDefines.getLicenseInfo(licName);
                String lic = matcher_new.group(2);
                boolean isPresent = lic != null && (!Pattern.matches(EMPTY_LIC, lic)); //96 0 (zeros) means not valid license

                if (licCode != null)
                    licStatus.add(new LicenseStatus(licCode, isPresent ));
            }
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
            if(mCurrentListener!=null)
                mConsole.removeDebugOutputListener(mCurrentListener);
            mCurrentListener = listener;
            mConsole.addDebugOutputListener(listener);
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

    private ReadLicenseStatusCallback mReadLicenseStatusCallback;

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

    private static String getLoadLicCommand(String licName,byte[] licCode){
        //lic code is a big endian array of 12 integer to little endian,
        String  licStrHex="";
        for (int i = 0; i< licCode.length; i+=4)
        {
            licStrHex += String.format("%02X",licCode[(4*(i/4 + 1))-1]);
            licStrHex += String.format("%02X",licCode[(4*(i/4 + 1))-2]);
            licStrHex += String.format("%02X",licCode[(4*(i/4 + 1))-3]);
            licStrHex += String.format("%02X",licCode[(4*(i/4 + 1))-4]);
        }

        int licNameCode = LIC_MAPPING.keyAt(LIC_MAPPING.indexOfValue(getAlgName(licName)));
        return String.format(LOAD_LIC, licNameCode, licStrHex);
    }

    @Override
    public boolean writeLicenseCode(String licName, byte[] licCode, WriteLicenseCallback callback) {
        if (isWaitingAnswer())
            return false;

        if (licCode.length != 48)
            return false;

        mWriteLicenseCallback = callback;

        ConsoleLoadLicenseLister loadConsole = new ConsoleLoadLicenseLister(licName,licCode);

        setConsoleListener(loadConsole);

        mBuffer.setLength(0); //reset the buffer
        loadConsole.start();


        return true;

    }

    private CleanLicenseCallback mCleanLicenseCallback=null;

    @Override
    public boolean cleanAllLicense(CleanLicenseCallback callback) {
        if (isWaitingAnswer())
            return false;

        mCleanLicenseCallback=callback;

        mBuffer.setLength(0); //reset the buffer
        setConsoleListener(mConsoleEraseListener);

        mConsole.write(ERASE_LICENSES);

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