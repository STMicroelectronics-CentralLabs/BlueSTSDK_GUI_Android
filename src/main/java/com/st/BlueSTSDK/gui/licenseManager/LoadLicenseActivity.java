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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.ActivityWithNode;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.LicenseConsole;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseManagerDBContract;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseManagerDbHelper;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Activity where the use has to paste the license code
 */
public class LoadLicenseActivity extends ActivityWithNode {

    private static final String LICENSE_INFO =LoadLicenseActivity.class.getCanonicalName()+"" +
            ".LICENSE_INFO";
    private static final String BOARD_ID = LoadLicenseActivity.class.getCanonicalName()+"" +
            ".BOARD_ID";

    /**
     * pattern used for detect the license code: 8 hexadecimal number in the format 0xXXXXXXXX
     */
    private static final Pattern EXTRACT_LIC_CODE = Pattern.compile("0x([0-9A-F]{8})");

    /**
     * pattern used for extract the license name
     */
    private static final Pattern EXTRACT_lIB_NAME = Pattern.compile("osx_.*(..)_license\\[3\\]\\[4\\]");

    /**
     * Create an intent for start this activity
     * @param c context
     * @param node node where load the license
     * @param boardId board id
     * @param info
     * @return Intent for start this activity
     */
    public static Intent getStartIntent(Context c, @NonNull Node node, String
            boardId, LicenseInfo info){
        Intent temp = ActivityWithNode.getStartIntent(c,LoadLicenseActivity.class,node,true);
        temp.putExtra(BOARD_ID,boardId);
        temp.putExtra(LICENSE_INFO,info);
        return temp;
    }

    /**
     * View where the user has to write the license data
     */
    private View mRootView;
    private TextView mLicText;

    private  LicenseInfo mLicenseInfo;
    /**
     * id of the board where load the data
     */
    private String mBoardId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_license);

        Bundle args;
        if(savedInstanceState!=null){
            args = savedInstanceState;
        }else
            args = getIntent().getExtras();

        mBoardId = args.getString(BOARD_ID);
        mLicenseInfo = args.getParcelable(LICENSE_INFO);
        mRootView = findViewById(R.id.loadLic_rootView);
        mLicText = (TextView) findViewById(R.id.loadLic_text);
        TextView mLicName = (TextView) findViewById(R.id.loadLic_licenseName);
        mLicName.setText(mLicenseInfo.longName);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BOARD_ID,mBoardId);
        outState.putParcelable(LICENSE_INFO,mLicenseInfo);
    }

    /**
     * parse the input data for extract the license data
     * @param licText input text
     * @return license code or null if a valid format is not recognized
     */
    private static @Nullable  byte[] extractLicenseCode(CharSequence licText){
        Matcher libCodeMatch = EXTRACT_LIC_CODE.matcher(licText);
        StringBuilder lic = new StringBuilder(96);

        int nCode=0;
        while(libCodeMatch.find()){
            lic.append(libCodeMatch.group(1));
            nCode++;
        }

        //if we found 12 match -> 12*8 = 96 hex digit
        if(nCode==12) {
            byte data[] = new BigInteger(lic.toString(), 16).toByteArray();
            //the byte array can add a byte for be secure that the first bit is 0 ->
            //check if the length is different from what we aspect we skip the fist bit.
            if(data.length == 49){
                return Arrays.copyOfRange(data,1,data.length); // skip the position 0
            }else
                return data;
        }else
            return null;

    }

    /**
     * extract the license short name
     * @param licText  input text
     * @return license name or null if it
     */
    private static @Nullable String extractLicenseName(CharSequence licText){
        Matcher libNameMatch = EXTRACT_lIB_NAME.matcher(licText);
        if(libNameMatch.find()){
            return libNameMatch.group(1).toUpperCase();
        }else
            return null;
    }


    private void startLoadLicenseTask(Node node, final String licName, final byte[] licCode){
        LicenseConsole console = LicenseConsole.getLicenseConsole(node);
        if(console==null)
            return;

        console.writeLicenseCode(licName,licCode,
                new AddDbEntryOnSuccess(console.getDefaultWriteLicenseCallback(this)));

    }

    private void startLoadOnNodeConnected(Node node, final String licName, final byte[] licCode){
        node.addNodeStateListener(new Node.NodeStateListener() {
            @Override
            public void onStateChange(Node node, Node.State newState, Node.State prevState) {
                if(newState== Node.State.Connected){
                    startLoadLicenseTask(node,licName,licCode);
                    node.removeNodeStateListener(this);
                }
            }
        });
    }

    /**
     * callback called when the fab is pressed, it parse the user input and start to load the
     * data on the board
     * @param v fab button
     */
    public void loadLicense(final View v){
        //get the text and extract the needed value
        CharSequence licText = mLicText.getText();
        final byte[] licCode = extractLicenseCode(licText);
        final String licName = extractLicenseName(licText);

        //if both data are present load the license
        if(licCode!=null && licName!=null){
            if(!licName.equalsIgnoreCase(mLicenseInfo.shortName)){
                Snackbar.make(mRootView, R.string.licenseManager_invalidLicenseType,
                        Snackbar.LENGTH_LONG).show();
                return;
            }//else
            final Node node = getNode();
            if(node!=null) {
                if (node.getState() == Node.State.Connected) {
                    startLoadLicenseTask(node, licName, licCode);
                } else
                    startLoadOnNodeConnected(node, licName, licCode);
            }
        }else{
            if(licCode==null)
                Snackbar.make(mRootView,R.string.licenseManager_licenseCodeNotFound,
                        Snackbar.LENGTH_SHORT).show();
            else
                Snackbar.make(mRootView,R.string.licenseManager_licenseNameNotFound,
                        Snackbar.LENGTH_SHORT).show();
        }
    }//loadLicenseStatus

    private class AddDbEntryOnSuccess implements LicenseConsole.WriteLicenseCallback{

        private LicenseConsole.WriteLicenseCallback mDefault;

        private ProgressDialog mLoadLicenseProgress=null;

        private void dismissDialog(){
            if(mLoadLicenseProgress!=null){
                mLoadLicenseProgress.dismiss();
                mLoadLicenseProgress=null;
            }
        }

        AddDbEntryOnSuccess(LicenseConsole.WriteLicenseCallback userCallback){
            mDefault=userCallback;
            mLoadLicenseProgress = new ProgressDialog(LoadLicenseActivity.this,
                    ProgressDialog.STYLE_SPINNER);
            mLoadLicenseProgress.setTitle(R.string.licenseManager_loadDataTitle);
            mLoadLicenseProgress.setMessage(getString(R.string.LicenseManager_uploadLicMsg));
            mLoadLicenseProgress.show();

        }

        @Override
        public void onLicenseLoadSuccess(LicenseConsole console, String licName, byte[] licCode) {
            LicenseManagerDbHelper.getInstance(LoadLicenseActivity.this)
                    .insert(new LicenseManagerDBContract.LicenseEntry(mBoardId, licName,licCode));
            dismissDialog();
            mDefault.onLicenseLoadSuccess(console,licName,licCode);

        }

        @Override
        public void onLicenseLoadFail(LicenseConsole console, String licName, byte[] licCode) {
            dismissDialog();
            mDefault.onLicenseLoadFail(console,licName,licCode);
        }
    }

}
