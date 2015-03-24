package org.thehellnet.mobile.navi.service;

import java.io.Serializable;

/**
 * Created by sardylan on 23/03/15.
 */
public class PositionData implements Serializable {
    private double latitude;
    private double longitude;
    private float accuracy;
    private String type;
    private int countNetwork;
    private int countGps;
    private int countSent;
    private int queueSize;

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getCountSent() {
        return countSent;
    }

    public void setCountSent(int countSent) {
        this.countSent = countSent;
    }

    public int getCountGps() {
        return countGps;
    }

    public void setCountGps(int countGps) {
        this.countGps = countGps;
    }

    public int getCountNetwork() {
        return countNetwork;
    }

    public void setCountNetwork(int countNetwork) {
        this.countNetwork = countNetwork;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
