package com.st.BlueSTSDK.gui.licenseManager;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.st.BlueSTSDK.gui.R;

import java.util.regex.Pattern;

public class RequestUserDataFragment extends Fragment {

    private static final Pattern VALID_EMAIL_PATTERN= Pattern.compile("^([a-zA-Z0-9_\\-\\.]+)"+
            "@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|" +
                    "(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");

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

    private String removeDangerousChar(CharSequence sequence){
        return sequence.toString().replace(",","").trim();
    }

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

    private boolean validateEmail() {
        String input = removeDangerousChar(mEMail.getText());

        if (input.isEmpty() || !isValidEmail(input)) {
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

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && VALID_EMAIL_PATTERN.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams
                    .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private static abstract class ValidateInputFiled implements TextWatcher {

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

    }

    public interface OnFragmentInteractionListener {
        void onDataIsInserted(String userName,String email,String company);
    }
    
}
