package com.st.BlueSTSDK.gui.licenseManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseDefines;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseManagerDBContract.LicenseEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog that will show the available license to the user.
 * The activity that will use this dialog MUST implement the interface
 * {@link LicenseSelectorCallback}
 * On default all the license will be selected
 */
public class LicenseSelectorDialogFragment extends DialogFragment {
    private static final String LICENSES= LicenseSelectorDialogFragment.class.getCanonicalName()
            +".LICENSE_TO_LOAD";

    /**
     * Interface used for notify that the user close the dialog
     */
    public interface LicenseSelectorCallback{

        /**
         * call when the user close the dialog if it press the cancel button the list will be empty
         * @param licenses selected license, the list can be empty
         */
        void onLicenseSelected(List<LicenseEntry> licenses);
    }


    /**
     * Instantiate the dialog fragment
     * @param licenseToSelect list of activity that the use can select
     * @return fragment instance
     */
    public static LicenseSelectorDialogFragment newInstance(
            ArrayList<LicenseEntry> licenseToSelect){

        LicenseSelectorDialogFragment temp = new LicenseSelectorDialogFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList(LICENSES,licenseToSelect);
        temp.setArguments(args);

        return temp;
    }//newInstance


    /**
     * Internal class used for add the selected flag to a LicenseEntry object
     */
    private static class SelectedLicense{
        /**
         * Object with the license information stored in the db
         */
        final LicenseEntry storedLicense;

        /**
         * Object with the license information
         */
        final LicenseInfo licenseInfo;

        /**
         * true if the item is selected for be uploaded on the node
         */
        boolean isSelected;

        /**
         * create a new object with the license data, and with isSelected = true
         * @param license license data
         */
        private SelectedLicense(LicenseEntry license) {
            this.storedLicense = license;
            licenseInfo = LicenseDefines.getLicenseInfo(license.getLicenseType());
            isSelected=true;
        }//SelectedLicense

    }//SelectedLicense


    /**
     * create a list of {@link SelectedLicense} from a list of {@link LicenseEntry}
     * @param in list of LicenseEntry
     * @return list of SelectedLicense, each item in the in list is present in this list
     */
    private static ArrayList<SelectedLicense> convertToSelectedLicense(ArrayList<LicenseEntry> in){
        ArrayList<SelectedLicense> temp = new ArrayList<>(in.size());

        for(LicenseEntry e: in){
            temp.add(new SelectedLicense(e));
        }

        return temp;
    }//convertToSelectedLicense


    /**
     * list of license to show to the user
     */
    private ArrayList<SelectedLicense> mLicense;

    /**
     * check that the activity implement the LicenseSelectorCallback interface otherwise throw a ClassCastException
     * @param activity activity where the dialog is shown
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            LicenseSelectorCallback temp= (LicenseSelectorCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LicenseSelectorCallback");
        }//try
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<LicenseEntry> data =getArguments().getParcelableArrayList(LICENSES);
        mLicense = convertToSelectedLicense(data);

        final Activity activity = getActivity();
        final ArrayAdapter<SelectedLicense> adapter= new LicenseSelectorListView(activity,mLicense);

        return new AlertDialog.Builder(activity)
                .setTitle(R.string.licenseManager_selectLicDialog_title)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mLicense.get(i).isSelected=!mLicense.get(i).isSelected;
                        adapter.notifyDataSetChanged();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((LicenseSelectorCallback)activity).onLicenseSelected
                                (extractSelectedLicense(mLicense));
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((LicenseSelectorCallback)activity)
                                .onLicenseSelected(new ArrayList<LicenseEntry>(0));
                        dialogInterface.dismiss();
                    }
                })
                .create();

    }

    /**
     * from a list of SelectedLicense, extract the selected LicenseEntry
     * @param lic list of object that the user can select
     * @return list of object that the user has select
     */
    private static List<LicenseEntry> extractSelectedLicense(List<SelectedLicense> lic){
        ArrayList<LicenseEntry> selected = new ArrayList<>(lic.size());
        for(SelectedLicense item : lic){
            if(item.isSelected){
                selected.add(item.storedLicense);
            }//if
        }//for
        return selected;
    }

    /**
     * Adapter for handle the list of Checkbox used for show the SelectLicense objects
     */
    private static class LicenseSelectorListView extends ArrayAdapter<SelectedLicense>{

        public LicenseSelectorListView(Context context, List<SelectedLicense> objects) {
            super(context, R.layout.dialog_load_license_item, objects);
        }

        public View getView (int position, View convertView, ViewGroup parent){

            if(convertView==null) { //allocate a new object is needed
                LayoutInflater inflater = (LayoutInflater) parent.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.dialog_load_license_item, parent,false);
            }//if

            SelectedLicense temp = getItem(position);

            //extract the combo box and set the value
            CheckBox item = (CheckBox) convertView.findViewById(R.id.licenseTypeName);
            item.setText(temp.licenseInfo.longName);
            item.setChecked(temp.isSelected);

            return convertView;
        }
    }

}
