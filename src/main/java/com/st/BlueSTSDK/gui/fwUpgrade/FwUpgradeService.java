package com.st.BlueSTSDK.gui.fwUpgrade;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.NotificationCompat;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwUpgradeConsole;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwVersion;

public class FwUpgradeService extends IntentService implements FwUpgradeConsole.FwUpgradeCallback{
    private static final int NOTIFICATION_ID = FwUpgradeService.class.hashCode();

    private static final String UPLOAD_FW =
            FwUpgradeService.class.getCanonicalName()+"action.uploadFw";

    private static final String FW_FILE_URI =
            FwUpgradeService.class.getCanonicalName()+"extra.fwUri";

    private static final String NODE_TAG =
            FwUpgradeService.class.getCanonicalName()+"extra.nodeTag";

    public static final String FW_UPLOAD_STARTED_ACTION = FwUpgradeService.class
            .getCanonicalName()+"action.FW_UPLOAD_STARTED_ACTION";

    public static final String FW_UPLOAD_STATUS_UPGRADE_ACTION = FwUpgradeService.class
            .getCanonicalName()+"action.FW_UPLOAD_STATUS_UPGRADE";
    public static final String FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE_EXTRA = FwUpgradeService.class
            .getCanonicalName()+"extra.FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE";
    public static final String FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE_EXTRA = FwUpgradeService.class
            .getCanonicalName()+"extra.FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE";

    public static final String FW_UPLOAD_FINISHED_ACTION = FwUpgradeService.class
            .getCanonicalName()+"action.FW_UPLOAD_FINISHED_ACTION";
    public static final String FW_UPLOAD_FINISHED_TIME_S_EXTRA = FwUpgradeService.class
            .getCanonicalName()+"extra.FW_UPLOAD_FINISHED_TIME_S";

    public static final String FW_UPLOAD_ERROR_ACTION = FwUpgradeService.class
            .getCanonicalName()+"action.FW_UPLOAD_ERROR_ACTION";
    public static final String FW_UPLOAD_ERROR_MESSAGE_EXTRA = FwUpgradeService.class
            .getCanonicalName()+"extra.FW_UPLOAD_ERROR_MESSAGE";


    private static String getErrorMessage(Context c,@UpgradeErrorType int errorCode){
        switch (errorCode) {
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_CORRUPTED_FILE:
                return c.getString(R.string.error_corrupted_file);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_INVALID_FW_FILE:
                return c.getString(R.string.error_invalid_file);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_TRANSMISSION:
                return c.getString(R.string.error_transmission);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_UNKNOWN:
                return c.getString(R.string.unknown_error);
        }
        return "";
    }

    public FwUpgradeService() {
        super(FwUpgradeService.class.getSimpleName());
    }


    public static void startUpload(Context context, Node node, Uri fwFile) {
        Intent intent = new Intent(context, FwUpgradeService.class);
        intent.setAction(UPLOAD_FW);
        intent.putExtra(FW_FILE_URI, fwFile);
        intent.putExtra(NODE_TAG,node.getTag());
        context.startService(intent);
    }

    public static IntentFilter getServiceActionFilter(){
        IntentFilter filter = new IntentFilter(FW_UPLOAD_FINISHED_ACTION);
        filter.addAction(FW_UPLOAD_STATUS_UPGRADE_ACTION);
        filter.addAction(FW_UPLOAD_STARTED_ACTION);
        filter.addAction(FW_UPLOAD_ERROR_ACTION);
        return filter;
    }

    private static Intent getFwUpgradeCompleteIntent(float durationS){
        return new Intent(FW_UPLOAD_FINISHED_ACTION)
                .putExtra(FW_UPLOAD_FINISHED_TIME_S_EXTRA,durationS);
    }

    private static Intent getFwUpgradeErrorIntent(String errorMessage){
        return new Intent(FW_UPLOAD_ERROR_ACTION)
                .putExtra(FW_UPLOAD_ERROR_MESSAGE_EXTRA,errorMessage);
    }

    private static Intent getFwUpgradeStatusIntent(long uploadBytes,long totalBytes){
        return new Intent(FW_UPLOAD_STATUS_UPGRADE_ACTION)
                .putExtra(FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE_EXTRA,totalBytes)
                .putExtra(FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE_EXTRA,uploadBytes);
    }

    private static Intent getFwUpgradeStartIntent(){
        return new Intent(FW_UPLOAD_STARTED_ACTION);
    }


    private long startUploadTime=-1;
    private LocalBroadcastManager mBroadcastManager;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotification;

    @Override
    public void onLoadFwError(FwUpgradeConsole console, Uri fwFile,
                              @UpgradeErrorType int error) {
        String errorMessage = getErrorMessage(this,error);
        mBroadcastManager.sendBroadcast( getFwUpgradeErrorIntent(errorMessage) );
        mNotification.setContentTitle("Error")
                .setContentText(errorMessage);
        mNotificationManager.notify(NOTIFICATION_ID,mNotification.build());
    }

    @Override
    public void onLoadFwComplete(FwUpgradeConsole console, Uri fwFile) {
        long totalTimeMs = System.currentTimeMillis()-startUploadTime;
        float totalTimeS= totalTimeMs/1000.0f;
        mBroadcastManager.sendBroadcast( getFwUpgradeCompleteIntent(totalTimeS) );
        mNotification.setContentTitle("Upload Complete")
                .setContentText("Reset the board for update");
        mNotificationManager.notify(NOTIFICATION_ID,mNotification.build());
    }

    private long mFileLength=Long.MAX_VALUE;
    @Override
    public void onLoadFwProgressUpdate(FwUpgradeConsole console, Uri fwFile, final long remainingBytes) {
        if(startUploadTime<0) {
            startUploadTime = System.currentTimeMillis();
            mFileLength=remainingBytes;
        }
        mNotification.setProgress((int)mFileLength,(int)(mFileLength-remainingBytes),false);
        mNotificationManager.notify(NOTIFICATION_ID,mNotification.build());
        mBroadcastManager.sendBroadcast(
                getFwUpgradeStatusIntent(mFileLength-remainingBytes,mFileLength));
    }

    static NotificationCompat.Builder buildUploadNotification(Context c){
        return new NotificationCompat.Builder(c)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setSmallIcon(R.drawable.ic_upload_license_white_24dp)
                .setContentTitle(c.getString(R.string.fwUpgrade_notificationTitle));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mNotificationManager =  (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mNotification = buildUploadNotification(this);

        if (intent != null) {
            final String action = intent.getAction();
            if (UPLOAD_FW.equals(action)) {
                final Uri file = intent.getParcelableExtra(FW_FILE_URI);
                final Node nodeTag = getNode(intent.getStringExtra(NODE_TAG));
                handleActionUpload(file, nodeTag);
            }
        }
    }

    private Node getNode(String tag){
        return Manager.getSharedInstance().getNodeWithTag(tag);
    }

    void handleActionUpload(Uri file,Node node){
        FwUpgradeConsole console =FwUpgradeConsole.getFwUpgradeConsole(node);
        if(console!=null) {
            console.setLicenseConsoleListener(this);
            mBroadcastManager.sendBroadcast(getFwUpgradeStartIntent());
            mNotificationManager.notify(NOTIFICATION_ID,mNotification.build());
            console.loadFw(FwUpgradeConsole.BOARD_FW, file);
        }
    }


    @Override
    public void onVersionRead(final FwUpgradeConsole console,
                              @FwUpgradeConsole.FirmwareType final int fwType,
                              final FwVersion version) { }
}
