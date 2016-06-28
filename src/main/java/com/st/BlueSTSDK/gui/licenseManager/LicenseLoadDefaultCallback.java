package com.st.BlueSTSDK.gui.licenseManager;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.LicenseConsole;

public class LicenseLoadDefaultCallback extends LicenseConsole.LicenseConsoleCallbackEmpty {


    public static class SimpleFragmentDialog extends DialogFragment {
        private static final String MESSAGE_ID = "Message_id";

        public static SimpleFragmentDialog newInstance(@StringRes int title) {
            SimpleFragmentDialog frag = new SimpleFragmentDialog();
            Bundle args = new Bundle();
            args.putInt(MESSAGE_ID, title);
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int messageId = getArguments().getInt(MESSAGE_ID);
            return new AlertDialog.Builder(getActivity())
                    .setMessage(messageId)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            }
                    )
                    .setCancelable(false)
                    .create();
        }

    }

    private FragmentManager mFragmentManager;

    LicenseLoadDefaultCallback(@NonNull FragmentManager fm){
        mFragmentManager=fm;
    }

    public void onLicenseLoadFail(LicenseConsole console) {
        super.onLicenseLoadFail(console);

        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string
                .licenseManager_licenseLoadFailMsg);

        dialog.show(mFragmentManager,"LicenseLoadDialog");
    }

    @Override
    public void onLicenseLoadSuccess(LicenseConsole console) {
        super.onLicenseLoadSuccess(console);
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string
                .licenseManager_licenseLoadSuccessMsg);
        dialog.show(mFragmentManager,"LicenseLoadDialog");
    }

    @Override
    public void onLicenseClearedFail(LicenseConsole console) {
        super.onLicenseClearedFail(console);
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string
                .licenseManager_errorClearBoardLic);
        dialog.show(mFragmentManager,"LicenseLoadDialog");
    }

    @Override
    public void onLicenseClearedSuccess(LicenseConsole console) {
        super.onLicenseClearedFail(console);
        SimpleFragmentDialog dialog = SimpleFragmentDialog.newInstance(R.string
                .licenseManager_clearBoardLicOk);
        dialog.show(mFragmentManager,"LicenseLoadDialog");
    }
}
