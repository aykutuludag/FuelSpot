package com.fuelspot.model;

public class PurchaseItem {

    private int ID;
    private String stationName;
    private String stationIcon;
    private String stationLocation;
    private String purchaseTime;
    private int fuelType1;
    private float fuelPrice1;
    private float fuelLiter1;
    private int fuelType2;
    private float fuelPrice2;
    private float fuelLiter2;
    private float totalPrice;
    private String billPhoto;

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

    public String getStationIcon() {
        return stationIcon;
    }

    public void setStationIcon(String stationIcon) {
        this.stationIcon = stationIcon;
    }

    public String getStationLocation() {
        return stationLocation;
    }

    public void setStationLocation(String stationLocation) {
        this.stationLocation = stationLocation;
    }

    public String getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(String purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public int getFuelType() {
        return fuelType1;
    }

    public void setFuelType(int fuelType1) {
        this.fuelType1 = fuelType1;
    }

    public float getFuelPrice() {
        return fuelPrice1;
    }

    public void setFuelPrice(float fuelPrice1) {
        this.fuelPrice1 = fuelPrice1;
    }

    public float getFuelLiter() {
        return fuelLiter1;
    }

    public void setFuelLiter(float fuelLiter1) {
        this.fuelLiter1 = fuelLiter1;
    }

    public int getFuelType2() {
        return fuelType2;
    }

    public void setFuelType2(int fuelType2) {
        this.fuelType2 = fuelType2;
    }

    public double getFuelPrice2() {
        return fuelPrice2;
    }

    public void setFuelPrice2(float fuelPrice2) {
        this.fuelPrice2 = fuelPrice2;
    }

    public float getFuelLiter2() {
        return fuelLiter2;
    }

    public void setFuelLiter2(float fuelLiter2) {
        this.fuelLiter2 = fuelLiter2;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(float totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getBillPhoto() {
        return billPhoto;
    }

    public void setBillPhoto(String billPhoto) {
        this.billPhoto = billPhoto;
    }
}