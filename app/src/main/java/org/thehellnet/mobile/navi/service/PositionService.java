package org.thehellnet.mobile.navi.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.thehellnet.java.socket.HellSocket;
import org.thehellnet.mobile.navi.R;
import org.thehellnet.mobile.navi.config.C;

import java.io.IOException;

public class PositionService extends Service {
    private static final String TAG = PositionService.class.getName();

    private LocationManager locationManager;
    private LocationListener networkListener;
    private LocationListener gpsListener;

    private boolean alreadyStarted;
    private Location networkLocation;
    private Location gpsLocation;

    private int countPositionsNetwork = 0;
    private int countPositionsGps = 0;
    private int countPositionsSent = 0;

    private static final HellSocket socket = new HellSocket(C.server.HOST, C.server.PORT);

    @Override
    public void onCreate() {
        super.onCreate();

        alreadyStarted = false;

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        networkListener = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "LOCATION CHANGED NETWORK");
                networkLocation = location;
                countPositionsNetwork++;
                updateLocation();
            }
        };

        gpsListener = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {
                Log.d(TAG, "LOCATION CHANGED GPS");
                gpsLocation = location;
                countPositionsGps++;
                updateLocation();
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service called");

        super.onStartCommand(intent, flags, startId);

        // Avoid NullPointerException on App close
        if (intent != null) {
            boolean status = intent.getBooleanExtra("status", false);
            enableListeners(status);
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void enableListeners(boolean status) {
        if (status == alreadyStarted) {
            return;
        }

        alreadyStarted = status;

        if (alreadyStarted) {
            Log.d(TAG, "Starting position listeners");

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);

            Toast.makeText(this, "Position Service started", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "Stopping thread");

            locationManager.removeUpdates(networkListener);
            locationManager.removeUpdates(gpsListener);

            stopSelf();

            try {
                socket.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Toast.makeText(this, "Position Service stop", Toast.LENGTH_LONG).show();
        }
    }

    private void updateLocation() {
        if (networkLocation == null && gpsLocation == null) {
            return;
        }

        if (gpsLocation == null || !gpsLocation.hasAccuracy()) {
            launchUpdate(networkLocation, "network");
            return;
        }

        if (networkLocation == null || !networkLocation.hasAccuracy()) {
            launchUpdate(gpsLocation, "gps");
            return;
        }

        Log.d(TAG, String.format("Accuracy -> GPS: %f - NETWORK: %f",
                gpsLocation.getAccuracy(),
                networkLocation.getAccuracy()));

        if (gpsLocation.getAccuracy() <= networkLocation.getAccuracy()) {
            launchUpdate(gpsLocation, LocationManager.GPS_PROVIDER);
        } else {
            launchUpdate(networkLocation, LocationManager.NETWORK_PROVIDER);
        }
    }

    private void launchUpdate(final Location location, final String type) {
        Log.d(TAG, String.format("Location update from %s", type));

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendDataToSocket(location, type);
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendIntent(location, type);
            }
        }).start();
    }

    private void sendDataToSocket(Location location, String type) {
        if (!socket.isConnected()) {
            try {
                socket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        SharedPreferences sharedPreferences = getSharedPreferences(C.config.PREFERENCES_NAME, MODE_PRIVATE);

        String payload = String.format("%s|%s|%s|%s|%.06f|%.06f|%.04f\n",
                DateTimeFormat.forPattern("yyyyMMddHHmmss").print(new DateTime()),
                sharedPreferences.getString(C.config.USERNAME, ""),
                Base64.encodeToString(sharedPreferences.getString(C.config.DESCRIPTION, "").getBytes(), Base64.URL_SAFE | Base64.NO_WRAP),
                type,
                location.getLatitude(),
                location.getLongitude(),
                location.getAccuracy());

        socket.send(payload);
    }

    private void sendIntent(Location location, String type) {
        PositionData positionData = new PositionData();

        positionData.setLatitude(location.getLatitude());
        positionData.setLongitude(location.getLongitude());
        positionData.setAccuracy(location.getAccuracy());
        positionData.setType(type);
        positionData.setCountNetwork(countPositionsNetwork);
        positionData.setCountGps(countPositionsGps);
        positionData.setCountSent(countPositionsSent);
        positionData.setQueueSize(0);

        Intent intent = new Intent(C.intent.UPDATE_LOCATION);
        intent.putExtra("data", positionData);
        getApplicationContext().sendBroadcast(intent);
    }
}
