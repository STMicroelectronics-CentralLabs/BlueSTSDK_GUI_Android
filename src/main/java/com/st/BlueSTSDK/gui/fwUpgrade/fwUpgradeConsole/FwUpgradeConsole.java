package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Node;

public abstract class FwUpgradeConsole {

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

    abstract public boolean readVersion();

    abstract public boolean loadFw(Uri fwFile);

    /**
     * change the object where notify the commands answer
     * @param callback object where notify the commands answer
     */
    public void setLicenseConsoleListener(FwUpgradeCallback callback) {
        mCallback = callback;
    }

    public interface FwUpgradeCallback{
        void onVersionRead(FwUpgradeConsole console, FwVersion version);
        void onLoadFwComplete(FwUpgradeConsole console, Uri fwFile, boolean status);
        void onLoadFwProgresUpdate(FwUpgradeConsole console, Uri fwFile, long loadBytes);
    }

}
