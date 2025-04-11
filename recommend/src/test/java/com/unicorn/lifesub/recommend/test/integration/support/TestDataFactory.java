package com.unicorn.lifesub.recommend.test.integration.support;

import com.unicorn.lifesub.recommend.domain.SpendingCategory;
import com.unicorn.lifesub.recommend.dto.RecommendCategoryDTO;
import com.unicorn.lifesub.recommend.repository.entity.RecommendedCategoryEntity;
import com.unicorn.lifesub.recommend.repository.entity.SpendingEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 테스트용 데이터 팩토리 클래스
 * 통합 테스트에서 사용할 테스트 데이터를 생성하는 메서드들을 제공합니다.
 */
public class TestDataFactory {

    /**
     * 테스트용 추천 카테고리 DTO를 생성합니다.
     *
     * @param categoryName 추천 카테고리명
     * @param spendingCategory 지출 카테고리명
     * @param totalSpending 총 지출 금액
     * @return 생성된 RecommendCategoryDTO 객체
     */
    public static RecommendCategoryDTO createRecommendCategoryDTO(
            String categoryName, String spendingCategory, Long totalSpending) {
        return RecommendCategoryDTO.builder()
                .categoryName(categoryName)
                .spendingCategory(spendingCategory)
                .baseDate(LocalDate.now())
                .totalSpending(totalSpending)
                .build();
    }

    /**
     * 테스트용 지출 카테고리 객체를 생성합니다.
     *
     * @param category 카테고리명
     * @param totalAmount 총 지출 금액
     * @return 생성된 SpendingCategory 객체
     */
    public static SpendingCategory createSpendingCategory(String category, Long totalAmount) {
        return SpendingCategory.builder()
                .category(category)
                .totalAmount(totalAmount)
                .build();
    }

    /**
     * 테스트용 추천 카테고리 엔티티를 생성합니다.
     *
     * @param id 엔티티 ID
     * @param spendingCategory 지출 카테고리명
     * @param recommendCategory 추천 카테고리명
     * @return 생성된 RecommendedCategoryEntity 객체
     */
    public static RecommendedCategoryEntity createRecommendedCategoryEntity(
            Long id, String spendingCategory, String recommendCategory) {
        return RecommendedCategoryEntity.builder()
                .id(id)
                .spendingCategory(spendingCategory)
                .recommendCategory(recommendCategory)
                .build();
    }

    /**
     * 테스트용 지출 내역 엔티티를 생성합니다.
     *
     * @param id 엔티티 ID
     * @param userId 사용자 ID
     * @param category 지출 카테고리
     * @param amount 지출 금액
     * @param daysAgo 현재로부터 몇 일 전의 지출인지
     * @return 생성된 SpendingEntity 객체
     */
    public static SpendingEntity createSpendingEntity(
            Long id, String userId, String category, Long amount, int daysAgo) {
        return SpendingEntity.builder()
                .id(id)
                .userId(userId)
                .category(category)
                .amount(amount)
                .spendingDate(LocalDate.now().minusDays(daysAgo))
                .build();
    }

    /**
     * 특정 사용자의 지출 내역 엔티티 목록을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param category 지출 카테고리
     * @param count 생성할 지출 내역 개수
     * @return 생성된 SpendingEntity 목록
     */
    public static List<SpendingEntity> createSpendingEntities(
            String userId, String category, int count) {
        List<SpendingEntity> spendings = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            spendings.add(SpendingEntity.builder()
                    .id((long) (i + 1))
                    .userId(userId)
                    .category(category)
                    .amount(50000L + (i * 10000L))
                    .spendingDate(LocalDate.now().minusDays(i * 3))
                    .build());
        }
        return spendings;
    }
}