/*
 * Copyright (c) 2017  STMicroelectronics – All rights reserved
 * The STMicroelectronics corporate logo is a trademark of STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this list of conditions
 *   and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice, this list of
 *   conditions and the following disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name nor trademarks of STMicroelectronics International N.V. nor any other
 *   STMicroelectronics company nor the names of its contributors may be used to endorse or
 *   promote products derived from this software without specific prior written permission.
 *
 * - All of the icons, pictures, logos and other images that are provided with the source code
 *   in a directory whose title begins with st_images may only be used for internal purposes and
 *   shall not be redistributed to any third party or modified in any way.
 *
 * - Any redistributions in binary form shall not include the capability to display any of the
 *   icons, pictures, logos and other images that are provided with the source code in a directory
 *   whose title begins with st_images.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package com.st.BlueSTSDK.gui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NodeScanActivity;

/**
 * Activity that will show the list of discovered nodes
 */
public abstract class NodeListActivity extends NodeScanActivity implements NodeRecyclerViewAdapter
.OnNodeSelectedListener, NodeRecyclerViewAdapter.FilterNode, View.OnClickListener{
    private final static String TAG = NodeListActivity.class.getCanonicalName();

    private Manager.ManagerListener mUpdateDiscoverGui = new Manager.ManagerListener() {

        /**
         * call the stopNodeDiscovery for update the gui state
         * @param m manager that start/stop the process
         * @param enabled true if a new discovery start, false otherwise
         */
        @Override
        public void onDiscoveryChange(Manager m, boolean enabled) {
            Log.d(TAG, "onDiscoveryChange " + enabled);
            if (!enabled)
                //run
                runOnUiThread(() -> stopNodeDiscovery());
        }//onDiscoveryChange

        /**
         * update the gui with the new node, and hide the SwipeRefreshLayout refresh after that
         * we discover the first node
         * @param m manager that discover the node
         * @param node new node discovered
         */
        @Override
        public void onNodeDiscovered(Manager m, Node node) {
            Log.d(TAG, "onNodeDiscovered " + node.getTag());
            runOnUiThread(() -> mSwipeLayout.setRefreshing(false));
        }//onNodeDiscovered
    };

    /**
     * number of millisecond that we spend looking for a new node
     */
    private final static int SCAN_TIME_MS = 10 * 1000; //10sec

    /**
     * adapter used for build the view that will contain the node
     */
    private NodeRecyclerViewAdapter mAdapter;
    /**
     * true if the user request to clear the device handler cache after the connection
     */
    private boolean mClearDeviceCache = false;

    /**
     * SwipeLayout used for refresh the list when the user pull down the fragment
     */
    private SwipeRefreshLayout mSwipeLayout;

    /**
     * button used for start/stop the discovery
     */
    private FloatingActionButton mStartStopButton;

    /**
     * class that manage the node discovery
     */
    private Manager mManager;

    /**
     * clear the adapter and the manager list of nodes
     */
    private void resetNodeList(){
        mManager.resetDiscovery();
        mAdapter.clear();
        //some nodes can survive if they are bounded with the device
        mAdapter.addAll(mManager.getNodes());
    }

    /**
     * Return the adapter view used for display the node
     * you can overwrite this method for use a custom adapter.
     * @return adapter view for the node.
     */
    protected NodeRecyclerViewAdapter getNodeAdapter(){
        return new NodeRecyclerViewAdapter(mManager.getNodes(),this,this);
    }

    /**
     * set the manager and and ask to draw the menu
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mManager = Manager.getSharedInstance();

        mAdapter = getNodeAdapter();
        //disconnect all the already discovered device
        mAdapter.disconnectAllNodes();

        setContentView(R.layout.activity_node_list);

        // Set the adapter
        RecyclerView recyclerView = findViewById(android.R.id.list);
        recyclerView.setAdapter(mAdapter);
        int nCol =getResources().getInteger(R.integer.nNodeListColum);
        if(nCol!=1){
            recyclerView.setLayoutManager(new GridLayoutManager(this,nCol));
        }

        //recyclerView.addItemDecoration(new BorderItemDecoration(this));

        mSwipeLayout = findViewById(R.id.swiperRefreshDeviceList);

        //onRefresh
        mSwipeLayout.setOnRefreshListener(() -> {
            resetNodeList();
            startNodeDiscovery();
        });

        //set refreshing color
        mSwipeLayout.setColorSchemeResources(R.color.swipeColor_1, R.color.swipeColor_2,
                R.color.swipeColor_3, R.color.swipeColor_4);

        mSwipeLayout.setSize(SwipeRefreshLayout.DEFAULT);
        mStartStopButton = findViewById(R.id.fab);
        if(mStartStopButton!=null)
            mStartStopButton.setOnClickListener(this);

    }

    /**
     * disconnect all the node and connect our adapter with the node manager for update the list
     * with new discover nodes and start the node discovery
     */
    @Override
    protected void onStart() {
        //add the listener that will hide the progress indicator when the first device is discovered
        mManager.addListener(mUpdateDiscoverGui);
        //disconnect all the already discovered device
        mAdapter.disconnectAllNodes();
        //add as listener for the new nodes
        mManager.addListener(mAdapter);
        resetNodeList();
        startNodeDiscovery();
        super.onStart();
    }//onListViewIsDisplayed

    /**
     * stop the discovery and remove all the lister that we attach to the manager
     */
    @Override
    protected void onStop() {

        //remove the listener add by this class
        mManager.removeListener(mUpdateDiscoverGui);
        mManager.removeListener(mAdapter);

        if (mManager.isDiscovering())
            stopNodeDiscovery();
        super.onStop();
    }

    /**
     * build the menu, it show the start/stop button in function of the manager state (if it is
     * scanning or not )
     *
     * @param menu     menu where add the items
     */

     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_node_list, menu);
        return true;
     }


    /** change the menu item name */
    private void changeDeviceCacheStatus(MenuItem item){
        mClearDeviceCache=!mClearDeviceCache;
        if(mClearDeviceCache)
            item.setTitle(R.string.ClearDeviceCacheMenuEnabled);
        else
            item.setTitle(R.string.ClearDeviceCacheMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear_list) {
            resetNodeList();
            return true;
        }//else
        if (id == R.id.menu_clear_device_cache) {
            changeDeviceCacheStatus(item);
            return true;
        }
        /*
        if (id == R.id.menu_add_node_emulator) {
            mManager.addVirtualNode();
            return true;
        }
        */
        return super.onOptionsItemSelected(item);
    }

    /**
     * method start a discovery and update the gui for the new state
     */
    private void startNodeDiscovery() {
        setRefreshing(mSwipeLayout, true);
        super.startNodeDiscovery(SCAN_TIME_MS);
        mStartStopButton.setImageResource(R.drawable.ic_close_24dp);
        //mManager.addVirtualNode();
    }

    /**
     * method that stop the discovery and update the gui state
     */
    @Override
    public void stopNodeDiscovery() {
        super.stopNodeDiscovery();
        mStartStopButton.setImageResource(R.drawable.ic_search_24dp);
        setRefreshing(mSwipeLayout, false);
    }

    public static void setRefreshing(final SwipeRefreshLayout swipeRefreshLayout, final boolean isRefreshing) {
        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(isRefreshing));
    }

    public void onClick(View view) {
        if(mManager.isDiscovering()){
            stopNodeDiscovery();
        }else{
            startNodeDiscovery();
        }
    }

    protected boolean clearCacheIsSelected(){
        return mClearDeviceCache;
    }

}
