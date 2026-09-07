package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
public class OwnerStaffCountRow {
    @SerializedName("username") private String username;
    @SerializedName("count") private long count;
    public String getUsername(){return username;} public void setUsername(String v){username=v;}
    public long getCount(){return count;} public void setCount(long v){count=v;}
}