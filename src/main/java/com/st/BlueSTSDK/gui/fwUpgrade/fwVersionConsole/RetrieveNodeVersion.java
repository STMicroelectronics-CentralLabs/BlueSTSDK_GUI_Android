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
