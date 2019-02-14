/** Copyright (c) 2017  STMicroelectronics – All rights reserved
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

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Features.Field;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

public class ImageFeature extends Feature {
    private static final String FEATURE_NAME = "Read Range Memory";
    /** name of the exported data */
    public static final String[] FEATURE_DATA_NAME = {"Flash_LB", "Flash_UB", "ProtocolVer"};
    /** max value of one component*/
    public static final long DATA_MAX = 0xFFFFFFFF;
    /** min value of one component*/
    public static final long DATA_MIN = 0;

    /** index where you can find gyroscope value/description in the x direction */
    public static final int Flash_LB_INDEX = 0;
    /** index where you can find gyroscope value/description in the y direction*/
    public static final int Flash_UB_INDEX = 1;
    /** index where you can find gyroscope value/description in the y direction*/
    public static final int ProtocolVer_INDEX = 2;


    public ImageFeature(Node n){
        super(FEATURE_NAME,n,new Field[]{
                new Field(FEATURE_DATA_NAME[Flash_LB_INDEX],null, Field.Type.UInt32,DATA_MAX,DATA_MIN),
                new Field(FEATURE_DATA_NAME[Flash_UB_INDEX],null, Field.Type.UInt32,DATA_MAX,DATA_MIN),
                new Field(FEATURE_DATA_NAME[ProtocolVer_INDEX],null, Field.Type.UInt8,255,DATA_MIN)
        });
    }

    public static long getFlash_LB(Sample s){
        if(hasValidIndex(s,Flash_LB_INDEX))
            return s.data[Flash_LB_INDEX].longValue();
        //else
        return DATA_MAX;
    }

    public static long getFlash_UB(Sample s){
        if(hasValidIndex(s,Flash_UB_INDEX))
            return s.data[Flash_UB_INDEX].longValue();
        //else
        return DATA_MIN;
    }

    public static int getProtocolVer(Sample s){
        if(hasValidIndex(s,ProtocolVer_INDEX))
            return s.data[ProtocolVer_INDEX].intValue();
        //else
        return 0;
    }

    @Override
    protected ExtractResult extractData(long timestamp, byte[] data, int dataOffset) {
        int numByte = 8;

        if (data.length - dataOffset < numByte)
            throw new IllegalArgumentException("There are byte available to read");

        long flash_LB = NumberConversion.BigEndian.bytesToUInt32(data,dataOffset);
        long flash_UB = NumberConversion.BigEndian.bytesToUInt32(data,dataOffset+4);

        int protocolVer = 0x10;// server
        if (data.length >= 9) {//protocol version > 1.2
            protocolVer = data[dataOffset + 8];
            numByte++;
        }
        return new ExtractResult(new Sample(new Number[]{flash_LB,flash_UB,protocolVer},getFieldsDesc()),numByte);
    }
}
