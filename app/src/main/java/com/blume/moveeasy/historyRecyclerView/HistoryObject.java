package com.blume.moveeasy.historyRecyclerView;

public class HistoryObject {

    private String rideId;
    private String time;

    public HistoryObject(String rideID, String time){
        this.rideId = rideID;
        this.time = time;

    }

    public String getRideId(){
        return rideId;
    }
    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getTime(){return time;}
    public void setTime(String time) {
        this.time = time;
    }
}
