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
        mView.showConnectionResetWarningDialog();
    }

    @Override
    public void onSelectFwFilePressed() {
        mView.openFileSelector();
    }

    @Override
    public void onFileSelected(@Nullable Uri file) {
        mView.performFileUpload(file);
    }

    @Override
    public void onConnectionResetWarningDismiss() {
       // Log.d("presenter","Sector: "+mView.getSectorToDelete()+" nSector: "+mView.getNSectorToDelete());
        mRebootFeature.rebootToFlash(mView.getSectorToDelete(), mView.getNSectorToDelete(),
                () -> mView.performFileUpload(null));

    }
}