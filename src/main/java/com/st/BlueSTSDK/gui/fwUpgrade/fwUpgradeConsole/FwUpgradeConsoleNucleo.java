package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.gui.licenseManager.LicenseStatus;
import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.LicenseConsole;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseDefines;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class FwUpgradeConsoleNucleo extends FwUpgradeConsole {

    static private final String GET_VERSION_FW="versionFw\n";
    static private final String UPLOAD_FW="updateFw%d\n";

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
    private Debug.DebugOutputListener mConsoleGetFwVersion = new Debug.DebugOutputListener() {

        @Override
        public void onStdOutReceived(Debug debug, String message) {
            if (message.endsWith("\r\n")) {
                mBuffer.append(message, 0, message.length() - 2);
                setConsoleListener(null);
                if (mCallback != null)
                    mCallback.onVersionRead(FwUpgradeConsoleNucleo.this,
                            new FwVersion(mBuffer.toString()));
            } else {
                mBuffer.append(message);
            }
        }

        @Override
        public void onStdErrReceived(Debug debug, String message) { }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) { }
    };

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
    public boolean readVersion() {
        if (isWaitingAnswer())
            return false;

        mBuffer.setLength(0); //reset the buffer
        setConsoleListener(mConsoleGetFwVersion);
        mConsole.write(GET_VERSION_FW);
        return true;
    }

    private byte[] startUploadCommand(long fileSize){
        byte[] size = NumberConversion.BigEndian.uint32ToBytes(fileSize);
        byte[] command = new byte[UPLOAD_FW.length()+size.length+1];
        for(int i=0; i<UPLOAD_FW.length();i++){
            command[i]=(byte)UPLOAD_FW.charAt(i);
        }
        System.arraycopy(command,UPLOAD_FW.length(),size,0,size.length);
        command[command.length-1]=(byte)'\n';
        return command;
    }

    @Override
    public boolean loadFw(final Uri fwFile) {
        if (isWaitingAnswer())
            return false;

        mBuffer.setLength(0); //reset the buffer

        final File f = new File(fwFile.getPath());
        final long fileSize= f.length();

        mConsole.write(String.format(UPLOAD_FW,fileSize));
        new AsyncTask<Void,Integer,Boolean>(){

            @Override
            protected Boolean doInBackground(Void... uris) {
                byte packet[] = new byte[20];
                try {
                    BufferedInputStream buf = new BufferedInputStream(new FileInputStream(f));
                    int nPacketSend = 0;
                    while (buf.read(packet,0,packet.length)>0){
                        mConsole.write(packet);
                        this.publishProgress(nPacketSend++);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                } catch (java.io.IOException e) {
                    e.printStackTrace();
                    return false;
                }

                return true;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if(mCallback!=null){
                    mCallback.onLoadFwProgresUpdate(FwUpgradeConsoleNucleo.this,fwFile,values[0]);
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
                if(mCallback!=null){
                    mCallback.onLoadFwComplete(FwUpgradeConsoleNucleo.this,fwFile,aBoolean);
                }
            }
        }.execute();
        return true;
    }
}
