package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature;

import com.st.BlueSTSDK.Utils.UUIDToFeatureMap;

import java.util.UUID;

public class OTASupportFeature extends UUIDToFeatureMap {

    public OTASupportFeature(){
        put(UUID.fromString("0000fe11-8e22-4541-9d4c-21edae82ed19"), RebootOTAModeFeature.class);

    }

}
