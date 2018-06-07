package com.st.BlueSTSDK.gui.util.InputChecker;

import android.text.Editable;
import android.text.TextWatcher;

public class MultipleInputChecker implements TextWatcher {
    
    private InputChecker[] mCheckers;
    
    public MultipleInputChecker(InputChecker... checkers){
        mCheckers = checkers;
    }
    
    
    @Override
    public void afterTextChanged(Editable s) {
        String str = s.toString();
        for (InputChecker check: mCheckers ) {
            check.afterTextChanged(s);
            //stop the loop at the fist check that fail, otherwise the error will be removed
            if(!check.validate(str)){
                break;
            }
        }
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {   }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {   }
}
