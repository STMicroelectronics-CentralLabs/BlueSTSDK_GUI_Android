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
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionConsole;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

// todo: add mTimeout management

public class FwUpgradeConsoleBLUENRG extends FwUpgradeConsole {

    // smart phone androids, BLUENRG service
    private ImageFeature mRangeMem;
    private NewImageFeature mParamMem;
    private NewImageTUContentFeature mChunkData;
    private ExpectedImageTUSeqNumberFeature mStartAckNotification;
    private Node.TypeService mServiceId;

    // IMAGE packet data for OTA transfer
    //private static final int DEFAULT_ATT_MTU_SIZE = 23;
    //private static final int DEFAULT_WRITE_DATA_LEN = (DEFAULT_ATT_MTU_SIZE - 3); // 20 bytes
    private static final int retriesMax = 10;
    private static final short retriesForChecksumErrorMax = 4;
    private static final short retriesForSequenceErrorMax = 4;
    private short retriesForChecksumError = 0;
    private short retriesForSequenceError = 0;
    private static final byte OTA_ACK_EVERY = 8;
    private byte mOta_Ack_Every = OTA_ACK_EVERY;
    private int fw_image_packet_size = 16;
    private long base_address;
    private long cnt;
    private long cntExtended;
    private long flash_LB;
    private long flash_UB;
    private short SeqNum = 0;
    private ProtocolStatePhase protocolState;
    private FwFileDescriptor fwFile;
    private InputStream fileOpened = null;
    private boolean resultState;
    private boolean onGoing;
    private int mProtocolVer; // server
    private byte imageToSend[];

    public static FwUpgradeConsoleBLUENRG buildForNode(Node node){
        ImageFeature rangeMem = node.getFeature(ImageFeature.class);
        NewImageFeature paramMem = node.getFeature(NewImageFeature.class);
        NewImageTUContentFeature chunkData = node.getFeature(NewImageTUContentFeature.class);
        ExpectedImageTUSeqNumberFeature startAckNotification = node.getFeature(ExpectedImageTUSeqNumberFeature.class);

        // BLUENRG1 or BLUENRG2?
        Node.TypeService serviceId = node.getTypeService();

        if(rangeMem!=null && paramMem!=null && startAckNotification!=null && chunkData!=null){
            return new FwUpgradeConsoleBLUENRG(rangeMem,paramMem,chunkData,startAckNotification,serviceId);
        }else{
            return null;
        }
    }
    private FwUpgradeConsoleBLUENRG(@NonNull ImageFeature rangeMem,
                                    @NonNull NewImageFeature paramMem,
                                    @NonNull NewImageTUContentFeature chunkData,
                                    @NonNull ExpectedImageTUSeqNumberFeature startAckNotification,
                                    @NonNull Node.TypeService serviceId){
        super(null);
        mRangeMem = rangeMem;
        mParamMem = paramMem;
        mChunkData = chunkData;
        mStartAckNotification = startAckNotification;
        mServiceId = serviceId;
    }

    private boolean checkRangeFlashMemAddress(){

        return (base_address >= flash_LB) && ((base_address + cntExtended) <= flash_UB) && ((base_address % 512) == 0);
    }

    private boolean ackResult(short nextExpectedCharBlock,byte ack){

        boolean result = false;
        switch (ack) {
            case (byte)0xFF:
                //ERROR('FLASH WRITE FAILED ON TARGET DEVICE: Repeat FW upgrade procedure')
                break;
            case 0x3C:
                //ERROR('FLASH WRITE FAILED ON TARGET DEVICE: Repeat FW upgrade procedure')
                break;
            case 0x0F:
                if(retriesForChecksumError < retriesForChecksumErrorMax) {
                    SeqNum = nextExpectedCharBlock;
                    result = true;
                    retriesForChecksumError++;
                }
                break;
            case (byte)0xF0:
                if(retriesForSequenceError < retriesForSequenceErrorMax) {
                    SeqNum = nextExpectedCharBlock;
                    result = true;
                    retriesForSequenceError++;
                }
                break;
            case 0x00:
                SeqNum = nextExpectedCharBlock;
                result = true;
                break;
            default:
                //ERROR('UNKNOWN ERROR ON TARGET DEVICE: Repeat FW upgrade procedure')
                break;
        }

        return result;
    }

    private Feature.FeatureListener onImageFeature = new Feature.FeatureListener(){
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            flash_LB = ImageFeature.getFlash_LB(sample);
            flash_UB = ImageFeature.getFlash_UB(sample);
            mProtocolVer = mRangeMem.getProtocolVer(sample);
            // Set base address
            base_address = flash_LB;
            protocolState = ProtocolStatePhase.PARAM_FLASH_MEM;
            EngineProtocolState();
        }
    };

    private Feature.FeatureListener onNewImageFeature = new Feature.FeatureListener() {
        private int retries = 0;
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            byte otaAckEveryRead = NewImageFeature.getOtaAckEvery(sample);
            long imageSizeRead = NewImageFeature.getImageSize(sample);
            long baseAddressRead = NewImageFeature.getBaseAddress(sample);
            if ((otaAckEveryRead != OTA_ACK_EVERY)||(imageSizeRead != cntExtended)||(baseAddressRead != base_address)){
                retries++;
                if(retries >= retriesMax) {
                    mCallback.onLoadFwError(FwUpgradeConsoleBLUENRG.this, fwFile, FwUpgradeCallback.ERROR_TRANSMISSION);
                    resultState = false;
                    retries = 0;
                }else{
                    EngineProtocolState();
                }
            }else {
                protocolState = ProtocolStatePhase.START_ACK_NOTIFICATION;
                EngineProtocolState();
                retries = 0;
            }
        }
    };

    private Feature.FeatureListener onAckNotification = new Feature.FeatureListener(){
        //private long residue = cntExtended%(OTA_ACK_EVERY * fw_image_packet_size); // residue byte size
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            if(protocolState == ProtocolStatePhase.START_ACK_NOTIFICATION) {
                protocolState = ProtocolStatePhase.FIRST_RECEIVED_NOTIFICATION;
                EngineProtocolState();
            }else {
                byte ack = ExpectedImageTUSeqNumberFeature.getAck(sample);
                short nextExpectedCharBlock = ExpectedImageTUSeqNumberFeature.getNextExpectedCharBlock(sample);
                boolean good = ackResult(nextExpectedCharBlock, ack); // check ack answer
                if (good) {
                    long sendData = cntExtended - SeqNum * fw_image_packet_size;
                    Log.d("OnProgress", "run: " + sendData);
                    mCallback.onLoadFwProgressUpdate(FwUpgradeConsoleBLUENRG.this, fwFile, sendData);
                    if (sendData <= 0) {
                        protocolState = ProtocolStatePhase.CLOSURE;
                    } else if((cntExtended - (SeqNum+OTA_ACK_EVERY) * fw_image_packet_size) < 0) { // if next sequence is the last one with residue size
                        //mOta_Ack_Every = (byte) (residue/fw_image_packet_size);
                        mOta_Ack_Every = (byte) (cntExtended/fw_image_packet_size - SeqNum); // to have sendData=0
                    }
                    EngineProtocolState();
                } else {
                    mCallback.onLoadFwError(FwUpgradeConsoleBLUENRG.this, fwFile, FwUpgradeCallback.ERROR_TRANSMISSION);
                    resultState = false;
                }
            }
        }
    };

    private enum ProtocolStatePhase {
        RANGE_FLASH_MEM,
        PARAM_FLASH_MEM,
        START_ACK_NOTIFICATION,
        FIRST_RECEIVED_NOTIFICATION,
        WRITE_CHUNK_DATA,
        CLOSURE
    }//ProtocolStatePhase


    private boolean EngineProtocolState(){
        switch (protocolState){
            case RANGE_FLASH_MEM:
                mRangeMem.addFeatureListener(onImageFeature); // remember to removeFeatureListener when it is the last
                mRangeMem.getParentNode().readFeature(mRangeMem);
                break;
            case PARAM_FLASH_MEM:
                cnt = fwFile.getLength();
                cntExtended = cnt;
                if(cnt%fw_image_packet_size != 0)
                    cntExtended = (cnt/fw_image_packet_size+1)*fw_image_packet_size; // to have always cntExtended as multiple of fw_image_packet_size
                if(!checkRangeFlashMemAddress()) {
                    mCallback.onLoadFwError(FwUpgradeConsoleBLUENRG.this, fwFile, FwUpgradeCallback.ERROR_TRANSMISSION);
                    resultState =  false;
                }else {
                    byte cmdValue = 0;
                    long crcValue = 0; // todo: replace with crc func  // not supported
                    mParamMem.writeParamMem(mProtocolVer,OTA_ACK_EVERY,cntExtended,base_address, crcValue, cmdValue);
                    mParamMem.addFeatureListener(onNewImageFeature); // remember to removeFeatureListener when it is the last
                    mParamMem.getParentNode().readFeature(mParamMem);
                }
                break;
            case START_ACK_NOTIFICATION:
                mStartAckNotification.addFeatureListener(onAckNotification);
                mStartAckNotification.getParentNode().enableNotification(mStartAckNotification);
                break;
            case FIRST_RECEIVED_NOTIFICATION:
                byte imageToSendTemp[] = new byte[(int)cnt];
                imageToSend = new byte[(int)cntExtended]; // all zero value
                try {
                    fileOpened = fwFile.openFile();
                    int nReadByte = fileOpened.read(imageToSendTemp);
                    if(nReadByte != cnt )
                    {
                        mCallback.onLoadFwError(this, fwFile, FwUpgradeCallback.ERROR_INVALID_FW_FILE);
                        resultState = false;
                    }else
                        System.arraycopy(imageToSendTemp,0,imageToSend,0,(int)cnt); // to have the last (cntExtended - cnt) bytes with value zero
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    mCallback.onLoadFwError(this, fwFile, FwUpgradeCallback.ERROR_INVALID_FW_FILE);
                    resultState = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    mCallback.onLoadFwError(this,fwFile,FwUpgradeCallback.ERROR_TRANSMISSION);
                    resultState =  false;
                }
                protocolState=ProtocolStatePhase.WRITE_CHUNK_DATA;
                EngineProtocolState();
                break;
            case WRITE_CHUNK_DATA:
                    mChunkData.upload(imageToSend,mOta_Ack_Every,fw_image_packet_size,SeqNum);
                break;
            case CLOSURE:
                mRangeMem.removeFeatureListener(onImageFeature);
                mParamMem.removeFeatureListener(onNewImageFeature);
                mStartAckNotification.getParentNode().disableNotification(mStartAckNotification);
                if(fileOpened != null) {
                    try {
                        fileOpened.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        mCallback.onLoadFwError(this, fwFile, FwUpgradeCallback.ERROR_INVALID_FW_FILE);
                        resultState = false;
                    }
                }
                if(resultState) {
                    mCallback.onLoadFwComplete(FwUpgradeConsoleBLUENRG.this, fwFile);
                }
                onGoing = false;
                break;
        }//switch

        if(onGoing && (!resultState)){
            protocolState=ProtocolStatePhase.CLOSURE;
            EngineProtocolState();
        }

        return resultState;
    }

    @Override
    public boolean loadFw(@FirmwareType int type, FwFileDescriptor fwFileIn,long startAddress) {

        fwFile = fwFileIn;
        resultState =  true;
        onGoing =  true;
        protocolState = ProtocolStatePhase.RANGE_FLASH_MEM;
        EngineProtocolState();

//        while(onGoing && resultState){
//
//        }
//
//        mStartAckNotification.getParentNode().disableNotification(mStartAckNotification);
//
//        return resultState;
        return true;
    }
}
