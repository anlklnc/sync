package com.nutomic.syncthingandroid.kife;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.nutomic.syncthingandroid.R;
import com.nutomic.syncthingandroid.activities.SyncthingActivity;
import com.nutomic.syncthingandroid.model.Connections;
import com.nutomic.syncthingandroid.model.Device;
import com.nutomic.syncthingandroid.model.SystemInfo;
import com.nutomic.syncthingandroid.service.SyncthingService;
import com.nutomic.syncthingandroid.util.Util;

import java.text.NumberFormat;
import java.util.ArrayList;

public class KifeActivity extends SyncthingActivity implements SyncthingService.OnApiChangeListener{

    String deviceId, cpuUsage, download, upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kife);
        Log.i("!!!", "Kife activity: on create");
    }

    @Override
    public void onApiChange(SyncthingService.State currentState) {
        update();
    }

    void update() {
        getApi().getSystemInfo(this::onReceiveSystemInfo);
        getApi().getConnections(this::onReceiveConnections);
    }

    /** Sistem bilgileri buraya geliyor. */
    public void onReceiveSystemInfo(SystemInfo info) {
        deviceId = info.myID;

        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(2);
        cpuUsage = percentFormat.format(info.cpuPercent / 100);

        Log.i("!!!", "Device id: " + deviceId);
        Log.i("!!!", "Cpu usage: " + cpuUsage);
    }

    /** Bağlantı bilgileri buraya geliyor. */
    private void onReceiveConnections(Connections connections) {
        Connections.Connection c = connections.total;
        download = Util.readableTransferRate(this, c.inBits);
        upload = Util.readableTransferRate(this, c.outBits);

        Log.i("!!!", "Download: " + download);
        Log.i("!!!", "Upload: " + upload);
    }

    public void restartService(View view) {
        Intent intent = new Intent(this, SyncthingService.class);
        intent.setAction(SyncthingService.ACTION_RESTART);
        this.startService(intent);
    }

    public void devices(View view) {
        ArrayList<Device> list = (ArrayList<Device>) getApi().getDevices(false);
        for(Device d:list) {
            Log.i("!!!", "device: " + d.name+"//"+d.deviceID);
        }
    }

    public void connections(View view) {
        getApi().getConnections(connections -> {
            String someDeviceId ="";
            Connections.Connection c = connections.connections.get(someDeviceId);
            int completion = -1;  //status by
            try {
                completion = c.completion;
            } catch (Exception e) {
                e.printStackTrace();
            }
            long in = -1;
            try {
                in = c.inBits;
            } catch (Exception e) {
                e.printStackTrace();
            }
            long out = -1;
            try {
                out = c.outBits;
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("!!!", "connections: " + completion+"//"+in+"//"+out);
        });
    }
}
