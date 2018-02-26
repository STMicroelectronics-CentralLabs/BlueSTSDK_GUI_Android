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
