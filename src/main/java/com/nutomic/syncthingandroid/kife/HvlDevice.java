package com.nutomic.syncthingandroid.kife;

import com.nutomic.syncthingandroid.model.Device;

import org.json.JSONObject;

/**
 * Created by asd on 5.6.2017.
 */

public class HvlDevice extends Device{

    public String introducer;
    public String skipIntroductionRemovals;
    public String introducedBy;
    public String address;
    public String paused;

    public HvlDevice(JSONObject data) {
    }

    public HvlDevice(String id, String name, String compression, String introducer, String skipIntroductionRemovals, String introducedBy, String address, String paused) {
        this.deviceID = id;
        this.name = name;
        this.compression = compression;
        this.introducer = introducer;
        this.skipIntroductionRemovals = skipIntroductionRemovals;
        this.introducedBy = introducedBy;
        this.address = address;
        this.paused = paused;
    }
}
