package com.nutomic.syncthingandroid.kife;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by asd on 2.6.2017.
 */

public class DeviceList {

    String name, email, home, mobile;

    public DeviceList(JSONObject json) {
        try {
            // Parsing json object response
            // response will be a json object
            String name = json.getString("name");
            String email = json.getString("email");
            JSONObject phone = json.getJSONObject("phone");
            String home = phone.getString("home");
            String mobile = phone.getString("mobile");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
