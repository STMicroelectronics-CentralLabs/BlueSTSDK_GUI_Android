package com.st.BlueSTSDK.gui.licenseManager;

import android.os.Parcel;
import android.os.Parcelable;

import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;

/**
 * Class containing the license information: the name and if it is present or not.
 */
public class LicenseStatus implements Parcelable{

    /**
     * License name
     */
    public final LicenseInfo info;

    /**
     * True if the license is present
     */
    public final boolean isPresent;

    /**
     * Create a new license status
     * @param lic license info
     * @param status license status
     */
    public LicenseStatus(LicenseInfo lic,boolean status){
        info=lic;
        isPresent=status;
    }


    ////////////////Start Parcelable Interface///////////////////////////

    protected LicenseStatus(Parcel in) {
        info = in.readParcelable(LicenseInfo.class.getClassLoader());
        isPresent = in.readByte() != 0;
    }

    public static final Creator<LicenseStatus> CREATOR = new Creator<LicenseStatus>() {
        @Override
        public LicenseStatus createFromParcel(Parcel in) {
            return new LicenseStatus(in);
        }

        @Override
        public LicenseStatus[] newArray(int size) {
            return new LicenseStatus[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(info, i);
        parcel.writeByte((byte) (isPresent ? 1 : 0));
    }

    ////////////////End Parcelable Interface///////////////////////////
}
