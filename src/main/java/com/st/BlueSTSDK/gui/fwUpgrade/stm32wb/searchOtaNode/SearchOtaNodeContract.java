package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.searchOtaNode;

import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Node;

public class SearchOtaNodeContract {

    public interface View{
        void startScan();
        void foundNode(@NonNull Node node);
        void nodeNodeFound();
    }


    public interface Presenter{
        void startScan();
        void stopScan();
    }

}
