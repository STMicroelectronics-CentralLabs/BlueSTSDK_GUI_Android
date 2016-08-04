package com.st.BlueSTSDK.gui;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.LogFeatureActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * class that ask to the use witch feature enable and log
 */
public abstract class AskWhatFeatureLogActivity extends LogFeatureActivity {

    //TODO: use save instance for keep it after screen rotation
    //set of feature that this class is logging
    private final Set<String> mFeatureLogSet = new HashSet<>();

    @Override
    public void startLogging(){

        List<String> availableFeatures = getAvailableFeatures();
        final String[] featureToLogList = new String[availableFeatures.size()];
        if (availableFeatures.size() > 0) {
            availableFeatures.toArray(featureToLogList);
            //build and show the MultiChoice dialog
            new AlertDialog.Builder(this)
                .setIcon(com.st.BlueSTSDK.R.drawable.ic_select_log_features_24dp)
                .setCancelable(false)
                .setMultiChoiceItems(featureToLogList, getEnabledFeature(featureToLogList),
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                            if (b)
                                mFeatureLogSet.add(featureToLogList[i]);
                            else
                                mFeatureLogSet.remove(featureToLogList[i]);
                        }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (Node n : getNodesToLog())
                            enableLoggingNodeNotification(n);
                        AskWhatFeatureLogActivity.super.startLogging();
                    }
                }).create().show();
        }

    }

    private List<String> getAvailableFeatures(){
        List<String> valuesList = new ArrayList<>();
        for(Node n :getNodesToLog())
            for (Feature f : n.getFeatures()){
                if (!valuesList.contains(f.getName()))
                    valuesList.add(f.getName());
            }// for feature
        return  valuesList;
    }//getAvailableFeatures

    private boolean[] getEnabledFeature(String [] featureNames){
        boolean[] valuesChekedList = new boolean[featureNames.length];
        for (int i =0; i<valuesChekedList.length; i++) {
            valuesChekedList[i]= false;
            for (Node n : getNodesToLog())
                for (Feature f : n.getFeatures()) {
                    if (featureNames[i].compareTo(f.getName()) == 0){
                        if(n.isEnableNotification(f)) {
                            valuesChekedList[i] = true;
                            mFeatureLogSet.add(featureNames[i]);
                        }
                    }
                }
        }
        invalidateOptionsMenu();

        return  valuesChekedList;
    }

    private Node.NodeStateListener mStateListener = new Node.NodeStateListener() {
        @Override
        public void onStateChange(Node node, Node.State newState, Node.State prevState) {
            if (newState == Node.State.Connected) {
                enableLoggingNodeNotification(node);
            }
        }
    };

    private void enableLoggingNodeNotification(Node node){
        for (Feature f : node.getFeatures()) {
            if (mFeatureLogSet.contains(f.getName()))
                node.enableNotification(f);
        }//for
    }

    @Override
    protected void stopLogging(Node n) {
        super.stopLogging(n);
        n.removeNodeStateListener(mStateListener);
        mFeatureLogSet.clear();
    }

    @Override
    protected void startLogging(Node n) {
        super.startLogging(n);
        n.addNodeStateListener(mStateListener);
    }
}
