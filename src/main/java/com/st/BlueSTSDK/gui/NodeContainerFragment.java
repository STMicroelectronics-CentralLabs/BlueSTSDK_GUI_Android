package com.st.BlueSTSDK.gui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

/**
 * this is headless fragment that is used for store a connected node. this fragment will not be
 * destroyed when an activity is destroyed for change its configuration -> using this fragment you
 * avoid to connect/disconnect multiple times in a short time..
 *
 * this class will start the connection with the node inside the onCreate and close it inside the
 * onDestroy.
 * If you move in nother activity that will use the same node you can avoid to disconnect calling
 * the method {@link NodeContainerFragment#keepConnectionOpen(boolean)}
 */
public class NodeContainerFragment extends Fragment {
    private final static String TAG = NodeContainerFragment.class.getCanonicalName();
    /**
     * string used for store our data in the fragment args
     */
    final static String NODE_TAG=NodeContainerFragment.class.getCanonicalName()+".NODE_TAG";
    final static String RESETCACHE_TAG=NodeContainerFragment.class.getCanonicalName()+".RESETCACHE_TAG";

    /**
     * prepare the arguments to pass to this fragment
     * @param node node that this fragment has to manage
     * @param resetCache true if you want to reload all the service and characteristics from the
     *                   device
     * @return bundle to pass as argument to a NodeContainerFragment
     */
    public static Bundle prepareArguments(Node node,boolean resetCache) {
        Bundle args = new Bundle();
        args.putString(NODE_TAG,node.getTag());
        args.putBoolean(RESETCACHE_TAG,resetCache);
        return args;
    }

    /**
     * prepare the arguments to pass to this fragment
     * @param node node that this fragment has to manage
     * @return bundle to pass as argument to a NodeContainerFragment
     */
    public static Bundle prepareArguments(Node node) {
        return prepareArguments(node,false);
    }

    /**
     * progress dialog to show when we wait that the node connection
     */
    private ProgressDialog mConnectionWait;

    /**
     * node handle by this class
     */
    private Node mNode=null;

    /**
     * true if the user ask to reset the device cache before open the connection
     */
    private boolean mResetNodeCache;
    /**
     * node state listener set by the user,
     */
    private Node.NodeStateListener mUserNodeStateListener=null;

    /**
     * true if the user ask to close the connection -> we can avoid to o it when we destroy the
     * fragment
     */
    boolean userAskToDisconnect=false;

    /**
     * true if the user ask to skip the disconnect when the fragment is destroyed
     */
    boolean userAskToKeepConnection =false;

    /**
     * node listener that will manage the dialog + pass the data to the user listener if it is set
     *
     */
    private Node.NodeStateListener mNodeStateListener = new Node.NodeStateListener() {
        @Override
        public void onStateChange(final Node node, Node.State newState, Node.State prevState) {
            final Activity activity = NodeContainerFragment.this.getActivity();
            //we connect -> hide the dialog
            if ((newState == Node.State.Connected) && activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //close the progress dialog
                        mConnectionWait.dismiss();
                    }
                });
            //error state -> show a toast message and start a new connection
            } else if ((newState == Node.State.Unreachable ||
                    newState == Node.State.Dead ||
                    newState == Node.State.Lost) && activity != null) {
                final String msg;
                switch (newState) {
                    case Dead:
                        msg = String.format(getResources().getString(R.string.progressDialogConnMsgDeadNodeError),
                                node.getName());
                        break;
                    case Unreachable:
                        msg = String.format(getResources().getString(R.string.progressDialogConnMsgUnreachableNodeError),
                                node.getName());
                        break;
                    case Lost:
                    default:
                        msg = String.format(getResources().getString(R.string
                                        .progressDialogConnMsgLostNodeError),
                                node.getName());
                        break;
                }//switch
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!mConnectionWait.isShowing())
                            mConnectionWait.show(); //show the dialog and set the listener for hide it
                        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();

                        mNode.connect(getActivity(), mResetNodeCache);
                    }
                });
            }
            if(mUserNodeStateListener!=null)
                mUserNodeStateListener.onStateChange(node,newState,prevState);
        }//onStateChange
    };


    /**
     * prepare the progress dialog tho be shown setting the title and the message
     * @param nodeName name of the node that we will use
     */
    private void setUpProgressDialog(String nodeName){
        mConnectionWait = new ProgressDialog(getActivity(),ProgressDialog.STYLE_SPINNER);
        mConnectionWait.setTitle(R.string.progressDialogConnTitle);
        mConnectionWait.setMessage(String.format(getResources().getString(R.string
                        .progressDialogConnMsg),
                nodeName));
    }//setUpProgressDialog

    /**
     * return the node handle by this fragment
     * @return return the node handle by this fragment
     */
    public Node getNode(){return mNode;}

    /**
     * add a user defined state listener for the node handle by this class.
     * this method is usefull because it can be called before the node is created/conneteed -> it
     * can intercept the connection event/process
     * @param listener used defined node state listener
     */
    public void setNodeStateListener(Node.NodeStateListener listener){
        mUserNodeStateListener=listener;
    }

    @Override
    public void onAttach(Context a){
        super.onAttach(a);
        String nodeTag = getArguments().getString(NODE_TAG);
        mResetNodeCache = getArguments().getBoolean(RESETCACHE_TAG,false);
        mNode = Manager.getSharedInstance().getNodeWithTag(nodeTag);
    }

    /**
     * set this fragment as retain state + retrive the node from the manager
     * @param savedInstanceState
     */
    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if(mNode!=null)
            setUpProgressDialog(mNode.getName());
    }

    /**
     * if not already connected, show the dialog and stat the connection with the node
     */
    @Override
    public void onResume(){
        super.onResume();
        if(mNode!=null) {
            Node.State state = mNode.getState();
            //avoid to start the connection if we are already doing a connection
            if (state != Node.State.Connected && state != Node.State.Connecting) {
                mConnectionWait.show(); //show the dialog and set the listener for hide it
                mNode.addNodeStateListener(mNodeStateListener);
                mNode.connect(getActivity(), mResetNodeCache);
                mResetNodeCache = false; //reset the chache only the first time that we connect
                userAskToDisconnect = false;
            }//if
        }//if !=null
    }


    /**
     * if we are still connection hide the progress dialog
     */
    @Override
    public void onPause(){
        Log.d(this.toString(), "onPause");

        //dismiss the dialog if we are showing it
        if(mConnectionWait!=null && mConnectionWait.isShowing()){
            mConnectionWait.dismiss();
        }//if

        super.onPause();
    }

    public void keepConnectionOpen(boolean doIt){
        userAskToKeepConnection =doIt;
    }

    /**
     * if the user did do explicity disconnect the node
     */
    @Override
    public void onDestroy(){
        Log.d(this.toString(), "onDestroy");
        if(mNode!=null && mNode.isConnected() && !userAskToDisconnect){
            if(!userAskToKeepConnection) {
                mNode.removeNodeStateListener(mNodeStateListener);
                mNode.disconnect();
            }
        }//if

        super.onDestroy();
    }

}

