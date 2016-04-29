package com.st.BlueSTSDK.gui.licenseManager.storage;

import android.support.annotation.Nullable;

import com.st.BlueSTSDK.gui.R;

import java.util.ArrayList;

/**
 * class containing the list of license that are manage by the library
 */
public class LicenseDefines {

    private static ArrayList<LicenseInfo> sLicenseInfo = new ArrayList<>();

    static{
        //mems sensor fusion
        sLicenseInfo.add(new LicenseInfo("FX","osxMotionFX","file:///android_asset/about" +
                ".html", R.string.licenseDesc_FX));

        //mems activity recognition
        sLicenseInfo.add(new LicenseInfo("AR","osxMotionAR","file:///android_asset/about" +
                ".html", R.string.licenseDesc_AR));

        //mems carry position
        sLicenseInfo.add(new LicenseInfo("CP","osxMotionCP","file:///android_asset/about" +
                ".html", R.string.licenseDesc_CP));

        //mems gesture recognition
        sLicenseInfo.add(new LicenseInfo("GR","osxMotionGR","file:///android_asset/about" +
                ".html", R.string.licenseDesc_GR));

        //mems pedometer
        sLicenseInfo.add(new LicenseInfo("PM","osxMotionPM","file:///android_asset/about" +
                ".html", R.string.licenseDesc_PM));
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