package com.example.store.service;

import com.example.store.service.entity.Review;
import com.example.store.service.entity.Store;
import com.example.store.service.entity.StoreSeat;
import com.example.store.service.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import com.example.store.service.repository.StoreRepository;
import com.example.store.service.repository.StoreSeatRepository;
import org.springframework.stereotype.Component;

/**
 * (비활성화 상태) 개발 편의를 위한 초기 데이터 주입 컴포넌트.
 * - 현재는 빈 등록이 되어 있지 않아 실행되지 않는다.
 * - 필요 시 dev 프로필에서만 동작하도록 @Profile("dev")와 @Component를 부여해 사용할 수 있다.
 */

@Component
@AllArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StoreRepository storeRepository;
    private final StoreSeatRepository storeSeatRepository;
    private final ReviewRepository reviewRepository;

    @Override
    public void run(String... args) throws Exception {
        // 테스트용 가게 데이터 추가
        Store store1 = Store.builder()
                .storeId("A001")
                .storeName("스타벅스 강남점")
                .categoryCode(1)
                .storeLocation("서울 강남구")
                .seatNum(50)
                .serviceTime("07:00~22:00")
                .build();

        Store store2 = Store.builder()
                .storeId("A002")
                .storeName("맥도날드 강남점")
                .categoryCode(2)
                .storeLocation("서울 강남구")
                .seatNum(30)
                .serviceTime("06:00~24:00")
                .build();

        Store store3 = Store.builder()
                .storeId("A003")
                .storeName("올리브영 강남점")
                .categoryCode(3)
                .storeLocation("서울 강남구")
                .seatNum(10)
                .serviceTime("10:00~22:00")
                .build();

        Store savedStore1 = storeRepository.save(store1);
        Store savedStore2 = storeRepository.save(store2);
        Store savedStore3 = storeRepository.save(store3);

        // StoreSeat 초기 데이터 추가
        StoreSeat storeSeat1 = StoreSeat.builder()
                .storeId(savedStore1.getStoreId())
                .inUsingSeat(10)
                .build();

        StoreSeat storeSeat2 = StoreSeat.builder()
                .storeId(savedStore2.getStoreId())
                .inUsingSeat(5)
                .build();

        StoreSeat storeSeat3 = StoreSeat.builder()
                .storeId(savedStore3.getStoreId())
                .inUsingSeat(0)
                .build();

        storeSeatRepository.save(storeSeat1);
        storeSeatRepository.save(storeSeat2);
        storeSeatRepository.save(storeSeat3);

        // Review 초기 데이터 추가
        Review review1 = Review.builder()
                .storeId("A001")
                .userId("user02")
                .comment("음식이 정말 맛있고 분위기가 좋았습니다!")
                .score(5)
                .build();

        Review review2 = Review.builder()
                .storeId("A002")
                .userId("user02")
                .comment("깔끔하고 서비스가 친절해서 좋았어요.")
                .score(4)
                .build();

        Review review3 = Review.builder()
                .storeId("A003")
                .userId("user02")
                .comment("좌석이 편해서 작업하기 좋았습니다.")
                .score(3)
                .build();

        reviewRepository.save(review1);
        reviewRepository.save(review2);
        reviewRepository.save(review3);

        Review review4 = Review.builder()
                .storeId("A003")
                .userId("user01")
                .comment("user01의 A003 가게애 대한 2점 리뷰")
                .score(2)
                .build();

        reviewRepository.save(review4);

        System.out.println("테스트 데이터가 초기화되었습니다!");
    }
}
