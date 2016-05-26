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
     * True if the license is present on the board
     */
    public final boolean isPresentOnTheBoard;

    /**
     * True if the license is present on the mobile DB
     */
    public boolean isPresentOnDB;

    /**
     * Create a new license status
     * @param lic license info
     * @param boardLicStatus license board status
     * @param DBLicStatus true if the license is present in the mobile db
     */
    public LicenseStatus(LicenseInfo lic,boolean boardLicStatus,boolean DBLicStatus){
        info=lic;
        isPresentOnTheBoard =boardLicStatus;
        isPresentOnDB=DBLicStatus;
    }

    public LicenseStatus(LicenseInfo lic,boolean boardLicStatus){
        this(lic,boardLicStatus,false);
    }


    ////////////////Start Parcelable Interface///////////////////////////

    protected LicenseStatus(Parcel in) {
        info = in.readParcelable(LicenseInfo.class.getClassLoader());
        isPresentOnTheBoard = in.readByte() != 0;
        isPresentOnDB = in.readByte() != 0;
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
        parcel.writeByte((byte) (isPresentOnTheBoard ? 1 : 0));
        parcel.writeByte((byte) (isPresentOnDB ? 1 : 0));
    }

    ////////////////End Parcelable Interface///////////////////////////
}
