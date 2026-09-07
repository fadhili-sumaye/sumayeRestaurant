package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;
public class OwnerOrderStats {
    @SerializedName("total") private long total;
    @SerializedName("completed") private long completed;
    @SerializedName("pending") private long pending;
    @SerializedName("cancelled") private long cancelled;
    @SerializedName("byStatus") private Map<String, Long> byStatus;
    @SerializedName("busyHour") private Integer busyHour;
    @SerializedName("busyHourLabel") private String busyHourLabel;
    public long getTotal(){return total;} public void setTotal(long v){total=v;}
    public long getCompleted(){return completed;} public void setCompleted(long v){completed=v;}
    public long getPending(){return pending;} public void setPending(long v){pending=v;}
    public long getCancelled(){return cancelled;} public void setCancelled(long v){cancelled=v;}
    public Map<String, Long> getByStatus(){return byStatus;} public void setByStatus(Map<String, Long> v){byStatus=v;}
    public Integer getBusyHour(){return busyHour;} public void setBusyHour(Integer v){busyHour=v;}
    public String getBusyHourLabel(){return busyHourLabel;} public void setBusyHourLabel(String v){busyHourLabel=v;}
}