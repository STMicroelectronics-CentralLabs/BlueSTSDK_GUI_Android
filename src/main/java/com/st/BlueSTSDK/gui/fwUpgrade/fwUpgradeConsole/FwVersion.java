package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FwVersion {

    private static final Pattern PARSE_FW_VERSION=Pattern.compile("(.*)_(.*)_(\\d+)\\.(\\d+)\\.(\\d+)");

    private String name;
    private String mcuType;
    private int majorVersion;
    private int minorVersion;
    private int patchVersion;

    public FwVersion(CharSequence version){
        Matcher matcher = PARSE_FW_VERSION.matcher(version);
        matcher.matches();
        mcuType = matcher.group(1);
        name = matcher.group(2);
        majorVersion = Integer.parseInt(matcher.group(3));
        minorVersion = Integer.parseInt(matcher.group(4));
        patchVersion = Integer.parseInt(matcher.group(5));
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public String getMcuType() {
        return mcuType;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public String getName() {
        return name;
    }

    public int getPatchVersion() {
        return patchVersion;
    }

    @Override
    public String toString() {
        return ""+majorVersion+"."+minorVersion+"."+patchVersion+"( "+mcuType+" )";
    }
}
