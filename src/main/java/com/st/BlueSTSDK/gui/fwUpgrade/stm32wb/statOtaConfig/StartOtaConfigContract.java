package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.statOtaConfig;

import android.net.Uri;
import android.support.annotation.Nullable;

public interface StartOtaConfigContract {

    interface View{
        short getSectorToDelete();
        short getNSectorToDelete();
        void showConnectionResetWarningDialog();
        void openFileSelector();
        void performFileUpload(@Nullable Uri file);
    }


    interface Presenter{
        void onRebootPressed();
        void onSelectFwFilePressed();
        void onFileSelected(@Nullable Uri file);
        void onConnectionResetWarningDismiss();
    }

}
