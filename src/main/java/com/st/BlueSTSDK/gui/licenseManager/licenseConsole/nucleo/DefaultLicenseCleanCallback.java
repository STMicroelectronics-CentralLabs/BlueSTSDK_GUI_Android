package com.st.BlueSTSDK.gui.licenseManager.licenseConsole.nucleo;

import android.app.FragmentManager;
import android.support.annotation.NonNull;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.LicenseConsole;

public class DefaultLicenseCleanCallback implements LicenseConsole.CleanLicenseCallback {

    private FragmentManager mFragmentManager;

    public DefaultLicenseCleanCallback(@NonNull FragmentManager fm){
        mFragmentManager=fm;
    }

    @Override
    public void onLicenseClearedFail(LicenseConsole console) {
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string
                .licenseManager_errorClearBoardLic);
        dialog.show(mFragmentManager,"LicenseLoadDialog");
    }

    @Override
    public void onLicenseClearedSuccess(LicenseConsole console) {
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string
                .licenseManager_clearBoardLicOk);
        dialog.show(mFragmentManager,"LicenseLoadDialog");
    }
}
