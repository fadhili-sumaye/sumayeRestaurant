package com.example.sumayerestaurant.data.model;
import com.google.gson.annotations.SerializedName;
public class Customer {@SerializedName("id") private Long id;@SerializedName("fullName") private String fullName;@SerializedName("phoneNumber") private String phoneNumber;@SerializedName("address") private String address; public Long getId(){return id;}public String getFullName(){return fullName;}public String getPhoneNumber(){return phoneNumber;}public String getAddress(){return address;}@Override public String toString(){return fullName+" — "+phoneNumber;}}
