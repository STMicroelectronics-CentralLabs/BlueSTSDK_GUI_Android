package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util;

import android.content.ContentResolver;
import android.net.Uri;
import android.support.annotation.IntDef;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FwFileDescriptor {

    @IntDef({BIN, IMG,UNKNOWN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FirmwareFileType {}

    public static final int UNKNOWN = 0;
    public static final int BIN = 1;
    public static final int IMG = 2;


    public static @FirmwareFileType int getFileType(Uri file){
        String fileName = file.getLastPathSegment().toLowerCase();

        if(fileName.endsWith("bin"))
            return BIN;
        if(fileName.endsWith("img"))
            return IMG;

        return UNKNOWN;

    }

    private @FirmwareFileType int mType;
    private ContentResolver mContentResolver;
    private Uri mFile;
    private long mFileLength;

    private void setFileLength(){
        try {
            InputStream in = mContentResolver.openInputStream(mFile);
            mFileLength=getFileLength(in);
        } catch (FileNotFoundException e) {
            mFileLength=0;
        }
    }

    public FwFileDescriptor(ContentResolver resolver, Uri file) {
        mType = getFileType(file);
        mContentResolver=resolver;
        mFile=file;
        setFileLength();
    }

    public @FirmwareFileType int getType() {
        return mType;
    }

    public long getLength(){
        return mFileLength;
    }

    public InputStream openFile() throws FileNotFoundException {
        InputStream in = mContentResolver.openInputStream(mFile);
        if(mType==IMG)
            return  new ImgFileInputStream(in,mFileLength);
        return in;
    }

    private static long getFileLength(InputStream input) {
        long nBytes = 0;
        try {
            while(input.read()>=0){
                nBytes++;
            }//while
        } catch (IOException e) {
            nBytes=0;
        }//try-catch

        return nBytes;
    }//getFileLength

}
