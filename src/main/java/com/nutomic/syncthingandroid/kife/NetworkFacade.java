package com.nutomic.syncthingandroid.kife;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by asd on 2.6.2017.
 */

public class NetworkFacade {


    String TAG = "NetworkFacade";
    String URL_SEND_DEVICE_ID = "httl://...";


    private static NetworkFacade mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private NetworkFacade(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized NetworkFacade getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NetworkFacade(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public void get(String url, ResponseListener listener) {

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, null, response -> {
                        listener.onResponse(response);
                }, error -> {
                });

        mRequestQueue.add(jsonObjReq);
    }

    public void sendDeviceID(String deviceID, ResponseListener listener) {
        String url = URL_SEND_DEVICE_ID + deviceID;
        get(url, listener);
    }
}
