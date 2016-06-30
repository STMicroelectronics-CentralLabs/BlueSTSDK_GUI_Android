package com.st.BlueSTSDK.gui.licenseManager.licenseConsole.nucleo;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;


public class SimpleFragmentDialog extends DialogFragment {
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
