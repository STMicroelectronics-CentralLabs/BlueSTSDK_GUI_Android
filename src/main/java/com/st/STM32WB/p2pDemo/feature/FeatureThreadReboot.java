package com.st.STM32WB.p2pDemo.feature;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.STM32WB.p2pDemo.Peer2PeerDemoConfiguration;

public class FeatureThreadReboot extends Feature {
    public static final String FEATURE_NAME = "ThreadReboot";

    private static final byte THREAD_REBOOT_COMMAND = 0x02;

    /**
     * build a carry position feature
     * @param n node that will send data to this feature
     */
    public FeatureThreadReboot(Node n) {
        super(FEATURE_NAME, n, new Field[0]);
    }//FeatureControlLed

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        return new ExtractResult(new Sample(timestamp,new Number[0],getFieldsDesc()),0);
    }

    /**
     *
     * @param device device where switch on the led
     */
    public void rebootToThreadRadio(Peer2PeerDemoConfiguration.DeviceID device,Runnable onCommandSent){
        writeData(new byte[]{device.getId(), THREAD_REBOOT_COMMAND},onCommandSent);
    }

}
