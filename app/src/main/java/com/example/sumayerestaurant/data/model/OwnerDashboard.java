package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class OwnerDashboard {
    @SerializedName("summary") private OwnerSummary summary;
    @SerializedName("salesSeries") private List<OwnerSeriesPoint> salesSeries;
    @SerializedName("payments") private List<OwnerPaymentMethodStat> payments;
    @SerializedName("expenses") private OwnerExpenseStats expenses;
    @SerializedName("orders") private OwnerOrderStats orders;
    @SerializedName("bestSellers") private List<OwnerBestSeller> bestSellers;
    @SerializedName("inventory") private OwnerInventorySummary inventory;
    @SerializedName("staff") private OwnerStaffPerformance staff;
    @SerializedName("monthly") private OwnerMonthlyPerformance monthly;
    @SerializedName("periodLabel") private String periodLabel;
    @SerializedName("generatedAt") private String generatedAt;
    public OwnerSummary getSummary(){return summary;} public void setSummary(OwnerSummary v){summary=v;}
    public List<OwnerSeriesPoint> getSalesSeries(){return salesSeries;} public void setSalesSeries(List<OwnerSeriesPoint> v){salesSeries=v;}
    public List<OwnerPaymentMethodStat> getPayments(){return payments;} public void setPayments(List<OwnerPaymentMethodStat> v){payments=v;}
    public OwnerExpenseStats getExpenses(){return expenses;} public void setExpenses(OwnerExpenseStats v){expenses=v;}
    public OwnerOrderStats getOrders(){return orders;} public void setOrders(OwnerOrderStats v){orders=v;}
    public List<OwnerBestSeller> getBestSellers(){return bestSellers;} public void setBestSellers(List<OwnerBestSeller> v){bestSellers=v;}
    public OwnerInventorySummary getInventory(){return inventory;} public void setInventory(OwnerInventorySummary v){inventory=v;}
    public OwnerStaffPerformance getStaff(){return staff;} public void setStaff(OwnerStaffPerformance v){staff=v;}
    public OwnerMonthlyPerformance getMonthly(){return monthly;} public void setMonthly(OwnerMonthlyPerformance v){monthly=v;}
    public String getPeriodLabel(){return periodLabel;} public void setPeriodLabel(String v){periodLabel=v;}
    public String getGeneratedAt(){return generatedAt;} public void setGeneratedAt(String v){generatedAt=v;}
}