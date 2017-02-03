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
package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;

import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.FwFileDescriptor;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface to implement for upload a file into the node, usually for upgrade the firmware.
 * it can handle the upgrade of the node firmware and the bluetooth firmware
 */
public abstract class FwUpgradeConsole {

    /**
     * enum for choose the type of firmware to upload
     */
    @IntDef({BLE_FW, BOARD_FW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FirmwareType {}

    /**
     * constant used for upload the bluetooth low energy firmware
     */
    public static final int BLE_FW = 0;

    /**
     * constant used for upload the node firmware
     */
    public static final int BOARD_FW = 1;

    /**
     * get an instance of this class that works with the node
     * @param node node where upload the firmware
     * @return null if isn't possible upload the firmware in the node, or a class for do it
     */
    static public @Nullable FwUpgradeConsole getFwUpgradeConsole(Node node){
        Debug debug = node.getDebug();
        if(debug==null)
            return null;

        switch (node.getType()) {
            case NUCLEO:
            case SENSOR_TILE:
            case BLUE_COIN:
                return new FwUpgradeConsoleNucleo(debug);
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

    /**
     * @return true if the class is already executing a command
     */
    abstract public boolean isWaitingAnswer();

    /**
     * ask to the node the firmware version, the result will be notify using the method:
     * {@link FwUpgradeConsole.FwUpgradeCallback#onVersionRead(FwUpgradeConsole, int, FwVersion)}
     * @param type version to read
     * @return true if the command is correctly send
     */
    abstract public boolean readVersion(@FirmwareType int type);

    /**
     * upload the file into the node
     * @param type type of firmware that we are going to upload
     * @param fwFile file path
     * @return true if the command is correctly start
     */
    abstract public boolean loadFw(@FirmwareType int type, FwFileDescriptor fwFile);

    /**
     * change the object where notify the commands answer
     * @param callback object where notify the commands answer
     */
    public void setLicenseConsoleListener(FwUpgradeCallback callback) {
        mCallback = callback;
    }

    /**
     * Interface with the callback for the  command send by this class
     */
    public interface FwUpgradeCallback{

        /**
         * enum with the possible upload error
         */
        @IntDef({ERROR_CORRUPTED_FILE, ERROR_TRANSMISSION,ERROR_INVALID_FW_FILE,ERROR_UNKNOWN})
        @Retention(RetentionPolicy.SOURCE)
        @interface UpgradeErrorType {}

        /**
         * error fired when the crc computed in the node isn't equal to the one computed on the
         * mobile.
         * this can happen when there are some error during the transmission
         */
        int ERROR_CORRUPTED_FILE = 0;

        /**
         * error fired when is not possible upload all the file
         */
        int ERROR_TRANSMISSION = 1;

        /**
         * error fired when is not possible open the file to upload
         */
        int ERROR_INVALID_FW_FILE=2;

        /**
         * unknown error
         */
        int ERROR_UNKNOWN=3;

        /**
         * called when the node respond to the readVersion command
         * @param console object where the readVersion was called
         * @param type version read
         * @param version object with the version read
         */
        void onVersionRead(FwUpgradeConsole console,@FirmwareType int type, FwVersion version);

        /**
         * called when the loadFw finish correctly
         * @param console object where loadFw was called
         * @param fwFile file upload to the node
         */
        void onLoadFwComplete(FwUpgradeConsole console, FwFileDescriptor fwFile);

        /**
         * called when the loadFw finish with an error
         * @param console object where loadFw was called
         * @param fwFile file upload fail
         * @param error error happen during the upload
         */
        void onLoadFwError(FwUpgradeConsole console, FwFileDescriptor fwFile,
                           @UpgradeErrorType int error);

        /**
         * function called for notify to the user that the uploading is running
         * @param console object where loadFw was called
         * @param fwFile file that we are uploading
         * @param loadBytes bytes loaded to the board
         */
        void onLoadFwProgressUpdate(FwUpgradeConsole console, FwFileDescriptor fwFile, long loadBytes);
    }

    /**
     * Utility class that implement the {@link FwUpgradeCallback} interface with empty methods
     */
    public static class SimpleFwUpgradeCallback implements FwUpgradeCallback{

        @Override
        public void onVersionRead(FwUpgradeConsole console, @FirmwareType int type,
                                  FwVersion version) {  }

        @Override
        public void onLoadFwComplete(FwUpgradeConsole console, FwFileDescriptor fwFile) {  }

        @Override
        public void onLoadFwError(FwUpgradeConsole console, FwFileDescriptor fwFile,
                                  @UpgradeErrorType int error) {       }

        @Override
        public void onLoadFwProgressUpdate(FwUpgradeConsole console, FwFileDescriptor fwFile, long loadBytes) { }
    }

}
