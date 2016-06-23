package com.st.BlueSTSDK.gui.licenseManager;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.LicenseConsole;

public class LicenseLoadDefaultCallback extends LicenseConsole.LicenseConsoleCallbackEmpty {

    private View mRootView;

    LicenseLoadDefaultCallback(@NonNull View root){
        mRootView=root;
    }

    public void onLicenseLoadFail(LicenseConsole console) {
        super.onLicenseLoadFail(console);
        Snackbar.make(mRootView, R.string.fwUpgrade_licenseLoadFailMsg,
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onLicenseLoadSuccess(LicenseConsole console) {
        super.onLicenseLoadSuccess(console);
        Snackbar.make(mRootView, R.string.fwUpgrade_licenseLoadSuccessMsg,
                Snackbar.LENGTH_LONG).show();
    }

}
