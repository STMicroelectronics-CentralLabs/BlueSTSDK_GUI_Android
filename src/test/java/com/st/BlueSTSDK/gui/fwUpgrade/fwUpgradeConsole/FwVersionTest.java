package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole;

import com.st.BlueSTSDK.Utils.FwVersion;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class FwVersionTest {

    private static final int MAJOR_VER =1;
    private static final int MINOR_VER =2;
    private static final int PATCH_VER =3;

    private FwVersion version;

    @Before
    public void createVersion(){
        version = new FwVersion(MAJOR_VER,MINOR_VER,PATCH_VER);
    }

    @Test
    public void testGetMajorVersion() {
        assertEquals(MAJOR_VER,version.getMajorVersion());
    }

    @Test
    public void testGetMinorVersion() {
        assertEquals(MINOR_VER,version.getMinorVersion());
    }

    @Test
    public void testGetPatchVersion() {
        assertEquals(PATCH_VER,version.getPatchVersion());
    }

    @Test
    public void testCompareToPrevVersion() {

        FwVersion prevVersion = new FwVersion(MAJOR_VER,MINOR_VER,PATCH_VER-1);
        assertTrue(version.compareTo(prevVersion)>0);

        prevVersion = new FwVersion(MAJOR_VER,MINOR_VER-1,PATCH_VER);
        assertTrue(version.compareTo(prevVersion)>0);

        prevVersion = new FwVersion(MAJOR_VER-1,MINOR_VER,PATCH_VER);
        assertTrue(version.compareTo(prevVersion)>0);

    }

    @Test
    public void testCompareToEqualVersion() {
        assertTrue(version.compareTo(version)==0);
    }

    @Test
    public void testCompareToNextVersion() {

        FwVersion prevVersion = new FwVersion(MAJOR_VER,MINOR_VER,PATCH_VER+1);
        assertTrue(version.compareTo(prevVersion)<0);

        prevVersion = new FwVersion(MAJOR_VER,MINOR_VER+1,PATCH_VER);
        assertTrue(version.compareTo(prevVersion)<0);

        prevVersion = new FwVersion(MAJOR_VER+1,MINOR_VER,PATCH_VER);
        assertTrue(version.compareTo(prevVersion)<0);

    }

    /*
    @Test
    public void storeOnParcel(){
        Parcel parcel = Parcel.obtain();

        version.writeToParcel(parcel,version.describeContents());
        parcel.setDataCapacity(0);

        FwVersion readVersion = FwVersion.CREATOR.createFromParcel(parcel);
        assertEquals(version,readVersion);
    }
    */
}