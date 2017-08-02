package com.nutomic.syncthingandroid.kife;

/**
 * Created by asd on 16.6.2017.
 */

public interface NetworkListener {
    void onResponse(Object o);
    void onError(int errorCode);
}
