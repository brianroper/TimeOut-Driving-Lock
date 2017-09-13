package com.brianroper.putitdown.model.realmObjects;

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
    private Date date;
    private String time;

    public DrivingEventLog() {}

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
