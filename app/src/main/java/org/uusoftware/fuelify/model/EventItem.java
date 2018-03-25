package org.uusoftware.fuelify.model;

public class EventItem {

    private int ID;
    private String title;
    private String description;
    private long startTime;
    private long endTime;
    private String location;
    private int owner;
    private int background;
    private int isMailActive;
    private int isSMSActive;
    private int isMessengerActive;
    private int isWhatsappActive;

    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getOwner() {
        return owner;
    }

    public void setOwner(int owner) {
        this.owner = owner;
    }

    public int getBackground() {
        return background;
    }

    public void setBackground(int background) {
        this.background = background;
    }

    public int getIsMailActive() {
        return isMailActive;
    }

    public void setIsMailActive(int isMailActive) {
        this.isMailActive = isMailActive;
    }

    public int getIsSMSActive() {
        return isSMSActive;
    }

    public void setIsSMSActive(int isSMSActive) {
        this.isSMSActive = isSMSActive;
    }

    public int getIsMessengerActive() {
        return isMessengerActive;
    }

    public void setIsMessengerActive(int isMessengerActive) {
        this.isMessengerActive = isMessengerActive;
    }

    public int getIsWhatsappActive() {
        return isWhatsappActive;
    }

    public void setIsWhatsappActive(int isWhatsappActivee) {
        this.isWhatsappActive = isWhatsappActivee;
    }
}