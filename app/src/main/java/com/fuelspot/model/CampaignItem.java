package com.fuelspot.model;

public class CampaignItem {

    private int ID;
    private int stationID;
    private String campaignName;
    private String campaignDesc;
    private String campaignPhoto;
    private String campaignStart;
    private String campaignEnd;
    // Only for global campaigns
    private String companyName;
    // Only for global campaigns

    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public int getStationID() {
        return stationID;
    }

    public void setStationID(int stationID) {
        this.stationID = stationID;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getCampaignDesc() {
        return campaignDesc;
    }

    public void setCampaignDesc(String campaignDesc) {
        this.campaignDesc = campaignDesc;
    }

    public String getCampaignPhoto() {
        return campaignPhoto;
    }

    public void setCampaignPhoto(String campaignPhoto) {
        this.campaignPhoto = campaignPhoto;
    }

    public String getCampaignStart() {
        return campaignStart;
    }

    public void setCampaignStart(String campaignStart) {
        this.campaignStart = campaignStart;
    }

    public String getCampaignEnd() {
        return campaignEnd;
    }

    public void setCampaignEnd(String campaignEnd) {
        this.campaignEnd = campaignEnd;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}