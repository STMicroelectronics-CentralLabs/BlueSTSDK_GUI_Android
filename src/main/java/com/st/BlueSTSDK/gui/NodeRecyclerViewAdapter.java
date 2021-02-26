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

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.NumberConversion;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter view for a list of discovered nodes
 */
public class NodeRecyclerViewAdapter extends RecyclerView.Adapter<NodeRecyclerViewAdapter.ViewHolder>
        implements Manager.ManagerListener{

    private final List<Node> mValues = new ArrayList<>();
    private Context context;

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
        context = parent.getContext();
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

        if(n.getAdvertiseInfo().getProtocolVersion()==1) {
            holder.mNodeRunningCodeDemo.setVisibility(View.GONE);
            holder.mNodeAdvertiseImage.setVisibility(View.GONE);
        } else {
            //For SDK protocol v2 we have the option bytes instead of feature Mask
            holder.mNodeRunningCodeDemo.setVisibility(View.VISIBLE);

            //Take the four Option bytes
            byte [] optBytes = NumberConversion.BigEndian.uint32ToBytes(n.getAdvertiseOptionBytes());

            holder.mNodeAdvertiseImage.setVisibility(View.VISIBLE);
            setBatteryImage(holder.mNodeAdvertiseImage,(optBytes[1]&0xF));

            setRunningCodeDemo(holder.mNodeRunningCodeDemo,optBytes[0]);
        }


        //On Press Item
        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onNodeSelected(holder.mItem);
            }
        });

        // On Long Press Item
        if(n.getAdvertiseInfo().getProtocolVersion()==2) {
            holder.mView.setOnLongClickListener(v -> {
                byte[] optBytes = NumberConversion.BigEndian.uint32ToBytes(n.getAdvertiseOptionBytes());
                AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                alertDialog.setTitle("Node Advertise Option Bytes");
                alertDialog.setMessage(String.format("Byte1 = 0x%02X\nByte2 = 0x%02X\nByte3 = 0x%02X\nByte4 = 0x%02X\n",
                        optBytes[0], optBytes[1], optBytes[2], optBytes[3]));
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Close",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;
            });
        }

        if(n.isSleeping()){
            holder.mNodeIsSleeping.setVisibility(View.VISIBLE);
        } else {
            holder.mNodeIsSleeping.setVisibility(View.GONE);
        }

        if(n.hasGeneralPurpose()){
            holder.mNodeHasExtension.setVisibility(View.VISIBLE);
        } else {
            holder.mNodeHasExtension.setVisibility(View.GONE);
        }
    }

    /**
     * Set the Functional pack name for the running code
     * @param mNodeRunningCodeDemo
     * @param optByte
     */
    private void setRunningCodeDemo(TextView mNodeRunningCodeDemo, byte optByte) {
        switch(optByte) {
            case 0x01:
                mNodeRunningCodeDemo.setText("FP-SNS-STBOX1");
                break;
            default:
                mNodeRunningCodeDemo.setText("Unknown");
                break;
        }
    }

    /**
     * Set the battery Icon image for the node
     * @param mNodeBatteryImage
     * @param batteryLevel
     */
    private void setBatteryImage(ImageView mNodeBatteryImage, int batteryLevel) {
        if(batteryLevel<=1) {
            mNodeBatteryImage.setImageResource(R.drawable.battery_00);
        } else if(batteryLevel<=2) {
            mNodeBatteryImage.setImageResource(R.drawable.battery_20);
        } else if(batteryLevel<=4) {
            mNodeBatteryImage.setImageResource(R.drawable.battery_40);
        } else if(batteryLevel<=6) {
            mNodeBatteryImage.setImageResource(R.drawable.battery_60);
        } else if(batteryLevel<=8) {
            mNodeBatteryImage.setImageResource(R.drawable.battery_80);
        } else {
            mNodeBatteryImage.setImageResource(R.drawable.battery_100);
        }
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
        final ImageView mNodeAdvertiseImage;
        final TextView mNodeRunningCodeDemo;
        Node mItem;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mNodeImage = view.findViewById(R.id.nodeBoardIcon);
            mNodeNameLabel = view.findViewById(R.id.nodeName);
            mNodeTagLabel = view.findViewById(R.id.nodeTag);
            mNodeHasExtension = view.findViewById(R.id.hasExtensionIcon);
            mNodeIsSleeping = view.findViewById(R.id.isSleepingIcon);
            mNodeAdvertiseImage = view.findViewById(R.id.hasAdvertiseIcon);
            mNodeRunningCodeDemo = view.findViewById(R.id.nodeRunningCodeName);
        }
    }
}
