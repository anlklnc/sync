package com.nutomic.syncthingandroid.kife;

import android.content.Context;
import android.util.Log;

import com.nutomic.syncthingandroid.model.Device;
import com.nutomic.syncthingandroid.util.ConfigXml;

import java.util.ArrayList;

/**
 * Created by asd on 2.6.2017.
 */

public class Kife {

    Context context;
    ConfigXml xml;

    public Kife(Context context, ConfigXml configXml) {
        this.context = context;
        xml = configXml;

        sendDevice();
        getDeviceList();
    }

    private void sendDevice() {
        Device device = new Device();
        device.deviceId = xml.getDeviceID();
        device.name = xml.getDeviceName();

        Network.getInstance().sendDevice(new NetworkListener() {
            @Override
            public void onResponse(Object o) {
                Log.i("!!!", "send device success");
                getDeviceList();
            }

            @Override
            public void onError(int errorCode) {
                Log.i("!!!", "send device failed");
            }
        }, device);
    }

    private void getDeviceList() {
        Network.getInstance().getDeviceList(new NetworkListener() {
            @Override
            public void onResponse(Object data) {
                ArrayList<Device> list = (ArrayList<Device>)data;
                handleList(list);
                Log.i("!!!", "get device list success");
            }

            @Override
            public void onError(int errorCode) {
                Log.i("!!!", "get device list failed");
            }
        });
    }

    private void handleList(ArrayList<Device> list) {

    }
}
