package com.project.restaurant_service.app.availability;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.restaurant_service.api.dto.requests.CheckAvailabilityRequest;
import com.project.restaurant_service.app.restaurant.Restaurant;
import com.project.restaurant_service.app.restaurant.RestaurantRepository;
import com.project.restaurant_service.app.table.TableEntity;
import com.project.restaurant_service.app.table.TableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@Testcontainers
class AvailabilityControllerIntegrationContainerTest {

    @Container
    static GenericContainer<?> reservationServiceContainer = new GenericContainer<>(
            DockerImageName.parse("reservation-service:latest"))
            .withExposedPorts(8082)
            .withEnv("SPRING_PROFILES_ACTIVE", "test")
            .withEnv("EUREKA_CLIENT_ENABLED", "false")
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:h2:mem:reservation-test-db")
            .withEnv("SPRING_JPA_HIBERNATE_DDL_AUTO", "create-drop")
            .waitingFor(Wait.forHttp("/actuator/health")
                    .forPort(8082)
                    .withStartupTimeout(Duration.ofMinutes(3)));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Disable Eureka
        registry.add("eureka.client.enabled", () -> "false");
        registry.add("eureka.client.registerWithEureka", () -> "false");
        registry.add("eureka.client.fetchRegistry", () -> "false");

        // Point Feign client to Testcontainer
        String reservationServiceUrl = String.format(
                "http://%s:%d",
                reservationServiceContainer.getHost(),
                reservationServiceContainer.getMappedPort(8082)
        );
        registry.add("feign.client.config.reservation-service.url", () -> reservationServiceUrl);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private TableRepository tableRepository;

    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        tableRepository.deleteAll();
        restaurantRepository.deleteAll();

        testRestaurant = Restaurant.builder()
                .name("Test Restaurant")
                .address("123 Test Street")
                .phone("+33123456789")
                .email("test@restaurant.fr")
                .isClosed(false)
                .build();
        testRestaurant = restaurantRepository.save(testRestaurant);

        TableEntity table = TableEntity.builder()
                .restaurant(testRestaurant)
                .tableNumber("T1")
                .capacity(4)
                .build();
        tableRepository.save(table);
    }

    @Test
    void shouldCheckAvailabilityWithRealReservationService() throws Exception {
        CheckAvailabilityRequest request = CheckAvailabilityRequest.builder()
                .restaurantId(testRestaurant.getId())
                .date(LocalDate.of(2026, 1, 15))
                .time(LocalTime.of(19, 0))
                .numberOfPeople(4)
                .build();

        mockMvc.perform(post("/api/availability/check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.availableTables", hasSize(greaterThanOrEqualTo(1))));
    }
}