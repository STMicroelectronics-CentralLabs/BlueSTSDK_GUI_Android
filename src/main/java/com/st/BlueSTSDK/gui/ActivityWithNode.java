package com.st.BlueSTSDK.gui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

public abstract class ActivityWithNode extends AppCompatActivity {

    private final static String NODE_FRAGMENT = ActivityWithNode.class.getCanonicalName()
            +".NODE_FRAGMENT";

    private final static String NODE_TAG = DebugConsoleActivity.class.getCanonicalName()
            + ".NODE_TAG";

    private final static String KEEP_CONNECTION_OPEN = ActivityWithNode.class.getCanonicalName() +
            ".KEEP_CONNECTION_OPEN";


    private boolean mKeepConnectionOpen;

    /** fragment used for keep the connection open */
    private NodeContainerFragment mNodeContainer;

    protected Node mNode;

    /**
     * create an intent for start the activity that will log the information from the node
     *
     * @param c    context used for create the intent
     * @param node note that will be used by the activity
     * @return intent for start this activity
     */
    protected static Intent getStartIntent(Context c, @NonNull Class activity, @NonNull Node
            node,boolean keepConnectionOpen) {
        Intent i = new Intent(c, activity);
        i.putExtras(NodeContainerFragment.prepareArguments(node));
        i.putExtra(NODE_TAG, node.getTag());
        i.putExtra(KEEP_CONNECTION_OPEN, keepConnectionOpen);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();

        // recover the node
        String nodeTag = i.getStringExtra(NODE_TAG);
        mNode = Manager.getSharedInstance().getNodeWithTag(nodeTag);
        mKeepConnectionOpen = i.getBooleanExtra(KEEP_CONNECTION_OPEN,false);

        //create/recover the NodeContainerFragment
        if (savedInstanceState == null) {
            mNodeContainer = new NodeContainerFragment();
            mNodeContainer.setArguments(i.getExtras());
            getFragmentManager().beginTransaction()
                    .add(mNodeContainer, NODE_FRAGMENT).commit();
        } else {
            mNodeContainer = (NodeContainerFragment) getFragmentManager()
                    .findFragmentByTag(NODE_FRAGMENT);
        }//if-else

    }//onCreate

    /**
     * if we have to leave this activity, we force to keep the connection open, since we go back
     * in the {@link DemosActivity}
     */
    @Override
    public void onBackPressed() {
        mNodeContainer.keepConnectionOpen(mKeepConnectionOpen);
        super.onBackPressed();
    }//onBackPressed

    /**
     * call when the user press the back button on the menu bar, we are leaving this activity so
     * we keep the connection open since we are going int the {@link DemosActivity}
     *
     * @param item menu item clicked
     * @return true if the item is handle by this function
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button, we go back in the same task
            //for avoid to recreate the DemoActivity
            case android.R.id.home:
                mNodeContainer.keepConnectionOpen(mKeepConnectionOpen);
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }//switch

        return super.onOptionsItemSelected(item);
    }//onOptionsItemSelected



}
