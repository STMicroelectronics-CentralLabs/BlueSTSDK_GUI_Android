package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FwVersion {

    protected int majorVersion;
    protected int minorVersion;
    protected int patchVersion;

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getPatchVersion() {
        return patchVersion;
    }

    @Override
    public String toString() {
        return majorVersion+"."+minorVersion+"."+patchVersion;
    }
}
