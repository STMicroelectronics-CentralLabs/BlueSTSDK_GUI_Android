package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.statOtaConfig;

import android.net.Uri;
import android.support.annotation.Nullable;

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
