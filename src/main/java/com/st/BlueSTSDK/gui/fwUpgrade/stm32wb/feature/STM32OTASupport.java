package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature;

import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.UUIDToFeatureMap;

import java.util.UUID;

public class STM32OTASupport{

    public static final byte OTA_NODE_ID = (byte)0x86;

    public static boolean isOTANode(@NonNull Node n){
        return n.getTypeId() ==  OTA_NODE_ID;
    }

    public static UUIDToFeatureMap getOTAFeatures(){
        UUIDToFeatureMap featureMap = new UUIDToFeatureMap();
        featureMap.put(UUID.fromString("0000fe11-8e22-4541-9d4c-21edae82ed19"), RebootOTAModeFeature.class);
        featureMap.put(UUID.fromString("0000fe22-8e22-4541-9d4c-21edae82ed19"), OTAControlFeature.class);
        featureMap.put(UUID.fromString("0000fe23-8e22-4541-9d4c-21edae82ed19"), OTABoardWillRebootFeature.class);
        featureMap.put(UUID.fromString("0000fe24-8e22-4541-9d4c-21edae82ed19"), OTAFileUpload.class);
        return featureMap;
    }

    /**
     * after the board reboot it will have the current address +1
     * @param node current node
     * @return node address/tag that the board will have when it is in ota mode
     */
    public static String getOtaAddressForNode(Node node){
        String currentAddress = node.getTag();

        int lastDigit = Short.valueOf(currentAddress.substring(currentAddress.length()-2),16);

        lastDigit = lastDigit + 1;
        return currentAddress.substring(0,currentAddress.length()-2) +
                String.format("%X",(byte)lastDigit);
    }

}
