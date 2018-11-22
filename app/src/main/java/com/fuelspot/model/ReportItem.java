package com.fuelspot.model;

public class ReportItem {

    private int ID;
    private String username;
    private int stationID;
    private String reportType;
    private String reportMessage;
    private String reportPhoto;
    private String prices;
    private int isReviewed;
    private float reward;
    private String reportTime;

    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getStationID() {
        return stationID;
    }

    public void setStationID(int stationID) {
        this.stationID = stationID;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getReportMessage() {
        return reportMessage;
    }

    public void setReportMessage(String reportMessage) {
        this.reportMessage = reportMessage;
    }

    public String getReportPhoto() {
        return reportPhoto;
    }

    public void setReportPhoto(String reportPhoto) {
        this.reportPhoto = reportPhoto;
    }

    public String getPrices() {
        return prices;
    }

    public void setPrices(String prices) {
        this.prices = prices;
    }

    public int getIsReviewed() {
        return isReviewed;
    }

    public void setIsReviewed(int isReviewed) {
        this.isReviewed = isReviewed;
    }

    public float getReward() {
        return reward;
    }

    public void setReward(float reward) {
        this.reward = reward;
    }

    public String getReportTime() {
        return reportTime;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }
}
