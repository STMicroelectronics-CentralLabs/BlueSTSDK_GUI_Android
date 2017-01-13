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
        setVisible(holder.mLicenseRequest,!lic.isPresentOnTheBoard);
        setVisible(holder.mLicenseUpload, !lic.isPresentOnTheBoard);
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
