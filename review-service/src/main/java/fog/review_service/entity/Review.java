package fog.review_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "REVIEW", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"storeid", "userid"})
})

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private Long reviewId;

    @Column(name = "STOREID")
    private String storeId;

    @Column(name = "USERID")
    private String userId;

    @Column(length = 50)
    private String comment;

    @Column(nullable = false)
    private Integer score;
}
