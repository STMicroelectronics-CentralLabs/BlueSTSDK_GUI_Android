package com.st.STM32WB.p2pDemo.feature;

import com.st.BlueSTSDK.Features.DeviceTimestampFeature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.STM32WB.p2pDemo.Peer2PeerDemoConfiguration;

import java.util.Locale;

/**
 * Feature used to know the device connected in the network, the network can contain at maximum 6
 * devices, for each device you can query if it is connected or not.
 */
public class FeatureNetworkStatus extends DeviceTimestampFeature {

    /** feature name */
    public static final String FEATURE_NAME = "Network Status";
    /** name of the data exported by this feature */
    public static final String FEATURE_DATA_NAME_FORMAT = "Device %d";
    /** maximum value for the feature data */
    public static final short DATA_MAX = 1;
    /** minimum value for the feature data */
    public static final short DATA_MIN = 0;

    public static final byte MAX_MANAGED_DEVICE = 6;

    /**
     *
     * @param sample feature sample
     * @param device device to query
     * @return true if the device is connected, false otherwise
     */
    public static boolean isDeviceConnected(Sample sample,
                                            Peer2PeerDemoConfiguration.DeviceID device){
        int index = device.getId()-1;
        if(hasValidIndex(sample,index)){
            byte isConnected = sample.data[index].byteValue();
            return  isConnected == 0x01;
        }//if
        return false;
    }

    /**
     * each device has its field with a boolean value to know if it is connected or not
     * @return list of fields that describes the network node status
     */
    private static Field[] createFieldDesc(){
        Field[] fields = new Field[MAX_MANAGED_DEVICE];
        for(int i = 0;i<MAX_MANAGED_DEVICE;i++){
            fields[i]=new Field(String.format(Locale.getDefault(),FEATURE_DATA_NAME_FORMAT,i+1),
                    null, Field.Type.UInt8, DATA_MAX,DATA_MIN);
        }
        return fields;
    }

    /**
     * build a carry position feature
     * @param n node that will send data to this feature
     */
    public FeatureNetworkStatus(Node n) {
        super(FEATURE_NAME, n, createFieldDesc());
    }//FeatureNetworkStatus

    /**
     * extract the End Dev Mgt Data from 4 byte
     * @param data array where read the Field data
     * @param dataOffset offset where start to read the data
     * @return number of read byte (7) and data extracted (the battery information)
     * @throws IllegalArgumentException if the data array has not enough data
     */
    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        if (data.length - dataOffset < MAX_MANAGED_DEVICE)
            throw new IllegalArgumentException("There are no "+MAX_MANAGED_DEVICE+" bytes available to read");

        Number[] dataPkt = new Number[MAX_MANAGED_DEVICE];
        for (int i=0; i<data.length; i++) {
            dataPkt[i] = data[dataOffset+i];
        }

        Sample temp = new Sample(dataPkt,getFieldsDesc());
        return new ExtractResult(temp,MAX_MANAGED_DEVICE);

    }


}

