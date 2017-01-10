package com.st.BlueSTSDK.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.preferences.LogPreferenceFragment;
import com.st.BlueSTSDK.gui.preferences.PreferenceFragmentWithNode;
import com.st.BlueSTSDK.gui.util.AppCompatPreferenceActivity;

import java.util.List;

/**
 * This Preference settings will keep the node information an pass to the PreferenceFragmentWithNode
 * that will open the connection with the node
 *
 * This class will show the log preference fragment in
 */
public class SettingsActivityWithNode extends AppCompatPreferenceActivity {

    private final static String NODE_TAG = SettingsActivityWithNode.class.getCanonicalName()
            + ".NODE_TAG";

    private final static String KEEP_CONNECTION_OPEN = SettingsActivityWithNode.class.getCanonicalName() +
            ".KEEP_CONNECTION_OPEN";

    private String nodeTag;
    private boolean mKeepConnectionOpen;

    /**
     * create an intent for start the activity that will log the information from the node
     *
     * @param c    context used for create the intent
     * @param node note that will be used by the activity
     * @return intent for start this activity
     */
    protected static Intent getStartIntent(Context c, @NonNull Class<? extends Activity> activity, @NonNull Node
            node, boolean keepConnectionOpen) {
        Intent i = new Intent(c, activity);
        i.putExtra(NODE_TAG, node.getTag());
        i.putExtra(KEEP_CONNECTION_OPEN, keepConnectionOpen);
        return i;
    }

    public static Intent getStartIntent(Context c, @NonNull Node node,boolean keepConnectionOpen) {
        return SettingsActivityWithNode.getStartIntent(c,SettingsActivityWithNode.class,node,keepConnectionOpen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState==null) {
            Intent i = getIntent();
            nodeTag = i.getStringExtra(NODE_TAG);
            mKeepConnectionOpen = i.getBooleanExtra(KEEP_CONNECTION_OPEN,false);
        }else{
            nodeTag = savedInstanceState.getString(NODE_TAG);
            mKeepConnectionOpen= savedInstanceState.getBoolean(KEEP_CONNECTION_OPEN,false);
        }
        // recover the node

    }//onCreate


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(NODE_TAG,nodeTag);
        outState.putBoolean(KEEP_CONNECTION_OPEN,mKeepConnectionOpen);
    }

    /**
     * Populate the activity with the top-level headers.
     */
    @CallSuper
    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.pref_headers_log, target);
    }

    @CallSuper
    @Override
    protected  boolean isValidFragment (String fragmentName){
        return fragmentName.equals(LogPreferenceFragment.class.getName()) ||
                super.isValidFragment(fragmentName) ;
    }

    @Override
    public void onHeaderClick(Header header, int position) {
        header.fragmentArguments = PreferenceFragmentWithNode.addStartArgs(header.fragmentArguments,nodeTag,mKeepConnectionOpen);
        super.onHeaderClick(header,position);
    }
}
