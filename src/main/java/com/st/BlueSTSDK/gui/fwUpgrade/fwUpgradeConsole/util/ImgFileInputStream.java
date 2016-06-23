package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Scanner;

/**
 * Utility class that read a img file converting the char data into byte.
 * the file is read as block of uint32(8char) and it will invert the byte order
 * it the file as the string "01020304" a sequence of read will return the bytes: 0x04,0x03,0x02,
 * 0x01
 */
public class ImgFileInputStream extends InputStream {

    /**
     * utility class used for read a line from the file
     */
    private Scanner mScanner;

    /**
     * stack used for keep the line content
     */
    private ArrayDeque<Integer> mBuffer= new ArrayDeque<>(4);

    private long mFileSize;

    public  ImgFileInputStream(InputStream input, long streamByteLength){
        long nLine = streamByteLength/10;
        mFileSize=(streamByteLength-2*nLine)/2;
        mScanner=new Scanner(input);
    }

    /**
     * fill the buffer stack with the line
     * @return true if the read is ok, false if it reach the EOF
     */
    private boolean readLine(){
        if(!mScanner.hasNextLine())
            return false;
        //else
        String line = mScanner.nextLine();
        for(int i=0;i<4;i++){
            String value = line.substring(2*i,2*i+2);
            mBuffer.add(Integer.parseInt(value,16));
        }
        return true;
    }

    @Override
    public int read() throws IOException {
        if(mBuffer.isEmpty())
            if(!readLine()) // if the read fail, the file ended
                return -1;
        return mBuffer.removeLast();
    }

    @Override
    public boolean markSupported(){
        return false;
    }

    public long length(){
       return mFileSize;
    }
}
