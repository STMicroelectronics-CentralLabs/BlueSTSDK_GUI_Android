/*******************************************************************************
 * COPYRIGHT(c) 2016 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;

import android.os.Parcel;
import android.os.Parcelable;

import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.IllegalVersionFormatException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FwVersionBoard extends FwVersion implements Parcelable{


    private static final Pattern PARSE_FW_VERSION=Pattern.compile("(.*)_(.*)_(\\d+)\\.(\\d+)\\.(\\d+)");

    private String name;
    private String mcuType;

    public FwVersionBoard(CharSequence version) throws IllegalVersionFormatException {
        Matcher matcher = PARSE_FW_VERSION.matcher(version);
        if (!matcher.matches())
            throw new IllegalVersionFormatException();
        mcuType = matcher.group(1);
        name = matcher.group(2);
        majorVersion = Integer.parseInt(matcher.group(3));
        minorVersion = Integer.parseInt(matcher.group(4));
        patchVersion = Integer.parseInt(matcher.group(5));
    }

    public FwVersionBoard(String boardName,String mcuType, int major,int minor,int path) {
        name = boardName;
        this.mcuType=mcuType;
        majorVersion=major;
        minorVersion=minor;
        patchVersion=path;
    }

    public String getMcuType() {
        return mcuType;
    }

    public String getName() {
        return name;
    }

    ///////////////////////Parcelable implementation//////////////////

    public static final Creator<FwVersionBoard> CREATOR = new Creator<FwVersionBoard>() {
        @Override
        public FwVersionBoard createFromParcel(Parcel in) {
            return new FwVersionBoard(in);
        }

        @Override
        public FwVersionBoard[] newArray(int size) {
            return new FwVersionBoard[size];
        }
    };

    protected FwVersionBoard(Parcel in) {
        super(in);
        mcuType = in.readString();
        name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        super.writeToParcel(parcel,i);
        parcel.writeString(mcuType);
        parcel.writeString(name);
    }

}
