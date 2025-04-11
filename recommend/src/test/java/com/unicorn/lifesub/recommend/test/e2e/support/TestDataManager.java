package com.unicorn.lifesub.recommend.test.e2e.support;

import com.unicorn.lifesub.recommend.repository.entity.RecommendedCategoryEntity;
import com.unicorn.lifesub.recommend.repository.entity.SpendingEntity;
import com.unicorn.lifesub.recommend.repository.jpa.RecommendRepository;
import com.unicorn.lifesub.recommend.repository.jpa.SpendingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * E2E 테스트를 위한 테스트 데이터 관리 클래스입니다.
 * 테스트에 필요한 구독 추천 및 지출 데이터를 초기화합니다.
 */
@Component
@RequiredArgsConstructor
public class TestDataManager {
    private final RecommendRepository recommendRepository;
    private final SpendingRepository spendingRepository;

    @Value("${test.user.id}")
    private String TEST_USER_ID;

    @Value("${test.user.spending.category}")
    private String TEST_SPENDING_CATEGORY;

    @Value("${test.user.spending.amount}")
    private Long TEST_SPENDING_AMOUNT;

    /**
     * 테스트 데이터를 설정합니다.
     * 기존 데이터를 모두 삭제하고 테스트에 필요한 데이터를 생성합니다.
     */
    @Transactional
    public void setupTestData() {
        // 기존 데이터 삭제
        spendingRepository.deleteAll();
        recommendRepository.deleteAll();

        // 카테고리 매핑 데이터 생성
        createCategoryMappings();

        // 테스트 사용자 지출 데이터 생성
        createTestUserSpendingData();
    }

    /**
     * 테스트용 빈 사용자를 생성합니다.
     * 이 사용자는 지출 데이터가 없어 추천을 받을 수 없습니다.
     */
    @Transactional
    public void setupEmptyUserData() {
        // 테스트를 위해 특정 사용자의 지출 데이터만 삭제
        spendingRepository.deleteAll(spendingRepository.findSpendingsByUserIdAndDateAfter(
                TEST_USER_ID, LocalDate.now().minusMonths(1)));
    }

    /**
     * 지출 카테고리와 추천 구독 카테고리 간의 매핑 데이터를 생성합니다.
     */
    private void createCategoryMappings() {
        List<RecommendedCategoryEntity> categories = Arrays.asList(
                RecommendedCategoryEntity.builder()
                        .spendingCategory("COSMETICS")
                        .recommendCategory("BEAUTY")
                        .build(),
                RecommendedCategoryEntity.builder()
                        .spendingCategory("ENTERTAINMENT")
                        .recommendCategory("OTT")
                        .build(),
                RecommendedCategoryEntity.builder()
                        .spendingCategory("EDUCATION")
                        .recommendCategory("EDU")
                        .build(),
                RecommendedCategoryEntity.builder()
                        .spendingCategory("RESTAURANT")
                        .recommendCategory("FOOD")
                        .build(),
                RecommendedCategoryEntity.builder()
                        .spendingCategory("MUSIC")
                        .recommendCategory("MUSIC")
                        .build(),
                RecommendedCategoryEntity.builder()
                        .spendingCategory("DAILY")
                        .recommendCategory("LIFE")
                        .build()
        );

        recommendRepository.saveAll(categories);
    }

    /**
     * 테스트 사용자의 지출 데이터를 생성합니다.
     */
    private void createTestUserSpendingData() {
        List<SpendingEntity> spendings = new ArrayList<>();

        // 테스트용 지출 데이터 생성 - 현재 날짜 기준 최근 30일 내의 데이터
        SpendingEntity spending = SpendingEntity.builder()
                .userId(TEST_USER_ID)
                .category(TEST_SPENDING_CATEGORY)
                .amount(TEST_SPENDING_AMOUNT)
                .spendingDate(LocalDate.now().minusDays(5))
                .build();

        spendings.add(spending);

        // 추가 더미 데이터 - 다른 카테고리 지출 (금액은 더 적게 설정)
        SpendingEntity additionalSpending = SpendingEntity.builder()
                .userId(TEST_USER_ID)
                .category("ENTERTAINMENT")
                .amount(50000L)
                .spendingDate(LocalDate.now().minusDays(10))
                .build();

        spendings.add(additionalSpending);

        spendingRepository.saveAll(spendings);
    }
}