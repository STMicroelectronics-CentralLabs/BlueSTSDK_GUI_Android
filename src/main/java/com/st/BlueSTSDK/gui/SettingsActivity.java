package com.st.BlueSTSDK.gui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.CallSuper;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.preferences.LogPreferenceFragment;
import com.st.BlueSTSDK.gui.preferences.PreferenceActivityWithNode;
import com.st.BlueSTSDK.gui.preferences.PreferenceFragmentWithNode;
import com.st.BlueSTSDK.gui.util.AppCompatPreferenceActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity implements
        PreferenceActivityWithNode {

    private final static String NODE_FRAGMENT = SettingsActivity.class.getCanonicalName()
            +".NODE_FRAGMENT";

    private final static String NODE_FRAGMENT_ARGS = SettingsActivity.class.getCanonicalName()
            +".NODE_FRAGMENT_ARGS";

    private final static String NODE_HAS_REGISTER = SettingsActivity.class.getCanonicalName()
            +".NODE_HAS_REGISTER";

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * fragment that will contain the node and start the connection with it
     */
    private NodeContainerFragment mNodeContainer;

    /**
     * list of fragment that are waiting the node
     * */
    private List<PreferenceFragmentWithNode> mPrefFragmentWithNode =new ArrayList<>();

    /**
     * true if the node is read, so we don't have to wait it
     */
    private boolean mNodeIsReady=false;

    /** create an intent for start the activity that contains the configuration option for the app
     *
     * @param c context used for create the intent
     * @param node note that will be used by the activity
     * @return intent for start this activity
     */
    public static Intent getStartIntent(Context c,Node node){
        Intent i = new Intent(c,SettingsActivity.class);
        if(node.getConfigRegister()!=null) {
            //show all the configuration
            i.putExtra(NODE_FRAGMENT_ARGS,  NodeContainerFragment.prepareArguments(node));
            i.putExtra(NODE_HAS_REGISTER, node.getConfigRegister() != null);
        }
        return  i;
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

    /**
     * in case of small screen this activity will be recreate for show a new fragment with the
     * selected options, we propagate the useful information to the fragment
     * @param fragmentName
     * @param args
     * @param titleRes
     * @param shortTitleRes
     * @return intent that will re start the the activity with the proper fragment
     */
    @Override
    public Intent onBuildStartFragmentIntent (String fragmentName, Bundle args, int titleRes, int
            shortTitleRes){
        Intent i = super.onBuildStartFragmentIntent(fragmentName,args,titleRes,shortTitleRes);

        if(mNodeContainer!=null) {
            if (mNodeContainer.getNode().getConfigRegister() != null)
                i.putExtra(NODE_FRAGMENT_ARGS, getIntent().getBundleExtra(NODE_FRAGMENT_ARGS));
        }//if mNodeContainer
        return i;
    }

    /**
     * is is not already created it instantiate a new node container,
     * but the fragment will not be added to the activity
     */
    private void initializeNodeContainer(){
        //try to recover an already created fragment otherwise we create a new one
        mNodeContainer = (NodeContainerFragment)getFragmentManager()
                .findFragmentByTag(NODE_FRAGMENT);
        if(mNodeContainer==null) {
            mNodeContainer = new NodeContainerFragment();
            Bundle nodeInfo = getIntent().getBundleExtra(NODE_FRAGMENT_ARGS);
            mNodeContainer.setArguments(nodeInfo);
        }//if
    }

    /**
     * if we have to leave this activity, we force the disconnection of the node
     */
    @Override
    public void onBackPressed(){
        if(mNodeContainer!=null)
            mNodeContainer.keepConnectionOpen(true,false);
        super.onBackPressed();
    }

    /**
     * if not already instantiate it instantiate a new fragment that will contain the node
     * @return fragment that will survive to the activity configuration change, and will contain
     * the node
     */
    @Override
    public NodeContainerFragment instantiateNodeContainer(){
        if(mNodeContainer==null){
            initializeNodeContainer();
        }//if
        return mNodeContainer;
    }

    /**
     * the connection start when the NodeContainerFragment is created, the NodeContainerFragment
     * is created when is added to the activity
     */
    @Override
    public void createNodeContainer() {
        //if the activity is not already added to the activity
        Fragment temp = getFragmentManager()
                .findFragmentByTag(NODE_FRAGMENT);
        //we add it -> start the connection with the node
        if(temp==null)
            getFragmentManager().beginTransaction()
                    .add(mNodeContainer,NODE_FRAGMENT)
                    .commit();
    }

    @Override
    public void notifyWhenNodeIsAvailable(PreferenceFragmentWithNode pref) {
        //if the node container is ready avoid to add the preference to the list
        if (mNodeIsReady) {
            pref.onNodeIsAvailable(mNodeContainer.getNode());
        } else {
            mPrefFragmentWithNode.add(pref);
        }//if
    }//addPreferenceFragment

    @Override
    public void  onAttachFragment(Fragment fragment){
        if(fragment==mNodeContainer){
            mNodeIsReady=true;
            Node node = mNodeContainer.getNode();
            for(PreferenceFragmentWithNode pref : mPrefFragmentWithNode)
                pref.onNodeIsAvailable(node);
            mPrefFragmentWithNode.clear();
        }//if
    }//onAttachFragment
}
