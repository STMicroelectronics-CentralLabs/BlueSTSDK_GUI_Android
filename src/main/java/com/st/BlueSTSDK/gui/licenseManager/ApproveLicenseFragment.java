package com.st.BlueSTSDK.gui.licenseManager;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.RawRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.st.BlueSTSDK.gui.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ApproveLicenseFragment extends Fragment {
    private static final String DISCLAIMER_FILE = ApproveLicenseFragment.class.getCanonicalName()
            +".DISCLAIMER_FILE";

    private OnFragmentInteractionListener mListener;

    private @RawRes int mDisclaimerFile;
    private TextView mLicenseTextView;

    public ApproveLicenseFragment() {
        // Required empty public constructor
    }


    public static ApproveLicenseFragment newInstance(@RawRes int disclaimerFile) {
        ApproveLicenseFragment fragment = new ApproveLicenseFragment();
        Bundle args = new Bundle();
        args.putInt(DISCLAIMER_FILE, disclaimerFile);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDisclaimerFile = getArguments().getInt(DISCLAIMER_FILE);
        }
    }

    private void loadLicenseFile(){

        InputStream fileStream = getResources().openRawResource(mDisclaimerFile);

        new AsyncTask<InputStreamReader,Void,CharSequence>(){

            @Override
            protected CharSequence doInBackground(InputStreamReader... files) {
                StringBuffer fileContent = new StringBuffer();
                String line = null;
                try {
                    BufferedReader reader = new BufferedReader(files[0]);
                    while ((line = reader.readLine()) != null) {
                        fileContent.append(line).append("\n");
                    }
                    reader.close();

                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }//try-catch
                return fileContent.toString();
            }

            @Override
            protected void onPostExecute(CharSequence fileContent) {
                mLicenseTextView.setText(fileContent);
            }
        }.execute(new InputStreamReader(fileStream));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_approve_license, container, false);
        mLicenseTextView = (TextView) root.findViewById(R.id.licView);

        loadLicenseFile();

        root.findViewById(R.id.licAgreeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener!=null){
                    mListener.onAgreeButtonPressed();
                }
            }
        });

        root.findViewById(R.id.licDisagreeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener!=null){
                    mListener.onDisagreeButtonPressed();
                }
            }
        });

        return root;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onAgreeButtonPressed();
        void onDisagreeButtonPressed();
    }
}
