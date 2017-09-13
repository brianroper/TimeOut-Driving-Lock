package com.brianroper.putitdown.model.realmObjects;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by brianroper on 5/2/17.
 */

public class NeuraEventLog extends RealmObject {
    @PrimaryKey
    private String id;
    private String eventName;
    private long timestamp;
    private Date date;
    private String time;

    public NeuraEventLog() {
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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
