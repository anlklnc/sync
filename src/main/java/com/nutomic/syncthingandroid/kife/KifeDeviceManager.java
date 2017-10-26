package com.nutomic.syncthingandroid.kife;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nutomic.syncthingandroid.model.Device;
import com.nutomic.syncthingandroid.service.SyncthingService;
import com.nutomic.syncthingandroid.util.ConfigXml;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by asd on 2.6.2017.
 */

public class KifeDeviceManager {

    static final int POLLING_TIME = 30;

    Context context;
    ConfigXml xml;
    ArrayList<Device> diskItems;
    boolean isSendSuccessful = false;
    private boolean isFirstIteration = true;
    int count = 0;

    public KifeDeviceManager(Context context, ConfigXml configXml) {
        this.context = context;
        xml = configXml;

        diskItems = Disk.load(context);   //lokaldeki cihazlar

        startPolling();
    }

    private void startPolling() {

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Log.i("!!!", "device request timer: ");
                count++;
                if(count == 2) {
                    isFirstIteration = false;
                }

                //send device cevabı alana kadar send device gönder, cevap gelince sürekli get device list gönder
                if(!isSendSuccessful) {
                    sendDevice();
                } else {
                    getDeviceList();
                }
            }
        };
        //schedule your timer to execute perodically
        timer.schedule(task, 0, POLLING_TIME*1000);
    }

    private void sendDevice() {
        Device device = xml.getSelfDevice();

        if(diskItems.size() == 0) {
            //cihazın kendisini diske kaydet
            diskItems.add(device);
            Disk.save(context, diskItems);
        }

        Network.getInstance().sendDevice(new NetworkListener() {
            @Override
            public void onResponse(Object o) {
                Log.i("!!!", "send device success");
                isSendSuccessful = true;
                getDeviceList();
            }

            @Override
            public void onError(int errorCode) {
                Log.i("!!!", "send device failed");
            }
        }, device);
    }

    public void getDeviceList() {
        Network.getInstance().getDeviceList(new NetworkListener() {
            @Override
            public void onResponse(Object data) {
                Log.i("!!!", "get device list success");
                ArrayList<Device> list = (ArrayList<Device>)data;
                handleList(list);
            }

            @Override
            public void onError(int errorCode) {
                Log.i("!!!", "get device list failed");
            }
        });
    }

    /** Server'dan gelen cihazları kontrol eder, kayıtlı olmayanları xml'e ekleyip diske kaydeder. */
    private void handleList(ArrayList<Device> serverItems) {

        ArrayList<Device> newItems = new ArrayList<>();
        ArrayList<Device> diskItems = Disk.load(context);   //lokaldeki cihazlar

        for(Device serverItem:serverItems) {    //serverdan gelen her bir cihaz için
            boolean occurred = false;
            for(Device diskItem:diskItems) {    //lokalde bu cihaz var mı diye kontrol et
                if(serverItem.deviceID == null || serverItem.deviceID.equals(diskItem.deviceID)) { //varsa işaretle
                    occurred = true;
                    break;
                }
            }
            if(!occurred) { //işaretlenmemişse serverdan gelen cihaz lokalde kayıtlı değildir, bunu cihazlara ekle
                newItems.add(serverItem);
            }
        }

        if(newItems.size()>0) {
            for(Device d:newItems) {
                xml.addDevice(d);   //yeni cihazı config.xml'e ekle
                diskItems.add(d);   //yeni cihazı diske kaydet
                Log.i("!!!", "ADDED: " + d.name + " // " + d.deviceID);
            }
            Disk.save(context, diskItems);
            if(!isFirstIteration) {  //after first iteration
                restartService();
            }
        }
    }

    public void restartService() {
        Log.i("!!!", "restartService: ");
        Intent intent = new Intent(context, SyncthingService.class);
        intent.setAction(SyncthingService.ACTION_RESTART);
        context.startService(intent);
    }
}
