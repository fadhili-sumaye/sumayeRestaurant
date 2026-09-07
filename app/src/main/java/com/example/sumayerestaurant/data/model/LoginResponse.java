package com.example.sumayerestaurant.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class LoginResponse implements Serializable {
    @SerializedName("accessToken")
    private String accessToken;
    
    @SerializedName("tokenType")
    private String tokenType;
    
    @SerializedName("expiresIn")
    private Long expiresIn;
    
    @SerializedName("user")
    private User user;

    public LoginResponse() {}

    public LoginResponse(String accessToken, String tokenType, Long expiresIn, User user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
