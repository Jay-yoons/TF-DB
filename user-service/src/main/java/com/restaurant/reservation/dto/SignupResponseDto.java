package com.restaurant.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class SignupResponseDto {
    private String userId;
    private String userName;
    private String phoneNumber;
    private String userLocation;
}
