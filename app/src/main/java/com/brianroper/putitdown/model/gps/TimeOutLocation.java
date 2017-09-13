package com.brianroper.putitdown.model.gps;

import android.location.Location;

/**
 * Created by brianroper on 9/10/17.
 */

public class TimeOutLocation extends Location {

    private boolean mUseMetricUnits = false;

    public TimeOutLocation(Location location, boolean useMetricUnits) {
        super(location);
        this.mUseMetricUnits = useMetricUnits;
    }

    public TimeOutLocation(Location location){
        this(location, true);
    }

    public boolean isUseMetricUnits() {
        return mUseMetricUnits;
    }

    public void setUseMetricUnits(boolean useMetricUnits) {
        mUseMetricUnits = useMetricUnits;
    }

    @Override
    public float distanceTo(Location dest) {
        float distance = super.distanceTo(dest);
        if(!this.isUseMetricUnits()){
            //convert meters to feet
            distance = distance * 3.280083989501312f;
        }
        return distance;
    }

    @Override
    public float getAccuracy() {
        float accuracy = super.getAccuracy();
        if(!this.isUseMetricUnits()){
            accuracy = accuracy * 3.280083989501312f;
        }
        return accuracy;
    }

    @Override
    public double getAltitude() {
        double altitude = super.getAltitude();
        if(!this.isUseMetricUnits()){
            altitude = altitude * 3.28083989501312d;
        }
        return altitude;
    }

    @Override
    public float getSpeed() {
        float speed = super.getSpeed() * 3.6f;
        if(!this.isUseMetricUnits()){
            speed = speed  * 2.2369362920544f/3.6f;
        }
        return speed;
    }
}
