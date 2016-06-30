package com.st.BlueSTSDK.gui.util;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.st.BlueSTSDK.gui.ActivityWithNode;

public class AlertAndFinishDialog extends DialogFragment {

    private static final String TITLE = AlertAndFinishDialog.class.getCanonicalName()+".TITLE";
    private static final String MESSAGE = AlertAndFinishDialog.class.getCanonicalName()+".MESSAGE";
    private static final String KEEP_CONNECTION_OPEN = AlertAndFinishDialog.class.getCanonicalName()
            +".KEEP_CONNECTION_OPEN";


    public static AlertAndFinishDialog newInstance(String title,String msg,boolean
            keepConnectionOpen) {
        AlertAndFinishDialog frag = new AlertAndFinishDialog();
        Bundle args = new Bundle();
        args.putString(TITLE, title);
        args.putString(MESSAGE, msg);
        args.putBoolean(KEEP_CONNECTION_OPEN,keepConnectionOpen);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(TITLE);
        String msg = getArguments().getString(MESSAGE);

        final ActivityWithNode actvity = (ActivityWithNode)getActivity();

        return new AlertDialog.Builder(actvity)
                .setTitle(title)
                .setMessage(msg)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setNeutralButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                actvity.keepConnectionOpen(true);
                                actvity.finish();
                            }
                        }).create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            ActivityWithNode  temp= (ActivityWithNode) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must extend ActivityWithNode");
        }//try
    }
}