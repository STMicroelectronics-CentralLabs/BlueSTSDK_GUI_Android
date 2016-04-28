package com.st.BlueSTSDK.gui.licenseManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;

public class ApproveLicenseActivity extends Activity {

    private static String LICENSE_INFO = ApproveLicenseActivity.class.getCanonicalName()+"" +
            ".LicenseInfo";
    private static String BOARD_ID = ApproveLicenseActivity.class.getCanonicalName()+".BoardId";

    public static Intent getStartIntent(Context c, LicenseInfo licenseName, String boardId){
        Intent i = new Intent(c,ApproveLicenseActivity.class);
        i.putExtra(LICENSE_INFO,licenseName);
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
        title.setText(mLicense.longName);

        WebView licView = (WebView) findViewById(R.id.licView);
        licView.loadUrl(mLicense.licensePage);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BOARD_ID,mBoardUid);
        outState.putParcelable(LICENSE_INFO,mLicense);
    }

    /**
     * prepare the mail content for request the license
     */
    void prepareLicenseRequestMail(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"recipient@example.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "Request License for: "+mLicense.longName);
        i.putExtra(Intent.EXTRA_TEXT, "License: "+mLicense.longName+"\n STM32 UID: "+mBoardUid);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    public void onAgreeClick(View view) {
        prepareLicenseRequestMail();
        finish();
    }

    public void onDisagreeClick(View view) {
        finish(); //go back
    }
}
