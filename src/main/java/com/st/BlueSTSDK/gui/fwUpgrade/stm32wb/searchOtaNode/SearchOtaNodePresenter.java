package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.searchOtaNode;

import com.st.BlueSTSDK.Manager;
import com.st.BlueSTSDK.Node;
import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature.STM32OTASupport;

public class SearchOtaNodePresenter implements SearchOtaNodeContract.Presenter{

    private static final int SCANNER_TIMEOUT_MS = 10*1000;


    private SearchOtaNodeContract.View mView;
    private Manager mManager;

    private Manager.ManagerListener mManagerListener = new Manager.ManagerListener() {
        @Override
        public void onDiscoveryChange(Manager m, boolean enabled) {
            if(!enabled){
                mManager.removeListener(this);
                mView.nodeNodeFound();
            }else{
                mView.startScan();
            }
        }

        @Override
        public void onNodeDiscovered(Manager m, Node node) {
            if(STM32OTASupport.isOTANode(node)){
                mManager.removeListener(this);
                mView.foundNode(node);
                mManager.stopDiscovery();
            }
        }
    };

    public SearchOtaNodePresenter(SearchOtaNodeContract.View view, Manager manager){
        mView = view;
        mManager = manager;
    }

    @Override
    public void startScan() {
        mManager.resetDiscovery();
        mManager.addListener(mManagerListener);
        mManager.startDiscovery(SCANNER_TIMEOUT_MS);
    }

    @Override
    public void stopScan() {
        mManager.stopDiscovery();
    }
}
