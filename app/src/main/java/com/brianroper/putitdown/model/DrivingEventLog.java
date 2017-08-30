package com.brianroper.putitdown.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by brianroper on 8/29/17.
 */

public class DrivingEventLog extends RealmObject {
    @PrimaryKey
    private String id;
    private boolean isSuccessful;
    private String date;
    private String time;

    public DrivingEventLog() {}

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
