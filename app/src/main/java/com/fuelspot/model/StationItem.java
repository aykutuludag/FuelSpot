package com.fuelspot.model;

public class StationItem {

    private int ID;
    private String stationName;
    private String vicinity;
    private String locStation;
    private float gasolinePrice;
    private float dieselPrice;
    private float lpgPrice;
    private float electricityPrice;
    private String googleMapID;
    private String photoURL;
    private String lastUpdated;
    private float distance;

    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public String getLocation() {
        return locStation;
    }

    public void setLocation(String locStation) {
        this.locStation = locStation;
    }

    public double getGasolinePrice() {
        return gasolinePrice;
    }

    public void setGasolinePrice(float gasolinePrice) {
        this.gasolinePrice = gasolinePrice;
    }

    public float getDieselPrice() {
        return dieselPrice;
    }

    public void setDieselPrice(float dieselPrice) {
        this.dieselPrice = dieselPrice;
    }

    public float getLpgPrice() {
        return lpgPrice;
    }

    public void setLpgPrice(float lpgPrice) {
        this.lpgPrice = lpgPrice;
    }

    public double getElectricityPrice() {
        return electricityPrice;
    }

    public void setElectricityPrice(float electricityPrice) {
        this.electricityPrice = electricityPrice;
    }

    public String getGoogleMapID() {
        return googleMapID;
    }

    public void setGoogleMapID(String googleMapID) {
        this.googleMapID = googleMapID;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}