/*
 * Copyright (c) 2019  STMicroelectronics – All rights reserved
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
package com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;

import java.util.ArrayList;
import java.util.List;

public class RetrieveNodeVersion implements Node.NodeStateListener, FwVersionConsole.FwVersionCallback {

    private List<OnVersionRead> mCallback = new ArrayList<>();
    private Node mCurrentNode;

    public void addListener(OnVersionRead callback){
        mCallback.add(callback);
    }

    @Override
    public void onStateChange(@NonNull Node node, @NonNull Node.State newState, @NonNull Node.State prevState) {
        if(newState == Node.State.Connected){
            mCurrentNode = node;
            FwVersionConsole fwInfo = FwVersionConsole.getFwVersionConsole(node);
            if(fwInfo!=null){
                fwInfo.setLicenseConsoleListener(this);
                //if we can't read the version just log the connection event
                if(!fwInfo.readVersion(FirmwareType.BOARD_FW)){
                    callCallback(node,null);
                }
            }else {
                callCallback(node,null);
            }
            node.removeNodeStateListener(this);
        }
    }

    private void callCallback(@NonNull Node node,@Nullable FwVersion version){
        for (OnVersionRead call : mCallback){
            call.onVersionRead(node,version);
        }
    }

    @Override
    public void onVersionRead(FwVersionConsole console, int type, @Nullable FwVersion version) {
        console.setLicenseConsoleListener(null);
        callCallback(mCurrentNode,version);
    }

    public interface OnVersionRead{
        void onVersionRead(@NonNull Node node,@Nullable FwVersion version);
    }

}