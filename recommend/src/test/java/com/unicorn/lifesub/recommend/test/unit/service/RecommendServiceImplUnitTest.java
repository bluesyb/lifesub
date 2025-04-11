package com.unicorn.lifesub.recommend.test.unit.service;

import com.unicorn.lifesub.common.exception.BusinessException;
import com.unicorn.lifesub.common.exception.ErrorCode;
import com.unicorn.lifesub.recommend.domain.RecommendedCategory;
import com.unicorn.lifesub.recommend.domain.SpendingCategory;
import com.unicorn.lifesub.recommend.dto.RecommendCategoryDTO;
import com.unicorn.lifesub.recommend.repository.entity.RecommendedCategoryEntity;
import com.unicorn.lifesub.recommend.repository.entity.SpendingEntity;
import com.unicorn.lifesub.recommend.repository.jpa.RecommendRepository;
import com.unicorn.lifesub.recommend.repository.jpa.SpendingRepository;
import com.unicorn.lifesub.recommend.service.RecommendServiceImpl;
import com.unicorn.lifesub.recommend.service.SpendingAnalyzer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * RecommendServiceImpl 단위 테스트
 * 구독추천 서비스의 핵심 비즈니스 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class RecommendServiceImplUnitTest {

    @Mock
    private RecommendRepository recommendRepository;

    @Mock
    private SpendingRepository spendingRepository;

    @Mock
    private SpendingAnalyzer spendingAnalyzer;

    @InjectMocks
    private RecommendServiceImpl recommendService;

    private static final String TEST_USER_ID = "user01";

    /**
     * 정상 케이스: 지출 데이터가 있고 추천 카테고리 매핑이 있는 경우 성공적인 추천 검증
     */
    @Test
    @DisplayName("givenUserWithSpendingData_whenGetRecommendedCategory_thenSuccess")
    void givenUserWithSpendingData_whenGetRecommendedCategory_thenSuccess() {
        // Given
        LocalDate startDate = LocalDate.now().minusMonths(1);
        List<SpendingEntity> spendings = createTestSpendings();
        SpendingCategory topSpending = SpendingCategory.builder()
                .category("COSMETICS")
                .totalAmount(150000L)
                .build();
        RecommendedCategoryEntity recommendedCategoryEntity = createTestRecommendCategoryEntity();

        given(spendingRepository.findSpendingsByUserIdAndDateAfter(TEST_USER_ID, startDate))
                .willReturn(spendings);
        given(spendingAnalyzer.analyzeSpending(spendings)).willReturn(topSpending);
        given(recommendRepository.findBySpendingCategory("COSMETICS"))
                .willReturn(Optional.of(recommendedCategoryEntity));

        // When
        RecommendCategoryDTO result = recommendService.getRecommendedCategory(TEST_USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCategoryName()).isEqualTo("BEAUTY");
        assertThat(result.getSpendingCategory()).isEqualTo("COSMETICS");
        assertThat(result.getTotalSpending()).isEqualTo(150000L);

        verify(spendingRepository).findSpendingsByUserIdAndDateAfter(TEST_USER_ID, startDate);
        verify(spendingAnalyzer).analyzeSpending(spendings);
        verify(recommendRepository).findBySpendingCategory("COSMETICS");
    }

    /**
     * 예외 케이스: 지출 데이터가 없는 경우 적절한 예외 발생 검증
     */
    @Test
    @DisplayName("givenUserWithNoSpendingData_whenGetRecommendedCategory_thenThrowException")
    void givenUserWithNoSpendingData_whenGetRecommendedCategory_thenThrowException() {
        // Given
        given(spendingRepository.findSpendingsByUserIdAndDateAfter(anyString(), any(LocalDate.class)))
                .willReturn(new ArrayList<>());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recommendService.getRecommendedCategory(TEST_USER_ID));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_SPENDING_DATA);
    }

    /**
     * 예외 케이스: 지출 분석 결과가 null인 경우 적절한 예외 발생 검증
     */
    @Test
    @DisplayName("givenAnalyzerReturnsNull_whenGetRecommendedCategory_thenThrowException")
    void givenAnalyzerReturnsNull_whenGetRecommendedCategory_thenThrowException() {
        // Given
        List<SpendingEntity> spendings = createTestSpendings();

        given(spendingRepository.findSpendingsByUserIdAndDateAfter(anyString(), any(LocalDate.class)))
                .willReturn(spendings);
        given(spendingAnalyzer.analyzeSpending(spendings)).willReturn(null);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recommendService.getRecommendedCategory(TEST_USER_ID));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_SPENDING_DATA);
    }

    /**
     * 예외 케이스: 추천 카테고리 매핑이 없는 경우 적절한 예외 발생 검증
     */
    @Test
    @DisplayName("givenNoRecommendationMapping_whenGetRecommendedCategory_thenThrowException")
    void givenNoRecommendationMapping_whenGetRecommendedCategory_thenThrowException() {
        // Given
        List<SpendingEntity> spendings = createTestSpendings();
        SpendingCategory topSpending = SpendingCategory.builder()
                .category("UNKNOWN")
                .totalAmount(150000L)
                .build();

        given(spendingRepository.findSpendingsByUserIdAndDateAfter(anyString(), any(LocalDate.class)))
                .willReturn(spendings);
        given(spendingAnalyzer.analyzeSpending(spendings)).willReturn(topSpending);
        given(recommendRepository.findBySpendingCategory("UNKNOWN"))
                .willReturn(Optional.empty());

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> recommendService.getRecommendedCategory(TEST_USER_ID));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_RECOMMENDATION_DATA);
    }

    // 테스트 헬퍼 메소드
    private List<SpendingEntity> createTestSpendings() {
        List<SpendingEntity> spendings = new ArrayList<>();
        spendings.add(SpendingEntity.builder()
                .id(1L)
                .userId(TEST_USER_ID)
                .category("COSMETICS")
                .amount(100000L)
                .spendingDate(LocalDate.now().minusDays(5))
                .build());
        spendings.add(SpendingEntity.builder()
                .id(2L)
                .userId(TEST_USER_ID)
                .category("COSMETICS")
                .amount(50000L)
                .spendingDate(LocalDate.now().minusDays(10))
                .build());
        return spendings;
    }

    private RecommendedCategoryEntity createTestRecommendCategoryEntity() {
        return RecommendedCategoryEntity.builder()
                .id(1L)
                .spendingCategory("COSMETICS")
                .recommendCategory("BEAUTY")
                .build();
    }
}