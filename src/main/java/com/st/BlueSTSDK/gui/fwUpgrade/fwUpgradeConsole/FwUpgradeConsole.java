package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;

import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Node;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class FwUpgradeConsole {

    @IntDef({BLE_FW, BOARD_FW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FirmwareType {}

    public static final int BLE_FW = 0;
    public static final int BOARD_FW = 1;

    static public @Nullable FwUpgradeConsole getFwUpgradeConsole(Node node){
        switch (node.getType()) {
            case NUCLEO:
            case SENSOR_TILE:
            case BLUE_COIN:
                return new FwUpgradeConsoleNucleo(node.getDebug());
        }
        return  null;
    }

    /**
     * console where send the command
     */
    protected Debug mConsole;

    /**
     * object where notify the command response
     */
    protected FwUpgradeCallback mCallback;

    /**
     *
     * @param console console where send the command
     * @param callback object where notify the command answer
     */
    protected FwUpgradeConsole(Debug console, FwUpgradeCallback callback) {
        mConsole = console;
        mCallback = callback;
    }

    abstract public boolean isWaitingAnswer();

    abstract public boolean readVersion(@FirmwareType int type);

    abstract public boolean loadFw(@FirmwareType int type, Uri fwFile);

    /**
     * change the object where notify the commands answer
     * @param callback object where notify the commands answer
     */
    public void setLicenseConsoleListener(FwUpgradeCallback callback) {
        mCallback = callback;
    }

    public interface FwUpgradeCallback{
        @IntDef({ERROR_CORRUPTED_FILE, ERROR_TRANSMISSION,ERROR_INVALID_FW_FILE,ERROR_UNKNOWN})
        @Retention(RetentionPolicy.SOURCE)
        @interface UpgradeErrorType {}

        int ERROR_CORRUPTED_FILE = 0;
        int ERROR_TRANSMISSION = 1;
        int ERROR_INVALID_FW_FILE=2;
        int ERROR_UNKNOWN=3;


        void onVersionRead(FwUpgradeConsole console,@FirmwareType int type, FwVersion version);
        void onLoadFwComplete(FwUpgradeConsole console, Uri fwFile);
        void onLoadFwError(FwUpgradeConsole console, Uri fwFile,
                           @UpgradeErrorType int error);
        void onLoadFwProgressUpdate(FwUpgradeConsole console, Uri fwFile, long loadBytes);
    }

    public static class SimpleFwUpgradeCallback implements FwUpgradeCallback{

        @Override
        public void onVersionRead(FwUpgradeConsole console, @FirmwareType int type, FwVersion version) {

        }

        @Override
        public void onLoadFwComplete(FwUpgradeConsole console, Uri fwFile) {

        }

        @Override
        public void onLoadFwError(FwUpgradeConsole console, Uri fwFile,
                                  @UpgradeErrorType int error) {

        }

        @Override
        public void onLoadFwProgressUpdate(FwUpgradeConsole console, Uri fwFile, long loadBytes) {

        }
    }

}
