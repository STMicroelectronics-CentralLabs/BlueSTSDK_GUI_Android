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

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;

/**
 * Activity that display the license text that the user has to agree for request the license and
 * the field to fill for requiring the license
 */
public class RequestLicenseActivity extends AppCompatActivity implements
        ApproveLicenseFragment.OnFragmentInteractionListener,
        RequestUserDataFragment.OnFragmentInteractionListener {

    private static String LICENSE_INFO = RequestLicenseActivity.class.getCanonicalName()+"" +
            ".LicenseInfo";
    private static String BOARD_ID = RequestLicenseActivity.class.getCanonicalName()+".BoardId";


    /**
     * Create an intent for start this activity
     * @param c context
     * @param license to request
     * @param boardId board where the license will run
     * @return intent for start the activity
     */
    public static Intent getStartIntent(Context c, LicenseInfo license, String boardId){
        Intent i = new Intent(c,RequestLicenseActivity.class);
        i.putExtra(LICENSE_INFO,license);
        i.putExtra(BOARD_ID,boardId);
        return i;
    }

    /**
     * name of the license to obtain
     */
    private LicenseInfo mLicense;

    /**
     * board id where the license will be used
     */
    private String mBoardUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_license);

        Bundle args;
        if(savedInstanceState!=null){
            args = savedInstanceState;
        }else
            args = getIntent().getExtras();

        mBoardUid = args.getString(BOARD_ID);
        mLicense = args.getParcelable(LICENSE_INFO);

        TextView title = (TextView) findViewById(R.id.licTitle);
        if(title!=null)
            title.setText(mLicense.longName);

        if(savedInstanceState==null) { // the first time add the fragment
            Fragment licApprove = ApproveLicenseFragment.newInstance(mLicense.disclaimerFile);
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.licRequestFragment, licApprove)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BOARD_ID,mBoardUid);
        outState.putParcelable(LICENSE_INFO,mLicense);
    }

    @Override
    public void onDisagreeButtonPressed() {
        finish();
    }

    /**
     * when the user agree the license, display the fragment for ask the data
     */
    @Override
    public void onAgreeButtonPressed() {
        getFragmentManager()
                .beginTransaction()
                    .replace(R.id.licRequestFragment,RequestUserDataFragment.newInstance())
                .commit();
    }

    /** when the user gives the data, send the request mail */
    @Override
    public void onDataIsInserted(String userName, String email, String company) {
        Intent i = new GenerateMailText(userName,company,email,mLicense,mBoardUid)
                .prepareSendMailIntent(this);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException ex){
            Toast.makeText(this, "Error: "+ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
