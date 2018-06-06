package com.st.BlueSTSDK.gui.fwUpgrade;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * enum for choose the type of firmware to upload
 */
@IntDef({FirmwareType.BLE_FW,FirmwareType.BOARD_FW})
@Retention(RetentionPolicy.SOURCE)
public @interface FirmwareType {

    /**
     * constant used for upload the bluetooth low energy firmware
     */
    public static final int BLE_FW = 0;

    /**
     * constant used for upload the node firmware
     */
    public static final int BOARD_FW = 1;

}
