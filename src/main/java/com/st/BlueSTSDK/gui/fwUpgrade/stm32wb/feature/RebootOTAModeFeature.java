package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature;

import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

public class RebootOTAModeFeature extends Feature {
    private static final String FEATURE_NAME = "Rebot OTA Mode";
    private static final Field[] DATA_DESC = new Field[0];

    private static final byte REBOOT_OTA_MODE = 0x01;

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n        node that will update this feature
     */
    public RebootOTAModeFeature(Node n) {
        super(FEATURE_NAME, n, DATA_DESC);
    }

    public void rebootToFlash(short sectorOffset,short numSector){
        writeData(new byte[]{REBOOT_OTA_MODE,(byte) sectorOffset,(byte)numSector});
    }


    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        return new ExtractResult(null,0);
    }
}
