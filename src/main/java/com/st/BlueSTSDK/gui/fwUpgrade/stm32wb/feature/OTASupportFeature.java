package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature;

import com.st.BlueSTSDK.Utils.UUIDToFeatureMap;

import java.util.UUID;

public class OTASupportFeature extends UUIDToFeatureMap {

    public OTASupportFeature(){
        put(UUID.fromString("0000fe11-8e22-4541-9d4c-21edae82ed19"), RebootOTAModeFeature.class);
        put(UUID.fromString("0000fe22-8e22-4541-9d4c-21edae82ed19"), OTAControlFeature.class);
        put(UUID.fromString("0000fe23-8e22-4541-9d4c-21edae82ed19"), OTABoardWillRebootFeature.class);
        put(UUID.fromString("0000fe24-8e22-4541-9d4c-21edae82ed19"), OTAFileUpload.class);
    }

}
