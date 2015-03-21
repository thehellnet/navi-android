package org.thehellnet.mobile.navi.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.thehellnet.mobile.navi.R;

public class PositionService extends Service {
    private static final String TAG = PositionService.class.getName();

    private LocationManager locationManager;
    private LocationListener networkListener;
    private LocationListener gpsListener;

    private boolean alreadyStarted;
    private Location networkLocation;
    private Location gpsLocation;

    public PositionService() {
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
    public void onCreate() {
        super.onCreate();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service called");

        boolean status = intent.getBooleanExtra(getString(R.string.intent_servicelaunch_status), false);
        enableListeners(status);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void enableListeners(boolean status) {
        if(status == alreadyStarted) {
            return;
        }

        alreadyStarted = status;

        if(alreadyStarted) {
            Log.d(TAG, "Starting position listeners");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
        } else {
            Log.d(TAG, "Stopping thread");
            locationManager.removeUpdates(networkListener);
            locationManager.removeUpdates(gpsListener);
            stopSelf();
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

        Log.d(TAG, String.format("Accuracy -> GPS: %f - NETWORK: %f",
                gpsLocation.getAccuracy(),
                networkLocation.getAccuracy()));

        if(gpsLocation.getAccuracy() <= networkLocation.getAccuracy()) {
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
        Intent intent = new Intent(getString(R.string.intent_update));
        intent.putExtra(getString(R.string.intent_update_location), location);
        intent.putExtra(getString(R.string.intent_update_type), type);
        getApplicationContext().sendBroadcast(intent);
    }
}
