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
public class LicenseStatusRecyclerViewAdapter extends
        RecyclerView.Adapter<LicenseStatusRecyclerViewAdapter.ViewHolder>{

    /**
     * Callback used for notify to the view that the use select a license
     */
    public interface LicenseStatusViewCallback{
        /**
         * Called when the user click on the request license button
         * @param lic license associate to that button
         */
        void onLicenseRequestClick(LicenseStatus lic);
        void onLicenseUploadClick(LicenseStatus lic);
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

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LicenseStatus lic = mAllLic.get(position);
        holder.mLic = lic;
        holder.mLicenseName.setText(lic.info.longName);
        holder.mLicenseDesc.setText(lic.info.licenseDesc);
        if(lic.isPresentOnTheBoard){ // if the license is present show only the tick image
            holder.mLicenseIsOk.setVisibility(View.VISIBLE);
            holder.mLicenseRequest.setVisibility(View.GONE);
            holder.mLicenseUpload.setVisibility(View.GONE);
        }else{ // otherwise show the load/request button
            holder.mLicenseIsOk.setVisibility(View.GONE);
            if(!lic.isPresentOnDB) // hide the request if the license is already present in the db
                holder.mLicenseRequest.setVisibility(View.VISIBLE);
            else
                holder.mLicenseRequest.setVisibility(View.GONE);
            holder.mLicenseUpload.setVisibility(View.VISIBLE);
        }//if-else
    }//onBindViewHolder

    @Override
    public int getItemCount() {
        return mAllLic.size();
    }

    /**
     * Class containing the view object used for display the license status
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mLicenseName;
        public final TextView mLicenseDesc;
        public final ImageView mLicenseIsOk;
        public final View mLicenseRequest;
        public final View mLicenseUpload;
        public LicenseStatus mLic;

        public ViewHolder(View view) {
            super(view);
            mLicenseName = (TextView) view.findViewById(R.id.licNameText);
            mLicenseDesc = (TextView) view.findViewById(R.id.licNameDesc);
            mLicenseIsOk = (ImageView) view.findViewById(R.id.licIsPresentImage);
            mLicenseRequest = view.findViewById(R.id.licRequestButton);
            mLicenseUpload = view.findViewById(R.id.licUploadButton);

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
        }//ViewHolder
    }//ViewHolder
}
