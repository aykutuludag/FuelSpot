package org.uusoftware.fuelify.model;

public class CommentItem {

    private int ID;
    private String comment;
    private String time;
    private String username;
    private String profile_pic;
    private int rate;

    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public int getRating() {
        return rate;
    }

    public void setRating(int rate) {
        this.rate = rate;
    }
}