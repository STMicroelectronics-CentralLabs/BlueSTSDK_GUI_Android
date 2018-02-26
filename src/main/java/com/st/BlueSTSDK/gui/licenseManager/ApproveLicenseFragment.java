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

import android.app.Activity;
import android.app.Fragment;
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

/**
 * Fragment that will show the license agreement and the button for agree it
 * the activity that will use this fragment MUST implement the
 * {@link ApproveLicenseFragment.OnFragmentInteractionListener}
 * interface
 */
public class ApproveLicenseFragment extends Fragment {
    private static final String DISCLAIMER_FILE = ApproveLicenseFragment.class.getCanonicalName()
            +".DISCLAIMER_FILE";

    /**
     * object used for notify the user action
     */
    private OnFragmentInteractionListener mListener;

    /**
     * reference to the file to show
     */
    private @RawRes int mDisclaimerFile;

    /**
     * text view where show the file
     */
    private TextView mLicenseTextView;

    public ApproveLicenseFragment() {
        // Required empty public constructor
    }

    /**
     * create the fragment and set its arguments
     * @param disclaimerFile resource to read for display the license
     * @return a fragment reference
     */
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
        }//if
    }

    /**
     * Create an async task for read the file and display the final string in the textView
     */
    private void asyncLoadLicenseFile(){

        InputStream fileStream = getResources().openRawResource(mDisclaimerFile);

        new AsyncTask<InputStreamReader,Void,CharSequence>(){

            @Override
            protected CharSequence doInBackground(InputStreamReader... files) {
                StringBuilder fileContent = new StringBuilder();
                String line;
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

        asyncLoadLicenseFile();

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
    public void onAttach(Activity context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }//if-else
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * interface used for notify the user interaction with this fragment
     */
    public interface OnFragmentInteractionListener {
        /**
         * called when the agree button is pressed
         */
        void onAgreeButtonPressed();

        /**
         * called when the disagrree button is pressed
         */
        void onDisagreeButtonPressed();
    }
}
