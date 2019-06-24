package com.st.BlueSTSDK.gui.util.InputChecker;

import android.support.annotation.StringRes;
import android.support.design.widget.TextInputLayout;

public class CheckMultipleOf extends InputChecker {
    final private long multiple;

    public CheckMultipleOf(TextInputLayout textInputLayout, @StringRes int errorMessageId,long multiple){
        super(textInputLayout,errorMessageId);
        this.multiple = multiple;
    }

    @Override
    protected boolean validate(String input) {
        try{
            long value = Long.decode(input);
            return (value % multiple)==0;
        }catch (NumberFormatException e){
            return false;
        }
    }
}
