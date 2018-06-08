package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature;


import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;

public class OTAControlFeature extends Feature {

    private static final String FEATURE_NAME = "OTA Control";
    private static final Field[] DATA_DESC = new Field[0];

    private static final byte STOP_COMMAND = 0x00;
    private static final byte START_M0_COMMAND = 0x01;
    private static final byte START_M4_COMMAND = 0x02;
    private static final byte UPLOAD_FINISHED_COMMAND = 0x07;
    private static final byte CANCEL_COMMAND = 0x08;

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n        node that will update this feature
     */
    public OTAControlFeature(Node n) {
        super(FEATURE_NAME, n, DATA_DESC);
    }

    public void startUpload(@FirmwareType int type, int address){
        byte command[] = NumberConversion.LittleEndian.uint32ToBytes(address);
        command[0] = type == FirmwareType.BLE_FW ? START_M0_COMMAND : START_M4_COMMAND;
        writeData(command);
    }

    public void uploadFinished(){
        writeData(new byte[]{UPLOAD_FINISHED_COMMAND});
    }

    public void cancelUpload(){
        writeData(new byte[]{CANCEL_COMMAND});
    }
    public void stopUpload(){
        writeData(new byte[]{STOP_COMMAND});
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        return new ExtractResult(null,0);
    }
}