package com.brianroper.putitdown.model.gps;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by brianroper on 9/10/17.
 */

public interface TimeOutGpsListener extends LocationListener, GpsStatus.Listener {
    void onLocationChanged(Location location);
    void onProviderDisabled(String provider);
    void onProviderEnabled(String provider);
    void onStatusChanged(String provider, int status, Bundle extras);
    void onGpsStatusChanged(int event);
}
