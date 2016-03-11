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
    private static final int AUTO_HIDE_DELAY_MILLIS = 2000;
    private static final String SPLASH_SCREEN_WAS_SHOWN = MainActivity.class.getCanonicalName()+"" +
            ".SplashWasShown";
    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private ImageView mSplashView;

    private final Runnable mShowSplashPart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mSplashView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowContentPart2Runnable = new Runnable() {
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


    private final Runnable mShowContentRunnable = new Runnable() {
        @Override
        public void run() {
            showContent();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mControlsView = findViewById(R.id.main_content_controls);
        mSplashView = (ImageView)findViewById(R.id.splashScreen_content);

        // Set up the user interaction to manually showContent or showSplashScreen the system UI.
        mSplashView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showContent();
            }
        });

        TextView versionText = (TextView) findViewById(R.id.versionText);
        TextView appText = (TextView) findViewById(R.id.appNameText);
        //show the version using the data in the manifest
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionText.append(pInfo.versionName);
            appText.setText(getPackageManager().getApplicationLabel(pInfo.applicationInfo));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }//try-catch

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
        mHideHandler.removeCallbacks(mShowContentPart2Runnable);
        mHideHandler.postDelayed(mShowSplashPart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void showContent() {
        // Show the system bar
        mSplashView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mShowSplashPart2Runnable);
        mHideHandler.postDelayed(mShowContentPart2Runnable, UI_ANIMATION_DELAY);
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
