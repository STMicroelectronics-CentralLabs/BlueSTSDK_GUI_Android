package com.st.BlueSTSDK.gui;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

public class NodeConnectionService extends Service {

    private static final String DISCONNECT_ACTION = NodeConnectionService.class.getName() + ".DISCONNECT";
    private static final String CONNECT_ACTION = NodeConnectionService.class.getName() + ".CONNECT";
    private static final String SHOW_NOTIFICATION_ACTION = NodeConnectionService.class.getName() + ".SHOW_NOTIFICATION";
    private static final String REMOVE_NOTIFICATION_ACTION = NodeConnectionService.class.getName() + ".REMOVE_NOTIFICATION";
    private static final String NODE_TAG_ARG = NodeConnectionService.class.getName() + ".NODE_TAG";
    private static final String RESET_CACHE_ARG = NodeConnectionService.class.getName() + ".RESET_CACHE";
    private static final String NOTIFICATION_ICON_ARG = NodeConnectionService.class.getName() + ".NOTIFICATION_ICON_ARG";
    private static final int STOP_SERVICE = 1;
    private static final int NOTIFICAITON_ID = 1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    static public void displayDisconnectNotification(Context c, Node n){
        Intent i = new Intent(c,NodeConnectionService.class);
        i.setAction(SHOW_NOTIFICATION_ACTION);
        i.putExtra(NODE_TAG_ARG,n.getTag());
        c.startService(i);
    }


    static public void removeDisconnectNotification(Context c){
        Intent i = new Intent(c,NodeConnectionService.class);
        i.setAction(REMOVE_NOTIFICATION_ACTION);
        c.startService(i);
    }

    static public void connect(Context c, Node n){
        connect(c,n,false);
    }

    static public void connect(Context c, Node n, boolean resetCache ){
        Intent i = new Intent(c,NodeConnectionService.class);
        i.setAction(CONNECT_ACTION);
        i.putExtra(NODE_TAG_ARG,n.getTag());
        i.putExtra(RESET_CACHE_ARG,resetCache);
        //i.putExtra(USER_DEFINE_FEATURE_ARG,(Serializable) userDefineFeature);
        c.startService(i);
    }

    private static Intent buildDisconnectIntent(Context c,Node n){
        Intent i = new Intent(c,NodeConnectionService.class);
        i.setAction(DISCONNECT_ACTION);
        i.putExtra(NODE_TAG_ARG,n.getTag());
        return i;
    }

    static public void disconnect(Context c, Node n){
        c.startService(buildDisconnectIntent(c,n));
    }


    /*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String intentAction = intent.getAction();
        if (intentAction != null)
            mBinder.stopLogging();
        else
            addLoggerNotification();
        return super.onStartCommand(intent, flags, startId);
    }
    */

    private Set<Node> mConnectedNodes = new HashSet<>();
    private NotificationManager mNotificationManager;


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
            showConnectionNotificaiton(intent);
        }else if(REMOVE_NOTIFICATION_ACTION.equals(action)){
            removeConnectionNotification();
        }

        return START_STICKY;
    }



    private void removeConnectionNotification() {
        mNotificationManager.cancel(NOTIFICAITON_ID);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(Node n : mConnectedNodes){
            if(n.isConnected()){
                n.disconnect();
            }
        }
        mNotificationManager.cancel(NOTIFICAITON_ID);
    }

    private NotificationCompat.Action buildDisconnectAction(Node n){
        Intent stopServiceIntent = buildDisconnectIntent(this,n);
        return new NotificationCompat.Action.Builder(
                android.R.drawable.ic_delete,
                getString(R.string.NodeConn_disconnect),
                PendingIntent.getService(this, STOP_SERVICE, stopServiceIntent, PendingIntent.
                        FLAG_ONE_SHOT)).build();
    }

    private @DrawableRes int getResourceLogo(){
        String packageName = getPackageName();
        try {
            final ApplicationInfo applicationInfo=getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return  applicationInfo.logo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return android.R.drawable.ic_delete;
        }
    }

    private void showConnectionNotificaiton(Intent intent) {
        String tag = intent.getStringExtra(NODE_TAG_ARG);
        @DrawableRes int notificationIcon = getResourceLogo();
        Node n = Manager.getSharedInstance().getNodeWithTag(tag);
        if(n!=null && mConnectedNodes.contains(n)) {
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(notificationIcon)
                    .setContentTitle("BlueST node still connected")
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                    .addAction(buildDisconnectAction(n));
            if(mConnectedNodes.size()==1)
                notificationBuilder.setContentText(getString(R.string.NodeConn_nodeIsConnected,n.getName()));
            else{
                notificationBuilder.setContentText("Multiple nodes").setNumber(mConnectedNodes.size());

            }//if-else
            mNotificationManager.notify(NOTIFICAITON_ID, notificationBuilder.build());
        }
    }

    private void connect(Intent intent) {
        String tag = intent.getStringExtra(NODE_TAG_ARG);
        boolean resetCache = intent.getBooleanExtra(RESET_CACHE_ARG,false);
        /*
        Map<UUID,List<Class< ? extends Feature>>> userDefineFeature =
                (Map<UUID,List<Class< ? extends Feature>>>) intent.getSerializableExtra(USER_DEFINE_FEATURE_ARG);
                */
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


    private @Nullable Node findConnectedNodeWithTag(String tag){
        for(Node n: mConnectedNodes){
            if(n.getTag().equals(tag))
                return n;
        }
        return null;
    }

    private void disconnect(Intent intent) {
        String tag = intent.getStringExtra(NODE_TAG_ARG);
        Node n = findConnectedNodeWithTag(tag);
        if(n!=null) {
            mConnectedNodes.remove(n);
            n.removeNodeStateListener(mStateListener);
            n.disconnect();
            mNotificationManager.cancel(NOTIFICAITON_ID);
            if(mConnectedNodes.size()==0){
                stopSelf();
            }
        }
    }

}