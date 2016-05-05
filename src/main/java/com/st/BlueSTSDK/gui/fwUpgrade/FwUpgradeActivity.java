package com.st.BlueSTSDK.gui.fwUpgrade;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.ActivityWithNode;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwUpgradeConsole;
import com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.FwVersion;

public class FwUpgradeActivity extends ActivityWithNode {

    private static final String FW_BOARD_FILE=FwUpgradeActivity.class.getCanonicalName()+
            ".FwBoardFile";
    private static final String FW_BLE_FILE=FwUpgradeActivity.class.getCanonicalName()+".FwBleFile";

    private static final int CHOOSE_BOARD_FILE_REQUESTCODE=1;
    private static final int CHOOSE_BLE_FILE_REQUESTCODE=2;


    public static Intent getStartIntent(Context c, Node node, boolean keepTheConnectionOpen) {
        return ActivityWithNode.getStartIntent(c,FwUpgradeActivity.class,node,
                keepTheConnectionOpen);
    }



    private Uri mFwBleFile;
    private TextView mVersionBleText;
    private TextView mFwFileBleText;

    private Uri mFwBoardFile;
    private TextView mVersionBoardText;
    private TextView mFwFileBoardText;

    private TextView mLoadStatus;
    private Node.NodeStateListener mOnConnected = new Node.NodeStateListener() {
        @Override
        public void onStateChange(Node node, Node.State newState, Node.State prevState) {
            if(newState==Node.State.Connected){
                initFwVersion();
            }
        }
    };

    private FwUpgradeConsole mConsole;
    private FwUpgradeConsole.FwUpgradeCallback mConsoleListener = new FwUpgradeConsole.FwUpgradeCallback() {
        @Override
        public void onVersionRead(final FwUpgradeConsole console,
                                  @FwUpgradeConsole.FirmwareType final int fwType,
                                  final FwVersion version) {
            FwUpgradeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(fwType==FwUpgradeConsole.BLE_FW){
                        mVersionBleText.setText(version.toString());
                        console.readVersion(FwUpgradeConsole.BOARD_FW);
                    }else{
                        mVersionBoardText.setText(version.toString());
                    }

                }
            });
        }

        @Override
        public void onLoadFwComplete(FwUpgradeConsole console, Uri fwFile, boolean status) {
            if(status)
                Toast.makeText(FwUpgradeActivity.this,"Update done",Toast.LENGTH_LONG).show();
            else
                Toast.makeText(FwUpgradeActivity.this,"Update FAIL",Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLoadFwProgresUpdate(FwUpgradeConsole console, Uri fwFile, final long loadBytes) {
            FwUpgradeActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoadStatus.setText("load: "+loadBytes);
                }
            });
        }
    };

    private  void initFwVersion(){
        mConsole = FwUpgradeConsole.getFwUpgradeConsole(mNode);
        if(mConsole!=null) {
            mConsole.setLicenseConsoleListener(mConsoleListener);
            mConsole.readVersion(FwUpgradeConsole.BLE_FW);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fw_upgrade);

        mVersionBleText = (TextView) findViewById(R.id.versionBleText);
        mFwFileBleText = (TextView) findViewById(R.id.fwBleFileText);

        mVersionBoardText = (TextView) findViewById(R.id.versionBoardText);
        mFwFileBoardText = (TextView) findViewById(R.id.fwBoardFileText);

        mLoadStatus  = (TextView) findViewById(R.id.loadStatusText);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.startUpgradeButton);
        if( fab!=null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startFwUpgrade();
                }
            });
        }

        if(mNode.isConnected()){
            initFwVersion();
        }else{
            mNode.addNodeStateListener(mOnConnected);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mNode.removeNodeStateListener(mOnConnected);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mFwBleFile = savedInstanceState.getParcelable(FW_BLE_FILE);
        mFwBoardFile = savedInstanceState.getParcelable(FW_BOARD_FILE);
        if(mFwBleFile!=null)
            mFwFileBleText.setText(mFwBleFile.getPath());
        if(mFwBleFile!=null)
            mFwFileBoardText.setText(mFwBoardFile.getPath());
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putParcelable(FW_BLE_FILE,mFwBleFile);
        outState.putParcelable(FW_BOARD_FILE,mFwBoardFile);
        super.onSaveInstanceState(outState, outPersistentState);
    }


    private Intent getFileSelectInttent(){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
        intent.setType("*/*");
        return intent;
        //Intent i = Intent.createChooser(intent, "Open firmwere file");
    }

    public void onSelectBleFileClick(View view) {
        keepConnectionOpen(true);
        startActivityForResult(getFileSelectInttent(), CHOOSE_BLE_FILE_REQUESTCODE);
    }

    public void onSelectFileBoardClick(View view) {
        keepConnectionOpen(true);
        startActivityForResult(getFileSelectInttent(), CHOOSE_BOARD_FILE_REQUESTCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if(requestCode==CHOOSE_BLE_FILE_REQUESTCODE){
                mFwBleFile=data.getData();
                mFwFileBleText.setText(mFwBleFile.getPath());
            }
            if(requestCode==CHOOSE_BOARD_FILE_REQUESTCODE) {
                mFwBoardFile=data.getData();
                mFwFileBoardText.setText(mFwBoardFile.getPath());
            }
        }

    }

    private void startFwUpgrade(){
        if(mConsole!=null){
            if(mFwBoardFile!=null) {
                mConsole.loadFw(FwUpgradeConsole.BOARD_FW, mFwBoardFile);
            }else if(mFwBleFile!=null)
                mConsole.loadFw(FwUpgradeConsole.BLE_FW, mFwBleFile);
        }
    }



}

