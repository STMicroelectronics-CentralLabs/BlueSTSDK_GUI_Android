package com.st.BlueSTSDK.gui.licenseManager.licenseConsole;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.licenseManager.LicenseStatus;

import java.util.List;

/**
 * Class that use the debug console for send/receive information about the license status
 */
public abstract class LicenseConsole {

    static public LicenseConsole getLicenseConsole(Node node){
        switch (node.getType()) {
            case STEVAL_WESU1:
                return new LicenseConsoleWesu(node.getDebug());
            case NUCLEO:
                return new LicenseConsoleNucleo(node.getDebug());
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
    protected LicenseConsoleCallback mCallback;

    /**
     *
     * @param console console where send the command
     * @param callback object where notify the command answer
     */
    protected LicenseConsole(Debug console, LicenseConsoleCallback callback) {
        mConsole = console;
        mCallback = callback;
    }

    /**
     * true if the object is waiting an answer from the console
     *
     * @return true if the object is busy
     */
    public abstract boolean isWaitingAnswer();

    /**
     * change the object where notify the commands answer
     * @param callback object where notify the commands answer
     */
    public void setLicenseConsoleListener(LicenseConsoleCallback callback) {
        mCallback = callback;
    }

    /**
     * request to read the board id
     * the board id is notify using the method
     * {@link LicenseConsoleCallback#onBoardIdRead(LicenseConsole, String)}
     * @return true if the command is correctly send
     */
    public abstract boolean readBoardId();

    /**
     * request to the node the license status.
     * the response is notify using the method
     * {@link LicenseConsoleCallback#onLicenseStatusRead(LicenseConsole , List)}
     * @return true if the request is correctly send, false otherwise.
     */
    public abstract boolean readLicenseStatus();

    /**
     * upload the license to the node, the success fill be notify using the method
     * {@link LicenseConsoleCallback#onLicenseLoad(LicenseConsole , boolean)}
     * @param licName license name
     * @param licCode license code
     * @return true if the message is correctly send, false otherwise
     */
    public abstract boolean writeLicenseCode(String licName, byte[] licCode);

    /**
     * callback interface used to notify to the caller that we receive an answer from the node
     */
    public interface LicenseConsoleCallback {

        /**
         * function called when we receive the board id.
         *
         * @param console object that request the board id
         * @param uid     board id
         */
        void onBoardIdRead(LicenseConsole console, String uid);

        /**
         * function called when we receive the license status from the board
         *
         * @param console  object that request the board status
         * @param licenses list of license available on the node
         */
        void onLicenseStatusRead(LicenseConsole console, List<LicenseStatus> licenses);

        /**
         * Function called when the license is fully load into the node
         *
         * @param console object that load the license
         * @param status  true if the license is valid, false otherwise
         */
        void onLicenseLoad(LicenseConsole console, boolean status);
    }//LicenseConsoleCallback

}
