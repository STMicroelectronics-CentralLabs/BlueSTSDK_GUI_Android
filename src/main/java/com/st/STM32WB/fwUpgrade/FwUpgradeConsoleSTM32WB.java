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
package com.st.STM32WB.fwUpgrade;

import android.support.annotation.NonNull;
import android.util.Log;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwUpgradeConsole;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.FwFileDescriptor;
import com.st.STM32WB.fwUpgrade.feature.OTABoardWillRebootFeature;
import com.st.STM32WB.fwUpgrade.feature.OTAControlFeature;
import com.st.STM32WB.fwUpgrade.feature.OTAFileUpload;

import java.io.FileNotFoundException;
import java.io.IOException;

public class FwUpgradeConsoleSTM32WB extends FwUpgradeConsole {


    public static FwUpgradeConsoleSTM32WB buildForNode(Node node){
        OTAControlFeature control = node.getFeature(OTAControlFeature.class);
        OTAFileUpload upload = node.getFeature(OTAFileUpload.class);
        OTABoardWillRebootFeature reboot = node.getFeature(OTABoardWillRebootFeature.class);
        if(control!=null && upload!=null && reboot!=null){
            return new FwUpgradeConsoleSTM32WB(control,upload,reboot);
        }else{
            return null;
        }
    }

    private OTAControlFeature mControl;
    private OTAFileUpload mUpload;
    private OTABoardWillRebootFeature mReset;

    private FwUpgradeConsoleSTM32WB(@NonNull OTAControlFeature control,
                                    @NonNull OTAFileUpload upload,
                                    @NonNull OTABoardWillRebootFeature reset){
        super(null);
        mControl = control;
        mUpload = upload;
        mReset = reset;
    }

    @Override
    public boolean loadFw(@FirmwareType int type, FwFileDescriptor fwFile,long startAddress) {
        Feature.FeatureListener onBoardWillReboot = (f, sample) -> {
            if(OTABoardWillRebootFeature.boardIsRebooting(sample))
                mCallback.onLoadFwComplete(FwUpgradeConsoleSTM32WB.this,fwFile);
            else
                mCallback.onLoadFwError(FwUpgradeConsoleSTM32WB.this,fwFile, FwUpgradeCallback.ERROR_TRANSMISSION);
            mReset.getParentNode().disableNotification(mReset);
        };
        mReset.addFeatureListener(onBoardWillReboot);
        mReset.getParentNode().enableNotification(mReset);
        mControl.startUpload(type,startAddress);
        try {

            Runnable onProgress = new Runnable() {
                    private long sendData = fwFile.getLength();
                    @Override
                    public void run() {
                        sendData-=OTAFileUpload.CHUNK_LENGTH;
                        Log.d("OnProgress", "run: "+sendData);
                        mCallback.onLoadFwProgressUpdate(FwUpgradeConsoleSTM32WB.this,fwFile,sendData);
                        if(sendData<=0){
                            mControl.uploadFinished(() -> {
                            //    mCallback.onLoadFwComplete(FwUpgradeConsoleSTM32WB.this,fwFile);
                            //    mReset.removeFeatureListener(onBoardWillReboot);
                            });
                        }
                    }
            };

            mUpload.upload(fwFile.openFile(),onProgress);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mControl.cancelUpload();
            mCallback.onLoadFwError(this,fwFile,FwUpgradeCallback.ERROR_INVALID_FW_FILE);
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            mControl.cancelUpload();
            mCallback.onLoadFwError(this,fwFile,FwUpgradeCallback.ERROR_TRANSMISSION);
            return false;
        }
        return true;
    }
}
