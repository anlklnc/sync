package com.nutomic.syncthingandroid.kife;

import android.content.Context;

import com.nutomic.syncthingandroid.util.ConfigXml;

/**
 * Created by asd on 2.6.2017.
 */

public class Kife {

    Context context;
    public String apiKey;
    public String deviceID;

    public Kife(Context context, String apiKey, String deviceID, ConfigXml configXml) {
        this.context = context;
        this.apiKey = apiKey;
        this.deviceID = deviceID;

        NetworkFacade.getInstance(context).sendDeviceID(deviceID, response -> {
            //dothis dothat
            DeviceList dl = new DeviceList(response);
            configXml.setDevices(dl);
        });
    }
}
