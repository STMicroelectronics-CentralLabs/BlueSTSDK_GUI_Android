package com.st.BlueSTSDK.gui.licenseManager.storage;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

/**
 * Define the db format, it has a single table with the license
 */
public class LicenseManagerDBContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public LicenseManagerDBContract() {}

    /* A entry in the license table */
    public static class LicenseEntry implements BaseColumns, Parcelable {
        public static final String TABLE_NAME = "License";
        public static final String COLUMN_NAME_BOARD_ID = "BoardId";
        public static final String COLUMN_NAME_LICENSE_TYPE = "Type";
        public static final String COLUMN_NAME_LICENSE_CODE = "Code";

        /**
         * Key used inside the db
         */
        private long id;

        /**
         * Board id that will use the license
         */
        private String boardId;

        /**
         * Name of the license
         */
        private String licenseType;

        /**
         * license code
         */
        private byte[] licenseCode;

        /**
         * create a new license entry
         * @param boardId board id
         * @param licenseType license name
         * @param licenseCode license code
         */
        public LicenseEntry(String boardId, String licenseType,byte[] licenseCode) {
            this.boardId = boardId;
            this.id=-1;
            this.licenseCode = licenseCode;
            this.licenseType = licenseType;
        }

        long getId() {
            return id;
        }

        void setId(long id) {
            this.id = id;
        }

        public byte[] getLicenseCode() {
            return licenseCode;
        }

        public void setLicenseCode(byte[] licenseCode) {
            this.licenseCode = licenseCode;
        }

        public String getLicenseType() {
            return licenseType;
        }

        public void setLicenseType(String licenseType) {
            this.licenseType = licenseType;
        }

        public String getBoardId() {
            return boardId;
        }

        public void setBoardId(String boardId) {
            this.boardId = boardId;
        }

        ////////////////Start Parcelable Interface///////////////////////////

        @Override
        public int describeContents() {
            return 0;
        }

        protected LicenseEntry(Parcel in) {
            id = in.readLong();
            boardId = in.readString();
            licenseType = in.readString();
            licenseCode = in.createByteArray();
        }

        public static final Creator<LicenseEntry> CREATOR = new Creator<LicenseEntry>() {
            @Override
            public LicenseEntry createFromParcel(Parcel in) {
                return new LicenseEntry(in);
            }

            @Override
            public LicenseEntry[] newArray(int size) {
                return new LicenseEntry[size];
            }
        };

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeLong(id);
            parcel.writeString(boardId);
            parcel.writeString(licenseType);
            parcel.writeByteArray(licenseCode);
        }

        ////////////////End Parcelable Interface///////////////////////////
    }

}
