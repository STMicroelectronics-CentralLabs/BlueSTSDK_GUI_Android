package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.statOtaConfig;

public interface StartOtaConfigContract {

    interface View{
        short getSectorToDelete();
        short getNSectorToDelete();
        void showConnectionResetWarningDialog();
    }


    interface Presenter{
        void onRebootPressed();
        void onConnectionResetWarningDismiss();
    }

}
