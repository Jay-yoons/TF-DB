package fog.review_service.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    private String storeId;
    private String comment;
    private Integer score;
}