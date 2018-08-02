package com.st.STM32WB.fwUpgrade.statOtaConfig;

import com.st.STM32WB.fwUpgrade.feature.RebootOTAModeFeature;

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
