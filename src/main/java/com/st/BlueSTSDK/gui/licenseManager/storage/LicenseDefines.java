/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

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