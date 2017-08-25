package com.nutomic.syncthingandroid.activities;

import android.content.Intent;
import android.os.Bundle;

import com.nutomic.syncthingandroid.R;
import com.nutomic.syncthingandroid.model.Connections;
import com.nutomic.syncthingandroid.model.SystemInfo;
import com.nutomic.syncthingandroid.service.RestApi;
import com.nutomic.syncthingandroid.service.SyncthingService;
import com.nutomic.syncthingandroid.util.Util;

import java.text.NumberFormat;

public class KifeActivity extends SyncthingActivity implements SyncthingService.OnApiChangeListener{

    String deviceId, cpuUsage, download, upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kife);
    }

    @Override
    public void onApiChange(SyncthingService.State currentState) {
        update();
    }

    void update() {
        getApi().getSystemInfo(this::onReceiveSystemInfo);
        getApi().getConnections(this::onReceiveConnections);
    }

    public void onReceiveSystemInfo(SystemInfo info) {
        deviceId = info.myID;

        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(2);
        cpuUsage = percentFormat.format(info.cpuPercent / 100);
    }

    private void onReceiveConnections(Connections connections) {
        Connections.Connection c = connections.total;
        download = Util.readableTransferRate(this, c.inBits);
        upload = Util.readableTransferRate(this, c.outBits);
    }

    private void restartService() {
        Intent intent = new Intent(this, SyncthingService.class);
        intent.setAction(SyncthingService.ACTION_RESTART);
        this.startService(intent);
    }

    private void devices() {
        getApi().getDevices(false);
    }

    private void connections() {
        getApi().getConnections(new RestApi.OnResultListener1<Connections>() {
            @Override
            public void onResult(Connections connections) {
                String someDeviceId ="";
                Connections.Connection c = connections.connections.get(someDeviceId);
                int completion = c.completion;  //status by
                long in = c.inBits;
                long out = c.outBits;
            }
        });
    }
}
