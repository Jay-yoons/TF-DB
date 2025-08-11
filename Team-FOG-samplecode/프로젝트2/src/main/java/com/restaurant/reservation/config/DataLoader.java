package com.restaurant.reservation.config;

import com.restaurant.reservation.entity.Store;
import com.restaurant.reservation.repository.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final StoreRepository storeRepository;

    public DataLoader(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("샘플 데이터 로딩 시작");
        
        // 기존 데이터 삭제
        storeRepository.deleteAll();
        
        // 샘플 가게 데이터 생성
        Store store1 = new Store();
        store1.setStoreId("STORE001");
        store1.setStoreName("맛있는 한식당");
        store1.setStoreLocation("서울시 강남구");
        store1.setSeatNum(10);
        store1.setCategoryCode("KOREAN");
        store1.setServiceTime("11:00-22:00");
        storeRepository.save(store1);
        
        Store store2 = new Store();
        store2.setStoreId("STORE002");
        store2.setStoreName("이탈리안 레스토랑");
        store2.setStoreLocation("서울시 서초구");
        store2.setSeatNum(10);
        store2.setCategoryCode("ITALIAN");
        store2.setServiceTime("12:00-21:00");
        storeRepository.save(store2);
        
        Store store3 = new Store();
        store3.setStoreId("STORE003");
        store3.setStoreName("일식 스시바");
        store3.setStoreLocation("서울시 마포구");
        store3.setSeatNum(10);
        store3.setCategoryCode("JAPANESE");
        store3.setServiceTime("11:30-22:30");
        storeRepository.save(store3);
        
        Store store4 = new Store();
        store4.setStoreId("STORE004");
        store4.setStoreName("중국집");
        store4.setStoreLocation("서울시 종로구");
        store4.setSeatNum(10);
        store4.setCategoryCode("CHINESE");
        store4.setServiceTime("10:30-21:30");
        storeRepository.save(store4);
        
        Store store5 = new Store();
        store5.setStoreId("STORE005");
        store5.setStoreName("스테이크하우스");
        store5.setStoreLocation("서울시 용산구");
        store5.setSeatNum(10);
        store5.setCategoryCode("WESTERN");
        store5.setServiceTime("17:00-23:00");
        storeRepository.save(store5);
        
        logger.info("샘플 데이터 로딩 완료: {}개 가게", storeRepository.count());
    }
} 