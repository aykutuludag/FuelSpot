package com.fuelspot.model;

public class VehicleItem {

    private int ID;
    private String vehicleBrand;
    private String vehicleModel;
    private int vehicleFuelPri;
    private int vehicleFuelSec;
    private int vehicleKilometer;
    private String vehiclePhoto;
    private String vehiclePlateNo;
    private float vehicleConsumption;
    private int vehicleEmission;

    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public int getVehicleFuelPri() {
        return vehicleFuelPri;
    }

    public void setVehicleFuelPri(int vehicleFuelPri) {
        this.vehicleFuelPri = vehicleFuelPri;
    }

    public int getVehicleFuelSec() {
        return vehicleFuelSec;
    }

    public void setVehicleFuelSec(int vehicleFuelSec) {
        this.vehicleFuelSec = vehicleFuelSec;
    }

    public int getVehicleKilometer() {
        return vehicleKilometer;
    }

    public void setVehicleKilometer(int vehicleKilometer) {
        this.vehicleKilometer = vehicleKilometer;
    }

    public String getVehiclePhoto() {
        return vehiclePhoto;
    }

    public void setVehiclePhoto(String vehiclePhoto) {
        this.vehiclePhoto = vehiclePhoto;
    }

    public String getVehiclePlateNo() {
        return vehiclePlateNo;
    }

    public void setVehiclePlateNo(String vehiclePlateNo) {
        this.vehiclePlateNo = vehiclePlateNo;
    }

    public float getVehicleConsumption() {
        return vehicleConsumption;
    }

    public void setVehicleConsumption(float vehicleConsumption) {
        this.vehicleConsumption = vehicleConsumption;
    }

    public int getVehicleEmission() {
        return vehicleEmission;
    }

    public void setVehicleEmission(int vehicleEmission) {
        this.vehicleEmission = vehicleEmission;
    }
}