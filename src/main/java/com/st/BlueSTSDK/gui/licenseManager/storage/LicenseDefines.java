package com.st.BlueSTSDK.gui.licenseManager.storage;

import android.support.annotation.Nullable;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo.LicenseType;

import java.util.ArrayList;

/**
 * class containing the list of license that are manage by the library
 */
public class LicenseDefines {

    private static ArrayList<LicenseInfo> sLicenseInfo = new ArrayList<>();

    static{
        //Mems sensor fusion
        sLicenseInfo.add(new LicenseInfo("FX","osxMotionFX","OSX_MOTION_FX_V140",
                "MotionFX v1.4.0 - 6x/9x Sensor Fusion",
                LicenseType.OpenMems, R.raw.fx_disclaimer,  R.string.licenseDesc_FX));

        //Mems activity recognition
        sLicenseInfo.add(new LicenseInfo("AR","osxMotionAR","OSX_MOTION_AR_V130",
                "MotionAR v1.3.0 - Activity Recognition",
                LicenseType.OpenMems,R.raw.generic_disclaimer, R.string.licenseDesc_AR));

        //Mems carry position
        sLicenseInfo.add(new LicenseInfo("CP","osxMotionCP","OSX_MOTION_CP_V120",
                "MotionCP v1.2.0 - Carry Position Recognition",
                LicenseType.OpenMems,R.raw.generic_disclaimer, R.string.licenseDesc_CP));

        //Mems gesture recognition
        sLicenseInfo.add(new LicenseInfo("GR","osxMotionGR","OSX_MOTION_GR_V110",
                "MotionGR v1.1.0 - Gesture Recognition",
                LicenseType.OpenMems,R.raw.generic_disclaimer,R.string.licenseDesc_GR));

        //Mems pedometer
        sLicenseInfo.add(new LicenseInfo("PM","osxMotionPM","OSX_MOTION_PM_V100",
                "MotionPM v1.0.0 - Pedometer",
                LicenseType.OpenMems,R.raw.generic_disclaimer,R.string.licenseDesc_PM));

        //Mems motion ID
        sLicenseInfo.add(new LicenseInfo("ID","osxMotionID","OSX_MOTION_ID_V100",
                "MotionID v1.0.0 - Intensity Detection",
                LicenseType.OpenMems,R.raw.generic_disclaimer,R.string.licenseDesc_ID));

        //Audio source localization
        sLicenseInfo.add(new LicenseInfo("SL","osxAcusticSL","OSX_ACOUSTIC_SL_V110",
                "AcousticSL v1.1.0 - Acoustic source-localization",
                LicenseType.OpenAudio,R.raw.generic_disclaimer,R.string.licenseDesc_SL));

        //Audio BlueVoice
        /*sLicenseInfo.add(new LicenseInfo("BV","osxAudioBV","OSX_BLUEVOICE_V110",
                "BlueVoice v1.1.0 - Voice over BTLE",
                LicenseType.OpenAudio,R.raw.generic_disclaimer,R.string.licenseDesc_BV));
        */
        sLicenseInfo.add(new LicenseInfo("BV","osxAudioBV","OSX_BLUEVOICE_V200",
                "BlueVoice v2.0.0 - Voice over BTLE",
                LicenseType.OpenAudio,R.raw.generic_disclaimer,R.string.licenseDesc_BV));

        sLicenseInfo.add(new LicenseInfo("BF","osxAudioBF","OSX_ACOUSTIC_BF_V110",
                "AcousticBF v1.1.0 - Acoustic beam-forming",
                LicenseType.OpenAudio,R.raw.generic_disclaimer,R.string.licenseDesc_BF));

    }

    /**
     * return the license info that has a specific short name
     * @param shortName search key, the comparison are case insensitive
     * @return the license info or null if the shortName is not found
     */
    public static @Nullable LicenseInfo getLicenseInfo(String shortName){
        for(LicenseInfo i: sLicenseInfo){
            if(i.shortName.equalsIgnoreCase(shortName))
                return i;
        }//for
        return null;
    }//getLicenseInfo

}//LicenseDefines