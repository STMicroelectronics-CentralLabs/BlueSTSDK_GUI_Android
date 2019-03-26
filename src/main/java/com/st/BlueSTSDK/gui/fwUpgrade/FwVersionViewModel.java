package com.st.BlueSTSDK.gui.fwUpgrade;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionBoard;
import com.st.BlueSTSDK.gui.fwUpgrade.fwVersionConsole.FwVersionConsole;

public class FwVersionViewModel extends ViewModel {

    private MutableLiveData<Boolean> mIsWaitingFwVersion = new MutableLiveData<>();
    private MutableLiveData<Boolean> mFwUpgradeSupported = new MutableLiveData<>();
    private MutableLiveData<FwVersionBoard> mFwUpgradeRequireUpdate = new MutableLiveData<>();
    private MutableLiveData<FwVersion> mFwVersion = new MutableLiveData<>();

    public LiveData<Boolean> isWaitingFwVersion(){
        return mIsWaitingFwVersion;
    }
    public LiveData<Boolean> supportFwUpgrade(){
        return mFwUpgradeSupported;
    }
    public LiveData<FwVersionBoard> requireManualUpdateTo(){
        return mFwUpgradeRequireUpdate;
    }

    public  LiveData<FwVersion> getFwVersion(){
        return mFwVersion;
    }

    private static final FwVersionBoard MIN_COMPATIBILITY_VERSION[] = new FwVersionBoard[]{
            new FwVersionBoard("BLUEMICROSYSTEM2","",2,0,1)
    };

    private void setFwIncompatibilityFlag(FwVersionBoard version){
        for(FwVersionBoard knowBoard : MIN_COMPATIBILITY_VERSION){
            if(version.getName().equals(knowBoard.getName())){
                if(version.compareTo(knowBoard)<0){
                    mFwUpgradeRequireUpdate.postValue(knowBoard);
                }//if
            }//if
        }//for
        mFwUpgradeRequireUpdate.postValue(null);
    }

    public void loadFwVersionFromNode(Node node){
        if(mFwVersion.getValue()!=null)
            return;
        FwVersionConsole console = FwVersionConsole.getFwVersionConsole(node);
        if(console != null){
            console.setLicenseConsoleListener((console1, type, version) -> {
                mIsWaitingFwVersion.postValue(false);
                mFwUpgradeSupported.postValue(true);

                mFwVersion.postValue(version);
                if(version instanceof FwVersionBoard){
                    setFwIncompatibilityFlag((FwVersionBoard) version);
                }
                console1.setLicenseConsoleListener(null);
            });
            mIsWaitingFwVersion.postValue(true);
            console.readVersion(FirmwareType.BOARD_FW);
        }else{
            mFwUpgradeSupported.postValue(false);
        }
        //TODO else
    }

}
