package com.st.BlueNRG.fwUpgrade;

import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.advertise.AdvertiseFilter;
import com.st.BlueSTSDK.Utils.advertise.AdvertiseParser;
import com.st.BlueSTSDK.Utils.advertise.BleAdvertiseInfo;

import java.util.Arrays;
import java.util.UUID;

import static com.st.BlueSTSDK.Utils.advertise.AdvertiseParser.split;

public class BlueNRGAdvertiseFilter implements AdvertiseFilter {

    private static final String DEFAULT_NAME = "BlueNRG OTA";
    private static final byte[] OTA_SERVICE_UUID =
            new byte[] {(byte)0x8a,(byte)0x97,(byte)0xf7,(byte)0xc0,(byte)0x85,(byte)0x06,(byte)0x11,
                    (byte)0xe3,(byte)0xba,(byte)0xa7,(byte)0x08,(byte)0x00,(byte)0x20,(byte)0x0c,(byte)0x9a,
                    (byte)0x66};


    public class BlueNRGAdvertiseInfo extends BleAdvertiseInfo{

        private UUID mExportedService;

        public BlueNRGAdvertiseInfo(String mName, byte mTxPower, String mAddress, int mFeatureMap, byte mDeviceId,
                                    short mProtocolVersion, Node.Type mBoardType, boolean mBoardSleeping, boolean mHasGeneralPurpose,
                                    UUID exportedService) {
            super(mName, mTxPower, mAddress, mFeatureMap, mDeviceId, mProtocolVersion, mBoardType, mBoardSleeping, mHasGeneralPurpose);
            mExportedService = exportedService;
        }

        public UUID getExportedService() {
            return mExportedService;
        }
    }

    @Nullable
    @Override
    public BleAdvertiseInfo filter(byte[] advData) {
        SparseArray<byte[]> splitAdv = split(advData);
        byte[] exportedService = splitAdv.get(AdvertiseParser.INCOMPLETE_LIST_OF_128_UUID);
        if(exportedService!=null && Arrays.equals(OTA_SERVICE_UUID,exportedService)){
            return new BlueNRGAdvertiseInfo(DEFAULT_NAME,(byte)0,null,0,
                    (byte)4,(short) 1,Node.Type.STEVAL_IDB008VX,
                    false,false,
                    UUID.nameUUIDFromBytes(OTA_SERVICE_UUID));
        }
        return null;

    }
}
