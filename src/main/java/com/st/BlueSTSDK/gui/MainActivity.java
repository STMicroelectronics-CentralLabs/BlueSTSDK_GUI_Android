package com.st.BlueSTSDK.gui;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public abstract class MainActivity extends AppCompatActivity {

    /**
     * The number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 1000;
    private static final String SPLASH_SCREEN_WAS_SHOWN = MainActivity.class.getCanonicalName()+"" +
            ".SplashWasShown";
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private final Handler mHideHandler = new Handler();

    private View mControlsView;
    private final Runnable mShowContentRunnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mControlsView = findViewById(R.id.main_content_controls);

        TextView versionText = (TextView) findViewById(R.id.versionText);
        TextView appText = (TextView) findViewById(R.id.appNameText);
        //show the version using the data in the manifest
        String version=null;
        CharSequence appName=null;

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            appName = getPackageManager().getApplicationLabel(pInfo.applicationInfo);
            if(versionText!=null) {
                String temp = pInfo.versionName;
                if (temp != null)
                    versionText.append(temp);
                else
                    versionText.setText("");
            }//if
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }//try-catch

        if(versionText!=null){
            if(version!=null){
                versionText.append(version);
            }else
                versionText.setText("");
        }

        if(appText!=null && appName!=null){
            appText.setText(appName);
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        boolean splashWasShown=false;
        if(savedInstanceState!=null){
            splashWasShown = savedInstanceState.getBoolean(SPLASH_SCREEN_WAS_SHOWN,false);
        }//if

        // Trigger the initial showSplashScreen() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        if(!splashWasShown) {
            showSplashScreen();
            delayedShowContent(AUTO_HIDE_DELAY_MILLIS);
        }else{
            mControlsView.setVisibility(View.VISIBLE);
        }
    }

    protected void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putBoolean(SPLASH_SCREEN_WAS_SHOWN,true);
    }

    private void showSplashScreen() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowContentRunnable);
    }

    /**
     * Schedules a call to showContent() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedShowContent(int delayMillis) {
        mHideHandler.removeCallbacks(mShowContentRunnable);
        mHideHandler.postDelayed(mShowContentRunnable, delayMillis);
    }

    public abstract void startScanBleActivity(View view);

    public abstract void startAboutActivity(View view);
}
