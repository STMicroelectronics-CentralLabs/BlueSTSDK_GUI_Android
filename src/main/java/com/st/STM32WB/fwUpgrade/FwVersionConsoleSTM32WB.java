package com.st.STM32WB.fwUpgrade;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionConsole;
import com.st.STM32WB.fwUpgrade.feature.OTAControlFeature;

public class FwVersionConsoleSTM32WB extends FwVersionConsole {


    public static FwVersionConsole buildForNode(Node node){
        if(node.getFeature(OTAControlFeature.class)!=null)
            return new FwVersionConsoleSTM32WB(null);
        return null;
    }

    /**
     * @param callback object where notify the command answer
     */
    private FwVersionConsoleSTM32WB(FwVersionCallback callback) {
        super(callback);
    }

    @Override
    public boolean readVersion(@FirmwareType int type) {
        if(mCallback==null)
            return true;
        FwVersionBoard version = new FwVersionBoard("STM32WB OTA","STM32WB",1,0,0);
        mCallback.onVersionRead(this,type,version);
        return true;
    }
}
