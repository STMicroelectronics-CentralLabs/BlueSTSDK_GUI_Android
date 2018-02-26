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

package com.st.BlueSTSDK.gui.licenseManager;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueSTSDK.gui.R;

import java.util.List;

/**
 * Adapter view for display a list of license status
 */
class LicenseStatusRecyclerViewAdapter extends
        RecyclerView.Adapter<LicenseStatusRecyclerViewAdapter.ViewHolder>{

    /**
     * Callback used for notify to the view that the use select a license
     */
    interface LicenseStatusViewCallback{
        /**
         * Called when the user click on the request license button
         * @param lic license associate to that button
         */
        void onLicenseRequestClick(LicenseStatus lic);

        /**
         * called when the user want to insert a new license value
         * @param lic license that would be insert by the user
         */
        void onLicenseUploadClick(LicenseStatus lic);

        /**
         * called when the user want to load a stored lincese
         * @param lic license to load
         */
        void onLicenseUploadStoreClick(LicenseStatus lic);
    }

    /**
     * list of license to display
     */
    private List<LicenseStatus> mAllLic;

    /**
     * Callback object used for notify the user action
     */
    private LicenseStatusViewCallback mCallback;

    /**
     * Create an adapter for display a list of license status
     * @param lic list of license to display
     * @param callback callback object used for notify the user action
     */
    LicenseStatusRecyclerViewAdapter(List<LicenseStatus> lic,
                                     LicenseStatusViewCallback callback){
        mAllLic=lic;
        mCallback = callback;
    }//LicenseStatusRecyclerViewAdapter

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.license_item_view, parent, false);
        return new ViewHolder(view);
    }

    private static void setVisible(View v, boolean isVisible){
        if(isVisible)
            v.setVisibility(View.VISIBLE);
        else
            v.setVisibility(View.GONE);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LicenseStatus lic = mAllLic.get(position);
        holder.mLic = lic;
        holder.mLicenseName.setText(lic.info.longName);
        holder.mLicenseDesc.setText(lic.info.licenseDesc);

        setVisible(holder.mLicenseIsOk,lic.isPresentOnTheBoard);
        //keep the request and upload visible button always visible because
        // the wesu board keep as valid license also wrong one so can be necessary upload an already
        //valid license
        //setVisible(holder.mLicenseRequest,!lic.isPresentOnTheBoard);
        //setVisible(holder.mLicenseUpload, !lic.isPresentOnTheBoard);
        setVisible(holder.mLicenseUploadStored, !lic.isPresentOnTheBoard && lic.isPresentOnDB);

    }//onBindViewHolder

    @Override
    public int getItemCount() {
        return mAllLic.size();
    }

    /**
     * Class containing the view object used for display the license status
     */
    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mLicenseName;
        final TextView mLicenseDesc;
        final ImageView mLicenseIsOk;
        final View mLicenseRequest;
        final View mLicenseUpload;
        final View mLicenseUploadStored;
        LicenseStatus mLic;

        ViewHolder(View view) {
            super(view);
            mLicenseName = (TextView) view.findViewById(R.id.licNameText);
            mLicenseDesc = (TextView) view.findViewById(R.id.licNameDesc);
            mLicenseIsOk = (ImageView) view.findViewById(R.id.licIsPresentImage);
            mLicenseRequest = view.findViewById(R.id.licRequestButton);
            mLicenseUpload = view.findViewById(R.id.licUploadButton);
            mLicenseUploadStored = view.findViewById(R.id.licUploadStoredButton);

            //when clicked call onLicenseRequestClick
            mLicenseRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mCallback!=null){
                        mCallback.onLicenseRequestClick(mLic);
                    }//if
                }//onClick
            });
            mLicenseUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mCallback!=null){
                        mCallback.onLicenseUploadClick(mLic);
                    }//if
                }//onClick
            });
            mLicenseUploadStored.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mCallback!=null){
                        mCallback.onLicenseUploadStoreClick(mLic);
                    }//if
                }
            });

        }//ViewHolder
    }//ViewHolder
}
