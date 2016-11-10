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
package com.st.BlueSTSDK.gui;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;

import java.util.ArrayList;
import java.util.List;

public class NodeRecyclerViewAdapter extends RecyclerView.Adapter<NodeRecyclerViewAdapter.ViewHolder>
        implements Manager.ManagerListener{

    private final List<Node> mValues = new ArrayList<>();

    public interface OnNodeSelectedListener{
        void onNodeSelected(Node n);
    }

    public interface FilterNode{
        boolean displayNode(Node n);
    }

    OnNodeSelectedListener mListener;
    FilterNode mFilterNode;

    public NodeRecyclerViewAdapter(List<Node> items, OnNodeSelectedListener listener,
                                   FilterNode filter) {
        mListener = listener;
        mFilterNode = filter;
        addAll(items);
    }//NodeRecyclerViewAdapter

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.node_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Node n = mValues.get(position);
        holder.mItem = n;
        holder.mNodeNameLabel.setText(n.getName());
        holder.mNodeTagLabel.setText(n.getTag());

        switch (n.getType()){
            case STEVAL_WESU1:
                holder.mNodeImage.setImageResource(R.drawable.board_steval_wesu1);
                break;
            case NUCLEO:
                holder.mNodeImage.setImageResource(R.drawable.board_nucleo);
                break;
            case SENSOR_TILE:
                holder.mNodeImage.setImageResource(R.drawable.board_sensor_tile);
                break;
            default:
                holder.mNodeImage.setImageResource(R.drawable.board_generic);
                break;
        }


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onNodeSelected(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public void onDiscoveryChange(Manager m, boolean enabled) {

    }

    public void clear(){
        mValues.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Node> items){
        for(Node n: items){
            if(mFilterNode.displayNode(n)){
                mValues.add(n);
            }//if
        }//for
        notifyDataSetChanged();
    }

    /**
     * disconnect al connected node manage by this adapter
     */
    public void disconnectAllNodes() {
        for(Node n: mValues){
            if (n.isConnected())
                n.disconnect();
        }//for
    }//disconnectAllNodes

    private Handler mUIThread = new Handler(Looper.getMainLooper());

    @Override
    public void onNodeDiscovered(Manager m,final Node node) {
        if(mFilterNode.displayNode(node)){
            mUIThread.post(new Runnable() {
                @Override
                public void run() {
                    mValues.add(node);
                    notifyItemInserted(mValues.size() - 1);
                };
            });
        }//if
    }//onNodeDiscovered

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mNodeNameLabel;
        public final TextView mNodeTagLabel;
        public final ImageView mNodeImage;
        public Node mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mNodeImage = (ImageView) view.findViewById(R.id.nodeBoardIcon);
            mNodeNameLabel = (TextView) view.findViewById(R.id.nodeName);
            mNodeTagLabel = (TextView) view.findViewById(R.id.nodeTag);
        }
    }
}
