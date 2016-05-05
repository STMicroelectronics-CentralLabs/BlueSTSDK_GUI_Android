package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;

import android.support.annotation.IntDef;

import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.IllegalVersionFormatException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.IllegalFormatException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FwVersionBle extends FwVersion {

    private static final String BLE_TYPE="Ble";
    private static final String BLEMS_TYPE="BleMS";

    @IntDef({BLE,BLE_MS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BleType {}

    public static final int BLE = 0;
    public static final int BLE_MS = 1;

    private @BleType int mType;

    private static final Pattern PARSE_FW_VERSION=Pattern.compile("((?:"+BLE_TYPE+")|(?:"+
            BLEMS_TYPE+"))_(\\d+)\\.(\\d+)\\.(\\w)");

    private static int charToPatch(char c){
        if(c=='0')
            return 0;
        else
            return (c-'a')+1;
    }

    private static char patchToChar(int v){
        if(v==0){
            return '0';
        }else
            return (char) ('a'+(v-1));
    }

    public FwVersionBle(CharSequence version) throws IllegalVersionFormatException {
        Matcher matcher = PARSE_FW_VERSION.matcher(version);
        if (!matcher.matches())
            throw new IllegalVersionFormatException();
        switch (matcher.group(1)){
            case BLE_TYPE:
                mType=BLE;
                break;
            case BLEMS_TYPE:
                mType=BLE_MS;
                break;
        }
        majorVersion = Integer.parseInt(matcher.group(2));
        minorVersion = Integer.parseInt(matcher.group(3));
        patchVersion = charToPatch(matcher.group(4).charAt(0));
    }

    @Override
    public String toString() {
        return ""+majorVersion+"."+minorVersion+"."+patchToChar(patchVersion)+"( "+
                (mType==BLE ? BLE_TYPE : BLEMS_TYPE)+" )";
    }
}
