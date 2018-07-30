package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.searchOtaNode;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.st.BlueSTSDK.Node;

public class SearchOtaNodeContract {

    public interface View{
        void startScan();
        void foundNode(@NonNull Node node);
        void nodeNodeFound();
    }


    public interface Presenter{
        void startScan(@Nullable String address);
        void stopScan();
    }

}
