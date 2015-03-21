package org.thehellnet.mobile.navi.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class PositionService extends IntentService {
    private static final String TAG = PositionService.class.getName();

    private final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    private LocationListener networkListener;
    private LocationListener gpsListener;

    private boolean alreadyStarted;
    private Location networkLocation;
    private Location gpsLocation;

    public PositionService() {
        super(TAG);

        alreadyStarted = false;

        networkListener = new LocationListener() {
            @Override public void onStatusChanged(String provider, int status, Bundle extras) {

            }
            @Override public void onProviderEnabled(String provider) {

            }
            @Override public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "LOCATION CHANGED NETWORK");
                networkLocation = location;
                updateLocation();
            }
        };

        gpsListener = new LocationListener() {
            @Override public void onStatusChanged(String provider, int status, Bundle extras) {

            }
            @Override public void onProviderEnabled(String provider) {

            }
            @Override public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "LOCATION CHANGED GPS");
                gpsLocation = location;
                updateLocation();
            }
        };
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service called");

        boolean status = intent.getBooleanExtra("status", false);

        enableListeners(status);
    }

    private void enableListeners(boolean status) {
        if(status == alreadyStarted) {
            return;
        }

        alreadyStarted = status;

        if(alreadyStarted) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
        } else {
            locationManager.removeUpdates(networkListener);
            locationManager.removeUpdates(gpsListener);
        }
    }

    private void updateLocation() {
        if(networkLocation == null && gpsLocation == null) {
            return;
        }

        if(gpsLocation == null || !gpsLocation.hasAccuracy()) {
            launchUpdate(networkLocation, "network");
            return;
        }

        if(networkLocation == null || !networkLocation.hasAccuracy()) {
            launchUpdate(gpsLocation, "gps");
            return;
        }

        if(gpsLocation.getAccuracy() >= networkLocation.getAccuracy()) {
            launchUpdate(gpsLocation, "gps");
        } else {
            launchUpdate(networkLocation, "network");
        }
    }

    private void launchUpdate(Location location, String type) {
        Log.d(TAG, String.format("Location update from %s", type));

        sendDataToSocket(location, type);
        sendIntent(location, type);
    }

    private void sendDataToSocket(Location location, String type) {

    }

    private void sendIntent(Location location, String type) {
        Intent intent = new Intent("org.thehellnet.mobile.navi.intents.UPDATE_LOCATION");
        intent.putExtra("type", type);
        intent.putExtra("location", location);
        getApplicationContext().sendBroadcast(intent);
    }
}
