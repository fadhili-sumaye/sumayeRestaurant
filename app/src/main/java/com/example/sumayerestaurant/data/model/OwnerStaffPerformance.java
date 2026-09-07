package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class OwnerStaffPerformance {
    @SerializedName("waiters") private List<OwnerStaffSalesRow> waiters;
    @SerializedName("cashiers") private List<OwnerStaffPaymentRow> cashiers;
    @SerializedName("kitchen") private List<OwnerStaffCountRow> kitchen;
    @SerializedName("riders") private List<OwnerRiderRow> riders;
    @SerializedName("kitchenOrdersTotal") private long kitchenOrdersTotal;
    @SerializedName("deliveriesCompleted") private long deliveriesCompleted;
    public List<OwnerStaffSalesRow> getWaiters(){return waiters;} public void setWaiters(List<OwnerStaffSalesRow> v){waiters=v;}
    public List<OwnerStaffPaymentRow> getCashiers(){return cashiers;} public void setCashiers(List<OwnerStaffPaymentRow> v){cashiers=v;}
    public List<OwnerStaffCountRow> getKitchen(){return kitchen;} public void setKitchen(List<OwnerStaffCountRow> v){kitchen=v;}
    public List<OwnerRiderRow> getRiders(){return riders;} public void setRiders(List<OwnerRiderRow> v){riders=v;}
    public long getKitchenOrdersTotal(){return kitchenOrdersTotal;} public void setKitchenOrdersTotal(long v){kitchenOrdersTotal=v;}
    public long getDeliveriesCompleted(){return deliveriesCompleted;} public void setDeliveriesCompleted(long v){deliveriesCompleted=v;}
}