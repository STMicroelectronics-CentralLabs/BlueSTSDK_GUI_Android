package com.st.BlueSTSDK.gui.licenseManager;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.ActivityWithNode;
import com.st.BlueSTSDK.gui.util.AlertAndFinishDialog;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.licenseManager.licenseConsole.LicenseConsole;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseInfo;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseManagerDBContract.LicenseEntry;
import com.st.BlueSTSDK.gui.licenseManager.storage.LicenseManagerDbHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that show the status of the license available in the node.
 */
public class LicenseManagerActivity extends ActivityWithNode implements
        LicenseStatusRecyclerViewAdapter.LicenseStatusViewCallback,
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final String FRAGMENT_DIALOG_TAG = "Dialog";

    private static final String BOARD_UID_KEY= LicenseManagerActivity.class.getCanonicalName
            ()+".BOARD_UID";
    private static final String LICENSE_STATUS_KEY= LicenseManagerActivity.class.getCanonicalName
            ()+".LICENSE_STATUS";
    private static final String LICENSE_KNOW_KEY = LicenseManagerActivity.class.getCanonicalName
            () + ".LICENSE_KNOW";

    /**
     * id used for load the cursor with the license available
     */
    private static final int LOAD_KNOW_LICENSE =0;

    /**
     * Create an intent for start this activity
     * @param c context
     * @param node node that the activity will use for detect the license
     * @param keepTheConnectionOpen true if you want that the node will remain connect when the
     *                              activity is destroyed
     * @return intent that start the activity
     */
    public static Intent getStartIntent(Context c,@NonNull Node node, boolean keepTheConnectionOpen){
        //it is safe store the tings in a static variable since is singleTop so is not recreated
        return ActivityWithNode.getStartIntent(c,LicenseManagerActivity.class,node,
                keepTheConnectionOpen);
    }

    /**
     * List view where show the license status
     */
    private RecyclerView mLicListView;

    /**
     * Object for communicate with the board through the debug console
     */
    private LicenseConsole mConsole;

    /**
     * Board id
     */
    private String mBoardUid=null;

    /**
     * list of license available in the board
     */
    private List<LicenseStatus> mLicStatus=null;

    /**
     * List of license for the current board available inside the Db (already uploaded one time)
     */
    private ArrayList<LicenseEntry> mKnowLic=null;

    /**
     * When the node is connected, start retrieving the license information
     */
    private Node.NodeStateListener mOnConnection = new Node.NodeStateListener() {
        @Override
        public void onStateChange(Node node, Node.State newState, Node.State prevState) {
            if(newState==Node.State.Connected){
                mConsole = LicenseConsole.getLicenseConsole(mNode);
                if(mBoardUid==null || mLicStatus==null)
                    loadLicenseStatus();
            }//if
        }//
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_license_manager);
        mLicListView = (RecyclerView) findViewById(R.id.licManager_listView);
        //if the fist time, load data from the node

        Node node = getNode();
        if(node==null)
            return;

        if(node.isConnected()){
            mConsole = LicenseConsole.getLicenseConsole(mNode);
            if(savedInstanceState==null)
                loadLicenseStatus();
        }else{
            node.addNodeStateListener(mOnConnection);
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle outState){
        super.onSaveInstanceState(outState);
        if(mLicStatus!=null && !mLicStatus.isEmpty())
            outState.putParcelableArrayList(LICENSE_STATUS_KEY,new ArrayList<>(mLicStatus));
        if(mKnowLic!=null && !mKnowLic.isEmpty())
            outState.putParcelableArrayList(LICENSE_KNOW_KEY,mKnowLic);
        if(mBoardUid!=null && !mBoardUid.isEmpty())
            outState.putString(BOARD_UID_KEY,mBoardUid);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mBoardUid = savedInstanceState.getString(BOARD_UID_KEY);
        mLicStatus = savedInstanceState.getParcelableArrayList(LICENSE_STATUS_KEY);
        mKnowLic = savedInstanceState.getParcelableArrayList(LICENSE_KNOW_KEY);
        //create the adapter and the console
        if(mLicStatus!=null)
            mLicListView.setAdapter(new LicenseStatusRecyclerViewAdapter
                    (mLicStatus, LicenseManagerActivity.this));
        else
            loadLicenseStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_license_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == R.id.menu_license_clearDb) {
            LicenseManagerDbHelper.getInstance(this).deleteLicenses(mBoardUid);
            Snackbar.make(mLicListView, R.string.licenseManager_clearDbMessage,
                    Snackbar.LENGTH_SHORT).show();
            return true;
        }else if (i == R.id.menu_license_clearBoard){
            clearBoardLicense();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }//onOptionsItemSelected

    /**
     * send the command for remove the board license and close this activity
     */
    private void clearBoardLicense(){
        mConsole.cleanAllLicense(mConsole.getDefaultCleanLicense(this));
    }//clearBoardLicense

    @Override
    public void onPause(){
        Node node = getNode();
        if(node!=null)
            node.removeNodeStateListener(mOnConnection);
        super.onPause();
    }

    /**
     * progress dialog used for notify what we are requesting to the board
     */
    private ProgressDialog mLoadLicenseWait;

    private static void releaseDialog(@Nullable Dialog d){
        if(d!=null && d.isShowing()) {
            d.dismiss();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseDialog(mLoadLicenseWait);
    }

    private class StoreUidAndReadLicenseStatus implements LicenseConsole.ReadBoardIdCallback{

        @Override
        public void onBoardIdRead(LicenseConsole console, String uid) {
            mBoardUid=uid;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoadLicenseWait.setMessage(getString(R.string.licenseManager_loadLicStatusDesc));
                }
            });
            console.readLicenseStatus(new UpdateLicenseStatusAdapter());
        }
    }

    private class UpdateLicenseStatusAdapter implements LicenseConsole.ReadLicenseStatusCallback{

        @Override
        public void onLicenseStatusRead(LicenseConsole console, List<LicenseStatus> licenses) {
            mLicStatus = licenses;
            if(licenses.isEmpty() || mBoardUid==null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showLicenseStatusNotAvailableDialog();
                    }
                });
                return;
            }//if

            //start load the previous license for that node
            getLoaderManager().restartLoader(LOAD_KNOW_LICENSE, null,
                    LicenseManagerActivity.this).forceLoad();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    releaseDialog(mLoadLicenseWait);
                    mLoadLicenseWait=null; // delete the object

                    mLicListView.setAdapter(new LicenseStatusRecyclerViewAdapter
                            (mLicStatus, LicenseManagerActivity.this));
                }//run
            });
        }
    }

    /**
     * show a message if the debug console is not available for this board, all the license
     * uploading procedure will not be available
     */
    private void debugConsoleNotAvailable(){
        final View viewRoot = findViewById(android.R.id.content);
        if(viewRoot!=null)
            Snackbar.make(viewRoot,R.string.licenseManager_debugNotAvailable,
                    Snackbar.LENGTH_SHORT).show();
        else
            Toast.makeText(this,R.string.licenseManager_debugNotAvailable,
                    Toast.LENGTH_SHORT).show();
    }

    /**
     * load the license status from the board
     */
    private void loadLicenseStatus() {

        //the first time we create the console
        if (mConsole == null){
            debugConsoleNotAvailable();
        }else{
            //the console is already doing something
            if (!mConsole.isWaitingAnswer()) {

                //create the progress bar
                mLoadLicenseWait = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
                mLoadLicenseWait.setTitle(R.string.licenseManager_loadDataTitle);
                mLoadLicenseWait.setMessage(getString(R.string.licenseManager_loadUidDesc));

                //start reading
                mConsole.readBoardId(new StoreUidAndReadLicenseStatus());

                //display the progress bar
                mLoadLicenseWait.show();
            }// if isWaiting
        }//if console ==null
    }//loadLicenseStatus

    /**
     * send the mail for request the license
     * @param lic license associate to that button
     */
    @Override
    public void onLicenseRequestClick(LicenseStatus lic) {
        keepConnectionOpen(true,false);
        startActivity(RequestLicenseActivity.getStartIntent(this,lic.info,mBoardUid));
    }


    private @Nullable LicenseEntry isKnowLicense(LicenseInfo lic){
        if(mKnowLic==null)
            return null;

        for(LicenseEntry entry : mKnowLic){
            if(lic.equals(entry))
                return entry;
        }
        return null;
    }

    private class ReloadLicenseStatus implements LicenseConsole.WriteLicenseCallback{

        private LicenseConsole.WriteLicenseCallback mDefault;
        private ProgressDialog mLoadLicProgress;

        public ReloadLicenseStatus(LicenseConsole.WriteLicenseCallback defaultImp){
            mDefault=defaultImp;
            mLoadLicProgress = new ProgressDialog(LicenseManagerActivity.this,
                    ProgressDialog.STYLE_SPINNER);
            mLoadLicProgress.setTitle(R.string.licenseManager_loadDataTitle);
            mLoadLicProgress.setMessage(getString(R.string.LicenseManager_uploadLicMsg));
            mLoadLicProgress.show();
        }

        @Override
        public void onLicenseLoadSuccess(LicenseConsole console, String licName, byte[] licCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadLicenseStatus();
                    releaseDialog(mLoadLicProgress);
                    mLoadLicProgress=null;
                }
            });
            mDefault.onLicenseLoadSuccess(console,licName,licCode);
        }

        @Override
        public void onLicenseLoadFail(LicenseConsole console, String licName, byte[] licCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    releaseDialog(mLoadLicProgress);
                    mLoadLicProgress=null;
                }
            });
            mDefault.onLicenseLoadFail(console, licName, licCode);
        }
    }

    private void loadKnowLicense(LicenseEntry lic){
        mConsole.writeLicenseCode(lic.getLicenseType(),lic.getLicenseCode(),
                new ReloadLicenseStatus(mConsole.getDefaultWriteLicenseCallback(this)));
    }

    @Override
    public void onLicenseUploadClick(LicenseStatus lic) {
        LicenseEntry knowLic = isKnowLicense(lic.info);
        Node node = getNode();
        if(node==null)
            return;
        if(knowLic==null) {
            keepConnectionOpen(true,false);
            startActivity(LoadLicenseActivity.getStartIntent(this, node, mBoardUid, lic.info));
        }else{
            loadKnowLicense(knowLic);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i){
            case LOAD_KNOW_LICENSE:
                return LicenseManagerDbHelper.getLicenseForBoard(this,mBoardUid);
            default:
                return null;
        }
    }//onCreateLoader

    /**
     * Return the intersection between the two list, only the license not present in the board
     * @param knowLic list of license uploaded see for this node
     * @param licStatus list of license supported by the fw
     * @return list of license that are in the DB but not in the node
     */
    private static ArrayList<LicenseEntry> findKnowLicense(List<LicenseEntry> knowLic,
                                                      List<LicenseStatus> licStatus){
        ArrayList<LicenseEntry> newKnowLic = new ArrayList<>();
        if(knowLic==null || licStatus==null)
            return newKnowLic;

        for(LicenseStatus s : licStatus){
            if(!s.isPresentOnTheBoard) // if the license is not present, search in the know list
                for(LicenseEntry e: knowLic){
                    //if we found the license
                    if(s.info.equals(e)){
                        newKnowLic.add(e);
                        break;
                    }//if
                }//for licenseEntry
        }//LicenseStatus
        return newKnowLic;
    }//findKnowLicense

    private void updateDBLicStatus(){
        for(LicenseStatus s: mLicStatus){
            s.isPresentOnDB=isKnowLicense(s.info)!=null;
        }
    }


    /**
     * called when the data are extracted from the DB
     * @param loader object used for create the cursor
     * @param cursor cursor with the query result -> list of license available in the db for the
     *               board
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        int nLicense = cursor.getCount();
        if(nLicense==0)
            return;
        //else

        //extract the license form the cursor
        List<LicenseEntry> temp = LicenseManagerDbHelper.buildLicenseEntryList(cursor);
        mKnowLic = findKnowLicense(temp, mLicStatus);
        if(!mKnowLic.isEmpty()) {
            updateDBLicStatus();
            mLicListView.getAdapter().notifyDataSetChanged();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) { }


    private void showLicenseStatusNotAvailableDialog(){
        DialogFragment newFragment = AlertAndFinishDialog.newInstance(
                getString(R.string.licenseManager_notAvailableDialogTitle),
                getString(R.string.licenseManager_notAvailableDialogMsg), true);
        newFragment.show(getFragmentManager(), FRAGMENT_DIALOG_TAG);
    }
}
