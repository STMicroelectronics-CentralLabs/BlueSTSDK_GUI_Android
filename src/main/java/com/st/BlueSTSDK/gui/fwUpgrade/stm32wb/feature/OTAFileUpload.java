package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

import java.io.IOException;
import java.io.InputStream;

public class OTAFileUpload extends Feature {

    public static final int CHUNK_LENGTH = 20;
    private static final String FEATURE_NAME = "OTA File Upload";
    private static final Field[] DATA_DESC = new Field[0];

    /**
     * build a new disabled feature, that doesn't need to be initialized in the node side
     *
     * @param n        node that will update this feature
     */
    public OTAFileUpload(Node n) {
        super(FEATURE_NAME, n, DATA_DESC);
    }


    public void upload(InputStream file , Runnable onUpload) throws IOException {
        byte buffer[] = new byte[CHUNK_LENGTH];
        int nReadByte;
        while ( (nReadByte = file.read(buffer))>0){
            //todo what if nReadByte!=20?
            writeData(buffer,onUpload);
            buffer = new byte[CHUNK_LENGTH];
        }
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        return new ExtractResult(null,0);
    }
}
