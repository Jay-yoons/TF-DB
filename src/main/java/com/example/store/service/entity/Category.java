package com.example.store.service.entity;

/**
 * 가게 카테고리 열거형.
 * code: DB/요청에서 사용하는 정수 코드
 * koreanName: 응답/화면에 표시할 한글명
 */
public enum Category {
    KOREAN(1, "한식"),
    JAPANESE(2, "일식"),
    WESTERN(3, "양식"),
    CHINESE(4, "중식"),
    CAFE(5, "카페");

    private final int code;
    private final String koreanName;

    Category(int code, String koreanName) {
        this.code = code;
        this.koreanName = koreanName;
    }

    public int getCode() {
        return code;
    }

    public String getKoreanName() {
        return koreanName;
    }

    public static Category fromCode(Integer code) {
        if (code == null) return null;
        for (Category c : values()) {
            if (c.code == code) return c;
        }
        return null;
    }
}


