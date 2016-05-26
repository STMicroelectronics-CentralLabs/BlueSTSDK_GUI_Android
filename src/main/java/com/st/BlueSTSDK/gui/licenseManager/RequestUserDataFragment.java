package com.st.BlueSTSDK.gui.licenseManager;

import android.app.Activity;
import android.app.Fragment;
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

/**
 * Fragment used for ask the user to insert its data, the activity that will contain this fragment
 * MUST implement the interface {@link RequestUserDataFragment.OnFragmentInteractionListener}
 */
public class RequestUserDataFragment extends Fragment {

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
        // Inflate the layout for this fragment
        root=inflater.inflate(R.layout.fragment_request_user_data, container, false);

        mUserNameLayout = (TextInputLayout) root.findViewById(R.id.usernameWrapper);
        mUserName = (EditText) root.findViewById(R.id.username);
        mUserName.addTextChangedListener(new ValidateInputFiled() {
            @Override
            public void afterTextChanged(Editable editable) {
                validateUserName();
            }
        });

        mEMailLayout = (TextInputLayout) root.findViewById(R.id.emailWrapper);
        mEMail = (EditText) root.findViewById(R.id.email);
        mEMail.addTextChangedListener(new ValidateInputFiled() {
            @Override
            public void afterTextChanged(Editable editable) {
                validateEmail();
            }
        });

        mCompanyLayout = (TextInputLayout) root.findViewById(R.id.companyWrapper);
        mCompany = (EditText) root.findViewById(R.id.company);
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
                    mListener.onDataIsInserted(removeDangerousChar(mUserName.getText()),
                            removeDangerousChar(mEMail.getText()),
                            removeDangerousChar(mCompany.getText()));
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
            Snackbar.make(root, R.string.invalidUserName, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if(!validateEmail()) {
            Snackbar.make(root, R.string.invalidEmail, Snackbar.LENGTH_SHORT).show();
            return false;
        }
        if(!validateCompanyName()) {
            Snackbar.make(root, R.string.invalidCompanyName, Snackbar.LENGTH_SHORT).show();
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
        if (input.isEmpty()) {
            mUserNameLayout.setErrorEnabled(true);
            mUserNameLayout.setError(getString(R.string.invalidUserName));
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

        if (!isValidEmail(input)) {
            mEMailLayout.setErrorEnabled(true);
            mEMailLayout.setError(getString(R.string.invalidEmail));
            requestFocus(mEMail);
            return false;
        } else {
            mUserNameLayout.setError(null);
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
        if (input.isEmpty()) {
            mCompanyLayout.setErrorEnabled(true);
            mCompanyLayout.setError(getString(R.string.invalidCompanyName));
            requestFocus(mCompany);
            return false;
        } else {
            mUserNameLayout.setError(null);
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
