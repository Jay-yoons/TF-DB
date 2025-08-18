package com.restaurant.reservation.dto;

public class SignupResponseDto {
    private String userId;
    private String userName;
    private String phoneNumber;
    private String userLocation;
    
    public SignupResponseDto() {}
    
    public SignupResponseDto(String userId, String userName, String phoneNumber, String userLocation) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.userLocation = userLocation;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getUserLocation() {
        return userLocation;
    }
    
    public void setUserLocation(String userLocation) {
        this.userLocation = userLocation;
    }
}
