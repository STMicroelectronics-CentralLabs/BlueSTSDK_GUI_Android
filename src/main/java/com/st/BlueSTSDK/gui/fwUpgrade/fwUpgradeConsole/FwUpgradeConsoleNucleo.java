package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.IllegalVersionFormatException;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.ImgFileInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class FwUpgradeConsoleNucleo extends FwUpgradeConsole {

    static private final String GET_VERSION_BOARD_FW="versionFw\n";
    static private final String GET_VERSION_BLE_FW="versionBle\n";
    static private final String UPLOAD_BOARD_FW="upgradeFw%d\n";
    static private final String UPLOAD_BLE_FW="upgradeBle%d\n";

    static private final String NODE_READY_TO_RECEIVE="Ready\r\n";
    static private final String ACK_MSG="\u0001";
    static private final String NACK_MSG="\u00ff";
    static private final int MAX_MSG_SIZE=16;
    static private final int LOST_MSG_TIMEOUT_MS=500;

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
    private GetVersionProtocol mConsoleGetFwVersion= new GetVersionProtocol();

    private class GetVersionProtocol implements Debug.DebugOutputListener {

        private @FirmwareType int mRequestFwType;

        public void requestVersion(@FirmwareType int fwType){
            mRequestFwType=fwType;
            switch (fwType) {
                case FwUpgradeConsole.BLE_FW:
                    mConsole.write(GET_VERSION_BLE_FW);
                    break;
                case FwUpgradeConsole.BOARD_FW:
                    mConsole.write(GET_VERSION_BOARD_FW);
                    break;
                default:
                    if(mCallback!=null){
                        mCallback.onVersionRead(FwUpgradeConsoleNucleo.this,fwType,null);
                    }
                    break;
            }
        }

        @Override
        public void onStdOutReceived(Debug debug, String message) {
            if (message.endsWith("\r\n")) {
                mBuffer.append(message, 0, message.length() - 2);
                setConsoleListener(null);
                FwVersion version=null;
                try {
                    switch (mRequestFwType) {
                        case FwUpgradeConsole.BLE_FW:
                            version = new FwVersionBle(mBuffer.toString());
                            break;
                        case FwUpgradeConsole.BOARD_FW:
                            version = new FwVersionBoard(mBuffer.toString());
                            break;
                    }
                }catch (IllegalVersionFormatException e){
                    e.printStackTrace();
                }
                if (mCallback != null)
                    mCallback.onVersionRead(FwUpgradeConsoleNucleo.this,mRequestFwType,version);
            } else {
                mBuffer.append(message);
            }
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) { }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) { }
    };


    private class UploadFileProtocol implements  Debug.DebugOutputListener{

        /**
         * file that we are uploading
         */
        private Uri mFile;

        /**
         * buffer where we are reading the file
         */
        private BufferedInputStream mFileData;

        /**
         * number of byte send to the node
         */
        private long mByteSend;

        private long mByteToSend;

        /**
         * size of the last package send
         */
        private byte[] mLastPackageSend;

        /**
         * if the timeout is rise, resend the last package
         */
        private Runnable onTimeout = new Runnable() {
            @Override
            public void run() {
                Log.d("onTimeout:","fired");

                onStdOutReceived(mConsole,NACK_MSG);
            }
        };

        private void onLoadComplete(boolean status){
            if(mCallback!=null)
                mCallback.onLoadFwComplete(FwUpgradeConsoleNucleo.this,mFile,status);
            setConsoleListener(null);
        }

        private void dumpFile(File f){
            File output = new File("/sdcard/dump.bin");
            try{
                ImgFileInputStream in=new ImgFileInputStream(f);
                FileOutputStream out = new FileOutputStream(output);
                byte data[] = new byte[MAX_MSG_SIZE];
                while (in.read(data)>0){
                    out.write(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void loadFile(@FirmwareType int fwType,Uri file){
            mFile=file;
            File f = new File(file.getPath());
            mByteToSend = f.length();
            try {
                if(f.getName().toLowerCase().endsWith("img")) {
                    ImgFileInputStream input = new ImgFileInputStream(f);
                    mFileData = new BufferedInputStream(input);
                    mByteToSend = input.length();
                    dumpFile(f);

                }else
                    mFileData = new BufferedInputStream(new FileInputStream(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                onLoadComplete(false);
                return;
            }
            //send the start command

            switch (fwType) {
                case FwUpgradeConsole.BLE_FW:
                    mConsole.write(String.format(UPLOAD_BLE_FW,mByteToSend));
                    break;
                case FwUpgradeConsole.BOARD_FW:
                    mConsole.write(String.format(UPLOAD_BOARD_FW,mByteToSend));
                    break;
                default:
                    onLoadComplete(false);
            }
        }

        //each time an ack is received a new package is send
        @Override
        public void onStdOutReceived(Debug debug, String message) {
            Log.d("onStdOutReceived",message);
            if(message.equalsIgnoreCase(ACK_MSG) ||
                    message.equalsIgnoreCase(NODE_READY_TO_RECEIVE)){
                //stop resend timeout
                mTimeout.removeCallbacks(onTimeout);

                if(mByteToSend!=mByteSend)
                    mCallback.onLoadFwProgresUpdate(FwUpgradeConsoleNucleo.this,mFile,
                            mByteToSend-mByteSend);
                else {
                    onLoadComplete(true);
                    return;
                }

                int byteToRead = (int)Math.min(mByteToSend-mByteSend,MAX_MSG_SIZE);
                byte buffer[] = new byte[byteToRead];
                try {
                    int byteRead = mFileData.read(buffer);
                    mByteSend+=byteRead;
                    mLastPackageSend=buffer;
                    if(byteToRead==byteRead) {
                        mConsole.write(buffer);
                        mTimeout.postDelayed(onTimeout,LOST_MSG_TIMEOUT_MS);
                    }else
                        //it read an unaspected number of byte, something bad happen
                        onLoadComplete(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    onLoadComplete(false);
                }//try-catch
            }else if(message.equals(NACK_MSG)){ //error
                Log.d("onStdOutReceived:","nack");
                onLoadComplete(false);

                /*mTimeout.removeCallbacks(onTimeout);
                mConsole.write(mLastPackageSend);
                mTimeout.postDelayed(onTimeout,LOST_MSG_TIMEOUT_MS);
                */
            }
        }//onStdOutReceived

        @Override
        public void onStdErrReceived(Debug debug, String message) { }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) { }
    };

    /**
     * object used for manage the get board id command
     */
    private UploadFileProtocol mConsoleUpgradeFw = new UploadFileProtocol();

    /**
     * handler used for the command timeout
     */
    private Handler mTimeout;

    /**
     * build a debug console without a callback
     * @param console console to use for send the command
     */
    FwUpgradeConsoleNucleo(Debug console){
        this(console,null);
    }

    /**
     *
     * @param console console where send the command
     * @param callback object where notify the command answer
     */
    FwUpgradeConsoleNucleo(Debug console, FwUpgradeConsole.FwUpgradeCallback callback) {
        super(console,callback);
        mTimeout = new Handler(Looper.getMainLooper());
        mBuffer = new StringBuilder();

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
    public boolean readVersion(@FirmwareType int fwType) {
        if (isWaitingAnswer())
            return false;

        mBuffer.setLength(0); //reset the buffer
        setConsoleListener(mConsoleGetFwVersion);
        mConsoleGetFwVersion.requestVersion(fwType);
        return true;
    }

    @Override
    public boolean loadFw(@FirmwareType int fwType,final Uri fwFile) {
        if (isWaitingAnswer())
            return false;

        mBuffer.setLength(0); //reset the buffer

        setConsoleListener(mConsoleUpgradeFw);
        mConsoleUpgradeFw.loadFile(fwType,fwFile);
        return  true;
    }
}
