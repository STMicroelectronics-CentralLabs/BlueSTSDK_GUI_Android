package com.st.STM32WB.p2pDemo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.ConnectionOption;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

/**
 * Activity that is containing the demo for the STM32WB board
 */
public class DemoSTM32WBActivity extends com.st.BlueSTSDK.gui.DemosActivity{

    /**
     * create an intent for start this activity
     *
     * @param c          context used for create the intent
     * @param node       node to use for the demo
     * @param option    parameters to use during the connection
     * @return intent for start a demo activity that use the node as data source
     */
    public static Intent getStartIntent(Context c, @NonNull Node node, ConnectionOption option) {
        Intent i = new Intent(c, DemoSTM32WBActivity.class);
        setIntentParameters(i, node, option);
        return i;
    }//getStartIntent

    /**
     * list of demo to show when connecting to a server node (the remote node)
     */
    @SuppressWarnings("unchecked")
    private final static Class<? extends DemoFragment> SERVER_DEMOS[] = new Class[]{
            LedButtonControlFragment.class,
    };

    /**
     * list of demo to show when connecting to a router node (the central node)
     */
    @SuppressWarnings("unchecked")
    private final static Class<? extends DemoFragment> ROUTER_DEMOS[] = new Class[]{
           LedButtonNetworkControlFragment.class,
    };


    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends DemoFragment>[] getAllDemos() {
        Node n = getNode();
        if(n.getTypeId() == Peer2PeerDemoConfiguration.WB_ROUTER_NODE_ID){
            return ROUTER_DEMOS;
        }else
            return SERVER_DEMOS;

    }

    @Override
    protected boolean enableFwUploading() {
        return false;
    }
}
