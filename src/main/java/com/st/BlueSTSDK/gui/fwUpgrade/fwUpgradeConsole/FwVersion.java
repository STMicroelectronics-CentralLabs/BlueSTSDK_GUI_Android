package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class that store a program version. The version is stored with 3 numbers, major version,
 * minor version and path version
 */
public class FwVersion implements Parcelable{

    protected int majorVersion;
    protected int minorVersion;
    protected int patchVersion;

    /**
     * create the default version with the value 1.0.0
     */
    public FwVersion(){
        this(1,0,0);
    }

    public FwVersion(int major,int minor,int patch){
        majorVersion=major;
        minorVersion=minor;
        patchVersion=patch;
    }

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

    //////////////////////Parcelable Interface ////////////////////////////////////

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

    protected FwVersion(Parcel in) {
        majorVersion = in.readInt();
        minorVersion = in.readInt();
        patchVersion = in.readInt();
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
