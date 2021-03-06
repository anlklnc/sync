package com.nutomic.syncthingandroid.kife;


import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.nutomic.syncthingandroid.model.Device;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by Anıl Kılınç on 13.6.2017.
 */

public class Network {

    public static final String BASE_URL = "http://192.168.2.2:8081/";

    private static final Network singleton = new Network();
    ControlPanelClient client;

    public static Network getInstance() {
        return singleton;
    }

    public interface ControlPanelClient {

        @GET("/api/sync/device/list")
//        @GET("/api/device/list")
        Call<ArrayList<Device>>deviceList();

        @GET("/api/sync/result/list")
        Call<ArrayList<SyncReport>>reportList();

        @POST("/api/sync/result")
        Call<SyncReport> sendSyncReport(@Body SyncReport report);

        @POST("/api/sync/device")
//        @POST("/api/device/create")
        Call<PostResponse> sendDevice(@Body Device user);
    }

    /** flight info */
    void getDeviceList(final NetworkListener listener) { setRequest(client.deviceList(), listener);}

    /** login for maintenance mode */
    void sendDevice(final NetworkListener listener, Device device) { setRequest(client.sendDevice(device), listener);}

    /** send download result */
    void sendSyncReport(SyncReport report, final NetworkListener listener) { setRequest(client.sendSyncReport(report), listener);}

    //////////////////////////////////////////////////////////////////

    public Network() {

        // Creates the json object which will manage the information received
        GsonBuilder builder = new GsonBuilder();

        // Register an adapter to manage the date types as long values
        builder.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        });

        Gson gson = builder.create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))

                .build();
        client =  retrofit.create(ControlPanelClient.class);
    }

    private void setRequest(Call call, final NetworkListener listener) {

        Callback callback = new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                Object data = response.body();

                //data boş ise hata kodu göster, değilse datayı ilet
                if(data == null) {
                    int errorCode = response.raw().code();
                    listener.onError(errorCode);
                } else {
                    listener.onResponse(data);
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.e("!!!", "Connection failed: " + t.getCause());
                listener.onError(-1);
            }
        };

        call.enqueue(callback);
    }
}
