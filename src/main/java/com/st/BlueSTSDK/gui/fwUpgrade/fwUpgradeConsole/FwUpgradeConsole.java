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
        void onVersionRead(FwUpgradeConsole console,@FirmwareType int type, FwVersion version);
        void onLoadFwComplete(FwUpgradeConsole console, Uri fwFile, boolean status);
        void onLoadFwProgresUpdate(FwUpgradeConsole console, Uri fwFile, long loadBytes);
    }

}
