package com.licenta.tracker.app;

public class RouteDetails {

    private String activityName;
    private String totalDistance;
    private String totaltime;

    private String trackID;

    public RouteDetails(String activityName, String totalDistance, String totaltime, String trackID) {
        this.activityName = activityName;
        this.totalDistance = totalDistance;
        this.totaltime = totaltime;
        this.trackID = trackID;
    }

    public void setTrackID(String trackID) {
        this.trackID = trackID;
    }
    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public void setTotalDistance(String totalDistance) {
        this.totalDistance = totalDistance;
    }

    public void setTotaltime(String totaltime) {
        this.totaltime = totaltime;
    }

    public String getActivityName() { return activityName;}

    public String getTotalDistance() {
        return totalDistance;
    }

    public String getTotaltime() {
        return totaltime;
    }

    public String getTrackID() {
        return trackID;
    }
}
