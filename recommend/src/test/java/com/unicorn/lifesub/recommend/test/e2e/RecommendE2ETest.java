package com.unicorn.lifesub.recommend.test.e2e;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicorn.lifesub.common.dto.ApiResponse;
import com.unicorn.lifesub.recommend.dto.RecommendCategoryDTO;
import com.unicorn.lifesub.recommend.repository.jpa.RecommendRepository;
import com.unicorn.lifesub.recommend.repository.jpa.SpendingRepository;
import com.unicorn.lifesub.recommend.test.e2e.config.TestContainerConfig;
import com.unicorn.lifesub.recommend.test.e2e.support.TestDataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 구독추천 서비스 E2E 테스트 클래스입니다.
 * 실제 운영 환경과 유사한 환경에서 구독추천 API의 전체 흐름을 테스트합니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e-test")
@Transactional
class RecommendE2ETest extends TestContainerConfig {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestDataManager testDataManager;

    @Autowired
    private SpendingRepository spendingRepository;

    @Autowired
    private RecommendRepository recommendRepository;

    private WebTestClient webClient;

    @Value("${test.user.id}")
    private String TEST_USER_ID;

    @Value("${test.user.spending.category}")
    private String TEST_SPENDING_CATEGORY;

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;

    /**
     * 각 테스트 실행 전 초기화 작업을 수행합니다.
     * WebTestClient를 설정하고 테스트 데이터를 준비합니다.
     */
    @BeforeEach
    void setUp() {
        webClient = MockMvcWebTestClient
                .bindToApplicationContext(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .configureClient()
                .build();

        // 기존 데이터 정리 후 테스트 데이터 설정
        testDataManager.setupTestData();
    }

    /**
     * 유효한 사용자 ID로 추천 카테고리를 조회할 때 성공하는 시나리오입니다.
     * 응답에는 사용자의 지출 패턴에 기반한 추천 카테고리가 포함되어야 합니다.
     */
    @Test
    @DisplayName("추천 카테고리 조회 성공 시나리오")
    void givenValidUserId_whenGetRecommendCategory_thenSuccess() {
        // Given - 테스트 데이터는 BeforeEach에서 설정됨
        String token = generateValidToken();

        // When & Then
        webClient.get().uri("/api/recommend/categories?userId=" + TEST_USER_ID)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApiResponse.class)
                .value(response -> {
                    assertThat(response.getStatus()).isEqualTo(200);
                    assertThat(response.getMessage()).isEqualTo("Success");

                    // 응답 데이터 검증
                    RecommendCategoryDTO categoryDTO = objectMapper.convertValue(
                            response.getData(), RecommendCategoryDTO.class);

                    assertThat(categoryDTO).isNotNull();
                    assertThat(categoryDTO.getSpendingCategory()).isEqualTo(TEST_SPENDING_CATEGORY);
                    assertThat(categoryDTO.getCategoryName()).isEqualTo("BEAUTY"); // COSMETICS -> BEAUTY 매핑
                    assertThat(categoryDTO.getBaseDate()).isNotNull();
                    assertThat(categoryDTO.getTotalSpending()).isNotNull();
                });
    }

    /**
     * 지출 데이터가 없는 사용자가 추천 카테고리를 조회할 때 적절한 오류를 반환하는 시나리오입니다.
     */
    @Test
    @DisplayName("지출 데이터 없음 실패 시나리오")
    void givenUserWithNoSpendingData_whenGetRecommendCategory_thenReturnError() {
        // Given
        testDataManager.setupEmptyUserData(); // 지출 데이터 없는 상태로 설정
        String token = generateValidToken();

        // When & Then
        webClient.get().uri("/api/recommend/categories?userId=" + TEST_USER_ID)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody(ApiResponse.class)
                .value(response -> {
                    assertThat(response.getStatus()).isEqualTo(400); // NO_SPENDING_DATA 에러코드
                    assertThat(response.getMessage()).isEqualTo("No spending data found");
                    assertThat(response.getData()).isNull();
                });
    }

    /**
     * 유효하지 않은 토큰으로 API를 호출할 때 인증 실패를 반환하는 시나리오입니다.
     */
    @Test
    @DisplayName("인증 실패 시나리오")
    void givenInvalidToken_whenGetRecommendCategory_thenAuthenticationFails() {
        // Given
        String invalidToken = "invalid-token";

        // When & Then
        webClient.get().uri("/api/recommend/categories?userId=" + TEST_USER_ID)
                .header("Authorization", "Bearer " + invalidToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    /**
     * 테스트용 유효한 JWT 토큰을 생성합니다.
     *
     * @return 생성된 JWT 토큰
     */
    private String generateValidToken() {
        Algorithm algorithm = Algorithm.HMAC512(SECRET_KEY);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1시간 후 만료

        return JWT.create()
                .withSubject(TEST_USER_ID)
                .withClaim("userId", TEST_USER_ID)
                .withClaim("userName", "Test User")
                .withClaim("auth", Arrays.asList("USER"))
                .withIssuedAt(now)
                .withExpiresAt(expiryDate)
                .sign(algorithm);
    }
}