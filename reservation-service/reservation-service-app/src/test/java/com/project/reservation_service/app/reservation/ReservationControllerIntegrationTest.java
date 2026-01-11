package com.project.reservation_service.app.reservation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.reservation_service.api.dto.requests.CreateReservationRequest;
import com.project.reservation_service.api.enums.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReservationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReservationRepository repository;

    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        testReservation = Reservation.builder()
                .restaurantId(1L)
                .tableId(2L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("0612345678")
                .date(LocalDate.of(2026, 1, 15))
                .time(LocalTime.of(19, 0))
                .numberOfPeople(4)
                .status(ReservationStatus.PENDING)
                .build();

        testReservation = repository.save(testReservation);
    }

    @Test
    void shouldGetReservationById() throws Exception {
        mockMvc.perform(get("/api/reservation/{id}", testReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testReservation.getId()))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void shouldReturn404WhenReservationNotFound() throws Exception {
        mockMvc.perform(get("/api/reservation/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void shouldGetAllReservations() throws Exception {
        mockMvc.perform(get("/api/reservation"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.reservations", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$._embedded.reservations[0].customerName").value("John Doe"))
            .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void shouldGetReservationsByEmail() throws Exception {
        mockMvc.perform(get("/api/reservation/customer/email/{email}", "john@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.reservations", hasSize(1)))
                .andExpect(jsonPath("$._embedded.reservations[0].customerEmail").value("john@example.com"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links['all-reservations'].href").exists());
    }

    @Test
    void shouldGetReservationsByPhone() throws Exception {
        mockMvc.perform(get("/api/reservation/customer/phone/{phone}", "0612345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.reservations", hasSize(1)))
                .andExpect(jsonPath("$._embedded.reservations[0].customerPhone").value("0612345678"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links['all-reservations'].href").exists());
    }

    @Test
    void shouldSearchReservationsByRestaurantAndDateAndTime() throws Exception {
        mockMvc.perform(post("/api/reservation/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "restaurantId": 1,
                                "date": "2026-01-15",
                                "time": "19:00"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.reservations", hasSize(1)))
                .andExpect(jsonPath("$._embedded.reservations[0].restaurantId").value(1))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links['all-reservations'].href").exists());
    }

    @Test
    void shouldSearchReservationsByRestaurantDateAndTime() throws Exception {
        mockMvc.perform(post("/api/reservation/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "restaurantId": 1,
                                "date": "2026-01-15",
                                "time": "19:00"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.reservations", hasSize(1)))
                .andExpect(jsonPath("$._embedded.reservations[0].time").value("19:00:00"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void shouldGetReservationsByRestaurantAndStatus() throws Exception {
        mockMvc.perform(get("/api/reservation/restaurant/{restaurantId}/status/{status}",
                    1L, "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.reservations", hasSize(1)))
                .andExpect(jsonPath("$._embedded.reservations[0].status").value("PENDING"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links['all-reservations'].href").exists());
    }

    @Test
    void shouldConfirmReservation() throws Exception {
        mockMvc.perform(patch("/api/reservation/{id}/confirm", testReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void shouldRejectReservation() throws Exception {
        mockMvc.perform(patch("/api/reservation/{id}/reject", testReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"))
                .andExpect(jsonPath("$.canceledAt").isNotEmpty());
    }

    @Test
    void shouldCancelReservation() throws Exception {
        mockMvc.perform(delete("/api/reservation/{id}", testReservation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void shouldReturn400WhenInvalidStatusTransition() throws Exception {
        // First confirm
        testReservation.setStatus(ReservationStatus.CONFIRMED);
        repository.save(testReservation);

        // Try to confirm again
        mockMvc.perform(patch("/api/reservation/{id}/confirm", testReservation.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid Status Transition"));
    }

    @Test
    void shouldValidateCreateReservationRequest() throws Exception {
        CreateReservationRequest invalidRequest = CreateReservationRequest.builder()
                .restaurantId(1L)
                // Missing required fields
                .build();

        mockMvc.perform(post("/api/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.errors").isMap());
    }

    @Test
    void shouldValidatePhoneNumber() throws Exception {
        CreateReservationRequest request = CreateReservationRequest.builder()
                .restaurantId(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("12345")
                .date(LocalDate.of(2026, 1, 20))
                .time(LocalTime.of(19, 0))
                .numberOfPeople(4)
                .build();

        mockMvc.perform(post("/api/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerPhone").exists());
    }
}