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

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Service that connect a node, in this way the node connection is bounded with the live cycle of the
 * service
 */
public class NodeConnectionService extends Service {

    private static final String DISCONNECT_ACTION = NodeConnectionService.class.getName() + ".DISCONNECT";
    private static final String CONNECT_ACTION = NodeConnectionService.class.getName() + ".CONNECT";
    private static final String SHOW_NOTIFICATION_ACTION = NodeConnectionService.class.getName() + ".SHOW_NOTIFICATION";
    private static final String REMOVE_NOTIFICATION_ACTION = NodeConnectionService.class.getName() + ".REMOVE_NOTIFICATION";
    private static final String NODE_TAG_ARG = NodeConnectionService.class.getName() + ".NODE_TAG";
    private static final String RESET_CACHE_ARG = NodeConnectionService.class.getName() + ".RESET_CACHE";

    private static final int STOP_SERVICE = 1;
    private static final int NOTIFICAITON_ID = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * ask to display a notification for inform the user that the ble connection is still open
     * @param c context to use for sent the request
     * @param n node that will remain connected
     */
    static public void displayDisconnectNotification(Context c, Node n){
        Intent i = new Intent(c,NodeConnectionService.class);
        i.setAction(SHOW_NOTIFICATION_ACTION);
        i.putExtra(NODE_TAG_ARG,n.getTag());
        c.startService(i);
    }


    /**
     * ask to the service to remove the notification that can appear on the notification bar
     * @param c
     */
    static public void removeDisconnectNotification(Context c){
        Intent i = new Intent(c,NodeConnectionService.class);
        i.setAction(REMOVE_NOTIFICATION_ACTION);
        c.startService(i);
    }

    /**
     * start the service asking to connect with the node
     * @param c context used for start the service
     * @param n node to connect
     */
    static public void connect(Context c, Node n){
        connect(c,n,false);
    }

    /**
     * start the service asking to connect with the node
     * @param c context used for start the service
     * @param n node to connect
     * @param resetCache true to try reset the ble uuid for the node
     */
    static public void connect(Context c, Node n, boolean resetCache ){
        Intent i = new Intent(c,NodeConnectionService.class);
        i.setAction(CONNECT_ACTION);
        i.putExtra(NODE_TAG_ARG,n.getTag());
        i.putExtra(RESET_CACHE_ARG,resetCache);
        c.startService(i);
    }


    /**
     * build the intent that will ask to disconnect the node
     * @param c context used for crate the intent
     * @param n node to disconnect
     * @return intent that will disconnect the node
     */
    private static Intent buildDisconnectIntent(Context c,Node n){
        Intent i = new Intent(c,NodeConnectionService.class);
        i.setAction(DISCONNECT_ACTION);
        i.putExtra(NODE_TAG_ARG,n.getTag());
        return i;
    }

    /**
     * ask to the service to disconnect the node
     * @param c context used for crate the intent
     * @param n node to disconnect
     */
    static public void disconnect(Context c, Node n){
        c.startService(buildDisconnectIntent(c,n));
    }


    /**
     * set of node managed by this service
     */
    private Set<Node> mConnectedNodes = new HashSet<>();

    /**
     * class used for manage the notification
     */
    private NotificationManager mNotificationManager;

    /**
     * if the node enter in a disconnected state try to connect again
     */
    private Node.NodeStateListener mStateListener = new Node.NodeStateListener() {
        @Override
        public void onStateChange(Node node, Node.State newState, Node.State prevState) {

        if ((newState == Node.State.Unreachable ||
             newState == Node.State.Dead ||
             newState == Node.State.Lost ) &&
             mConnectedNodes.contains(node)) {
              node.connect(NodeConnectionService.this);
          }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null){
            removeConnectionNotification();
            stopSelf();
            return START_NOT_STICKY;
        }

        if(mNotificationManager == null){
            mNotificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
        }

        String action = intent.getAction();

        if(CONNECT_ACTION.equals(action)){
            connect(intent);
        }else if (DISCONNECT_ACTION.equals(action)) {
            disconnect(intent);
        }else if (SHOW_NOTIFICATION_ACTION.equals(action)){
            showConnectionNotification(intent);
        }else if(REMOVE_NOTIFICATION_ACTION.equals(action)){
            removeConnectionNotification();
        }

        return START_STICKY;
    }

    /**
     * if present remove the connection notification
     */
    private void removeConnectionNotification() {
        if(mNotificationManager!=null)
            mNotificationManager.cancel(NOTIFICAITON_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //disconnect all the nodes and remove the notificaiton
        for(Node n : mConnectedNodes){
            if(n.isConnected()){
                n.disconnect();
            }
        }
        removeConnectionNotification();
    }

    @Override
    public void onTaskRemoved (Intent rootIntent){
        //remove the notification, the will be destroyed by the system
        removeConnectionNotification();
    }

    private PendingIntent getDisconnectPendingIntent(Node n){
        Intent stopServiceIntent = buildDisconnectIntent(this,n);
        return PendingIntent.getService(this, STOP_SERVICE, stopServiceIntent,
                PendingIntent.FLAG_ONE_SHOT);
    }

    /**
     * create the action to visualize into the notification
     * @param disconnectIntent itent to exec when the user click on the notificaiton
     * @return action that will disconnect the node
     */
    private NotificationCompat.Action buildDisconnectAction(PendingIntent disconnectIntent){
        return new NotificationCompat.Action.Builder(
                android.R.drawable.ic_delete,
                getString(R.string.NodeConn_disconnect),disconnectIntent).build();
    }//buildDisconnectAction

    /**
     * get the logo to display in the notificaiton, if present it will use the app logo, otherwise the
     * ic_dialog_alert icon
     * @return icon to use in the notificaiton
     */
    private @DrawableRes int getResourceLogo(){
        String packageName = getPackageName();
        @DrawableRes int logo=R.drawable.ic_warning_24dp;
        try {
            final ApplicationInfo applicationInfo=getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if(applicationInfo.logo!=0)
                logo = applicationInfo.logo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return logo;
    }

    /**
     * display the notificaiton that remeber to the use that it has a connected node
     * @param intent data to display in the notification
     */
    private void showConnectionNotification(Intent intent) {
        String tag = intent.getStringExtra(NODE_TAG_ARG);
        @DrawableRes int notificationIcon = getResourceLogo();
        Node n = Manager.getSharedInstance().getNodeWithTag(tag);
        // no connected node = no notification to show
        if (n==null || !mConnectedNodes.contains(n) )
            return;
        PendingIntent disconnectNode = getDisconnectPendingIntent(n);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.NodeConn_nodeConnectedTitile))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                .setDeleteIntent(disconnectNode)
                .addAction(buildDisconnectAction(disconnectNode))
                .setContentText(getString(R.string.NodeConn_nodeIsConnected,n.getName()));

        if(Build.VERSION.SDK_INT>=21){
            notificationBuilder.setSmallIcon(notificationIcon);
        }else{
            notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_warning);
        }

        mNotificationManager.notify(NOTIFICAITON_ID, notificationBuilder.build());
    }

    /**
     * start the connection with the node
     * @param intent node to connect
     */
    private void connect(Intent intent) {
        String tag = intent.getStringExtra(NODE_TAG_ARG);
        boolean resetCache = intent.getBooleanExtra(RESET_CACHE_ARG,false);
        Node n = Manager.getSharedInstance().getNodeWithTag(tag);
        if(n!=null)
            if(!mConnectedNodes.contains(n)) {
                mConnectedNodes.add(n);
                n.addNodeStateListener(mStateListener);
                n.connect(this,resetCache);
            }else{
                mNotificationManager.cancel(NOTIFICAITON_ID);
            }
    }

    /**
     * get the connected node manage by the service or null if there are no nodes with that tag
     * that area manage by the service
     * @param tag node that to search
     * @return the node with that tag manage by the service or null
     */
    private @Nullable Node findConnectedNodeWithTag(String tag){
        for(Node n: mConnectedNodes){
            if(n.getTag().equals(tag))
                return n;
        }//for
        return null;
    }

    /**
     * disconnect the node
     * @param intent node to disconnect
     */
    private void disconnect(Intent intent) {
        String tag = intent.getStringExtra(NODE_TAG_ARG);
        Node n = findConnectedNodeWithTag(tag);
        if(n==null)
            return;

        mConnectedNodes.remove(n);
        n.removeNodeStateListener(mStateListener);
        n.disconnect();
        mNotificationManager.cancel(NOTIFICAITON_ID);
        if(mConnectedNodes.size()==0){
            stopSelf();
        }//if

    }

}