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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.st.BlueSTSDK.gui.R;

import java.util.regex.Pattern;

/**
 * Fragment used for ask the user to insert its data, the activity that will contain this fragment
 * MUST implement the interface {@link RequestUserDataFragment.OnFragmentInteractionListener}
 */
public class RequestUserDataFragment extends Fragment {

    private static final Pattern ONLY_LATIN_CHAR = Pattern.compile("[\\u0020-\\u007F]*");

    private static final String USER_NAME = RequestUserDataFragment.class.getName()+".USER_NAME";
    private static final String USER_EMAIL = RequestUserDataFragment.class.getName()+".USER_EMAIL";
    private static final String USER_COMPANY = RequestUserDataFragment.class.getName()+".USER_COMPANY";

    private OnFragmentInteractionListener mListener;

    private View root;

    private TextInputLayout mUserNameLayout;
    private EditText mUserName;

    private TextInputLayout mEMailLayout;
    private EditText mEMail;

    private TextInputLayout mCompanyLayout;
    private EditText mCompany;


    public RequestUserDataFragment() {
        // Required empty public constructor
    }

    public static RequestUserDataFragment newInstance() {
        return new RequestUserDataFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);

        // Inflate the layout for this fragment
        root=inflater.inflate(R.layout.fragment_request_user_data, container, false);

        mUserNameLayout = (TextInputLayout) root.findViewById(R.id.usernameWrapper);
        mUserName = (EditText) root.findViewById(R.id.username);
        mUserName.setText(pref.getString(USER_NAME,""));
        mUserName.addTextChangedListener(new ValidateInputFiled() {
            @Override
            public void afterTextChanged(Editable editable) {
                validateUserName();
            }
        });

        mEMailLayout = (TextInputLayout) root.findViewById(R.id.emailWrapper);
        mEMail = (EditText) root.findViewById(R.id.email);
        mEMail.setText(pref.getString(USER_EMAIL,""));
        mEMail.addTextChangedListener(new ValidateInputFiled() {
            @Override
            public void afterTextChanged(Editable editable) {
                validateEmail();
            }
        });

        mCompanyLayout = (TextInputLayout) root.findViewById(R.id.companyWrapper);
        mCompany = (EditText) root.findViewById(R.id.company);
        mCompany.setText(pref.getString(USER_COMPANY,""));
        mCompany.addTextChangedListener(new ValidateInputFiled() {
            @Override
            public void afterTextChanged(Editable editable) {
                validateCompanyName();
            }
        });

        root.findViewById(R.id.sendRequestButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null && validateInput()) {
                    saveInputAsDefault();
                    mListener.onDataIsInserted(removeDangerousChar(mUserName.getText()),
                            removeDangerousChar(mEMail.getText()),
                            removeDangerousChar(mCompany.getText()));

                }
            }
        });

        return root;
    }

    private void saveInputAsDefault() {
        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        pref.edit()
                .putString(USER_NAME,mUserName.getText().toString())
                .putString(USER_EMAIL,mEMail.getText().toString())
                .putString(USER_COMPANY,mCompany.getText().toString())
                .apply();
    }

    @Override
    public void onAttach(Activity context) {
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
     * check that all the input are correct, if one is wrong show a message
     * @return true if all the input are correct
     */
    private boolean validateInput(){
        if(!validateUserName()) {
            Snackbar.make(root, R.string.LicenseManager_invalidUserName, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if(!validateEmail()) {
            Snackbar.make(root, R.string.LicenseManager_invalidEmail, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if(!validateCompanyName()) {
            Snackbar.make(root, R.string.LicenseManager_invalidCompanyName, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * remove strange characters from the string and remove the starting end ending space
     * @param sequence string to clean
     * @return string without strange characters
     */
    private String removeDangerousChar(CharSequence sequence){
        return sequence.toString().replace(",","").trim();
    }

    /**
     * check it the user name is valid
     * @return true if is not empty
     */
    private boolean validateUserName() {
        String input = removeDangerousChar(mUserName.getText());
        if(!ONLY_LATIN_CHAR.matcher(input).matches()){
            mUserNameLayout.setErrorEnabled(true);
            mUserNameLayout.setError(getString(R.string.LicenseManager_notLatinChar));
            requestFocus(mUserName);
            return false;
        }else if (input.isEmpty()) {
            mUserNameLayout.setErrorEnabled(true);
            mUserNameLayout.setError(getString(R.string.LicenseManager_invalidUserName));
            requestFocus(mUserName);
            return false;
        } else {
            mUserNameLayout.setError(null);
            mUserNameLayout.setErrorEnabled(false);
        }
        return true;
    }

    /**
     * check the the string is a valid email
     * @param email string to check
     * @return true if is not empty and with a valid email format
     */
    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * check it the email is valid
     * @return true if is not empty and a valid email
     */
    private boolean validateEmail() {
        String input = removeDangerousChar(mEMail.getText());
        if(!ONLY_LATIN_CHAR.matcher(input).matches()){
            mEMailLayout.setErrorEnabled(true);
            mEMailLayout.setError(getString(R.string.LicenseManager_notLatinChar));
            requestFocus(mEMail);
            return false;
        }else if (!isValidEmail(input)) {
            mEMailLayout.setErrorEnabled(true);
            mEMailLayout.setError(getString(R.string.LicenseManager_invalidEmail));
            requestFocus(mEMail);
            return false;
        } else {
            mEMailLayout.setError(null);
            mEMailLayout.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * check it the company name is valid
     * @return true if is not empty
     */
    private boolean validateCompanyName() {
        String input = removeDangerousChar(mCompany.getText());
        if(!ONLY_LATIN_CHAR.matcher(input).matches()){
            mCompanyLayout.setErrorEnabled(true);
            mCompanyLayout.setError(getString(R.string.LicenseManager_notLatinChar));
            requestFocus(mCompany);
            return false;
        }else if (input.isEmpty()) {
            mCompanyLayout.setErrorEnabled(true);
            mCompanyLayout.setError(getString(R.string.LicenseManager_invalidCompanyName));
            requestFocus(mCompany);
            return false;
        } else {
            mCompanyLayout.setError(null);
            mCompanyLayout.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams
                    .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    /**
     * helper used for remove the 2 method from the interface
     */
    private static abstract class ValidateInputFiled implements TextWatcher {

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

    }


    public interface OnFragmentInteractionListener {

        /**
         * callback called when the user finish to insert the data
         * @param userName inserted user name
         * @param email inserted email
         * @param company inserted company
         */
        void onDataIsInserted(String userName,String email,String company);
    }
    
}
