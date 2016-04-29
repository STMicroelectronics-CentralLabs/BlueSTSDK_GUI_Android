package com.st.BlueSTSDK.gui.licenseManager.storage;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringRes;

/**
 * class that contains the information about a license
 */
public class LicenseInfo implements Parcelable{
    /**
     * short name for the license (2 char)
     */
    public final String shortName;

    /**
     * license full name
     */
    public final String longName;

    /**
     * resource url with the license that the use have to accept for request the license
     */
    public final String licensePage; //is not a resource since it not need to be localized

    /**
     * license description
     */
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
        parcel.writeString(licensePage);
        parcel.writeInt(licenseDesc);
    }
}
