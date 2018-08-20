package com.st.STM32WB.p2pDemo.feature;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.STM32WB.p2pDemo.Peer2PeerDemoConfiguration;

/**
 * Write feature used to switch on and off the board led
 */
public class FeatureControlLed extends Feature {
    public static final String FEATURE_NAME = "ControlLed";

    private static final byte SWITCH_ON_COMMAND  = 0x01;
    private static final byte SWITCH_OFF_COMMAND = 0x00;

    /**
     * build a carry position feature
     * @param n node that will send data to this feature
     */
    public FeatureControlLed(Node n) {
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
    public void switchOnLed(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(),SWITCH_ON_COMMAND});
    }

    /**
     *
     * @param device device where switch off the led
     */
    public void switchOffLed(Peer2PeerDemoConfiguration.DeviceID device){
        writeData(new byte[]{device.getId(),SWITCH_OFF_COMMAND});
    }

}
