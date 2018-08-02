package com.st.STM32WB.fwUpgrade;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.st.BlueSTSDK.gui.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class FwUpgradeSTM32WBActivityFragment extends Fragment {

    public FwUpgradeSTM32WBActivityFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fw_upgrade_stm32_wb, container, false);
    }
}
