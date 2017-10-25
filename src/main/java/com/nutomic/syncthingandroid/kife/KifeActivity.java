package com.nutomic.syncthingandroid.kife;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.nutomic.syncthingandroid.BuildConfig;
import com.nutomic.syncthingandroid.R;
import com.nutomic.syncthingandroid.activities.SyncthingActivity;
import com.nutomic.syncthingandroid.model.Connections;
import com.nutomic.syncthingandroid.model.Device;
import com.nutomic.syncthingandroid.model.Folder;
import com.nutomic.syncthingandroid.model.Model;
import com.nutomic.syncthingandroid.model.SystemInfo;
import com.nutomic.syncthingandroid.service.SyncthingService;
import com.nutomic.syncthingandroid.util.Util;

import java.text.NumberFormat;
import java.util.ArrayList;

public class KifeActivity extends SyncthingActivity implements SyncthingService.OnApiChangeListener{

    String deviceId, cpuUsage, download, upload;
    TextView twId, twLabel, twPath, twPercentage, twItems, twSize, twState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kife);

        twId = (TextView) findViewById(R.id.tw_id);
        twLabel = (TextView) findViewById(R.id.tw_label);
        twPath = (TextView) findViewById(R.id.tw_path);
        twPercentage = (TextView) findViewById(R.id.tw_percentage);
        twItems = (TextView) findViewById(R.id.tw_items);
        twSize = (TextView) findViewById(R.id.tw_size);
        twState = (TextView) findViewById(R.id.tw_state);
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
            ArrayList<Device> devices = (ArrayList<Device>) getApi().getDevices(false);
            String someDeviceId =devices.get(0).deviceID;    //todo device id
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

    ///// folderla ilgili kısım



    public void updateFolderInfo(View view) {
        Folder folder = getApi().getFolders().get(0);   //ilk folderı al
        String label = TextUtils.isEmpty(folder.label) ? folder.id : folder.label;
        String directory = folder.path;
        getApi().getModel(folder.id, this::onReceiveModel); //updateFolderInfo'a ait modeli al
        Log.i("!!!", "twId: "+folder.id);
        Log.i("!!!", "twLabel: "+label);
        Log.i("!!!", "twPath: "+directory);
        updateFolderUi1(folder.id, label, directory);
    }

    public void onReceiveModel(String s, Model model) {

        int percentage = (model.localBytes != 0)
                ? Math.round(100 * model.inSyncBytes / model.localBytes)
                : 100;
        String state = getLocalizedState(this, model.state, percentage);
        String items = this.getString(R.string.files, model.inSyncFiles, model.localFiles);
        String size = this.getString(R.string.folder_size_format,
                Util.readableFileSize(this, model.inSyncBytes),
                Util.readableFileSize(this, model.localBytes));

        Log.i("!!!", "perc: "+percentage);
        Log.i("!!!", "twState: "+state);
        Log.i("!!!", "twItems: "+items);
        Log.i("!!!", "twSize: "+size);
        updateFolderUi2(percentage+"", state, items, size);
    }

    public static String getLocalizedState(Context c, String state, int percentage) {
        switch (state) {
            case "idle":     return c.getString(R.string.state_idle);
            case "scanning": return c.getString(R.string.state_scanning);
            case "cleaning": return c.getString(R.string.state_cleaning);
            case "syncing":  return c.getString(R.string.state_syncing, percentage);
            case "error":    return c.getString(R.string.state_error);
            case "unknown":  // Fallthrough
            case "":         return c.getString(R.string.state_unknown);
        }
        if (BuildConfig.DEBUG) {
            throw new AssertionError("Unexpected updateFolderInfo twState " + state);
        }
        return "";
    }

    void updateFolderUi1(String id, String label, String path) {
        twId.setText(id);
        twLabel.setText(label);
        twPath.setText(path);
    }

    void updateFolderUi2(String perc, String state, String items, String size) {
        twPercentage.setText(perc);
        twState.setText(state);
        twItems.setText(items);
        twSize.setText(size);
    }
}
