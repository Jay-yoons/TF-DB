package com.restaurant.reservation.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequestDto {
    private String userId;
    private String userName;
    private String phoneNumber;
    private String userLocation;
    
    public SignupRequestDto() {}
    
    public SignupRequestDto(String userId, String userName, String phoneNumber, String userLocation) {
        this.userId = userId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.userLocation = userLocation;
    }
}
