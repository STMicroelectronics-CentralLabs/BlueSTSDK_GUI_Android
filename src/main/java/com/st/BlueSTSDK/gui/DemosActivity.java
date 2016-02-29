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

import com.st.BlueSTSDK.Utils.LogFeatureActivity;
import com.st.BlueSTSDK.Debug;
import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Log.FeatureLogCSVFile;
import com.st.BlueSTSDK.Log.FeatureLogDB;
import com.st.BlueSTSDK.Log.FeatureLogLogCat;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that will show the available demos, each demo is a fragment that extend
 * {@link com.st.BlueSTSDK.gui.demos.DemoFragment}
 * <p>
 * The activity will required that the node is already connected or in a connecting state
 * </p>
 */
public abstract class DemosActivity extends LogFeatureActivity implements NavigationView
        .OnNavigationItemSelectedListener {

    private final static String NODE_FRAGMENT = DemosActivity.class.getCanonicalName() + "" +
            ".NODE_FRAGMENT";

    private final static String DEBUG_CONSOLE = DemosActivity.class.getCanonicalName() + "" +
            ".DEBUG_CONSOLE";

    private final static String CURRENT_DEMO = DemosActivity.class.getCanonicalName() + "" +
            ".CURRENT_DEMO";

    private final static String SHOW_HELP = DemosActivity.class.getCanonicalName() + "" +
            ".SHOW_HELP";


    protected abstract List<Class<? extends DemoFragment>> getAllDemos();


    /**
     * create an intent for start this activity
     *
     * @param c    context used for create the intent
     * @param node node to use for the demo
     * @return intent for start a demo activity that use the node as data source
     */
    public static Intent getStartIntent(Context c, @NonNull Node node) {
        Intent i = new Intent(c, DemosActivity.class);
        i.putExtras(NodeContainerFragment.prepareArguments(node));
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
        i.putExtras(NodeContainerFragment.prepareArguments(node, resetCache));
        return i;
    }//getStartIntent

    /*
     * widget that will contain all the demo fragment
     */
    private ViewPager mPager;

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

    /**
     * true if we are showing the debug console
     */
    private boolean mShowDebugConsole = false;

    private NodeContainerFragment mNodeContainer;


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

        setContentView(R.layout.activity_demos);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.demoDrawerLayout);
        mNavigationTab = (NavigationView) findViewById(R.id.demoNavigationView);

        mConsoleText = (TextView) findViewById(R.id.consoleText);
        mConsoleView = (ScrollView) findViewById(R.id.consoleView);

        if (savedInstanceState == null) {
            Intent i = getIntent();
            mNodeContainer = new NodeContainerFragment();
            mNodeContainer.setArguments(i.getExtras());
            //we set if only when we create the fragment, when the fragment is already created
            // the on resume will do the work
            mNodeContainer.setNodeStateListener(new Node.NodeStateListener() {
                @Override
                public void onStateChange(Node node, Node.State newState, Node.State prevState) {
                    if (newState == Node.State.Connected) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //when we connect we know if the debug service is available
                                showConsoleOutput(mShowDebugConsole);
                            } //run
                        });//runOnUiThread
                    }//if
                }//onStateChange
            });//setNodeListener

            getFragmentManager().beginTransaction()
                    .add(mNodeContainer, NODE_FRAGMENT).commit();
            mShowDebugConsole = i.getBooleanExtra(DEBUG_CONSOLE, false);
        } else {
            mNodeContainer = (NodeContainerFragment) getFragmentManager()
                    .findFragmentByTag(NODE_FRAGMENT);
            mShowDebugConsole = savedInstanceState.getBoolean(DEBUG_CONSOLE);
        }//if-else

        mPager = (ViewPager) findViewById(R.id.pager);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.showDemoList, R
                .string.closeDemoList);

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (needShowHelpView()) {
            View view = findViewById(R.id.helpDemoLayout);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setVisibility(View.GONE);
                }//onClick
            });
            view.setVisibility(View.VISIBLE);
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
    }


    @Override
    protected void onStart() {
        super.onStart();
        //initialize the adapter and the menu only if it is the first time
        if(mPager.getAdapter()!=null)
            return;
        Node node = mNodeContainer.getNode();
        if(node==null){
            startActivity(new Intent(this,NodeListActivity.class));
            finish();
            return;
        }
        //else
        //we have to initialize here the adapter since now the nodeContainer is build
        final DemosTabAdapter adapter=new DemosTabAdapter(node,getAllDemos(), getFragmentManager());
        mPager.setAdapter(adapter);
        int nDemo = adapter.getCount();
        Menu navigationMenu = mNavigationTab.getMenu();
        //remove the old items
        navigationMenu.clear();
        for (int i = 0; i < nDemo; i++) {
            MenuItem temp = navigationMenu.add(adapter.getPageTitle(i));
            temp.setIcon(adapter.getDemoIconRes(i));
        }//for

        mNavigationTab.setNavigationItemSelectedListener(this);
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
        if (mNodeContainer.getNode().isConnected())
            showConsoleOutput(mShowDebugConsole);
    }

    @Override
    protected void onPause() {
        if (mShowDebugConsole) {
            Debug debug = mNodeContainer.getNode().getDebug();
            //remove the listener
            if (debug != null)
                debug.setDebugOutputListener(null);
        }//if
        super.onPause();
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

        return super.onCreateOptionsMenu(menu);
    }


    /**
     * if we have to leave this activity, we force the disconnection of the node
     */
    @Override
    public void onBackPressed() {
        mNodeContainer.keepConnectionOpen(false);
        super.onBackPressed();
    }

    /**
     * get the node used for this demos
     *
     * @return the node that we will use for this demos
     */
    public Node getNode() {
        return mNodeContainer.getNode();
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
        String logType = "";//sharedPref.getString(LogPreferenceFragment.KEY_PREF_LOG_STORE, "LogCat");
        String dumpPath = ""; //sharedPref.getString(LogPreferenceFragment.KEY_PREF_LOG_DUMP_PATH,"");
        switch (logType) {
            case "LogCat":
                return new FeatureLogLogCat();
            case "DB":
                return new FeatureLogDB(this, mNodeContainer.getNode().getFeatures());
            case "File":
                return new FeatureLogCSVFile(dumpPath);
            default:
                return null;
        }//switch
    }//getFeatureLogger

    protected Node getNodeToLog(){
        return mNodeContainer.getNode();
    }

    protected String getLogDirectory(){
        final SharedPreferences sharedPref = PreferenceManager
                                .getDefaultSharedPreferences(DemosActivity.this);
        return "";// sharedPref.getString(LogPreferenceFragment.KEY_PREF_LOG_DUMP_PATH,"");
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
        Debug debug = mNodeContainer.getNode().getDebug();
        if (debug == null) {
            Toast.makeText(this, R.string.debugNotAvailable, Toast.LENGTH_SHORT).show();
            return;
        }//else
        if (enable) {
            mConsoleView.setVisibility(View.VISIBLE);
            debug.setDebugOutputListener(mDebugListener);
        } else {
            mConsoleView.setVisibility(View.GONE);
            debug.setDebugOutputListener(null);
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
            mNodeContainer.keepConnectionOpen(true);
            //startActivity(SettingsActivity.getStartIntent(this, mNodeContainer.getNode()));
            return true;
        }

        if(id == R.id.openDebugConsole){
            //Intent i = DebugConsoleActivity.getStartIntent(this, mNodeContainer.getNode());
            mNodeContainer.keepConnectionOpen(true);
            //startActivity(i);
            return true;
        }

        if (id == R.id.showDebugConsole) {
            mShowDebugConsole = !mShowDebugConsole;
            showConsoleOutput(mShowDebugConsole);
            invalidateOptionsMenu();
            return true;
        }
        if (id == android.R.id.home)
            mNodeContainer.keepConnectionOpen(false);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
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

        public DemosTabAdapter(@NonNull Node node,List<Class<? extends
                DemoFragment>> demos, FragmentManager fm) {
            super(fm);
            for (Class<? extends DemoFragment> demo :demos ) {
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

        public
        @DrawableRes
        int getDemoIconRes(int position) {
            return mDemos.get(position).getAnnotation(DemoDescriptionAnnotation.class).iconRes();
        }
    }

}
