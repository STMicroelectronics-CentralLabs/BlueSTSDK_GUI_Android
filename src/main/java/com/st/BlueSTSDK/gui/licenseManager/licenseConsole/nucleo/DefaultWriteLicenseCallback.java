package com.st.BlueSTSDK.gui.licenseManager.licenseConsole.nucleo;

import android.app.FragmentManager;
import android.support.annotation.NonNull;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.LicenseConsole;

public class DefaultWriteLicenseCallback implements LicenseConsole.WriteLicenseCallback {

    private FragmentManager mFragmentManager;

    public DefaultWriteLicenseCallback(@NonNull FragmentManager fm){
        mFragmentManager=fm;
    }

    @Override
    public void onLicenseLoadFail(LicenseConsole console,String licName,byte[] licCode) {

        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string
                .licenseManager_licenseLoadFailMsg);
        dialog.show(mFragmentManager,"LicenseLoadDialog");
    }

    @Override
    public void onLicenseLoadSuccess(LicenseConsole console, String licName,byte[] licCode) {
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string
                .licenseManager_licenseLoadSuccessMsg);
        dialog.show(mFragmentManager,"LicenseLoadDialog");
    }



}
