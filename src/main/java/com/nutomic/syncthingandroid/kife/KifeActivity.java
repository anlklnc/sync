package com.nutomic.syncthingandroid.kife;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.nutomic.syncthingandroid.BuildConfig;
import com.nutomic.syncthingandroid.R;
import com.nutomic.syncthingandroid.activities.SyncthingActivity;
import com.nutomic.syncthingandroid.model.Connections;
import com.nutomic.syncthingandroid.model.Device;
import com.nutomic.syncthingandroid.model.Folder;
import com.nutomic.syncthingandroid.model.Model;
import com.nutomic.syncthingandroid.service.SyncthingService;
import com.nutomic.syncthingandroid.util.Util;

import java.util.ArrayList;

public class KifeActivity extends SyncthingActivity{

    static final int UPDATE_TIME = 2;
    static final int CONNECTION_DISPLAY_CYCLE = 6;
    boolean isPolling = false;
    boolean indicator = true;
    int counter = 0;
    int apiState = 0;

    TextView twId, twLabel, twPath, twPercentage, twItems, twSize, twState, twTotal, twUp, twDown;
    View indicatorView;
    ListView list;
    private ArrayList<Device> devices;
    private CustomArrayAdapter adapter;
    private ArrayList<Pair> items;
    private long[] downloadList;
    private long[] uploadList;

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
        twTotal = (TextView) findViewById(R.id.tw_total);
        twUp = (TextView) findViewById(R.id.tw_up);
        twDown = (TextView) findViewById(R.id.tw_down);
        indicatorView = findViewById(R.id.indicator);

        list = (ListView)findViewById(R.id.list);
        items = new ArrayList<>();
//        items.add("atos"); items.add("portos"); items.add("paramis");
        adapter = new CustomArrayAdapter(items);
        list.setAdapter(adapter);
    }

    public void restartService(View view) {
        Intent intent = new Intent(this, SyncthingService.class);
        intent.setAction(SyncthingService.ACTION_RESTART);
        this.startService(intent);
    }

    public void folders(View v) {

        if(isPolling) { //buraya iki defa girmesini engelle
            return;
        }
        isPolling = true;

        //update folder info
        Folder folder = getApi().getFolders().get(0);   //ilk folderı al
        String label = TextUtils.isEmpty(folder.label) ? folder.id : folder.label;
        twId.setText(folder.id);
        twLabel.setText(label);
        twPath.setText(folder.path);

        //update sync details
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getApi().getModel(folder.id, KifeActivity.this::onReceiveModel); //updateFolderInfo'a ait modeli al
                if(counter == 0) {
                    connections(null);
                }
                counter++;
                if(counter==CONNECTION_DISPLAY_CYCLE) {
                    counter = 0;
                }
                handler.postDelayed(this, UPDATE_TIME*1000);
            }
        }, UPDATE_TIME);
    }

    public void devices(View view) {
        ArrayList<Device> list = (ArrayList<Device>) getApi().getDevices(false);
        for(Device d:list) {
            Log.i("!!!", "device: " + d.name+"//"+d.deviceID);
        }
    }

    public void connections(View view) {
        getApi().getConnections(connections -> {
            try {
                devices = (ArrayList<Device>)getApi().getDevices(false);
                if(devices == null) {
                    return;
                }

                //ilk girişte initialize et
                if(uploadList == null) {
                    uploadList = new long[devices.size()];
                    for(int i = 0; i<uploadList.length; i++) {
                        uploadList[i] = 0;
                    }
                    downloadList = new long[devices.size()];
                    for(int i=0; i<downloadList.length; i++) {
                        downloadList[i] = 0;
                    }
                }

                items.clear();
                for(int i=0; i<devices.size(); i++) {

                    Device device = devices.get(i);
                    String someDeviceId = device.deviceID;
                    Connections.Connection c = connections.connections.get(someDeviceId);

                    long totalUpload = uploadList[i];
                    long totalDownload = downloadList[i];

                    int completion = -1;  //status by
                    try {
                        completion = c.completion;
//                        twTotal.setText(completion+"");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //add download
                    try {
                        long downloadRate = c.inBits;
                        downloadRate *= (UPDATE_TIME*CONNECTION_DISPLAY_CYCLE);
                        totalDownload += downloadRate;
                    } catch (Exception e) {}

                    //add upload
                    try {
                        long uploadRate = c.outBits;
                        uploadRate *= (UPDATE_TIME*CONNECTION_DISPLAY_CYCLE);
                        totalUpload += uploadRate;
                    } catch (Exception e) {}

                    uploadList[i] = totalUpload;
                    downloadList[i] = totalDownload;

                    String download = Util.readableTransferRate(this, totalDownload);
                    String upload = Util.readableTransferRate(this, totalUpload);
                    Pair<String, String> item = new Pair(device.getDisplayName()+" | "+completion+"%", "d: "+download+"   |   u: "+upload);
                    items.add(item);
                }
                adapter.notifyDataSetChanged();

            } catch (Exception e) {}
        });
    }

    /** folderla ilgili kısım */
    public void onReceiveModel(String s, Model model) {

        int percentage = (model.localBytes != 0)
                ? Math.round(100 * model.inSyncBytes / model.localBytes)
                : 100;
        String state = getLocalizedState(this, model.state, percentage);
        String items = this.getString(R.string.files, model.inSyncFiles, model.localFiles);
        String size = this.getString(R.string.folder_size_format,
                Util.readableFileSize(this, model.inSyncBytes),
                Util.readableFileSize(this, model.localBytes));

//        Log.i("!!!", "perc: "+percentage);
//        Log.i("!!!", "twState: "+state);
//        Log.i("!!!", "twItems: "+items);
//        Log.i("!!!", "twSize: "+size);

        twPercentage.setText(percentage+"");
        twState.setText(state);
        twItems.setText(items);
        twSize.setText(size);

        indicate();
    }

    public String getLocalizedState(Context c, String state, int percentage) {
        switch (state) {
            case "idle":
                twState.setTextColor(ContextCompat.getColor(this, R.color.state_idle));
                return c.getString(R.string.state_idle);
            case "scanning":
                twState.setTextColor(ContextCompat.getColor(this, R.color.state_scanning));
                return c.getString(R.string.state_scanning);
            case "cleaning":
                twState.setTextColor(ContextCompat.getColor(this, R.color.state_cleaning));
                return c.getString(R.string.state_cleaning);
            case "syncing":
                twState.setTextColor(ContextCompat.getColor(this, R.color.state_syncing));
                return c.getString(R.string.state_syncing, percentage);
            case "error":
                twState.setTextColor(ContextCompat.getColor(this, R.color.state_error));
                return c.getString(R.string.state_error);
            case "unknown":  // Fallthrough
            case "":
                twState.setTextColor(ContextCompat.getColor(this, R.color.state_unknown));
                return c.getString(R.string.state_unknown);
        }
        if (BuildConfig.DEBUG) {
            throw new AssertionError("Unexpected updateFolderInfo twState " + state);
        }
        return "";
    }

    void indicate() {
        if(indicator) {
            indicator = false;
            indicatorView.setBackgroundResource(R.color.green);
        } else {
            indicator = true;
            indicatorView.setBackgroundResource(R.color.green2);
        }
    }

    @Override
    protected void showLoadingDialog() {
        super.showLoadingDialog();
        apiState = 1;
    }

    /** Uygulama çalışmaya hazır olduğunda bu çağırılır. */
    @Override
    protected void dismissLoadingDialog() {
        super.dismissLoadingDialog();
        if(apiState == 1) {
            apiState = 0;
            startPolling();
        }
    }

    void startPolling() {
        folders(null);
    }

    public void shutdown(View v) {
        getApi().shutdown();
    }

    @Override
    protected void onDestroy() {
        //todo get service stop service
        super.onDestroy();
    }

    public class CustomArrayAdapter extends BaseArrayAdapter<Pair> {

        static final int LAYOUT = R.layout.item_device_data;

        public CustomArrayAdapter(ArrayList<Pair> items) {
            super(KifeActivity.this, LAYOUT, R.id.tw1, items);
        }

        @Override
        public void handleView(View view, int position) {
            TextView tw1 = (TextView) view.findViewById(R.id.tw1);
            String s1 = (String) getItem(position).first;
            tw1.setText(s1);

            TextView tw2 = (TextView) view.findViewById(R.id.tw2);
            String s2 = (String) getItem(position).second;
            tw2.setText(s2);
        }
    }
}
