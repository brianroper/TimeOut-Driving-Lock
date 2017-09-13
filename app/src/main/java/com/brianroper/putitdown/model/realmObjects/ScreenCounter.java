package com.brianroper.putitdown.model.realmObjects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by brianroper on 8/10/17.
 */

public class ScreenCounter extends RealmObject{
    @PrimaryKey
    private String id;
    private int counter;

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public String getId() {
        return id;
    }
}
