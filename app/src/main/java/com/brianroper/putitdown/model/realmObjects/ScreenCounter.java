package com.brianroper.putitdown.model.realmObjects;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by brianroper on 8/10/17.
 */

public class ScreenCounter extends RealmObject{
    @PrimaryKey
    private String id;
    private int counter;
    private Date date;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
