package com.fuelspot.model;

public class PurchaseItem {

    private int ID;
    private String stationName;
    private String stationIcon;
    private String stationLocation;
    private String purchaseTime;
    private String fuelType1;
    private double fuelPrice1;
    private double fuelLiter1;
    private String fuelType2;
    private double fuelPrice2;
    private double fuelLiter2;
    private double totalPrice;
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

    public String getFuelType() {
        return fuelType1;
    }

    public void setFuelType(String fuelType1) {
        this.fuelType1 = fuelType1;
    }

    public double getFuelPrice() {
        return fuelPrice1;
    }

    public void setFuelPrice(double fuelPrice1) {
        this.fuelPrice1 = fuelPrice1;
    }

    public double getFuelLiter() {
        return fuelLiter1;
    }

    public void setFuelLiter(double fuelLiter1) {
        this.fuelLiter1 = fuelLiter1;
    }

    public String getFuelType2() {
        return fuelType2;
    }

    public void setFuelType2(String fuelType2) {
        this.fuelType2 = fuelType2;
    }

    public double getFuelPrice2() {
        return fuelPrice2;
    }

    public void setFuelPrice2(double fuelPrice2) {
        this.fuelPrice2 = fuelPrice2;
    }

    public double getFuelLiter2() {
        return fuelLiter2;
    }

    public void setFuelLiter2(Double fuelLiter2) {
        this.fuelLiter2 = fuelLiter2;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getBillPhoto() {
        return billPhoto;
    }

    public void setBillPhoto(String billPhoto) {
        this.billPhoto = billPhoto;
    }
}