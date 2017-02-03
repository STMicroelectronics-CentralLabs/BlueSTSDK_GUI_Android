/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */

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
