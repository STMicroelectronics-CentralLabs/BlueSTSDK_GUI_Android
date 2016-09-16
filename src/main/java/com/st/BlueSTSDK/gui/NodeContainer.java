package com.st.BlueSTSDK.gui;

import com.st.BlueSTSDK.Node;

/**
 * Interface used for object that will contain a node
 */
public interface NodeContainer {

    /**
     * get the node inside the container
     *
     * @return the node that is inside the container
     */
    Node getNode();

    /**
     * Keep the node connection open also when the activity is destroyed
     * @param keepOpen true for keep the node connection
     * @param showNotification true if it has to show a notificaiton for say that the node is still
     *                         connected
     */
    void keepConnectionOpen(boolean keepOpen,boolean showNotification);

}
