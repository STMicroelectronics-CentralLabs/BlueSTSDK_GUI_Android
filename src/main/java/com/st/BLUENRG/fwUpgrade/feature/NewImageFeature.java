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
package com.st.BLUENRG.fwUpgrade.feature;

import android.util.Log;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.DeviceTimestampFeature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;

import java.util.Arrays;

public class NewImageFeature extends DeviceTimestampFeature {

    private static final String FEATURE_NAME = "Write or Read Memory Param";
    private static final String[] FEATURE_DATA_NAME = {"OtaAckEvery", "ImageSize", "BaseAddress"};
    /** max value of one component*/
    private static final long DATA_MAX = 0xFFFFFFFF;
    /** min value of one component*/
    private static final long DATA_MIN = 0;

    /** index where you can find gyroscope value/description in the x direction */
    private static final int OtaAckEvery_INDEX = 0;
    /** index where you can find gyroscope value/description in the y direction*/
    private static final int ImageSize_INDEX = 1;
    /** index where you can find gyroscope value/description in the y direction*/
    private static final int BaseAddress_INDEX = 2;


    public NewImageFeature(Node n){
        super(FEATURE_NAME,n,new Field[]{
                new Field(FEATURE_DATA_NAME[OtaAckEvery_INDEX],null, Field.Type.UInt8,255,0),
                new Field(FEATURE_DATA_NAME[ImageSize_INDEX],null, Field.Type.UInt32,DATA_MAX,DATA_MIN),
                new Field(FEATURE_DATA_NAME[BaseAddress_INDEX],null, Field.Type.UInt32,DATA_MAX,DATA_MIN)
        });
    }

    public static byte getOtaAckEvery(Sample s){
        if(hasValidIndex(s,OtaAckEvery_INDEX))
            return s.data[OtaAckEvery_INDEX].byteValue();
        //else
        return 0;
    }

    public static long getImageSize(Sample s){
        if(hasValidIndex(s,ImageSize_INDEX))
            return s.data[ImageSize_INDEX].longValue();
        //else
        return 0;
    }

    public static long getBaseAddress(Sample s){
        if(hasValidIndex(s,BaseAddress_INDEX))
            return s.data[BaseAddress_INDEX].intValue();
        //else
        return DATA_MAX;
    }

    public void writeParamMem(int protocolVer,byte otaAckEvery,long imageSize, long baseAddress, long crcValue, byte cmdValue){
        int nByte = 9;
//        if(protocolVer >= 0x12)// not supported
//            nByte = 14;
        byte buffer[] = new byte[nByte];
        byte temp[];
        buffer[0] = otaAckEvery;
        temp = NumberConversion.LittleEndian.uint32ToBytes(imageSize);
        System.arraycopy(temp,0,buffer,1,temp.length);
        temp = NumberConversion.LittleEndian.uint32ToBytes(baseAddress);
        System.arraycopy(temp,0,buffer,5,temp.length);
//        if(protocolVer >= 0x12) {// not supported
//            temp = NumberConversion.LittleEndian.uint32ToBytes(crcValue);
//            System.arraycopy(temp,0,buffer,9,temp.length);
//            buffer[13] = cmdValue;
//        }

        writeData(buffer);
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        int numByte = 9;

        if (data.length - dataOffset < numByte)
            throw new IllegalArgumentException("There are byte available to read");

        byte otaAckEvery = data[dataOffset];
        long imageSize = NumberConversion.LittleEndian.bytesToUInt32(data,dataOffset+1);
        long baseAddress = NumberConversion.LittleEndian.bytesToUInt32(data,dataOffset+5);

        return new ExtractResult(new Sample(new Number[]{otaAckEvery,imageSize,baseAddress},getFieldsDesc()),numByte);
    }
}