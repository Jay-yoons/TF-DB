package com.restaurant.reservation.dto.favorite;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class FavoriteRequestDto {
    private String userId;
    private String storeId;
}
