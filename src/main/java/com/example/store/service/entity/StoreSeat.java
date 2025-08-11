package com.example.store.service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "STORE_SEAT")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreSeat {
    @Id
    @Column(name = "STORE_ID")
    private Long storeId;
    
    @Column(name = "IN_USING_SEAT")
    private int inUsingSeat;
}
