package com.example.group13_hw10;

import java.io.Serializable;

public class Trip implements Serializable {
        String tripId, tripName, tripStartTime, tripCompleteTime, tripStatus, tripCreatedByName, tripCreatedByUid, tripDistance;
        Double startLocationLat, startLocationLng;


    public Trip() {
    }

    public Trip(String tripId, String tripName, String tripStartTime, String tripCompleteTime, String tripStatus, String tripCreatedByName, String tripCreatedByUid, String tripDistance, Double startLocationLat, Double startLocationLng) {
        this.tripId = tripId;
        this.tripName = tripName;
        this.tripStartTime = tripStartTime;
        this.tripCompleteTime = tripCompleteTime;
        this.tripStatus = tripStatus;
        this.tripCreatedByName = tripCreatedByName;
        this.tripCreatedByUid = tripCreatedByUid;
        this.tripDistance = tripDistance;
        this.startLocationLat = startLocationLat;
        this.startLocationLng = startLocationLng;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getTripStartTime() {
        return tripStartTime;
    }

    public void setTripStartTime(String tripStartTime) {
        this.tripStartTime = tripStartTime;
    }

    public String getTripCompleteTime() {
        return tripCompleteTime;
    }

    public void setTripCompleteTime(String tripCompleteTime) {
        this.tripCompleteTime = tripCompleteTime;
    }

    public String getTripStatus() {
        return tripStatus;
    }

    public void setTripStatus(String tripStatus) {
        this.tripStatus = tripStatus;
    }

    public String getTripCreatedByName() {
        return tripCreatedByName;
    }

    public void setTripCreatedByName(String tripCreatedByName) {
        this.tripCreatedByName = tripCreatedByName;
    }

    public String getTripCreatedByUid() {
        return tripCreatedByUid;
    }

    public void setTripCreatedByUid(String tripCreatedByUid) {
        this.tripCreatedByUid = tripCreatedByUid;
    }

    public String getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(String tripDistance) {
        this.tripDistance = tripDistance;
    }

    public Double getStartLocationLat() {
        return startLocationLat;
    }

    public void setStartLocationLat(Double startLocationLat) {
        this.startLocationLat = startLocationLat;
    }

    public Double getStartLocationLng() {
        return startLocationLng;
    }

    public void setStartLocationLng(Double startLocationLng) {
        this.startLocationLng = startLocationLng;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "tripId='" + tripId + '\'' +
                ", tripName='" + tripName + '\'' +
                ", tripStartTime='" + tripStartTime + '\'' +
                ", tripCompleteTime='" + tripCompleteTime + '\'' +
                ", tripStatus='" + tripStatus + '\'' +
                ", tripCreatedByName='" + tripCreatedByName + '\'' +
                ", tripCreatedByUid='" + tripCreatedByUid + '\'' +
                ", tripDistance='" + tripDistance + '\'' +
                ", startLocationLat=" + startLocationLat +
                ", startLocationLng=" + startLocationLng +
                '}';
    }
}
