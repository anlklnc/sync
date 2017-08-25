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

import okhttp3.Headers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

/**
 * Created by asd on 13.6.2017.
 */

public class Network {

//    public static final String BASE_URL = "http://192.168.43.237:8080/";
//    public static final String BASE_URL = "http://192.168.43.206:8080/";
    public static final String BASE_URL = "http://192.168.2.183:8080/";

    private static final Network singleton = new Network();
    ControlPanelClient client;

    public static Network getInstance() {
        return singleton;
    }

    /** Verilen key ve listener ile tüm request'lerde kullanılabilen generic bir NetworkListener oluşturur.*/
    NetworkListener getListener(final String url, final ObjectResponseListener listener) {

        return  new NetworkListener() {
            @Override
            public void onResponse(Object data) {
                listener.onResponse(data);
            }

            @Override
            public void onError(int errorCode) {
                Log.i("!!!", "onError: " + errorCode);
            }
        };
    }

    public interface ControlPanelClient {

        @GET("/api/sync/device/list")
        Call<ArrayList<Device>>deviceList();

        @POST("/api/sync/device")
        Call<PostResponse> sendDevice(@Body Device user);
//
//        @FormUrlEncoded
//        @POST("user/edit")
//        Call<User> updateUser(@Field("first_name") String first, @Field("last_name") String last);
//
//        @Multipart
//        @PUT("user/photo")
//        Call<User> updateUser(@Part("photo") RequestBody photo, @Part("description") RequestBody description);
//
//        @GET("user")
//        Call<User> getUser(@Header("Authorization") String authorization)
    }

    /** flight info */
    void getDeviceList(final NetworkListener listener) { setRequest(client.deviceList(), listener);}

    /** login for maintenance mode */
    void sendDevice(final NetworkListener listener, Device device) { setRequest(client.sendDevice(device), listener);}

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

    private String getUrl(Call<?> call) {
        //display outgoing headers for post request
        Headers h = call.request().headers();
        String s = h.toString();
        Log.i("!!!", "headers: " + s);
        return call.request().url().toString();
    }
}
