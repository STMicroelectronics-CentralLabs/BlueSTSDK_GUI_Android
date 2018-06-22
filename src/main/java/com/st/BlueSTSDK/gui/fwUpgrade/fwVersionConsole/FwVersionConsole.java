package com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole;


import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.FwVersionConsoleSTM32WB;

public abstract  class FwVersionConsole {

    /**
     * get an instance of this class that works with the node
     *
     * @param node node where upload the firmware
     * @return null if isn't possible upload the firmware in the node, or a class for do it
     */
    static public @Nullable
    FwVersionConsole getFwVersionConsole(Node node) {
        FwVersionConsole stm32wbConsole = FwVersionConsoleSTM32WB.buildForNode(node);
        if( stm32wbConsole!=null)
            return stm32wbConsole;

        Debug debug = node.getDebug();
        if (debug == null)
            return null;

        switch (node.getType()) {
            case NUCLEO:
            case SENSOR_TILE:
            case BLUE_COIN:
                return new FwVersionConsoleNucleo(debug);
        }
        return null;
    }

    /**
     * object where notify the command response
     */
    protected FwVersionCallback mCallback;

    /**
     * @param callback object where notify the command answer
     */
    protected FwVersionConsole(FwVersionCallback callback) {
        mCallback = callback;
    }

    /**
     * ask to the node the firmware version, the result will be notify using the method:
     * {@link FwVersionConsole.FwVersionCallback#onVersionRead(FwVersionConsole, int, FwVersion)}
     *
     * @param type version to read
     * @return true if the command is correctly send
     */
    abstract public boolean readVersion(@FirmwareType int type);


    /**
     * change the object where notify the commands answer
     *
     * @param callback object where notify the commands answer
     */
    public void setLicenseConsoleListener(FwVersionCallback callback) {
        mCallback = callback;
    }

    /**
     * Interface with the callback for the  command send by this class
     */
    public interface FwVersionCallback {


        /**
         * called when the node respond to the readVersion command
         *
         * @param console object where the readVersion was called
         * @param type    version read
         * @param version object with the version read, if some error happen it will be null
         */
        void onVersionRead(FwVersionConsole console, @FirmwareType int type, @Nullable FwVersion version);

    }

}