package com.example.store.service;

import com.example.store.service.entity.Store;
import com.example.store.service.entity.StoreSeat;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.example.store.service.repository.StoreRepository;
import com.example.store.service.repository.StoreSeatRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final StoreRepository storeRepository;
    private final StoreSeatRepository storeSeatRepository;

    public DataInitializer(StoreRepository storeRepository, StoreSeatRepository storeSeatRepository) {
        this.storeRepository = storeRepository;
        this.storeSeatRepository = storeSeatRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 테스트용 가게 데이터 추가
        Store store1 = Store.builder()
                .storeName("스타벅스 강남점")
                .categoryCode("CAFE")
                .storeLocation("서울 강남구")
                .seatNum(50)
                .serviceTime("07:00~22:00")
                .build();

        Store store2 = Store.builder()
                .storeName("맥도날드 강남점")
                .categoryCode("FASTFOOD")
                .storeLocation("서울 강남구")
                .seatNum(30)
                .serviceTime("06:00~24:00")
                .build();

        Store store3 = Store.builder()
                .storeName("올리브영 강남점")
                .categoryCode("COSMETIC")
                .storeLocation("서울 강남구")
                .seatNum(0)
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

        System.out.println("테스트 데이터가 초기화되었습니다!");
    }
}
