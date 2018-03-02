package com.nutomic.syncthingandroid.kife;

/**
 * Created by Anıl Kılınç on 16.6.2017.
 */

public interface NetworkListener {
    void onResponse(Object o);
    void onError(int errorCode);
}
