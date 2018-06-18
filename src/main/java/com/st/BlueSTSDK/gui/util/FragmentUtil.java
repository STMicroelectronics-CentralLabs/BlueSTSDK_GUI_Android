package com.st.BlueSTSDK.gui.util;

import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * Utility class for the framgments
 */
public class FragmentUtil {

    /**
     * run the task in the activity ui thread
     * @param f fragment that submit the task
     * @param task task to run in the activity ui thread
     */
    public static void runOnUiThread(Fragment f, Runnable task){
        Activity activity = f.getActivity();
        if(activity!=null && !activity.isFinishing())
            activity.runOnUiThread(task);

    }

}
