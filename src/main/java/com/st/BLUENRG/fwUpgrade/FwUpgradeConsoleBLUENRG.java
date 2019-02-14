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
package com.st.BLUENRG.fwUpgrade;

import android.support.annotation.NonNull;
import android.util.Log;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwUpgradeConsole;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.FwFileDescriptor;
import com.st.BLUENRG.fwUpgrade.feature.ImageFeature;
import com.st.BLUENRG.fwUpgrade.feature.NewImageFeature;
import com.st.BLUENRG.fwUpgrade.feature.NewImageTUContentFeature;
import com.st.BLUENRG.fwUpgrade.feature.ExpectedImageTUSeqNumberFeature;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FwUpgradeConsoleBLUENRG extends FwUpgradeConsole {

    // smart phone androids, BLUENRG service
    private ImageFeature mRangeMem;
    private NewImageFeature mParamMem;
    private NewImageTUContentFeature mStartAckNotification;
    private ExpectedImageTUSeqNumberFeature mChunkData;
    private Node.TypeService mServiceId;

    // IMAGE packet data for OTA transfer
    private static final int DEFAULT_ATT_MTU_SIZE = 23;
    private static final int DEFAULT_WRITE_DATA_LEN = (DEFAULT_ATT_MTU_SIZE - 3); // 20 bytes
    private static final int OTA_SUPPORT_INFO_SIZE = 4; // Sequence Number (2 bytes), NeedsAcks (1 byte), Checksum (1 byte)
    private static final byte OTA_ACK_EVERY = 8;
    private int fw_image_packet_size = 16;
    private int number_blocks_x_packet = (DEFAULT_WRITE_DATA_LEN-OTA_SUPPORT_INFO_SIZE)/fw_image_packet_size; // number of 16 bytes blocks for each single ATT MTU packet
    // Set number of bytes sent on a single write without response
    private int Write_Data_Len = fw_image_packet_size + OTA_SUPPORT_INFO_SIZE;
    private long base_address;
    private long cnt;
    private long flash_LB;
    private long flash_UB;
    private int protocolState;
    private FwFileDescriptor fwFile;
    private InputStream fileOpened;
    private boolean resultState;
    private boolean onGoing;
    private int mProtocolVer; // server

    public static FwUpgradeConsoleBLUENRG buildForNode(Node node){
        ImageFeature rangeMem = node.getFeature(ImageFeature.class);
        NewImageFeature paramMem = node.getFeature(NewImageFeature.class);
        NewImageTUContentFeature startAckNotification = node.getFeature(NewImageTUContentFeature.class);
        ExpectedImageTUSeqNumberFeature chunkData = node.getFeature(ExpectedImageTUSeqNumberFeature.class);

        // BLUENRG1 or BLUENRG2?
        Node.TypeService serviceId = node.getTypeService();

        if(rangeMem!=null && paramMem!=null && startAckNotification!=null && chunkData!=null){
            return new FwUpgradeConsoleBLUENRG(rangeMem,paramMem,startAckNotification,chunkData,serviceId);
        }else{
            return null;
        }
    }

    private boolean checkRangeFlashMemAddress(){

        if ((base_address < flash_LB) || ((base_address+cnt) > flash_UB) || ((base_address%512)!=0)){
            // ERROR('Image above free memory range or base address is not aligned')
            return false;
        }
        return true;
    }

    private Feature.FeatureListener onImageFeature = new Feature.FeatureListener(){

        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            flash_LB = ImageFeature.getFlash_LB(sample);
            flash_UB = ImageFeature.getFlash_LB(sample);
            mProtocolVer = ImageFeature.getProtocolVer(sample);
            // Set base address
            base_address = flash_LB;
            protocolState++;
            EngineProtocolState();
        }
    };

    Feature.FeatureListener onNewImageFeature = new Feature.FeatureListener() {
        private int retries = 0;
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            byte otaAckEveryRead = NewImageFeature.getOtaAckEvery(sample);
            long imageSizeRead = NewImageFeature.getImageSize(sample);
            long baseAddressRead = NewImageFeature.getBaseAddress(sample);
            if ((otaAckEveryRead != OTA_ACK_EVERY)||(imageSizeRead != cnt)||(baseAddressRead != base_address)){
                retries++;
                if(retries > 10) {
                    mCallback.onLoadFwError(FwUpgradeConsoleBLUENRG.this, fwFile, FwUpgradeCallback.ERROR_TRANSMISSION);
                    resultState = false;
                    retries = 0;
                }else{
                    EngineProtocolState();
                }
            }else {
                protocolState++;
                EngineProtocolState();
                retries = 0;
            }
        }
    };

    private Feature.FeatureListener onAckNotification = new Feature.FeatureListener(){
        private long sendData = fwFile.getLength();
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            // check ack answer here
            boolean good = NewImageTUContentFeature.ack();
            if(good) {
                sendData -= ImageFeature.CHUNK_LENGTH*OTA_ACK_EVERY; // multiply by OTA_ACK_EVERY
                Log.d("OnProgress", "run: " + sendData);
                mCallback.onLoadFwProgressUpdate(FwUpgradeConsoleBLUENRG.this, fwFile, sendData);
                if (sendData <= 0) {
                    protocolState++;
                }
                EngineProtocolState();
            }else{
                mCallback.onLoadFwError(FwUpgradeConsoleBLUENRG.this,fwFile, FwUpgradeCallback.ERROR_TRANSMISSION);
            }
        }
    };

//    private Runnable onProgress = new Runnable() {
//        private long sendData = fwFile.getLength();
//        @Override
//        public void run() {
//            // check ack answer here
//            boolean good = NewImageTUContentFeature.ack();
//            if(good) {
//                sendData -= ImageFeature.CHUNK_LENGTH*OTA_ACK_EVERY; // multiply by OTA_ACK_EVERY
//                Log.d("OnProgress", "run: " + sendData);
//                mCallback.onLoadFwProgressUpdate(FwUpgradeConsoleBLUENRG.this, fwFile, sendData);
//                if (sendData <= 0) {
//                    protocolState++;
//                }
//                EngineProtocolState();
//            }else{
//                mCallback.onLoadFwError(FwUpgradeConsoleBLUENRG.this,fwFile, FwUpgradeCallback.ERROR_TRANSMISSION);
//            }
//        }
//    };

    private enum ProtocolStatePhase {
        RANGE_FLASH_MEM,
        PARAM_FLASH_MEM,
        START_ACK_NOTIFICATION,
        WRITE_CHUNK_DATA,
        CLOSURE
    }//ProtocolStatePhase


    private ProtocolStatePhase boolean ProtocolStatePhaseEngineProtocolState(){
        switch (protocolState){
            case ProtocolStatePhase.RANGE_FLASH_MEM:
                mRangeMem.addFeatureListener(onImageFeature); // remember to removeFeatureListener when it is the last
                mRangeMem.getParentNode().readFeature(mRangeMem);
            case ProtocolStatePhase.PARAM_FLASH_MEM:
                long cnt = fwFile.getLength();
                if(!checkRangeFlashMemAddress()) {
                    mCallback.onLoadFwError(FwUpgradeConsoleBLUENRG.this, fwFile, FwUpgradeCallback.ERROR_TRANSMISSION);
                    resultState =  false;
                }else {
                    byte cmdValue = 0;
                    long crcValue = 0; // todo: replace with crc func
                    mParamMem.writeParamMem(mProtocolVer,OTA_ACK_EVERY,cnt,base_address, crcValue, cmdValue);
                    mParamMem.addFeatureListener(onNewImageFeature); // remember to removeFeatureListener when it is the last
                    mParamMem.getParentNode().readFeature(mParamMem);
                }
            case ProtocolStatePhase.START_ACK_NOTIFICATION:
                mStartAckNotification.addFeatureListener(onAckNotification);
                mStartAckNotification.getParentNode().enableNotification(mStartAckNotification);
                try {
                    fileOpened = fwFile.openFile();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    mCallback.onLoadFwError(this, fwFile, FwUpgradeCallback.ERROR_INVALID_FW_FILE);
                    resultState = false;
                }
                protocolState++;
                EngineProtocolState();
            case ProtocolStatePhase.WRITE_CHUNK_DATA:
                try {
                    mChunkData.upload(fileOpened);

                } catch (IOException e) {
                    e.printStackTrace();
                    mCallback.onLoadFwError(this,fwFile,FwUpgradeCallback.ERROR_TRANSMISSION);
                    resultState =  false;
                }
            case ProtocolStatePhase.CLOSURE:
                mRangeMem.removeFeatureListener(onImageFeature);
                mParamMem.removeFeatureListener(onNewImageFeature);
                mCallback.onLoadFwComplete(FwUpgradeConsoleBLUENRG.this, fwFile);
                try {
                    fileOpened.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    mCallback.onLoadFwError(this, fwFile, FwUpgradeCallback.ERROR_INVALID_FW_FILE);
                    resultState = false;
                }
                onGoing = false;

        }//switch

        return resultState;
    }

    private FwUpgradeConsoleBLUENRG(@NonNull ImageFeature rangeMem,
                                    @NonNull NewImageFeature paramMem,
                                    @NonNull NewImageTUContentFeature startAckNotification,
                                    @NonNull ExpectedImageTUSeqNumberFeature chunkData,
                                    @NonNull Node.TypeService serviceId){
        super(null);
        mRangeMem = rangeMem;
        mParamMem = paramMem;
        mStartAckNotification = startAckNotification;
        mChunkData = chunkData;
        mServiceId = serviceId;
    }

    @Override
    public boolean loadFw(@FirmwareType int type, FwFileDescriptor fwFileIn,long startAddress) {

        fwFile = fwFileIn;
        resultState =  true;
        onGoing =  true;
        protocolState = 0;
        EngineProtocolState();

        while(onGoing && resultState);

        mStartAckNotification.getParentNode().disableNotification(mStartAckNotification);

        return resultState;
    }
}
