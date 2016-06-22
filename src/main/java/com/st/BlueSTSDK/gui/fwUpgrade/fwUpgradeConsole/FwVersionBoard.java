package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;

import android.os.Parcel;
import android.os.Parcelable;

import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.IllegalVersionFormatException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FwVersionBoard extends FwVersion implements Parcelable{


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

    public FwVersionBoard(String boardName,String mcuType, int major,int minor,int path) {
        name = boardName;
        this.mcuType=mcuType;
        majorVersion=major;
        minorVersion=minor;
        patchVersion=path;
    }

    public String getMcuType() {
        return mcuType;
    }

    public String getName() {
        return name;
    }

    ///////////////////////Parcelable implementation//////////////////

    public static final Creator<FwVersionBoard> CREATOR = new Creator<FwVersionBoard>() {
        @Override
        public FwVersionBoard createFromParcel(Parcel in) {
            return new FwVersionBoard(in);
        }

        @Override
        public FwVersionBoard[] newArray(int size) {
            return new FwVersionBoard[size];
        }
    };

    protected FwVersionBoard(Parcel in) {
        super(in);
        mcuType = in.readString();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel,i);
        parcel.writeString(mcuType);
        parcel.writeString(name);
    }

}
