package com.st.STM32WB.p2pDemo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.st.BlueSTSDK.Feature;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.NodeConnectionService;
import com.st.BlueSTSDK.gui.R;
import com.st.BlueSTSDK.gui.demos.DemoDescriptionAnnotation;
import com.st.STM32WB.p2pDemo.feature.FeatureControlLed;
import com.st.STM32WB.p2pDemo.feature.FeatureSwitchStatus;
import com.st.STM32WB.p2pDemo.feature.FeatureThreadReboot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@DemoDescriptionAnnotation(name="Led Control",
        requareAll = {FeatureSwitchStatus.class,FeatureControlLed.class})
public class LedButtonControlFragment extends RssiDemoFragment {


    private static final String DEVICE_ID_KEY = LedButtonControlFragment.class.getName()+".DEVICE_ID_KEY";
    private static final String LED_STATUS_KEY = LedButtonControlFragment.class.getName()+".LED_STATUS_KEY";
    private static final String ALARM_STATUS_KEY = LedButtonControlFragment.class.getName()+".ALARM_STATUS_KEY";
    private static final String ALARM_TEXT_KEY = LedButtonControlFragment.class.getName()+".ALARM_TEXT_KEY";
    private static SimpleDateFormat ALLARM_FORMATTER = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

    private TextView mDeviceName;
    private TextView mInstructionText;
    private ImageView mLedImage;

    private boolean mLedStatus=false;

    private Group mAlarmViewGroup;
    private Group mLedViewGroup;
    private TextView mAlarmText;

    private FeatureSwitchStatus mButtonFeature;
    private FeatureControlLed mLedControlFeature;

    private Peer2PeerDemoConfiguration.DeviceID mCurrentDevice;

    /**
     * this function is needed due to a bug/missing feature in the constraint layout group,
     * that doesn't handle correctly the invisible state
     * https://issuetracker.google.com/issues/80085885
     * https://issuetracker.google.com/issues/70883899
     * @param view viewgroup that has to change the visibility
     * @param visibility new visibility
     */
    private static void changeGroupVisibility(Group view, int visibility){
        view.setVisibility(visibility);
        view.requestLayout();
    }

    //we cant initialize the listener here because we need to wait that the fragment is attached
    // to an activity
    private Feature.FeatureListener mButtonListener = new  Feature.FeatureListener () {

        @Override
        public void onUpdate(Feature f, Feature.Sample sample) {
            if(mCurrentDevice==null){ //first time
                mCurrentDevice = FeatureSwitchStatus.getDeviceSelection(sample);
                updateGui(()-> showDeviceDetected(mCurrentDevice));
            }

            if(FeatureSwitchStatus.isSwitchOn(sample)){
                final String eventDate = ALLARM_FORMATTER.format(new Date(sample.notificationTime));
                updateGui(() -> {
                    mAlarmText.setText(getString(R.string.stm32wb_single_eventFormat,eventDate));
                    changeGroupVisibility(mAlarmViewGroup,View.VISIBLE);
                });
            }else{
                updateGui(() ->changeGroupVisibility(mAlarmViewGroup,View.INVISIBLE));
            }
        }//on update
    };

    public LedButtonControlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getRssiLabelId() {
        return R.id.stm32wb_single_rssiText;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_stm32wb_led_single_control, container, false);

        mLedImage = root.findViewById(R.id.stm32wb_single_ledImage);

        mLedImage.setOnClickListener(v -> {
            if(mCurrentDevice!=null && mLedControlFeature !=null){
                mLedStatus = !mLedStatus;
                changeLedStatusImage(mLedStatus);
                changeRemoteLedStatus(mLedStatus);
            }
        });
        mInstructionText = root.findViewById(R.id.stm32wb_single_instruction);
        mLedViewGroup = root.findViewById(R.id.stm32wb_single_ledView);
        mAlarmViewGroup = root.findViewById(R.id.stm32wb_single_alarmView);
        mAlarmText = root.findViewById(R.id.stm32wb_single_alarmText);
        mDeviceName = root.findViewById(R.id.stm32wb_single_titleText);

        if(savedInstanceState!=null &&
                savedInstanceState.containsKey(DEVICE_ID_KEY)){
            Peer2PeerDemoConfiguration.DeviceID device = (Peer2PeerDemoConfiguration.DeviceID)
                    savedInstanceState.getSerializable(DEVICE_ID_KEY);
            if(device!=null)
                showDeviceDetected(device);

            mLedStatus = savedInstanceState.getBoolean(LED_STATUS_KEY,false);
            changeLedStatusImage(mLedStatus);

            changeGroupVisibility(mAlarmViewGroup,savedInstanceState.getInt(ALARM_STATUS_KEY,View.INVISIBLE));
            mAlarmText.setText(savedInstanceState.getString(ALARM_TEXT_KEY));
        }

       return root;
    }

    private void changeRemoteLedStatus(boolean newState){
        if(mLedControlFeature ==null)
            return;
        if(newState){
            mLedControlFeature.switchOnLed(mCurrentDevice);
        }else{
            mLedControlFeature.switchOffLed(mCurrentDevice);
        }
    }

    private void changeLedStatusImage(boolean newState){
        if(newState){
            mLedImage.setImageResource(R.drawable.stm32wb_led_on);
        }else{
            mLedImage.setImageResource(R.drawable.stm32wb_led_off);
        }
    }

    private final int ENABLE_REBOOT_THREAD_ADVERTISE_MASK = 0x00004000;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Node node = getNode();
        if(node!=null && ((node.getAdvertiseBitMask() & ENABLE_REBOOT_THREAD_ADVERTISE_MASK) != 0)){
            inflater.inflate(R.menu.stm32wb_thread_reboot,menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.stm32wb_single_switchRadio){
            switchToThreadRadio();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchToThreadRadio() {
        Node node = getNode();
        if(node!=null) {
            FeatureThreadReboot reboot = node.getFeature(FeatureThreadReboot.class);
            if(reboot!=null) {
                reboot.rebootToThreadRadio(mCurrentDevice, () -> {
                    //disconnect and close the demo
                    NodeConnectionService.disconnect(requireContext(), node);
                    NavUtils.navigateUpFromSameTask(requireActivity());
                });
            }
        }
    }

    private void showDeviceDetected(@NonNull Peer2PeerDemoConfiguration.DeviceID currentDevice){
        mCurrentDevice = currentDevice;
        mDeviceName.setText(getString(R.string.stm32wb_deviceNameFormat,currentDevice.getId()));
        //TODO add animation?
        mLedViewGroup.setVisibility(View.VISIBLE);
        mInstructionText.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mCurrentDevice!=null){
            outState.putSerializable(DEVICE_ID_KEY,mCurrentDevice);
            outState.putBoolean(LED_STATUS_KEY,mLedStatus);
            outState.putInt(ALARM_STATUS_KEY,mAlarmViewGroup.getVisibility());
            outState.putString(ALARM_TEXT_KEY,mAlarmText.getText().toString());
        }
    }

    @Override
    protected void enableNeededNotification(@NonNull Node node) {
        super.enableNeededNotification(node);
        mButtonFeature = node.getFeature( FeatureSwitchStatus.class);
        mLedControlFeature = node.getFeature(FeatureControlLed.class);

        mButtonFeature = node.getFeature(FeatureSwitchStatus.class);
        if (mButtonFeature != null) {
            mButtonFeature.addFeatureListener(mButtonListener);
            node.enableNotification(mButtonFeature);
            node.readFeature(mButtonFeature);
        }

        mCurrentDevice = Peer2PeerDemoConfiguration.DeviceID.fromBoardId(node.getTypeId());
        if(mCurrentDevice!=null){
            showDeviceDetected(mCurrentDevice);
        }
    }

    @Override
    protected void disableNeedNotification(@NonNull Node node) {
        super.disableNeedNotification(node);
        if(mButtonFeature!=null){
            mButtonFeature.removeFeatureListener(mButtonListener);
            node.disableNotification(mButtonFeature);
        }
    }

    @Override
    public void onTxPowerChange(Node node, int newPower) {}
}

