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

package com.st.BlueSTSDK.gui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;


/**
 * Activity to display with the app info
 */
public class AboutActivity extends AppCompatActivity {
    private static final String ABOUT_PAGE_URL = AboutActivity.class.getCanonicalName()+".ABOUT_PAGE_URL";
    private static final String PRIVACY_PAGE_URL = AboutActivity.class.getCanonicalName()+".PRIVACY_PAGE_URL";


    public static void startActivityWithAboutPage(Context c, @Nullable String aboutPageUrl,@Nullable String privacyUrl){
        Intent intent = new Intent(c,AboutActivity.class);
        intent.putExtra(ABOUT_PAGE_URL,aboutPageUrl);
        intent.putExtra(PRIVACY_PAGE_URL,privacyUrl);
        c.startActivity(intent);
    }

    private void setUpAppVersion(){
        TextView tvVersion = (TextView) findViewById(R.id.textViewVersion);
        try {
            Context appContext = getApplicationContext();
            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText(getString(R.string.about_version,pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setUpAppName(){
        TextView tvAppName = (TextView) findViewById(R.id.textViewName);
        try {
            Context appContext = getApplicationContext();
            PackageInfo pInfo = appContext.getPackageManager().getPackageInfo(getPackageName(), 0);
            tvAppName.setText(appContext.getPackageManager().getApplicationLabel(pInfo.applicationInfo));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setUpMainPage(@Nullable String aboutPageUrl){
        if(aboutPageUrl==null)
            return;

        WebView browser = (WebView) findViewById(R.id.aboutText);
        browser.setVerticalScrollBarEnabled(false);
        browser.loadUrl(aboutPageUrl);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setUpMainPage(getIntent().getStringExtra(ABOUT_PAGE_URL));


        setUpAppName();
        setUpAppVersion();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_about, menu);

        MenuItem item = menu.findItem(R.id.menu_about_show_privacy);

        setUpPrivacyMenu(item, getIntent().getStringExtra(PRIVACY_PAGE_URL));

        return super.onCreateOptionsMenu(menu);
    }

    private void setUpPrivacyMenu(MenuItem item, @Nullable String privacyPage) {
        if(privacyPage == null)
            item.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_about_show_privacy){

            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
