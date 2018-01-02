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
