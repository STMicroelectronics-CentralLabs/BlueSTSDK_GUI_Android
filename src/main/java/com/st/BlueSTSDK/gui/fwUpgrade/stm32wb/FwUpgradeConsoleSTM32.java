package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb;

import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.fwUpgrade.FirmwareType;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwUpgradeConsole;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.FwFileDescriptor;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionConsole;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature.OTABoardWillRebootFeature;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature.OTAControlFeature;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature.OTAFileUpload;

import java.io.FileNotFoundException;
import java.io.IOException;

public class FwUpgradeConsoleSTM32 extends FwUpgradeConsole {


    public static FwUpgradeConsoleSTM32 buildForNode(Node node){
        OTAControlFeature control = node.getFeature(OTAControlFeature.class);
        OTAFileUpload upload = node.getFeature(OTAFileUpload.class);
        OTABoardWillRebootFeature reboot = node.getFeature(OTABoardWillRebootFeature.class);
        if(control!=null && upload!=null && reboot!=null){
            return new FwUpgradeConsoleSTM32(control,upload,reboot);
        }else{
            return null;
        }
    }

    private OTAControlFeature mControl;
    private OTAFileUpload mUpload;
    private OTABoardWillRebootFeature mReset;

    public FwUpgradeConsoleSTM32(@NonNull OTAControlFeature control,
                                 @NonNull OTAFileUpload upload,
                                 @NonNull OTABoardWillRebootFeature reset){
        super(null);
        mControl = control;
        mUpload = upload;
        mReset = reset;
    }


    @Override
    public boolean loadFw(@FirmwareType int type, FwFileDescriptor fwFile) {
        mReset.addFeatureListener((f, sample) -> {
            if(OTABoardWillRebootFeature.boardIsRebooting(sample))
                mCallback.onLoadFwComplete(FwUpgradeConsoleSTM32.this,fwFile);
            else
                mCallback.onLoadFwError(FwUpgradeConsoleSTM32.this,fwFile, FwUpgradeCallback.ERROR_TRANSMISSION);
            mReset.getParentNode().disableNotification(mReset);
        });
        mReset.getParentNode().enableNotification(mReset);
        mControl.startUpload(type,0x7000);
        try {
            mUpload.upload(fwFile.openFile());
            Node n = mControl.getParentNode();
            while (!n.writeQueueIsEmpty()){

            }
            mControl.uploadFinished();
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
