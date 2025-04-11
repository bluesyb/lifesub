package com.unicorn.lifesub.recommend.test.unit.domain;

import com.unicorn.lifesub.recommend.domain.RecommendedCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RecommendedCategory 도메인 모델 단위 테스트
 * 객체 생성 및 속성 검증을 수행합니다.
 */
class RecommendedCategoryUnitTest {

    /**
     * 객체 생성 테스트
     * 모든 속성이 올바르게 설정되었는지 검증합니다.
     */
    @Test
    @DisplayName("givenCategoryInfo_whenCreateObject_thenPropertiesCorrectlySet")
    void givenCategoryInfo_whenCreateObject_thenPropertiesCorrectlySet() {
        // Given
        String spendingCategory = "COSMETICS";
        String recommendCategory = "BEAUTY";
        LocalDate baseDate = LocalDate.now();

        // When
        RecommendedCategory category = RecommendedCategory.builder()
                .spendingCategory(spendingCategory)
                .recommendCategory(recommendCategory)
                .baseDate(baseDate)
                .build();

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getSpendingCategory()).isEqualTo(spendingCategory);
        assertThat(category.getRecommendCategory()).isEqualTo(recommendCategory);
        assertThat(category.getBaseDate()).isEqualTo(baseDate);
    }

    /**
     * null 값 검증 테스트
     * baseDate가 null이라도 객체 생성이 가능한지 검증합니다.
     */
    @Test
    @DisplayName("givenNullBaseDate_whenCreateObject_thenAcceptNull")
    void givenNullBaseDate_whenCreateObject_thenAcceptNull() {
        // Given
        String spendingCategory = "EDUCATION";
        String recommendCategory = "EDU";

        // When
        RecommendedCategory category = RecommendedCategory.builder()
                .spendingCategory(spendingCategory)
                .recommendCategory(recommendCategory)
                .baseDate(null)
                .build();

        // Then
        assertThat(category).isNotNull();
        assertThat(category.getSpendingCategory()).isEqualTo(spendingCategory);
        assertThat(category.getRecommendCategory()).isEqualTo(recommendCategory);
        assertThat(category.getBaseDate()).isNull();
    }
}