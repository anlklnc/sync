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
    ArrayList<Device> diskItems;

    public Kife(Context context, ConfigXml configXml) {
        this.context = context;
        xml = configXml;

        diskItems = Disk.load(context);   //lokaldeki cihazlar

        sendDevice();
//        Disk.save(context, new ArrayList<>());
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
        }
    }
}
