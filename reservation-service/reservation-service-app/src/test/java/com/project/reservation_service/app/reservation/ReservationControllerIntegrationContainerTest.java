package com.project.reservation_service.app.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.reservation_service.api.dto.requests.CreateReservationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class ReservationControllerIntegrationContainerTest {

    @Container
    static GenericContainer<?> restaurantServiceContainer = new GenericContainer<>(
            DockerImageName.parse("restaurant-service:latest"))
            .withExposedPorts(8081)
            .withEnv("SPRING_PROFILES_ACTIVE", "test")
            .withEnv("EUREKA_CLIENT_ENABLED", "false")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:h2:mem:restaurant-test-db")
            .withEnv("SPRING_JPA_HIBERNATE_DDL_AUTO", "create-drop")
            .waitingFor(Wait.forHttp("/actuator/health")
                    .forPort(8081)
                    .withStartupTimeout(Duration.ofMinutes(3)));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("eureka.client.registerWithEureka", () -> "false");
        registry.add("eureka.client.fetchRegistry", () -> "false");

        String restaurantServiceUrl = String.format(
                "http://%s:%d",
                restaurantServiceContainer.getHost(),
                restaurantServiceContainer.getMappedPort(8081)
        );

        registry.add("feign.client.config.reservation-service.url", () -> restaurantServiceUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateReservationWithRealRestaurantService() throws Exception {
        CreateReservationRequest request = CreateReservationRequest.builder()
                .restaurantId(1L)
                .customerName("Jane Smith")
                .customerEmail("jane@example.com")
                .customerPhone("+33687654321")
                .date(LocalDate.of(2026, 2, 1))
                .time(LocalTime.of(20, 0))
                .numberOfPeople(2)
                .build();

        mockMvc.perform(post("/api/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerName").value("Jane Smith"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.restaurantId").value(1));
    }

    @Test
    void shouldReturn404WhenRestaurantDoesNotExist() throws Exception {
        CreateReservationRequest request = CreateReservationRequest.builder()
                .restaurantId(999L)  // Non-existent restaurant
                .customerName("Jane Smith")
                .customerEmail("jane@example.com")
                .customerPhone("+33687654321")
                .date(LocalDate.of(2026, 2, 1))
                .time(LocalTime.of(20, 0))
                .numberOfPeople(2)
                .build();

        mockMvc.perform(post("/api/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void shouldHandleRestaurantServiceTimeout() throws Exception {
        // This test verifies timeout handling
        // Stop the container temporarily to simulate timeout
        restaurantServiceContainer.stop();

        CreateReservationRequest request = CreateReservationRequest.builder()
                .restaurantId(1L)
                .customerName("Jane Smith")
                .customerEmail("jane@example.com")
                .customerPhone("+33687654321")
                .date(LocalDate.of(2026, 2, 1))
                .time(LocalTime.of(20, 0))
                .numberOfPeople(2)
                .build();

        mockMvc.perform(post("/api/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());

        // Restart for other tests
        restaurantServiceContainer.start();
    }
}