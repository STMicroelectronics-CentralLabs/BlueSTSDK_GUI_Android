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
    private static final short retriesForChecksumErrorMax = 4;
    private static final short retriesForSequenceErrorMax = 4;
    private short retriesForChecksumError = 0;
    private short retriesForSequenceError = 0;
    private static final byte OTA_ACK_EVERY = 8;
    private int fw_image_packet_size = 16;
    private long base_address;
    private long cnt;
    private long flash_LB;
    private long flash_UB;
    private short SeqNum = 0;
    private ProtocolStatePhase protocolState;
    private FwFileDescriptor fwFile;
    private InputStream fileOpened;
    private boolean resultState;
    private boolean onGoing;
    private int mProtocolVer; // server
    private FwVersionConsole mConsole;
    private byte imageToSend[];

    public static FwUpgradeConsoleBLUENRG buildForNode(Node node){
        ImageFeature rangeMem = node.getFeature(ImageFeature.class);
        NewImageFeature paramMem = node.getFeature(NewImageFeature.class);
        NewImageTUContentFeature chunkData = node.getFeature(NewImageTUContentFeature.class);
        ExpectedImageTUSeqNumberFeature startAckNotification = node.getFeature(ExpectedImageTUSeqNumberFeature.class);

        // BLUENRG1 or BLUENRG2?
        Node.TypeService serviceId = node.getTypeService();

        FwVersionConsole console = FwVersionConsole.getFwVersionConsole(node);

        if(rangeMem!=null && paramMem!=null && startAckNotification!=null && chunkData!=null){
            return new FwUpgradeConsoleBLUENRG(rangeMem,paramMem,chunkData,startAckNotification,console,serviceId);
        }else{
            return null;
        }
    }
    private FwUpgradeConsoleBLUENRG(@NonNull ImageFeature rangeMem,
                                    @NonNull NewImageFeature paramMem,
                                    @NonNull NewImageTUContentFeature chunkData,
                                    @NonNull ExpectedImageTUSeqNumberFeature startAckNotification,
                                    FwVersionConsole console,
                                    @NonNull Node.TypeService serviceId){
        super(null);
        mRangeMem = rangeMem;
        mParamMem = paramMem;
        mChunkData = chunkData;
        mStartAckNotification = startAckNotification;
        mServiceId = serviceId;
        mConsole = console;
    }

    private boolean checkRangeFlashMemAddress(){

        return (base_address >= flash_LB) && ((base_address + cnt) <= flash_UB) && ((base_address % 512) == 0);
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
            flash_UB = ImageFeature.getFlash_LB(sample);
            mProtocolVer = mRangeMem.getProtocolVer();
            if(mConsole!=null) // no upgrade fw supported
                mConsole.readVersion(mProtocolVer);//read the current fw version
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
                protocolState = ProtocolStatePhase.START_ACK_NOTIFICATION;
                EngineProtocolState();
                retries = 0;
            }
        }
    };

    private Feature.FeatureListener onAckNotification = new Feature.FeatureListener(){
        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            byte ack = ExpectedImageTUSeqNumberFeature.getAck(sample);
            short nextExpectedCharBlock = ExpectedImageTUSeqNumberFeature.getNextExpectedCharBlock(sample);
            boolean good = ackResult(nextExpectedCharBlock,ack); // check ack answer
            if(good) {
                long sendData = cnt - SeqNum*fw_image_packet_size;
                Log.d("OnProgress", "run: " + sendData);
                mCallback.onLoadFwProgressUpdate(FwUpgradeConsoleBLUENRG.this, fwFile, sendData);
                if (sendData <= 0) {
                    protocolState = ProtocolStatePhase.CLOSURE;
                }
                EngineProtocolState();
            }else{
                mCallback.onLoadFwError(FwUpgradeConsoleBLUENRG.this,fwFile, FwUpgradeCallback.ERROR_TRANSMISSION);
                resultState = false;
            }
        }
    };

    private enum ProtocolStatePhase {
        RANGE_FLASH_MEM,
        PARAM_FLASH_MEM,
        START_ACK_NOTIFICATION,
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
                if(!checkRangeFlashMemAddress()) {
                    mCallback.onLoadFwError(FwUpgradeConsoleBLUENRG.this, fwFile, FwUpgradeCallback.ERROR_TRANSMISSION);
                    resultState =  false;
                }else {
                    byte cmdValue = 0;
                    long crcValue = 0; // todo: replace with crc func  // not supported
                    mParamMem.writeParamMem(mProtocolVer,OTA_ACK_EVERY,cnt,base_address, crcValue, cmdValue);
                    mParamMem.addFeatureListener(onNewImageFeature); // remember to removeFeatureListener when it is the last
                    mParamMem.getParentNode().readFeature(mParamMem);
                }
                break;
            case START_ACK_NOTIFICATION:
                mStartAckNotification.addFeatureListener(onAckNotification);
                mStartAckNotification.getParentNode().enableNotification(mStartAckNotification);
                imageToSend = new byte[(int)cnt];
                try {
                    fileOpened = fwFile.openFile();
                    fileOpened.read(imageToSend);
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
                    mChunkData.upload(imageToSend,OTA_ACK_EVERY,fw_image_packet_size,SeqNum);
                break;
            case CLOSURE:
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
                break;
        }//switch

        return resultState;
    }

    @Override
    public boolean loadFw(@FirmwareType int type, FwFileDescriptor fwFileIn,long startAddress) {

        fwFile = fwFileIn;
        resultState =  true;
        onGoing =  true;
        protocolState = ProtocolStatePhase.RANGE_FLASH_MEM;
        EngineProtocolState();

        while(onGoing && resultState){

        }

        mStartAckNotification.getParentNode().disableNotification(mStartAckNotification);

        return resultState;
    }
}
