package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.statOtaConfig;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature.RebootOTAModeFeature;

public class StartOtaRebootPresenter implements StartOtaConfigContract.Presenter {

    private StartOtaConfigContract.View mView;
    private RebootOTAModeFeature mRebootFeature;

    public StartOtaRebootPresenter(StartOtaConfigContract.View view, RebootOTAModeFeature feature) {
        mView = view;
        mRebootFeature = feature;
    }

    @Override
    public void onRebootPressed() {
        mRebootFeature.rebootToFlash(mView.getSectorToDelete(), mView.getNSectorToDelete(),
                () -> mView.performFileUpload());
    }

    @Override
    public void onSelectFwFilePressed() {
        mView.openFileSelector();
    }


}
