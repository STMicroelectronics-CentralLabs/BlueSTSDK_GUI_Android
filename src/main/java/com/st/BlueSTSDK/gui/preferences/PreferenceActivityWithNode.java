package com.st.BlueSTSDK.gui.preferences;

import com.st.BlueSTSDK.gui.NodeContainerFragment;

/**
 * An activity that want to add this fragment must implement this interface, used
 * for retrieve the NodeContainerFragment that will survive in case of change of configuration
 * of the activity and can't be include direclty in this fragment
 */
public interface PreferenceActivityWithNode{
    /**
     * instantiate a NodeContainerFragment in the parent activity
     * @return fragment that belong to the activity
     */
    NodeContainerFragment instantiateNodeContainer();

    /**
     * add the fragment to the activity triggering the onCreate method that will
     * start the connection with the node
     */
    void createNodeContainer();

    /**
     * Register a class for be notify when the node is available
     * @param pref fragment that have to use the node
     */
    void notifyWhenNodeIsAvailable(PreferenceFragmentWithNode pref);
}