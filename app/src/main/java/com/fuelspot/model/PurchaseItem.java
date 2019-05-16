package com.fuelspot.model;

public class PurchaseItem {

    private int ID;
    private String plateNo;
    private int kilometer;
    private int stationID;
    private String stationName;
    private String stationIcon;
    private String stationLocation;
    private int fuelType;
    private float fuelPrice;
    private float fuelLiter;
    private float fuelTax;
    private float subTotal;
    private int fuelType2;
    private float fuelPrice2;
    private float fuelLiter2;
    private float fuelTax2;
    private float subTotal2;
    private float totalPrice;
    private String billPhoto;
    private float bonus;
    private int isVerified;
    private String purchaseTime;

    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public String getPlateNo() {
        return plateNo;
    }

    public void setPlateNo(String plateNo) {
        this.plateNo = plateNo;
    }

    public int getKilometer() {
        return kilometer;
    }

    public void setKilometer(int kilometer) {
        this.kilometer = kilometer;
    }

    public int getStationID() {
        return stationID;
    }

    public void setStationID(int stationID) {
        this.stationID = stationID;
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

    public int getFuelType() {
        return fuelType;
    }

    public void setFuelType(int fuelType) {
        this.fuelType = fuelType;
    }

    public float getFuelPrice() {
        return fuelPrice;
    }

    public void setFuelPrice(float fuelPrice) {
        this.fuelPrice = fuelPrice;
    }

    public float getFuelLiter() {
        return fuelLiter;
    }

    public void setFuelLiter(float fuelLiter) {
        this.fuelLiter = fuelLiter;
    }

    public float getFuelTax() {
        return fuelTax;
    }

    public void setFuelTax(float fuelTax) {
        this.fuelTax = fuelTax;
    }

    public float getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(float subTotal) {
        this.subTotal = subTotal;
    }

    public int getFuelType2() {
        return fuelType2;
    }

    public void setFuelType2(int fuelType2) {
        this.fuelType2 = fuelType2;
    }

    public float getFuelPrice2() {
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

    public float getFuelTax2() {
        return fuelTax2;
    }

    public void setFuelTax2(float fuelTax2) {
        this.fuelTax2 = fuelTax2;
    }

    public float getSubTotal2() {
        return subTotal2;
    }

    public void setSubTotal2(float subTotal2) {
        this.subTotal2 = subTotal2;
    }

    public float getTotalPrice() {
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

    public float getBonus() {
        return bonus;
    }

    public void setBonus(float bonus) {
        this.bonus = bonus;
    }

    public int getIsVerified() {
        return isVerified;
    }

    public void setIsVerified(int isVerified) {
        this.isVerified = isVerified;
    }

    public String getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(String purchaseTime) {
        this.purchaseTime = purchaseTime;
    }
}