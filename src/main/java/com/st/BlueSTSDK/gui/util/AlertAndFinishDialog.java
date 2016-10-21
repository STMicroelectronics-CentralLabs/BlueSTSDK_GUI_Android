package com.st.BlueSTSDK.gui.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import com.st.BlueSTSDK.gui.ActivityWithNode;

/**
 * Display and close close the actvity when the use visualize the message
 */
public class AlertAndFinishDialog extends DialogFragment {

    private static final String TITLE = AlertAndFinishDialog.class.getCanonicalName()+".TITLE";
    private static final String MESSAGE = AlertAndFinishDialog.class.getCanonicalName()+".MESSAGE";
    private static final String KEEP_CONNECTION_OPEN = AlertAndFinishDialog.class.getCanonicalName()
            +".KEEP_CONNECTION_OPEN";


    /**
     * create the dialog
     * @param title dialog title
     * @param msg dialog message
     * @param keepConnectionOpen keep the node connection open also if the activity is stopped
     * @return dialog that display the message and close the activity after the user press the ok button
     */
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
        Bundle args = getArguments();
        String title = args.getString(TITLE);
        String msg = args.getString(MESSAGE);
        final boolean keepConnectionOpen = args.getBoolean(KEEP_CONNECTION_OPEN,false);

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
                                actvity.keepConnectionOpen(keepConnectionOpen,false);
                                actvity.finish();
                            }
                        }).create();
    }


    @TargetApi(23)
    @Override public void onAttach(Context context) {
        //This method avoid to call super.onAttach(context) if I'm not using api 23 or more
        //if (Build.VERSION.SDK_INT >= 23) {
        super.onAttach(context);
        onAttachToContext(context);
        //}
    }

    /*
     * Deprecated on API 23
     * Use onAttachToContext instead
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    /*
     * This method will be called from one of the two previous method
     */
    private void onAttachToContext(Context context) {
        try {
            ActivityWithNode  temp= (ActivityWithNode) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getName() + " must extend ActivityWithNode");
        }//try
    }

}