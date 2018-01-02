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
package com.st.BlueSTSDK.gui.demos;

import android.app.Activity;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.DemosActivity;

/**
 * Base class for a fragment that have to show a particular demo inside the DemoActivity activity
 * this class will call the {@link com.st.BlueSTSDK.gui.demos.DemoFragment#enableNeededNotification(com.st.BlueSTSDK.Node)}
 * when the node is connected and the fragment started (inside the onResume method).
 * And call the {@link com.st.BlueSTSDK.gui.demos.DemoFragment#disableNeedNotification(com.st.BlueSTSDK.Node)}
 * inside the onPause method for ask to the node to stop send data when they aren't used anymore
 */
public abstract class DemoFragment extends Fragment {

    /**
     * utility method that show an message as a toast
     *
     * @param msg resource string
     */
    protected void showActivityToast(@StringRes final int msg) {
        updateGui(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                }//run
            });
    }//showActivityToast

    /**
     * state of the demo, if it is running or not -> if the fragment is visualized or not
     */
    private boolean mIsRunning;

    public DemoFragment(){
        if(!getClass().isAnnotationPresent(DemoDescriptionAnnotation.class)){
            throw new RuntimeException("A DemoFragment must have an annotation of type DemoDescriptionAnnotation");
        }
    }

    /**
     * this fragment must be attached to a DemoActivity activity, this method check this condition
     *
     * @param activity activity where the fragment is attached
     * @throws java.lang.ClassCastException if the activity doesn't extend DemosActivity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            DemosActivity  temp= (DemosActivity) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must extend DemosActivity");
        }//try
    }//onAttach



    /**
     * tell if the demo in this fragment is running or not
     *
     * @return true if the demo is running false otherwise
     */
    public boolean isRunning() {
        return mIsRunning;
    }//isRunning

    /**
     * Check that the fragment is attached to an activity, if yes run the tast on ui thread
     * @param task task to run on the uithread
     */
    protected void updateGui(Runnable task){
        Activity activity = getActivity();
        if(activity!=null)
            activity.runOnUiThread(task);
    }

    protected @Nullable Node getNode(){
        DemosActivity act = (DemosActivity) getActivity();
        if(act!=null)
            return act.getNode();
        return  null;
    }

    /**
     * enable all the notification needed for running the specific demo
     *
     * @param node node where the notification will be enabled
     */
    protected abstract void enableNeededNotification(@NonNull Node node);

    /**
     * disable all the notification used by this demo
     *
     * @param node node where disable the notification
     */
    protected abstract void disableNeedNotification(@NonNull Node node);

    /**
     * listener that will be used for enable the notification when the node is connected
     */
    private Node.NodeStateListener mNodeStatusListener = new Node.NodeStateListener() {
        @Override
        public void onStateChange(final Node node, Node.State newState, Node.State prevState) {
            if (newState == Node.State.Connected) {
                DemoFragment.this.updateGui(new Runnable() {
                    @Override
                    public void run() {
                        enableNeededNotification(node);
                    }
                });
            }else if (newState ==  Node.State.Lost || newState == Node.State.Dead ||
                    newState == Node.State.Unreachable){
                DemoFragment.this.updateGui(new Runnable() {
                    @Override
                    public void run() {
                        disableNeedNotification(node);
                    }
                });

            }
        }//onStateChange
    };

    /**
     * method called for start the demo, it will check that the node is connect and call the
     * enableNeededNotification method
     */
    public void startDemo() {
        //Log.d("DemoFragment","Start Demo");
        Node node = getNode();
        if(node==null)
            return;
        if (node.isConnected())
            enableNeededNotification(node);
        //we add the listener for restart restart the demo in the case of reconnection
        node.addNodeStateListener(mNodeStatusListener);
        mIsRunning = true;
    }//startDemo

    /**
     * stop the demo and disable the notification if the node is connected
     */
    public void stopDemo() {
        //Log.d("DemoFragment","Stop Demo");
        Node node = getNode();
        if(node==null)
            return;
        node.removeNodeStateListener(mNodeStatusListener);
        //if the node is already disconnected we don't care of disable the notification
        if (mIsRunning && node.isConnected()) {
            disableNeedNotification(node);
        }//if
        mIsRunning = false;
    }//stopDemo


    @Override
    public void setUserVisibleHint(boolean visible){
        //Log.d("DemoFragment","isVisible: "+visible +" isResumed: "+isResumed());
        //since this fragment will be used inside a viewPager that will preload the page we have to
        //override this function for be secure to start & stop the demo when the fragment is hide
        //NOTE: when we rotate the screen the fragment is restored + setUserVisibleHint(false)
        // +setUserVisibleHit(true) -> the we start, stop and start again the demo..
        super.setUserVisibleHint(visible);
        //if the fragment is loaded
        if(isResumed()) {
            //if it became visible and the demo is not already running
            if (visible) {
                if (!isRunning())
                    startDemo();
            }else {
                stopDemo();
            }
        }//isResumed
    }

    /**
     * the fragment is displayed -> start the demo
     */
    @Override
    public void onResume() {
        //Log.d("DemoFragment","OnResume, isVisible:"+getUserVisibleHint());
        super.onResume();
        if (getUserVisibleHint() && !isRunning()){
            startDemo();
        }
    }//onResume


    /**
     * the fragment is hide -> stop the demo
     */
    @Override
    public void onPause() {
        if (isRunning()){
            stopDemo();
        }
        super.onPause();
    }//stopDemo

}//DemoFragment
