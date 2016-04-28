package com.st.BlueSTSDK.gui.licenseManager.storage;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;

public class LicenseInfo implements Parcelable{
    public final String shortName;
    public final String longName;
    public final String licensePage;
    public final @StringRes int licenseDesc;

    LicenseInfo(String shortName, String longName, String licensePage, int licenseDesc){
        this.shortName = shortName;
        this.longName = longName;
        this.licensePage = licensePage;
        this.licenseDesc = licenseDesc;
    }

    protected LicenseInfo(Parcel in) {
        shortName = in.readString();
        longName = in.readString();
        licensePage = in.readString();
        licenseDesc = in.readInt();
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
        parcel.writeString(licensePage);
        parcel.writeInt(licenseDesc);
    }
}
