package com.st.STM32WB.p2pDemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import com.st.BlueSTSDK.gui.R;


import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.demos.DemoFragment;

/**
 * Demo fragment that will periodically (every 2 seconds) request and display the rssi value
 */
public abstract class RssiDemoFragment extends DemoFragment implements Node.BleConnectionParamUpdateListener {

    /**
     * rssi update period, in milliseconds
     */
    private static final long RSSI_UPDATE_PERIOD_MS=2000;

    /**
     * thread where post the rssi update
     */
    private Handler mUpdateRssiRequestQueue;

    /**
     * text view where display the rssi value
     */
    private TextView mRssiText;

    /**
     * task to request the rssi update
     */
    private Runnable mAskNewRssi = new Runnable() {
        @Override
        public void run() {
            Node n = getNode();
            if(n!=null){
                n.readRssi();
                mUpdateRssiRequestQueue.postDelayed(mAskNewRssi,RSSI_UPDATE_PERIOD_MS);
            }
        }//run
    };

    @Override
    public void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        HandlerThread looper = new HandlerThread(RssiDemoFragment.class.getName()+".UpdateRssi");
        looper.start();
        mUpdateRssiRequestQueue = new Handler(looper.getLooper());
    }

    /**
     * get the text view id where upgrade the rssi value
     * @return view id of a text view
     */
    protected abstract @IdRes int getRssiLabelId();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRssiText = view.findViewById(getRssiLabelId());
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        node.addBleConnectionParamListener(this);
        mUpdateRssiRequestQueue.post(mAskNewRssi);
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        node.removeBleConnectionParamListener(this);
        mUpdateRssiRequestQueue.removeCallbacks(mAskNewRssi);

    }

    @Override
    public void onRSSIChanged(Node node, final int newRSSIValue) {
        updateGui(() -> mRssiText.setText(getString(R.string.stm32wb_rssiFormat,newRSSIValue)));
    }//onRSSIChanged
}
