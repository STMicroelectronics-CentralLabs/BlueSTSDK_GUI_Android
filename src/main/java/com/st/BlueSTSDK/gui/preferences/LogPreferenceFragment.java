package com.st.BlueSTSDK.gui.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.st.BlueSTSDK.Log.FeatureLogBase;
import com.st.BlueSTSDK.Log.FeatureLogCSVFile;
import com.st.BlueSTSDK.Log.FeatureLogDB;
import com.st.BlueSTSDK.gui.R;

/**
 * fragment that contains the log preference -> where and how store the data
 */

public class LogPreferenceFragment extends PreferenceFragment {

    public final static String KEY_PREF_LOG_STORE="prefLog_logStore";
    public final static String KEY_PREF_LOG_DUMP_PATH="prefLog_exportPath";

    /** preference widget */
    private Preference mClearLog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_logging);
        mClearLog = findPreference("prefLog_clearLog");

    }

    /** remove the generated log files */
    void clearLog(){
        SharedPreferences sharedPref = getPreferenceManager().getSharedPreferences();
        String path = sharedPref.getString(LogPreferenceFragment.KEY_PREF_LOG_DUMP_PATH,"");
        FeatureLogBase.clean(getActivity(), path);
    }

    /**
     * if we click on the clear log preference we remove the generated file
     * @param preferenceScreen
     * @param preference preference that the user clicked
     * @return true if the user click the clearLog preference
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         @NonNull Preference preference) {
        if(preference==mClearLog){
            clearLog();
            Toast.makeText(getActivity(),R.string.pref_logClearDone,Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }//onPreferenceTreeClick
}//LogPreferenceFragment
