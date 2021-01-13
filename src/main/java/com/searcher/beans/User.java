package com.searcher.beans;

import javax.validation.constraints.NotBlank;

import org.bson.BsonObjectId;
import org.bson.BsonValue;

public class User {
    private String appId;
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    private String plan;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String bsonValue) {
        this.appId = bsonValue;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    @Override
    public String toString() {
        return "User [appId=" + appId + ", email=" + email + ", password=" + password + ", plan=" + plan + "]";
    }
    
}