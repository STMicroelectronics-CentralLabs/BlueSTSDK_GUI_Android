package com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.statOtaConfig;

import com.st.BlueSTSDK.gui.fwUpgrade.stm32wb.feature.RebootOTAModeFeature;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StartOtaRebootPresenterTest {

    private static final short SECTOR_TO_DELETE = 0XFF;
    private static final short NUM_SECTOR_TO_DELETE = 0XFE;

    @Mock
    private StartOtaConfigContract.View mView;
    @Mock
    private RebootOTAModeFeature mFeature;

    private StartOtaRebootPresenter mPresenter;

    @Before
    public void setUp(){

        when(mView.getSectorToDelete()).thenReturn(SECTOR_TO_DELETE);
        when(mView.getNSectorToDelete()).thenReturn(NUM_SECTOR_TO_DELETE);

        mPresenter = new StartOtaRebootPresenter(mView,mFeature);
    }

    @Test
    public void whenRebootPressedADialogIsShown(){
        mPresenter.onRebootPressed();
        verify(mView).showConnectionResetWarningDialog();
    }


    @Test
    public void whenDialogIsDismissTheSectorAreRetrived(){
        mPresenter.onConnectionResetWarningDismiss();
        verify(mView).getNSectorToDelete();
        verify(mView).getSectorToDelete();
    }

    @Test
    public void whenDialogIsDismissTheRebootIsCalled(){
        mPresenter.onConnectionResetWarningDismiss();
        verify(mFeature).rebootToFlash(SECTOR_TO_DELETE,NUM_SECTOR_TO_DELETE);
    }

}