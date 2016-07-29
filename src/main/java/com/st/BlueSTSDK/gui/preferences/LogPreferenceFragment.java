package com.st.BlueSTSDK.gui.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.util.Log;

import com.st.BlueSTSDK.Log.FeatureLogBase;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.gui.R;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * fragment that contains the log preference -> where and how store the data
 */

public class LogPreferenceFragment extends PreferenceFragment {

    private static final String EXPORT_LOG_FILE_EXTENSION = ".csv";
    private static final Pattern PATTERN_FILE_NAME_SESSION = Pattern.compile("^(\\d{8})_(\\d{6})_.*csv$");

    public final static String KEY_PREF_LOG_STORE="prefLog_logStore";
    public final static String KEY_PREF_LOG_DUMP_PATH="prefLog_exportPath";
    private static final String TAG = LogPreferenceFragment.class.getCanonicalName();

    /** preference widget */
    private Preference mClearLog;
    private Preference mExportSessionLog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_logging);
        mClearLog = findPreference("prefLog_clearLog");
        mExportSessionLog = findPreference("prefLog_exportSessionLog");

    }

    /** remove the generated log files */
    void clearLog(){
        SharedPreferences sharedPref = getPreferenceManager().getSharedPreferences();
        String path = sharedPref.getString(LogPreferenceFragment.KEY_PREF_LOG_DUMP_PATH,"");
        FeatureLogBase.clean(getActivity(), path);
    }

    /**
     * get all the log file in the directory
     * @param directoryPath path where search the file
     * @return all file in the directory with an extension .csv
     */
    static public File[] getLogFiles(String directoryPath, final String session){
        File directory = new File(directoryPath);
        //find all the csv files
        final FileFilter csvFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                final String fileName = pathname.getName();
                if (session != null)
                    return fileName.endsWith(EXPORT_LOG_FILE_EXTENSION) && fileName.startsWith(session);
                else
                    return fileName.endsWith(EXPORT_LOG_FILE_EXTENSION);
            }//accept
        };
        return directory.listFiles(csvFilter);
    }//getLogFiles

    /**
     * remove all the csv file in the directory
     * @param c context where the file were created
     * @param directoryPath directory where this class dumped the feature data
     */
    static public void deleteSession(Context c, String directoryPath, String session){
        File files[] =getLogFiles(directoryPath, session);
        if(files==null || files.length==0) //nothing to do
            return;

        for(File f: files ){
            if(!f.delete())
                Log.e(TAG, "Error deleting the file " + f.getAbsolutePath());
            c.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));
        }//for
    }//clean

    private String getLogPath(){
        SharedPreferences sharedPref = getPreferenceManager().getSharedPreferences();
        return sharedPref.getString(LogPreferenceFragment.KEY_PREF_LOG_DUMP_PATH,"");
    }


    private String[] getSessions(){
        File files[] = getLogFiles(getLogPath(), null);
        List<String> strSessions = new ArrayList<>();

        for (File f: files){
            Matcher matcher = PATTERN_FILE_NAME_SESSION.matcher(f.getName());
            if (matcher.matches()) {
                String session = matcher.group(1) + " " + matcher.group(2);
                if (strSessions.indexOf(session)<0)
                    strSessions.add(session);
            }
        }
        return strSessions.toArray(new String[strSessions.size()]);
    }

    private String [] mSessionsLocal;
    private boolean [] mCheckedSession;

    private void createSessionDialog(String title, int resId,  DialogInterface.OnClickListener actionListener ){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mSessionsLocal = getSessions();
        mCheckedSession = new boolean[mSessionsLocal.length];
        for (int i = 0; i< mCheckedSession.length; i++)
        {
            mCheckedSession[i] = false;
            if (title.startsWith("Export"))
                mCheckedSession[i] = true;
        }
        builder.setTitle(title);

        builder.setIcon(resId);
        builder.setCancelable(false);
        builder.setMultiChoiceItems(mSessionsLocal, mCheckedSession, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                //featureToLog(featureToLogList[i], b);
                mCheckedSession[i]=b;
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, actionListener);
        builder.create().show();
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
                createSessionDialog("Remove Sessions Log", android.R.drawable.ic_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mCheckedSession != null && mCheckedSession.length > 0) {
                            for (int j = 0; j < mCheckedSession.length; j++)
                            {
                                if (mCheckedSession[j])
                                    deleteSession(getActivity(), getLogPath(), mSessionsLocal[j].replace(" ","_"));
                            }
                        }
                    }
                });
                //clearLog();
                //Notify.message(getActivity(),R.string.pref_logClearDone);
                return true;
            }
            if(preference==mExportSessionLog){
                //TODO CHECK + SHOW ONLY IF THERE ARE SOMETHING TO EXPORT
                createSessionDialog("Export Sessions Log", android.R.drawable.ic_dialog_email, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mCheckedSession != null && mCheckedSession.length > 0) {
                            List<File> mFileToExport = new ArrayList<>();
                            for (int j = 0; j < mCheckedSession.length; j++)
                            {
                                if (mCheckedSession[j]) {
                                    File[] files = getLogFiles(getLogPath(), mSessionsLocal[j]
                                            .replace(" ", "_"));
                                    mFileToExport.addAll(Arrays.asList(files));
                                }

                            }

                            if (mFileToExport.size()> 0)
                                LogFeatureActivity.exportDataByMail(getActivity(),
                                        getLogPath(),
                                        mFileToExport.toArray(new File[mFileToExport.size()]),
                                        false);
                        }
                    }
                });
                //clearLog();
                //Notify.message(getActivity(),R.string.pref_logClearDone);
                return true;
            }
            return false;
        }//onPreferenceTreeClick
    }//LogPreferenceFragment