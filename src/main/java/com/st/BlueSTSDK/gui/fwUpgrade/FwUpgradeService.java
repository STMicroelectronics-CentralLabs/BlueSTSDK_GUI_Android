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
package com.st.BlueSTSDK.gui.fwUpgrade;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.Utils.FwVersion;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwUpgradeConsole;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util.FwFileDescriptor;

/**
 * Service that will upload the file as a background task, it will notify to the user the progres
 * using a LocalBroadcast message.
 */
public class FwUpgradeService extends IntentService implements FwUpgradeConsole.FwUpgradeCallback {

    /**
     * action send when the service start the uploading
     */
    public static final String FW_UPLOAD_STARTED_ACTION = FwUpgradeService.class
            .getCanonicalName() + "action.FW_UPLOAD_STARTED_ACTION";

    /**
     * action send when the service notify a progress in the uploading
     */
    public static final String FW_UPLOAD_STATUS_UPGRADE_ACTION = FwUpgradeService.class
            .getCanonicalName() + "action.FW_UPLOAD_STATUS_UPGRADE";
    /**
     * key used in the upload status intent for store the file size
     */
    public static final String FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE_EXTRA = FwUpgradeService.class
            .getCanonicalName() + "extra.FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE";
    /**
     * key used in the upload status intent for store the number of bytes stored
     */
    public static final String FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE_EXTRA = FwUpgradeService.class
            .getCanonicalName() + "extra.FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE";

    /**
     * action send when the upload end correctly
     */
    public static final String FW_UPLOAD_FINISHED_ACTION = FwUpgradeService.class
            .getCanonicalName() + "action.FW_UPLOAD_FINISHED_ACTION";
    /**
     * key used in the upload finished intent for store the seconds needed for upload the file
     */
    public static final String FW_UPLOAD_FINISHED_TIME_S_EXTRA = FwUpgradeService.class
            .getCanonicalName() + "extra.FW_UPLOAD_FINISHED_TIME_S";

    /**
     * action send when an error happen
     */
    public static final String FW_UPLOAD_ERROR_ACTION = FwUpgradeService.class
            .getCanonicalName() + "action.FW_UPLOAD_ERROR_ACTION";
    /**
     * key used in the error intent for store the error message
     */
    public static final String FW_UPLOAD_ERROR_MESSAGE_EXTRA = FwUpgradeService.class
            .getCanonicalName() + "extra.FW_UPLOAD_ERROR_MESSAGE";

    /**
     * id used for display the notification from this service
     */
    private static final int NOTIFICATION_ID = FwUpgradeService.class.hashCode();

    /**
     * action used in the intent for create this service
     */
    private static final String UPLOAD_FW =
            FwUpgradeService.class.getCanonicalName() + "action.uploadFw";
    /**
     * key used in the intent for create this service, for store the file to upload
     */
    private static final String FW_FILE_URI =
            FwUpgradeService.class.getCanonicalName() + "extra.fwUri";

    /**
     * key used in the intent for create this service, for store the node where upload the file
     */
    private static final String NODE_TAG =
            FwUpgradeService.class.getCanonicalName() + "extra.nodeTag";

    /**
     * object used for send the broadcast message
     */
    private LocalBroadcastManager mBroadcastManager;

    /**
     * object used for publish the upload notification
     */
    private NotificationManager mNotificationManager;

    /**
     * object for create the notification to display
     */
    private NotificationCompat.Builder mNotification;

    /**
     * timestamp when the upload start
     */
    private long mStartUploadTime = -1;
    /**
     * size of the file that the service will upload
     */
    private long mFileLength = Long.MAX_VALUE;

    public FwUpgradeService() {
        super(FwUpgradeService.class.getSimpleName());
    }

    /**
     * convert an error code into a string
     * @param errorCode code to convert
     * @return string to display when the error happen
     */
    private String getErrorMessage(@UpgradeErrorType int errorCode) {
        switch (errorCode) {
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_CORRUPTED_FILE:
                return getString(R.string.fwUpgrade_error_corrupted_file);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_TRANSMISSION:
                return getString(R.string.fwUpgrade_error_transmission);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_UNKNOWN:
                return getString(R.string.fwUpgrade_error_unknown);
            case FwUpgradeConsole.FwUpgradeCallback.ERROR_INVALID_FW_FILE:
                return getString(R.string.fwUpgrade_error_invalid_file);
        }
        return "";
    }

    /**
     * start this service
     * @param context context used for start the service
     * @param node node where upload the file
     * @param fwFile file to upload
     */
    public static void startUploadService(Context context, Node node, Uri fwFile) {
        Intent intent = new Intent(context, FwUpgradeService.class);
        intent.setAction(UPLOAD_FW);
        intent.putExtra(FW_FILE_URI, fwFile);
        intent.putExtra(NODE_TAG, node.getTag());
        context.startService(intent);
    }

    /**
     * create an intent filter that will select all the action send by this service
     * @return IntentFilter for select all the intent send by this service
     */
    public static IntentFilter getServiceActionFilter() {
        IntentFilter filter = new IntentFilter(FW_UPLOAD_FINISHED_ACTION);
        filter.addAction(FW_UPLOAD_STATUS_UPGRADE_ACTION);
        filter.addAction(FW_UPLOAD_STARTED_ACTION);
        filter.addAction(FW_UPLOAD_ERROR_ACTION);
        return filter;
    }

    /**
     * create an action intent for notify that the upload is finished
     * @param durationS time spent for upload the file
     * @return intent for notify that the upload finished
     */
    private static Intent getFwUpgradeCompleteIntent(float durationS) {
        return new Intent(FW_UPLOAD_FINISHED_ACTION)
                .putExtra(FW_UPLOAD_FINISHED_TIME_S_EXTRA, durationS);
    }

    private static Intent getFwUpgradeErrorIntent(String errorMessage) {
        return new Intent(FW_UPLOAD_ERROR_ACTION)
                .putExtra(FW_UPLOAD_ERROR_MESSAGE_EXTRA, errorMessage);
    }

    private static Intent getFwUpgradeStatusIntent(long uploadBytes, long totalBytes) {
        return new Intent(FW_UPLOAD_STATUS_UPGRADE_ACTION)
                .putExtra(FW_UPLOAD_STATUS_UPGRADE_TOTAL_BYTE_EXTRA, totalBytes)
                .putExtra(FW_UPLOAD_STATUS_UPGRADE_SEND_BYTE_EXTRA, uploadBytes);
    }

    private static Intent getFwUpgradeStartIntent() {
        return new Intent(FW_UPLOAD_STARTED_ACTION);
    }

    /**
     * crate the notification for display the upload status
     * @return object that will build the notification
     */
    private NotificationCompat.Builder buildUploadNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)

                .setContentTitle(getString(R.string.fwUpgrade_notificationTitle));

        if(Build.VERSION.SDK_INT>=21){
            builder.setSmallIcon(R.drawable.ic_upload_license_white_24dp);
        }else{
            builder.setSmallIcon(android.R.drawable.ic_menu_upload);
        }
        return builder;
    }

    @Override
    public void onLoadFwError(FwUpgradeConsole console, FwFileDescriptor fwFile,
                              @UpgradeErrorType int error) {
        String errorMessage = getErrorMessage(error);
        mBroadcastManager.sendBroadcast(getFwUpgradeErrorIntent(errorMessage));
        mNotification.setContentTitle(getString(R.string.fwUpgrade_errorNotificationTitle))
                .setContentText(errorMessage);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
    }

    @Override
    public void onLoadFwComplete(FwUpgradeConsole console, FwFileDescriptor fwFile) {
        long totalTimeMs = System.currentTimeMillis() - mStartUploadTime;
        float totalTimeS = totalTimeMs / 1000.0f;
        mBroadcastManager.sendBroadcast(getFwUpgradeCompleteIntent(totalTimeS));
        mNotification.setContentTitle(getString(R.string.fwUpgrade_upgradeCompleteNotificationTitle))
                .setContentText(getString(R.string.fwUpgrade_upgradeCompleteNotificationContent));
        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
    }

    @Override
    public void onLoadFwProgressUpdate(FwUpgradeConsole console, FwFileDescriptor fwFile, final long remainingBytes) {
        if (mStartUploadTime < 0) { //if is the first time
            mStartUploadTime = System.currentTimeMillis();
            mFileLength = remainingBytes;
        }
        mNotification.setProgress((int) mFileLength, (int) (mFileLength - remainingBytes), false);
        mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
        mBroadcastManager.sendBroadcast(
                getFwUpgradeStatusIntent(mFileLength - remainingBytes, mFileLength));
    }

    /**
     * check that the intent is correct and start the service
     * @param intent inent for start the service
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (UPLOAD_FW.equals(action)) {
                final Uri file = intent.getParcelableExtra(FW_FILE_URI);
                final Node node = getNode(intent.getStringExtra(NODE_TAG));
                handleActionUpload(file, node);
            }
        }
    }

    /**
     * extract the node from the manager
     * @param tag node unique name
     * @return tat with that tag
     */
    private Node getNode(String tag) {
        return Manager.getSharedInstance().getNodeWithTag(tag);
    }

    /**
     * Service constructor
     * @param file file to upload
     * @param node node where upload the file
     */
    void handleActionUpload(Uri file, Node node) {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        mNotification = buildUploadNotification();
        FwUpgradeConsole console = FwUpgradeConsole.getFwUpgradeConsole(node);
        if (console != null) {
            console.setLicenseConsoleListener(this);
            mBroadcastManager.sendBroadcast(getFwUpgradeStartIntent());
            mNotificationManager.notify(NOTIFICATION_ID, mNotification.build());
            console.loadFw(FwUpgradeConsole.BOARD_FW, new FwFileDescriptor(getContentResolver(),
                    file));
        }//if console
    }


    @Override
    public void onVersionRead(final FwUpgradeConsole console,
                              @FwUpgradeConsole.FirmwareType final int fwType,
                              final FwVersion version) {
        //not used
    }
}
