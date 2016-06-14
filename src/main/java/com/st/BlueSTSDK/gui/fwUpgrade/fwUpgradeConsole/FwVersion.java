package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FwVersion implements Parcelable{

    protected int majorVersion;
    protected int minorVersion;
    protected int patchVersion;

    protected FwVersion(Parcel in) {
        majorVersion = in.readInt();
        minorVersion = in.readInt();
        patchVersion = in.readInt();
    }

    public FwVersion(){
        this(1,0,0);
    }

    public FwVersion(int major,int minor,int patch){
        majorVersion=major;
        minorVersion=minor;
        patchVersion=patch;
    }

    public static final Creator<FwVersion> CREATOR = new Creator<FwVersion>() {
        @Override
        public FwVersion createFromParcel(Parcel in) {
            return new FwVersion(in);
        }

        @Override
        public FwVersion[] newArray(int size) {
            return new FwVersion[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(majorVersion);
        parcel.writeInt(minorVersion);
        parcel.writeInt(patchVersion);
    }
}
