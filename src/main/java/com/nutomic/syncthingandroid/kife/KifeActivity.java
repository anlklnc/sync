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
    static final int CONNECTION_DISPLAY_CYCLE = 1;
    boolean isPolling = false;
    int indicator = 0;
    int counter = 0;
    int apiState = 0;

    TextView twId, twLabel, twPath, twSomeData, twPercentage, twItems, twSize, twState, twTotalUp,  twTotalDown, twUp, twDown;
    TextView deviceNameView, totalSyncTimeView, totalScanTimeView;
    TextView indicatorView;
    ListView list;
    private ArrayList<Device> devices;
    private CustomArrayAdapter adapter;
    private ArrayList<Pair> items;
    private ArrayList<Long> downloadList;
    private ArrayList<Long> uploadList;
    private long totalUpload = 0;
    private long totalDownload = 0;
    private int devicesSize = 0;
    private long downloadOnStart = 0;

    int deviceNumber = -1;
    int state = 0;
    long start = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kife);

        twId = (TextView) findViewById(R.id.tw_id);
        twLabel = (TextView) findViewById(R.id.tw_label);
        twPath = (TextView) findViewById(R.id.tw_path);
        twSomeData = (TextView) findViewById(R.id.tw_somedata);
        twPercentage = (TextView) findViewById(R.id.tw_percentage);
        twItems = (TextView) findViewById(R.id.tw_items);
        twSize = (TextView) findViewById(R.id.tw_size);
        twState = (TextView) findViewById(R.id.tw_state);
        twTotalUp = (TextView) findViewById(R.id.tw_total_upload);
        twTotalDown = (TextView) findViewById(R.id.tw_total_download);
        twUp = (TextView) findViewById(R.id.tw_up);
        twDown = (TextView) findViewById(R.id.tw_down);
        indicatorView = (TextView) findViewById(R.id.tw_indicator);
        deviceNameView = (TextView) findViewById(R.id.tw_device_name);
        totalSyncTimeView = (TextView) findViewById(R.id.tw_total_sync_time);
        totalScanTimeView = (TextView) findViewById(R.id.tw_total_scan_time);

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

        String selfId = getApi().getLocalDevice().deviceID.substring(0,3);
        deviceNameView.setText(selfId);

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

                int size = devices.size();
                if(devicesSize == 0) {  //ilk girişte devices büyüklüğünde upload ve downloadlist oluşturup içnii sıfırla doldur
                    devicesSize = size;
                    uploadList = new ArrayList<>(size);
                    for(int i = 0; i<size; i++) {
                        uploadList.add((long) 0);
                    }
                    downloadList = new ArrayList<>(size);
                    for(int i=0; i<size; i++) {
                        downloadList.add((long) 0);
                    }
                } else if(devicesSize != size) {    //yeni cihaz eklendiğinden bu cihaz için up ve download list'e yeni entry ekle
                    uploadList.add((long) 0);
                    downloadList.add((long) 0);
                }

                items.clear();

                long instantDownloadRate = 0;
                long instantUploadRate = 0;

                for(int i=0; i<size; i++) {

                    Device device = devices.get(i);
                    String someDeviceId = device.deviceID;
                    Connections.Connection c = connections.connections.get(someDeviceId);

                    long uploadSum = uploadList.get(i);
                    long downloadSum = downloadList.get(i);

                    if (i == 0) {
                        try {
                            twPercentage.setText(c.completion+"%");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    //add download
                    try {
                        long downloadRate = c.inBits;
                        instantDownloadRate+= downloadRate;
                        downloadRate *= (UPDATE_TIME*CONNECTION_DISPLAY_CYCLE);
                        downloadSum += downloadRate;
                        totalDownload += downloadRate;
                    } catch (Exception e) {}

                    //add upload
                    try {
                        long uploadRate = c.outBits;
                        instantUploadRate += uploadRate;
                        uploadRate *= (UPDATE_TIME*CONNECTION_DISPLAY_CYCLE);
                        uploadSum += uploadRate;
                        totalUpload += uploadRate;
                    } catch (Exception e) {}

                    uploadList.set(i, uploadSum);
                    downloadList.set(i, downloadSum);

                    int j = i+1;
                    if(deviceNumber != -1) {
                        if(j>=deviceNumber) {
                            j++;
                        }
                    }

                    String id = someDeviceId.substring(0,3);

                    String download = Util.readableTransferRate(this, downloadSum);
                    String upload = Util.readableTransferRate(this, uploadSum);
                    Pair<String, String> item = new Pair(device.getDisplayName()+" ("+id+')', "d: "+download+"   |   u: "+upload);
                    items.add(item);
                }

                twUp.setText(Util.readableTransferRate(this, instantUploadRate));
                twDown.setText(Util.readableTransferRate(this, instantDownloadRate));
                twTotalUp.setText(Util.readableTransferRate(this, totalUpload));
                twTotalDown.setText(Util.readableTransferRate(this, totalDownload));

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

        twSomeData.setText(percentage+"");
        twState.setText(state);
        twItems.setText(items);
        twSize.setText(size);

        indicate();
    }

    public String getLocalizedState(Context c, String state, int percentage) {

        if(!state.equals("syncing") && this.state == 1) {
            this.state = 2;
            endTimer(1);
        } else if(!state.equals("scanning") && this.state == 3) {
            this.state = 4;
            endTimer(2);
        }

        switch (state) {
            case "idle":
                twState.setTextColor(ContextCompat.getColor(this, R.color.state_idle));
                return c.getString(R.string.state_idle);
            case "scanning":
                if(this.state != 3) {
                    this.state = 3;
                    startTimer();
                }
                twState.setTextColor(ContextCompat.getColor(this, R.color.state_scanning));
                return c.getString(R.string.state_scanning);
            case "cleaning":
                twState.setTextColor(ContextCompat.getColor(this, R.color.state_cleaning));
                return c.getString(R.string.state_cleaning);
            case "syncing": {
                if (this.state != 1) {   // sync başlamışsa zamanlamayı başlat
                    this.state = 1;
                    startTimer();
                }
                twState.setTextColor(ContextCompat.getColor(this, R.color.state_syncing));
                return c.getString(R.string.state_syncing, percentage);
            }
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
        indicator++;
        if (indicator == 4) {
            indicator = 0;
        }
        switch (indicator){
            case 0:
                indicatorView.setText("|");
                break;
            case 1:
                indicatorView.setText("/");
                break;
            case 2:
                indicatorView.setText("--");
                break;
            case 3:
                indicatorView.setText("\\");
                break;
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

    private void startTimer() {
        start = System.currentTimeMillis();
        totalSyncTimeView.setText("");
        downloadOnStart = totalDownload;
    }

    private void endTimer(int param) {
        long end = System.currentTimeMillis();
        long dif = end-start;
        dif /= 10;
        String d = dif+"";
        String time;
        try {
            int timePassed = Integer.parseInt(d.substring(0, d.length()-2));
            time = getHour(timePassed);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            time = "?";
        }

        if(param == 1) {
            dif = totalDownload-downloadOnStart;
            String downloadAmount = Util.readableTransferRate(this, dif);
            twTotalDown.setText(Util.readableTransferRate(this, totalDownload));
            totalSyncTimeView.setText(downloadAmount  +" sync finished in " + time);
        } else if(param == 2) {
            totalScanTimeView.setText("Scan finished in " + time);
        }
    }

    public static String getHour(int totalMinutes) {
        String result = "";
        if(totalMinutes<0) {
            result = "-";
            totalMinutes = totalMinutes*-1;
        }
        int h = totalMinutes/60;
        int m = totalMinutes%60;
        result += addZero(h)+":"+addZero(m);
        return result;
    }

    public static String addZero(int i) {
        String s = i+"";
        if(i >= 0 && i < 10) {
            s = '0' + s;
        }
        return s;
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
