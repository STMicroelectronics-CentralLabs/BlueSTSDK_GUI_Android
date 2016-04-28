package com.st.BlueSTSDK.gui.licenseManager.storage;

import android.support.annotation.Nullable;

import com.st.BlueSTSDK.gui.R;

import java.util.ArrayList;

public class LicenseDefines {

    private static ArrayList<LicenseInfo> sLicenseInfo = new ArrayList<>();

    static{
        sLicenseInfo.add(new LicenseInfo("FX","osxMotionFX","file:///android_asset/about" +
                ".html", R.string.licenseDesc_FX));
        sLicenseInfo.add(new LicenseInfo("AR","osxMotionAR","file:///android_asset/about" +
                ".html", R.string.licenseDesc_AR));
        sLicenseInfo.add(new LicenseInfo("CP","osxMotionCP","file:///android_asset/about" +
                ".html", R.string.licenseDesc_CP));
        sLicenseInfo.add(new LicenseInfo("GR","osxMotionGR","file:///android_asset/about" +
                ".html", R.string.licenseDesc_GR));
        sLicenseInfo.add(new LicenseInfo("PM","osxMotionPM","file:///android_asset/about" +
                ".html", R.string.licenseDesc_PM));
    }

    public static @Nullable LicenseInfo getLicenseInfo(String shortName){
        for(LicenseInfo i: sLicenseInfo){
            if(i.shortName.equalsIgnoreCase(shortName))
                return i;
        }
        return null;
    }

}