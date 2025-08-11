package fog.review_service;

import fog.review_service.dto.ReviewRequestDto;
import fog.review_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ReviewService reviewService;

    @Override
    public void run(String... args) throws Exception {
        log.info("예시 데이터 생성 시작...");
        
        // 예시 리뷰 데이터 생성
        createSampleReviews();
        
        log.info("예시 데이터 생성 완료!");
    }

    private void createSampleReviews() {
        try {
            // STORE001의 리뷰들
            reviewService.createReview("STORE001", "USER001", 
                new ReviewRequestDto("STORE001", "맛있는 음식이에요! 서비스도 친절하고 분위기도 좋습니다.", 5));
            
            reviewService.createReview("STORE001", "USER002", 
                new ReviewRequestDto("STORE001", "가격 대비 괜찮은 편이에요. 하지만 조금 시끄러워요.", 3));
            
            reviewService.createReview("STORE001", "USER003", 
                new ReviewRequestDto("STORE001", "완벽한 맛집입니다! 꼭 다시 방문하고 싶어요.", 5));

            // STORE002의 리뷰들
            reviewService.createReview("STORE002", "USER001", 
                new ReviewRequestDto("STORE002", "음식은 맛있는데 대기가 너무 길어요.", 4));
            
            reviewService.createReview("STORE002", "USER004", 
                new ReviewRequestDto("STORE002", "깔끔하고 맛있어요. 추천합니다!", 5));

            // STORE003의 리뷰들
            reviewService.createReview("STORE003", "USER002", 
                new ReviewRequestDto("STORE003", "기대했던 것보다는 별로예요. 맛이 좀 아쉬워요.", 2));
            
            reviewService.createReview("STORE003", "USER005", 
                new ReviewRequestDto("STORE003", "분위기가 좋고 음식도 맛있어요. 데이트하기 좋은 곳이에요.", 4));

            // STORE004의 리뷰들
            reviewService.createReview("STORE004", "USER001", 
                new ReviewRequestDto("STORE004", "가족들이랑 가기 좋은 곳이에요. 아이들 메뉴도 있어요.", 4));
            
            reviewService.createReview("STORE004", "USER003", 
                new ReviewRequestDto("STORE004", "음식이 신선하고 맛있어요. 특히 해산물이 최고!", 5));

            // STORE005의 리뷰들
            reviewService.createReview("STORE005", "USER004", 
                new ReviewRequestDto("STORE005", "분위기가 독특하고 음식도 특별해요. 한 번쯤 가볼 만해요.", 3));

        } catch (Exception e) {
            log.warn("일부 예시 데이터 생성 중 오류 발생: {}", e.getMessage());
        }
    }
}
