package myron.com.crimereporter;

import android.util.Log;

public class MapPointer {

    private String id;
    private Double latitude;
    private Double longitude;
    private String reviews;
    private Double rating;
    private Integer votes;

    public MapPointer() {}

    public MapPointer(String id, Double latitude, Double longitude, String reviews, Double rating, Integer Vote) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.reviews = reviews;
        this.rating = rating;
        this.votes = Vote;
    }

    public String getID() {
        return this.id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setReviews (String reviews){
        this.reviews = reviews;
    }

    public String getReviews(){
        return reviews;
    }

    public void setRating (Double rating){
        this.rating = rating;
    }

    public Double getRating(){
        return rating;
    }

    public void setVotes (Integer votes){
        this.votes = votes;
    }

    public Integer getVotes(){
        return votes;
    }

}
