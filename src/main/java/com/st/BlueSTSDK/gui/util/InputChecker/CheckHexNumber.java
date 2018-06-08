package com.st.BlueSTSDK.gui.util.InputChecker;

import android.support.design.widget.TextInputLayout;

import java.util.regex.Pattern;

public class CheckHexNumber extends CheckRegularExpression {

    private static final Pattern HEX_PATTERN = Pattern.compile("(0[xX])?[0-9a-fA-F]+");

    /**
     * check that the user input match a pattern
     *
     * @param textInputLayout layout containing the text view
     * @param errorMessageId  error to display if the input do not match the  pattern
     */
    public CheckHexNumber(TextInputLayout textInputLayout, int errorMessageId) {
        super(textInputLayout, errorMessageId, HEX_PATTERN);
    }
}
