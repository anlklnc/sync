package com.nutomic.syncthingandroid.kife;

/**
 * Created by Anıl Kılınç on 13.12.2017.
 */

public class SyncReport {

    private String id, amount, time;

    public SyncReport(String id, String amount, String time) {
        this.id = id;
        this.amount = amount;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getAmount() {
        return amount;
    }

    public String getTime() {
        return time;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
