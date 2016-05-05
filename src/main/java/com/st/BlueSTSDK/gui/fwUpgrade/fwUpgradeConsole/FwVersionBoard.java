package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;

import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.IllegalVersionFormatException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FwVersionBoard extends FwVersion {

    private static final Pattern PARSE_FW_VERSION=Pattern.compile("(.*)_(.*)_(\\d+)\\.(\\d+)\\.(\\d+)");

    private String name;
    private String mcuType;

    public FwVersionBoard(CharSequence version) throws IllegalVersionFormatException {
        Matcher matcher = PARSE_FW_VERSION.matcher(version);
        if (!matcher.matches())
            throw new IllegalVersionFormatException();
        mcuType = matcher.group(1);
        name = matcher.group(2);
        majorVersion = Integer.parseInt(matcher.group(3));
        minorVersion = Integer.parseInt(matcher.group(4));
        patchVersion = Integer.parseInt(matcher.group(5));
    }

    public String getMcuType() {
        return mcuType;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return ""+majorVersion+"."+minorVersion+"."+patchVersion+"( "+mcuType+" )";
    }

}
