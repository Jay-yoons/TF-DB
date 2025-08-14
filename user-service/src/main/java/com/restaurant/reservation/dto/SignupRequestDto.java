package com.restaurant.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SignupRequestDto {
    private String userId;
    private String userName;
    private String phoneNumber;
    private String userLocation;
}
