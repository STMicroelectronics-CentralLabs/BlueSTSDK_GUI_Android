package com.st.BlueSTSDK.gui.licenseManager.storage;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;

/**
 * class that contains the information about a license
 */
public class LicenseInfo implements Parcelable{

    public enum  LicenseType{
        OpenMems,
        OpenAudio,
        OpenRf;

        @Override
        public String toString() {
            switch (this){
                case OpenMems:
                    return "Open.Mems";
                case OpenAudio:
                    return "Open.Audio";
                case OpenRf:
                    return "Open.RF";
                default:
                    return super.toString();
            }
        }
    }


    /**
     * short name for the license (2 char)
     */
    public final String shortName;

    /**
     * license full name
     */
    public final String longName;

    public final LicenseType type;

    public final String requestCodeName;
    public final String requestCodeNameLong;


    /**
     * resource url with the license that the use have to accept for request the license
     */
    public final @RawRes int disclaimerFile;

    /**
     * license description
     */
    public final @StringRes int licenseDesc;

    LicenseInfo(String shortName, String longName,String requestCodeName,String requestCodeNameLong,
                LicenseType type, @RawRes int disclaimerFile, @StringRes int licenseDesc){
        this.shortName = shortName;
        this.longName = longName;
        this.requestCodeName=requestCodeName;
        this.requestCodeNameLong=requestCodeNameLong;
        this.type=type;
        this.disclaimerFile = disclaimerFile;
        this.licenseDesc=licenseDesc;
    }

    protected LicenseInfo(Parcel in) {
        shortName = in.readString();
        longName = in.readString();
        requestCodeName=in.readString();
        requestCodeNameLong = in.readString();
        disclaimerFile = in.readInt();
        licenseDesc = in.readInt();
        type = (LicenseType) in.readSerializable();
    }

    public boolean equals(LicenseManagerDBContract.LicenseEntry entry){
        return shortName.equalsIgnoreCase(entry.getLicenseType());
    }

    public static final Creator<LicenseInfo> CREATOR = new Creator<LicenseInfo>() {
        @Override
        public LicenseInfo createFromParcel(Parcel in) {
            return new LicenseInfo(in);
        }

        @Override
        public LicenseInfo[] newArray(int size) {
            return new LicenseInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(shortName);
        parcel.writeString(longName);
        parcel.writeString(requestCodeName);
        parcel.writeString(requestCodeNameLong);
        parcel.writeInt(disclaimerFile);
        parcel.writeInt(licenseDesc);
        parcel.writeSerializable(type);
    }
}
