/*******************************************************************************
 * COPYRIGHT(c) 2016 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK.gui.preferences;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.st.BlueSTSDK.gui.NodeContainerFragment;
import com.st.BlueSTSDK.Node;

/**
 * A preference fragment that have to use a node have to extend this class and
 * implement the {@link #onNodeIsAvailable(Node)} function that will be called when the
 * node will be available in the preference activity.
 * the preference activity where this fragment will be attached have to implement the
 * {@link PreferenceActivityWithNode} interface for be able to create the node.
 */
public abstract class PreferenceFragmentWithNode extends PreferenceFragment {

    /**
     * parent activity
     */
    private PreferenceActivityWithNode mPreferenceActivity;

    /**
     * fragment that contains the node, this can't be instantiate directly inside the fragment
     * because it has setRetainInstance(true)
     */
    protected NodeContainerFragment mNodeContainer;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        if(activity instanceof PreferenceActivityWithNode)
            mPreferenceActivity = (PreferenceActivityWithNode)activity;
        else
            throw new ClassCastException(activity.toString()
                    + " must implement PreferenceActivityWithNode");
    }//onAttach

    /**
     * Ask to the Activity to create the nodeContainer, and attach it to the activity
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNodeContainer = mPreferenceActivity.instantiateNodeContainer();
        mPreferenceActivity.createNodeContainer();
    }

    /**
     * Ask to the activity to call the {@link #onNodeIsAvailable(Node)} when the node is available,
     */
    public void onStart(){
        super.onStart();
        //ask to have the node or call directly the setNode method
        mPreferenceActivity.notifyWhenNodeIsAvailable(this);
    }

    /**
     * method called by the activity when the node is available
     * @param n node to use for set the preference
     */
    abstract public void onNodeIsAvailable(Node n);

    @Override
    public void onDetach() {
        super.onDetach();
        mPreferenceActivity = null;
    }
}
