package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.DeviceTimestampFeature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;

public class OTABoardWillRebootFeature extends DeviceTimestampFeature {
    private static final String FEATURE_NAME = "OTA Will Reboot";

    public OTABoardWillRebootFeature(Node n){
        super(FEATURE_NAME,n,new Field[]{
                new Field("IsRebooing",null, Field.Type.UInt8,1,0)
        });
    }

    public static boolean boardIsRebooting(Sample s){
        if(hasValidIndex(s,0))
            return s.data[0].byteValue()!=0;
        return false;
    }


    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < 1)
            throw new IllegalArgumentException("There are byte available to read");
        return new ExtractResult(new Sample(new Number[]{data[dataOffset]},getFieldsDesc()),1);
    }
}
