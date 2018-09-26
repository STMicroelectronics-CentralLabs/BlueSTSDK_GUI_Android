package com.st.BlueSTSDK.gui;

import android.support.annotation.DrawableRes;

import com.st.BlueSTSDK.Node;

public class NodeGui {

    public static @DrawableRes
    int getBoardTypeImage(Node.Type type){
        switch (type){
            case STEVAL_WESU1:
                return R.drawable.board_steval_wesu1;
            case SENSOR_TILE:
                return R.drawable.board_sensor_tile;
            case BLUE_COIN:
                return R.drawable.board_bluecoin;
            case STEVAL_IDB008VX:
                return R.drawable.board_bluenrg;
            case STEVAL_BCN002V1:
                return R.drawable.board_bluenrg;
            case NUCLEO:
                return R.drawable.board_nucleo;
            case GENERIC:
            default:
                return R.drawable.board_generic;
        }
    }

}
