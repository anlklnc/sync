package com.nutomic.syncthingandroid.kife;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nutomic.syncthingandroid.model.Device;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by asd on 2.8.2017.
 */

public class Disk {

    public static final String SYNC_SHARED_PREF = "com.anilkilinc.syncthing";
    public static final String DEVICE_LIST = "com.anilkilinc.syncthing.device_list";

    /** Paylaşım yapılan cihazların listesini dosyaya yazar. */
    public static void save(Context context, ArrayList<Device> list) {

        SharedPreferences.Editor editor = context.getSharedPreferences(SYNC_SHARED_PREF, Context.MODE_PRIVATE).edit();

        Gson gson = new Gson();
        String serializedData = gson.toJson(list);

        editor.putString(DEVICE_LIST, serializedData).commit();
    }

    /** Cihaz listesini dosyadan okur. */
    public static ArrayList<Device> load(Context context) {

        SharedPreferences sp = context.getSharedPreferences(SYNC_SHARED_PREF, Context.MODE_PRIVATE);
        String serialized = sp.getString(DEVICE_LIST, "");
        if (serialized != null && serialized.length() > 0) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Device>>(){}.getType();
            ArrayList<Device> data = gson.fromJson(serialized, type);
            return data;
        } else {
            return new ArrayList<>();
        }
    }
}
