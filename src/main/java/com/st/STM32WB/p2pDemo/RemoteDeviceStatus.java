package com.st.STM32WB.p2pDemo;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Class containing the remote device status
 */
class RemoteDeviceStatus implements Parcelable{

    /**
     * name of the remote device
     */
    Peer2PeerDemoConfiguration.DeviceID id;

    /**
     * true if the light is on, false otherwise, default value is false
     */
    boolean ledStatus;

    /**
     * true if the button was pressed, false otherwise, default value is false
     */
    boolean buttonStatus;

    RemoteDeviceStatus(@NonNull Peer2PeerDemoConfiguration.DeviceID id) {
        this.id = id;
        ledStatus=false;
        buttonStatus=false;
    }

    RemoteDeviceStatus(RemoteDeviceStatus status){
        id = status.id;
        ledStatus = status.ledStatus;
        buttonStatus = status.buttonStatus;
    }

    private RemoteDeviceStatus(Parcel in) {
        id = (Peer2PeerDemoConfiguration.DeviceID) in.readSerializable();
        ledStatus = in.readByte() != 0;
        buttonStatus = in.readByte() != 0;
    }

    public static final Creator<RemoteDeviceStatus> CREATOR = new Creator<RemoteDeviceStatus>() {
        @Override
        public RemoteDeviceStatus createFromParcel(Parcel in) {
            return new RemoteDeviceStatus(in);
        }

        @Override
        public RemoteDeviceStatus[] newArray(int size) {
            return new RemoteDeviceStatus[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RemoteDeviceStatus status = (RemoteDeviceStatus) o;

        return ledStatus == status.ledStatus &&
                buttonStatus == status.buttonStatus &&
                id == status.id;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (ledStatus ? 1 : 0);
        result = 31 * result + (buttonStatus ? 1 : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(id);
        dest.writeByte((byte) (ledStatus ? 1 : 0));
        dest.writeByte((byte) (buttonStatus ? 1 : 0));
    }
}