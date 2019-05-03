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

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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

/**
 * Adapter view for a list of discovered nodes
 */
public class NodeRecyclerViewAdapter extends RecyclerView.Adapter<NodeRecyclerViewAdapter.ViewHolder>
        implements Manager.ManagerListener{

    private final List<Node> mValues = new ArrayList<>();

    /**
     * Interface to use when a node is selected by the user
     */
    public interface OnNodeSelectedListener{
        /**
         * function call when a node is selected by the user
         * @param n node selected
         */
        void onNodeSelected(@NonNull Node n);
    }

    /**
     * Interface used to filter the node
     */
    public interface FilterNode{
        /**
         * function for filter the node to display
         * @param n node to display
         * @return true if the node must be displayed, false otherwise
         */
        boolean displayNode(@NonNull  Node n);
    }

    private OnNodeSelectedListener mListener;
    private FilterNode mFilterNode;

    public NodeRecyclerViewAdapter(List<Node> items, OnNodeSelectedListener listener,
                                   FilterNode filter) {
        mListener = listener;
        mFilterNode = filter;
        addAll(items);
    }//NodeRecyclerViewAdapter

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.node_list_item, parent, false);
        return new ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Node n = mValues.get(position);
        holder.mItem = n;
        holder.mNodeNameLabel.setText(n.getName());
        holder.mNodeTagLabel.setText(n.getTag());

        @DrawableRes int boardImageRes = NodeGui.getBoardTypeImage(n.getType());
        Drawable boardImage = ContextCompat.getDrawable(holder.mNodeHasExtension.getContext(),boardImageRes);
        holder.mNodeImage.setImageDrawable(boardImage);


        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onNodeSelected(holder.mItem);
            }
        });

        if(n.isSleeping()){
            holder.mNodeIsSleeping.setVisibility(View.VISIBLE);
        }else
            holder.mNodeIsSleeping.setVisibility(View.GONE);

        if(n.hasGeneralPurpose()){
            holder.mNodeHasExtension.setVisibility(View.VISIBLE);
        }else
            holder.mNodeHasExtension.setVisibility(View.GONE);

    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public void onDiscoveryChange(@NonNull Manager m, boolean enabled) {

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

    private Handler mUIThread = new Handler(Looper.getMainLooper());

    @Override
    public void onNodeDiscovered(@NonNull Manager m, @NonNull final Node node) {
        if(mFilterNode.displayNode(node)){
            mUIThread.post(() -> {
                mValues.add(node);
                notifyItemInserted(mValues.size() - 1);
            });
        }//if
    }//onNodeDiscovered

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mNodeNameLabel;
        final TextView mNodeTagLabel;
        final ImageView mNodeImage;
        final ImageView mNodeIsSleeping;
        final ImageView mNodeHasExtension;
        Node mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mNodeImage = view.findViewById(R.id.nodeBoardIcon);
            mNodeNameLabel = view.findViewById(R.id.nodeName);
            mNodeTagLabel = view.findViewById(R.id.nodeTag);
            mNodeHasExtension = view.findViewById(R.id.hasExtensionIcon);
            mNodeIsSleeping = view.findViewById(R.id.isSleepingIcon);
        }
    }
}
