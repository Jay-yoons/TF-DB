package com.example.store.service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * S3에 저장된 가게 이미지의 퍼블릭 URL을 구성한다.
 * 샘플 구현: 공개 버킷을 가정하고 URL만 조합한다.
 * 운영에서는 프리사인 URL 발급 또는 프록시 서빙으로 전환 권장.
 */
@Service
public class StoreImageService {

    @Value("${cloud.aws.region.static:ap-northeast-2}")
    private String region;

    @Value("${app.s3.bucket.name:}")
    private String bucketName;

    @Value("${app.s3.store-image.prefix:stores/}")
    private String keyPrefix;

    private volatile S3Client cachedClient;

    /**
     * 예시 키 규칙: stores/{storeId}.jpg
     */
    public String getImageUrl(String storeId) {
        if (bucketName == null || bucketName.isBlank()) {
            return null;
        }
        String key = (keyPrefix == null ? "" : keyPrefix) + storeId + ".jpg";
        // 공개 버킷 가정 URL
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;
    }

    /**
     * 해당 매장의 모든 이미지 URL을 반환한다. 규칙: {prefix}{storeId}/ 디렉터리 하위의 오브젝트들.
     * 공개 버킷 가정. limit로 최대 개수를 제어한다.
     */
    public List<String> listImageUrls(String storeId, int limit) {
        if (bucketName == null || bucketName.isBlank()) {
            return Collections.emptyList();
        }
        String prefix = (keyPrefix == null ? "" : keyPrefix) + storeId + "/";
        try {
            ListObjectsV2Request req = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix(prefix)
                    .maxKeys(Math.max(1, limit))
                    .build();
            ListObjectsV2Response res = getClient().listObjectsV2(req);
            List<String> urls = new ArrayList<>();
            for (S3Object obj : res.contents()) {
                String key = obj.key();
                if (key.endsWith("/")) continue; // 디렉터리 스킵
                urls.add("https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key);
                if (urls.size() >= limit) break;
            }
            // 폴더가 비어있다면 단일 파일 규칙(stores/{storeId}.jpg)도 시도
            if (urls.isEmpty()) {
                String single = getImageUrl(storeId);
                if (single != null) urls.add(single);
            }
            return urls;
        } catch (Exception e) {
            // 권한/네트워크 문제 시 안전하게 빈 리스트 반환
            return Collections.emptyList();
        }
    }

    private S3Client getClient() {
        S3Client client = cachedClient;
        if (client == null) {
            synchronized (this) {
                if (cachedClient == null) {
                    cachedClient = S3Client.builder().region(Region.of(region)).build();
                }
                client = cachedClient;
            }
        }
        return client;
    }
}


