package com.st.STM32WB.fwUpgrade.statOtaConfig;

public interface StartOtaConfigContract {

    interface View{
        short getSectorToDelete();
        short getNSectorToDelete();
        void openFileSelector();
        void performFileUpload();
    }


    interface Presenter{
        void onRebootPressed();
        void onSelectFwFilePressed();
    }

}
