/*******************************************************************************
 * COPYRIGHT(c) 2016 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK.gui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;

import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Log.FeatureLogCSVFile;
import com.st.BlueSTSDK.Log.FeatureLogDB;
import com.st.BlueSTSDK.Log.FeatureLogLogCat;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;
import com.st.BlueSTSDK.gui.fwUpgrade.FwUpgradeActivity;
import com.st.BlueSTSDK.gui.licenseManager.LicenseManagerActivity;
import com.st.BlueSTSDK.gui.preferences.LogPreferenceFragment;
import com.st.BlueSTSDK.gui.util.ConnectProgressDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Activity that will show the available demos, each demo is a fragment that extend
 * {@link com.st.BlueSTSDK.gui.demos.DemoFragment}
 * <p>
 * The activity will required that the node is already connected or in a connecting state
 * </p>
 */
public abstract class DemosActivity extends LogFeatureActivity implements NodeContainer,
        NavigationView.OnNavigationItemSelectedListener {

    private final static String NODE_TAG_ARG = DemosActivity.class.getCanonicalName() + "" +
            ".NODE_TAG";
    private final static String RESET_CACHE_ARG = DemosActivity.class.getCanonicalName() + "" +
            ".RESET_CACHE";


    private final static String DEBUG_CONSOLE = DemosActivity.class.getCanonicalName() + "" +
            ".DEBUG_CONSOLE";

    private final static String CURRENT_DEMO = DemosActivity.class.getCanonicalName() + "" +
            ".CURRENT_DEMO";

    private final static String SHOW_HELP = DemosActivity.class.getCanonicalName() + "" +
            ".SHOW_HELP";


    protected abstract Class<? extends DemoFragment>[] getAllDemos();

    /**
     * create an intent for start this activity
     *
     * @param c    context used for create the intent
     * @param node node to use for the demo
     * @return intent for start a demo activity that use the node as data source
     */
    protected static Intent getStartIntent(Context c, @NonNull Node node) {
        Intent i = new Intent(c, DemosActivity.class);
        setIntentParameters(i,node,false);
        return i;
    }//getStartIntent

    /**
     * create an intent for start this activity
     *
     * @param c          context used for create the intent
     * @param node       node to use for the demo
     * @param resetCache true if you want to reload the service and characteristics from the device
     * @return intent for start a demo activity that use the node as data source
     */
    public static Intent getStartIntent(Context c, @NonNull Node node, boolean resetCache) {
        Intent i = new Intent(c, DemosActivity.class);
        setIntentParameters(i,node,resetCache);
        return i;
    }//getStartIntent

    protected static void setIntentParameters(Intent i, @NonNull Node node, boolean resetCache){
        i.putExtra(NODE_TAG_ARG,node.getTag());
        i.putExtra(RESET_CACHE_ARG,resetCache);
    }

    /*
     * widget that will contain all the demo fragment
     */
    private ViewPager mPager;

    private ViewPager.OnPageChangeListener mUpdateActivityTitle = new ViewPager.SimpleOnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            setTitle(mPager.getAdapter().getPageTitle(position));
        }

    };

    //layout with the demo and demo menu
    private DrawerLayout mDrawerLayout;

    //button for show the demo menu
    private ActionBarDrawerToggle mDrawerToggle;

    //demo menu
    private NavigationView mNavigationTab;

    /**
     * text view that will show the debug message
     */
    private TextView mConsoleText;
    /**
     * scrollview attached to the console text
     */
    private ScrollView mConsoleView;

    private View mHelpView;

    private ConnectProgressDialog mConnectionProgressDialog;

    private boolean mKeepConnectionOpen;
    private boolean mShowKeepConnectionOpenNotification = false;
    private Node mNode;
    private boolean mResetChaceOnConnection;

    /**
     * true if we are showing the debug console
     */
    private boolean mShowDebugConsole = false;

    private Node.NodeStateListener mUpdateMenuOnConnection = new Node.NodeStateListener() {
        @Override
        public void onStateChange(Node node, Node.State newState, Node.State prevState) {
            if(newState==Node.State.Connected){
                DemosActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DemosActivity.this.invalidateOptionsMenu();
                    }
                });
            }
        }
    };


    /**
     * return true if we have to show the help screen, the first time that we show this activity
     *
     * @return the first time that the user display this activity
     */
    private boolean needShowHelpView() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean showHelp = preferences.getBoolean(SHOW_HELP, true);
        if (showHelp) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(SHOW_HELP, false);
            editor.apply();
        }//if
        return showHelp;
    }//needShowHelpView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConnectionProgressDialog = new ConnectProgressDialog(this,"");

        //set default settings for the logging
        PreferenceManager.setDefaultValues(this, R.xml.pref_logging, false);

        setContentView(R.layout.activity_demos);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.demoDrawerLayout);
        mNavigationTab = (NavigationView) findViewById(R.id.demoNavigationView);

        mConsoleText = (TextView) findViewById(R.id.consoleText);
        mConsoleView = (ScrollView) findViewById(R.id.consoleView);

        if (savedInstanceState == null) {
            Intent i = getIntent();
            mNode = Manager.getSharedInstance().getNodeWithTag(i.getStringExtra(NODE_TAG_ARG));
            mResetChaceOnConnection = i.getBooleanExtra(RESET_CACHE_ARG,false);
            mShowDebugConsole = i.getBooleanExtra(DEBUG_CONSOLE, false);
        } else {
            mNode = Manager.getSharedInstance().getNodeWithTag(savedInstanceState.getString(NODE_TAG_ARG));
            mShowDebugConsole = savedInstanceState.getBoolean(DEBUG_CONSOLE);
        }//if-else

        mPager = (ViewPager) findViewById(R.id.pager);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.showDemoList, R
                .string.closeDemoList);

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mHelpView = findViewById(R.id.helpDemoLayout);
        if (needShowHelpView()) {
            mHelpView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setVisibility(View.GONE);
                }
            });
            mHelpView.setVisibility(View.VISIBLE);
        }//if

        //Log.d(TAG, "onCreate Activity" + mNodeContainer);

    }//onCreate

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(DEBUG_CONSOLE, mShowDebugConsole);
        savedInstanceState.putInt(CURRENT_DEMO, mPager.getCurrentItem());
        savedInstanceState.putString(NODE_TAG_ARG,mNode.getTag());
    }


    private void buildDemoAdapter(Node node){
        mPager.addOnPageChangeListener(mUpdateActivityTitle);
        final DemosTabAdapter adapter=new DemosTabAdapter(node,getAllDemos(), getFragmentManager());
        mPager.setAdapter(adapter);
        mUpdateActivityTitle.onPageSelected(0);
        int nDemo = adapter.getCount();
        Menu navigationMenu = mNavigationTab.getMenu();
        //remove the old items
        navigationMenu.clear();
        for (int i = 0; i < nDemo; i++) {
            MenuItem temp = navigationMenu.add(adapter.getPageTitle(i));
            temp.setIcon(adapter.getDemoIconRes(i));
        }//for

        mNavigationTab.setNavigationItemSelectedListener(this);

        //if only a demo is available hide the left menu
        if(adapter.getCount()==1){
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            mDrawerToggle.setDrawerIndicatorEnabled(false);
            mDrawerToggle.syncState();
            mHelpView.setVisibility(View.GONE);
        }

        if(!node.isConnected()){
            node.addNodeStateListener(mUpdateMenuOnConnection);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mShowDebugConsole = savedInstanceState.getBoolean(DEBUG_CONSOLE);
        mPager.setCurrentItem(savedInstanceState.getInt(CURRENT_DEMO, 0));
    }


    @Override
    protected void onResume() {
        super.onResume();
        //if the node is connected -> this frame is recreated
        if (mNode==null){
            onBackPressed(); // go to the previous activity
            return;
        }
        keepConnectionOpen(true,true);
        NodeConnectionService.removeDisconnectNotification(this);
        mConnectionProgressDialog = new ConnectProgressDialog(this,mNode.getName());
        mNode.addNodeStateListener(mConnectionProgressDialog);
        if(!mNode.isConnected()){
            mNode.addNodeStateListener(new Node.NodeStateListener() {
                @Override
                public void onStateChange(final Node node, Node.State newState, Node.State
                        prevState) {
                    if(newState==Node.State.Connected) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                invalidateOptionsMenu();
                                buildDemoAdapter(node);
                                showConsoleOutput(mShowDebugConsole);
                            }
                        });
                        node.removeNodeStateListener(this);
                    }//if
                }
            });
            NodeConnectionService.connect(this,mNode,mResetChaceOnConnection);
        }else{
            buildDemoAdapter(mNode);
            showConsoleOutput(mShowDebugConsole);
        }//if-else
    }

    @Override
    protected void onPause() {
        mPager.removeOnPageChangeListener(mUpdateActivityTitle);
        if (mShowDebugConsole) {
            if(mNode!=null) {
                Debug debug = mNode.getDebug();
                //remove the listener
                if (debug != null)
                    debug.removeDebugOutputListener(mDebugListener);
            }
        }//if
        super.onPause();
    }

    @Override
    protected void onStop(){
        if(mNode!=null){
            mNode.removeNodeStateListener(mUpdateMenuOnConnection);
            mNode.removeNodeStateListener(mConnectionProgressDialog);
            if(!mKeepConnectionOpen){
                NodeConnectionService.disconnect(this,mNode);
            }else if(mShowKeepConnectionOpenNotification){
                NodeConnectionService.displayDisconnectNotification(this,mNode);
            }
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.acitivity_demos, menu);

        if (mShowDebugConsole) {
            menu.findItem(R.id.showDebugConsole).setTitle(R.string.hideDebugConsole);
        } else {
            menu.findItem(R.id.showDebugConsole).setTitle(R.string.showDebugConsole);
        }//if-else

        //hide debug stuff if not available
        if(mNode!=null) {
            Debug debug = mNode.getDebug();
            if (debug == null) {
                menu.findItem(R.id.openDebugConsole).setVisible(false);
                menu.findItem(R.id.showDebugConsole).setVisible(false);
            }

            if(!enableLicenseManager() || debug==null){
                menu.findItem(R.id.menu_start_license_manager).setVisible(false);
            }
            if(!enableFwUploading() || debug==null){
                menu.findItem(R.id.menu_start_fw_upgrade).setVisible(false);
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * @return true if you want enable the license manger, false otherwise
     */
    protected abstract boolean enableLicenseManager();

    /**
     * @return true if you want enable the fw uploading using the debug console, false otherwise
     */
    protected abstract boolean enableFwUploading();

    /**
     * if we have to leave this activity, we force the disconnection of the node
     */
    @Override
    public void onBackPressed() {
        keepConnectionOpen(false,false);
        super.onBackPressed();
    }

    /**
     * get the node used for this demos
     *
     * @return the node that we will use for this demos
     */
    public Node getNode() {
        return mNode;
    }

    @Override
    public void keepConnectionOpen(boolean keepOpen, boolean showNotification){
        mKeepConnectionOpen = keepOpen;
        mShowKeepConnectionOpenNotification= keepOpen && showNotification;
    }

    /**
     * create a logger in function of the preference selected by the users
     * <p>the default logger is a logCat logger</p>
     *
     * @return logger to use for for dump the features data
     */
    @Override
    protected Feature.FeatureLoggerListener getLogger() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String logType = sharedPref.getString(LogPreferenceFragment.KEY_PREF_LOG_STORE, "LogCat");
        String dumpPath = getLogDirectory();
        switch (logType) {
            case "LogCat":
                return new FeatureLogLogCat();
            case "DB":
                return new FeatureLogDB(this,dumpPath,getNodesToLog());
            case "File":
                return new FeatureLogCSVFile(dumpPath,getNodesToLog());
            default:
                return null;
        }//switch
    }//getFeatureLogger

    protected List<Node> getNodesToLog(){
        return Collections.singletonList(mNode);
    }

    protected String getLogDirectory() {
        final SharedPreferences sharedPref = PreferenceManager
                                .getDefaultSharedPreferences(DemosActivity.this);
        return sharedPref.getString(LogPreferenceFragment.KEY_PREF_LOG_DUMP_PATH,"");
    }

    /**
     * listener that will show the debug message on the textView
     */
    private Debug.DebugOutputListener mDebugListener = new Debug.DebugOutputListener() {
        @Override
        public void onStdOutReceived(Debug debug, final String message) {
            DemosActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConsoleText.append(message);
                    mConsoleView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }

        @Override
        public void onStdErrReceived(Debug debug, final String message) {
            DemosActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mConsoleText.append(message);
                    mConsoleView.fullScroll(View.FOCUS_DOWN);
                }
            });
        }

        @Override
        public void onStdInSent(Debug debug, String message, boolean writeResult) {
        }
    };

    /**
     * show/hide the debug text view and start the debug logging
     *
     * @param enable true if we have to show/enable false for hide/disable
     */
    private void showConsoleOutput(boolean enable) {
        if(mNode==null)
            return;

        Debug debug = mNode.getDebug();
        if (enable) {
            if (debug == null) {
                Toast.makeText(this, R.string.debugNotAvailable, Toast.LENGTH_SHORT).show();
                return;
            }//else
            mConsoleView.setVisibility(View.VISIBLE);
            debug.addDebugOutputListener(mDebugListener);
        } else {
            mConsoleView.setVisibility(View.GONE);
            if (debug!=null) debug.removeDebugOutputListener(mDebugListener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (id == R.id.settings) {
            startSettingsActivity(this, mNode);
            return true;
        }

        if(id == R.id.openDebugConsole){
            startDebugConsoleActivity(this, mNode);
            return true;
        }

        if (id == R.id.showDebugConsole) {
            mShowDebugConsole = !mShowDebugConsole;
            showConsoleOutput(mShowDebugConsole);
            invalidateOptionsMenu();
            return true;
        }
        if(id == R.id.menu_start_license_manager){
            if(mShowDebugConsole)
                showConsoleOutput(false);
            startLicenseManagerActivity(this,mNode);
            return true;
        }
        if(id == R.id.menu_start_fw_upgrade){
            if(mShowDebugConsole)
                showConsoleOutput(false);
            startFwUpgradeActivity(this,mNode);
            return true;
        }

        if(id==android.R.id.home)
            keepConnectionOpen(false,false);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        CharSequence title = menuItem.getTitle();
        FragmentPagerAdapter adapter = (FragmentPagerAdapter) mPager.getAdapter();
        int nDemo = adapter.getCount();
        for (int i = 0; i < nDemo; i++) {
            if (adapter.getPageTitle(i).equals(title)) {
                mPager.setCurrentItem(i);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }//if
        }//for
        return false;
    }//onNavigationItemSelected

    /**
     * start an activity that will show the debug console
     * @param c context used for create the intent
     * @param n node where send the message
     */
    protected void startDebugConsoleActivity(Context c,Node n){
        keepConnectionOpen(true,false);
        Intent i = DebugConsoleActivity.getStartIntent(c,n);
        startActivity(i);
    }

    /**
     * start an activity where the use can change the settings
     * @param c context used for create the intent
     * @param n node that will be configurated
     */
    protected void startSettingsActivity(Context c,Node n){
        keepConnectionOpen(true,false);
        Intent i = SettingsActivity.getStartIntent(c,n);
        startActivity(i);
    }

    protected void startLicenseManagerActivity(Context context, Node node) {
        keepConnectionOpen(true,false);
        Intent i = LicenseManagerActivity.getStartIntent(context, node,true);
        startActivity(i);
    }

    protected void startFwUpgradeActivity(Context context, Node node) {
        keepConnectionOpen(true,false);
        Intent i = FwUpgradeActivity.getStartIntent(context,node,true);
        startActivity(i);
    }

    /**
     * adapter that contains all the demos to show. The demos are a subset of {@code
     * DemosActivity.ALL_DEMOS}
     */
    private static class DemosTabAdapter extends FragmentPagerAdapter {

        /**
         * demos that will be displayed to the user
         */
        private ArrayList<Class<? extends DemoFragment>> mDemos = new
                ArrayList<>();

        /**
         * tell if the demo will show something if we run it
         * @param demoClass demo that we want show, it must be annotated with the annotation
         * {@link DemoDescriptionAnnotation}
         * @param node node where we will extract the information
         * @return true the node has the needed features for the demo
         */
        private boolean demoIsWorking(Class<? extends DemoFragment> demoClass, Node node) {

            DemoDescriptionAnnotation desc =
                    demoClass.getAnnotation(DemoDescriptionAnnotation.class);
            if (desc == null) //we don't have information -> let it pass
                return true;

            //check that we have all the feature in the requeareAll field
            //return false if one feature is missing
            Class<? extends Feature> requireAll[] = desc.requareAll();
            for (Class<? extends Feature> f : requireAll) {
                if (node.getFeature(f) == null)
                    return false;
            }//for

            //check that we have all the feature in the requeareOne field
            //return true if we have almost one feature
            Class<? extends Feature> requireOneOf[] = desc.requareOneOf();
            for (Class<? extends Feature> f : requireOneOf) {
                if (node.getFeature(f)  != null)
                    return true;
            }//for

            //return true if we don't have constrains
            return requireOneOf.length == 0;
        }//demoIsWorking

        public DemosTabAdapter(@NonNull Node node,Class<? extends
                DemoFragment>[] demos, FragmentManager fm) {
            super(fm);
            for (Class<? extends DemoFragment> demo : demos ) {
                if (demoIsWorking(demo, node))
                    mDemos.add(demo);
            }//for
        }//

        @Override
        public Fragment getItem(int position) {
            Class<? extends DemoFragment> frag = mDemos.get(position);
            try {
                return frag.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
                return null;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public int getCount() {
            return mDemos.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mDemos.get(position).getAnnotation(DemoDescriptionAnnotation.class).name();
        }

        public @DrawableRes int getDemoIconRes(int position) {
            return mDemos.get(position).getAnnotation(DemoDescriptionAnnotation.class).iconRes();
        }
    }



}
