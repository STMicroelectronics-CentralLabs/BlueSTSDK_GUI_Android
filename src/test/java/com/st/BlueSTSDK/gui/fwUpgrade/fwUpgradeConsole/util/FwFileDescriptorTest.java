package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util;

import android.content.ContentResolver;
import android.net.Uri;

import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FwFileDescriptorTest {

    private static Uri createFileWithExtension(String extension){
        return new Uri.Builder()
                .appendPath("file."+extension)
                .build();
    };

    @Test
    public void binExtensionIsBinFileType(){
        Uri file = createFileWithExtension("bin");
        assertEquals(FwFileDescriptor.BIN, FwFileDescriptor.getFileType(file));
        file = createFileWithExtension("BIN");
        assertEquals(FwFileDescriptor.BIN, FwFileDescriptor.getFileType(file));
    }

    @Test
    public void imgExtensionIsImgFileType(){
        Uri file = createFileWithExtension("img");
        assertEquals(FwFileDescriptor.IMG, FwFileDescriptor.getFileType(file));
        file = createFileWithExtension("IMG");
        assertEquals(FwFileDescriptor.IMG, FwFileDescriptor.getFileType(file));
    }

    @Test
    public void otherExtensionIsUnknownFileType(){
        Uri file = createFileWithExtension("txt");
        assertEquals(FwFileDescriptor.UNKNOWN, FwFileDescriptor.getFileType(file));
        file = createFileWithExtension("TXT");
        assertEquals(FwFileDescriptor.UNKNOWN, FwFileDescriptor.getFileType(file));
    }


    @Test
    @Ignore
    public void readFileLengthOnSetup() throws FileNotFoundException {
        Uri file = createFileWithExtension("bin");
        ContentResolver cr = mock(ContentResolver.class);
        final int FILE_LENGTH=10;
        InputStream simpleStream = new ByteArrayInputStream(new byte[FILE_LENGTH]);
        when(cr.openInputStream(eq(file))).thenReturn(simpleStream);

        //doReturn(simpleStream).when(cr).openInputStream(file);

        FwFileDescriptor mDescriptor = new FwFileDescriptor(cr,file);
        assertEquals(FILE_LENGTH,mDescriptor.getLength());

    }


}