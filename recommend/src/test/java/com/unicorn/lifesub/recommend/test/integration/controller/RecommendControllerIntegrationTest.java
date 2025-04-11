package com.unicorn.lifesub.recommend.test.integration.controller;

import com.unicorn.lifesub.common.dto.ApiResponse;
import com.unicorn.lifesub.common.exception.BusinessException;
import com.unicorn.lifesub.common.exception.ErrorCode;
import com.unicorn.lifesub.recommend.controller.RecommendController;
import com.unicorn.lifesub.recommend.dto.RecommendCategoryDTO;
import com.unicorn.lifesub.recommend.service.RecommendService;
import com.unicorn.lifesub.recommend.test.integration.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * RecommendController 통합 테스트 클래스
 * 구독 추천 관련 API의 요청/응답을 검증합니다.
 */
@WebMvcTest(RecommendController.class)
@Import({RecommendControllerIntegrationTest.TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("integration-test")
class RecommendControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecommendService recommendService;

    /**
     * 테스트를 위한 설정 클래스입니다.
     * Controller와 의존성들을 직접 등록하여 테스트 환경을 구성합니다.
     */
    @Configuration
    static class TestConfig implements WebMvcConfigurer {
        @Bean
        public RecommendController recommendController() {
            return new RecommendController(recommendService());
        }

        @Bean
        public RecommendService recommendService() {
            return mock(RecommendService.class);
        }

        // 추가: ExceptionHandler가 포함된 클래스를 빈으로 등록
        @Bean
        public RestExceptionHandler restExceptionHandler() {
            return new RestExceptionHandler();
        }
    }

    /**
     * 추천 카테고리 조회 성공 케이스를 테스트합니다.
     */
    @Test
    @DisplayName("추천 카테고리 조회 성공 테스트")
    void givenUserId_whenGetRecommendedCategory_thenSuccess() throws Exception {
        // Given
        String userId = "user01";
        RecommendCategoryDTO recommendCategory = RecommendCategoryDTO.builder()
                .categoryName("OTT")
                .spendingCategory("ENTERTAINMENT")
                .baseDate(LocalDate.now())
                .totalSpending(350000L)
                .build();

        given(recommendService.getRecommendedCategory(userId)).willReturn(recommendCategory);

        // When
        ResultActions result = mockMvc.perform(get("/api/recommend/categories")
                        .param("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // Then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.data.categoryName").value("OTT"))
                .andExpect(jsonPath("$.data.spendingCategory").value("ENTERTAINMENT"))
                .andExpect(jsonPath("$.data.totalSpending").value(350000));
    }

    /**
     * 지출 데이터가 없는 경우의 추천 카테고리 조회 테스트
     */
    @Test
    @DisplayName("추천 카테고리 조회 실패 테스트 - 지출 데이터 없음")
    void givenUserIdWithNoSpendingData_whenGetRecommendedCategory_thenHandleException() throws Exception {
        // Given
        String userId = "newUser";

        // 예외를 발생시키도록 모킹
        given(recommendService.getRecommendedCategory(anyString()))
                .willThrow(new BusinessException(ErrorCode.NO_SPENDING_DATA));

        // When & Then - 예외가 발생해도 컨트롤러에서 처리되어 200 OK와 함께 에러 정보가 반환되어야 함
        mockMvc.perform(get("/api/recommend/categories")
                        .param("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError())  // 변경: 400 상태코드 예상
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("No spending data found"));
    }

    /**
     * 추천 데이터가 없는 경우의 추천 카테고리 조회 테스트
     */
    @Test
    @DisplayName("추천 카테고리 조회 실패 테스트 - 추천 데이터 없음")
    void givenUserIdWithNoRecommendationData_whenGetRecommendedCategory_thenHandleException() throws Exception {
        // Given
        String userId = "user02";

        // 예외를 발생시키도록 모킹
        given(recommendService.getRecommendedCategory(anyString()))
                .willThrow(new BusinessException(ErrorCode.NO_RECOMMENDATION_DATA));

        // When & Then - 예외가 발생해도 컨트롤러에서 처리되어 200 OK와 함께 에러 정보가 반환되어야 함
        mockMvc.perform(get("/api/recommend/categories")
                        .param("userId", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError())  // 변경: 400 상태코드 예상
                .andExpect(jsonPath("$.status").value(410))
                .andExpect(jsonPath("$.message").value("추천 구독 카테고리 없음"));
    }
}