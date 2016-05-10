package com.st.BlueSTSDK.gui.fwUpgrade.fwUpgradeConsole.util;

import com.st.BlueSTSDK.Utils.NumberConversion;

import java.util.zip.Checksum;


public class STM32Crc32 implements Checksum {

    private static final int INITIAL_VALUE =0xffffffff;
    private static final int CRC_TABLE[] = { // Nibble lookup table for 0x04C11DB7 polynomial
            0x00000000,0x04C11DB7,0x09823B6E,0x0D4326D9,0x130476DC,0x17C56B6B,0x1A864DB2,0x1E475005,
            0x2608EDB8,0x22C9F00F,0x2F8AD6D6,0x2B4BCB61,0x350C9B64,0x31CD86D3,0x3C8EA00A,0x384FBDBD };

    private static int Crc32Fast(int Crc, int Data)
    {
        Crc = Crc ^ Data; // Apply all 32-bits

        // Process 32-bits, 4 at a time, or 8 rounds

        Crc = (Crc << 4) ^ CRC_TABLE[Crc >>> 28]; // Assumes 32-bit reg, masking index to 4-bits
        Crc = (Crc << 4) ^ CRC_TABLE[Crc >>> 28]; //  0x04C11DB7 Polynomial used in STM32
        Crc = (Crc << 4) ^ CRC_TABLE[Crc >>> 28];
        Crc = (Crc << 4) ^ CRC_TABLE[Crc >>> 28];
        Crc = (Crc << 4) ^ CRC_TABLE[Crc >>> 28];
        Crc = (Crc << 4) ^ CRC_TABLE[Crc >>> 28];
        Crc = (Crc << 4) ^ CRC_TABLE[Crc >>> 28];
        Crc = (Crc << 4) ^ CRC_TABLE[Crc >>> 28];

        return(Crc);
    }


    private int mCurrentCrc  = INITIAL_VALUE;

    @Override
    public long getValue() {
        return mCurrentCrc;
    }

    @Override
    public void reset() {
        mCurrentCrc=INITIAL_VALUE;
    }


    @Override
    public void update(byte[] bytes, int offset, int length) {
        if(length%4!=0)
            throw new IllegalArgumentException("length must be multiple of 4");
        //else
        for(int i=0;i<length;i+=4){
            int val =NumberConversion.LittleEndian.bytesToInt32(bytes,offset+i*4);
            mCurrentCrc=Crc32Fast(mCurrentCrc,val);
        }//for
    }

    @Override
    public void update(int i) {
        update(NumberConversion.BigEndian.uint32ToBytes(i),0,4);
    }
}
