package com.brianroper.putitdown.model;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by brianroper on 9/10/17.
 */

public interface TimeOutGpsListener extends LocationListener, GpsStatus.Listener {
    public void onLocationChanged(Location location);
    public void onProviderDisabled(String provider);
    public void onProviderEnabled(String provider);
    public void onStatusChanged(String provider, int status, Bundle extras);
    public void onGpsStatusChanged(int event);
}
