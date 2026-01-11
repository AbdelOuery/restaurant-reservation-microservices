package com.project.reservation_service.app.reservation;

import com.project.reservation_service.api.dto.ReservationDTO;
import com.project.reservation_service.api.dto.requests.CreateReservationRequest;
import com.project.reservation_service.api.enums.ReservationStatus;
import com.project.reservation_service.api.exception.InvalidStatusTransitionException;
import com.project.reservation_service.api.exception.ReservationNotFoundException;
import com.project.restaurant_service.api.dto.TableDTO;
import com.project.restaurant_service.api.dto.requests.CheckAvailabilityRequest;
import com.project.restaurant_service.api.dto.response.AvailabilityResponse;
import com.project.restaurant_service.api.exception.NoAvailabilityException;
import com.project.restaurant_service.client.RestaurantServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository repository;

    @Mock
    private RestaurantServiceClient restaurantClient;

    @Mock
    private ReservationMapper mapper;

    @InjectMocks
    private ReservationService service;

    private CreateReservationRequest createRequest;
    private Reservation reservation;
    private ReservationDTO reservationDTO;
    private AvailabilityResponse availabilityResponse;
    private TableDTO tableDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        createRequest = CreateReservationRequest.builder()
                .restaurantId(1L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("0612345678")
                .date(LocalDate.of(2026, 1, 15))
                .time(LocalTime.of(19, 0))
                .numberOfPeople(4)
                .build();

        tableDTO = TableDTO.builder()
                .id(2L)
                .tableNumber("T2")
                .capacity(4)
                .build();

        availabilityResponse = AvailabilityResponse.builder()
                .closed(false)
                .available(true)
                .availableTables(List.of(tableDTO))
                .message("1 table available")
                .build();

        reservation = Reservation.builder()
                .id(1L)
                .restaurantId(1L)
                .tableId(2L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("0612345678")
                .date(LocalDate.of(2026, 1, 15))
                .time(LocalTime.of(19, 0))
                .numberOfPeople(4)
                .status(ReservationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        reservationDTO = ReservationDTO.builder()
                .id(1L)
                .restaurantId(1L)
                .tableId(2L)
                .customerName("John Doe")
                .customerEmail("john@example.com")
                .customerPhone("0612345678")
                .date(LocalDate.of(2026, 1, 15))
                .time(LocalTime.of(19, 0))
                .numberOfPeople(4)
                .status("PENDING")
                .build();
    }

    @Test
    void shouldCreateReservationWhenTablesAvailable() {
        when(restaurantClient.checkAvailability(any(CheckAvailabilityRequest.class)))
                .thenReturn(availabilityResponse);
        when(mapper.toEntity(any(CreateReservationRequest.class)))
                .thenReturn(Reservation.builder()
                        .restaurantId(1L)
                        .customerName("John Doe")
                        .customerEmail("john@example.com")
                        .customerPhone("0612345678")
                        .date(LocalDate.of(2026, 1, 15))
                        .time(LocalTime.of(19, 0))
                        .numberOfPeople(4)
                        .status(ReservationStatus.PENDING)
                        .build());
        when(repository.save(any(Reservation.class))).thenReturn(reservation);
        when(mapper.toDTO(any(Reservation.class))).thenReturn(reservationDTO);

        ReservationDTO result = service.create(createRequest);

        assertNotNull(result);
        assertEquals("PENDING", result.getStatus());
        assertEquals(2L, result.getTableId());
        verify(restaurantClient).checkAvailability(any(CheckAvailabilityRequest.class));
        verify(repository).save(any(Reservation.class));
        verify(mapper).toDTO(any(Reservation.class));
    }

    @Test
    void shouldThrowExceptionWhenRestaurantIsClosed() {
        AvailabilityResponse noAvailability = AvailabilityResponse.builder()
                .closed(true)
                .available(false)
                .availableTables(List.of())
                .message("Restaurant is closed")
                .build();

        when(restaurantClient.checkAvailability(any(CheckAvailabilityRequest.class)))
                .thenReturn(noAvailability);

        assertThrows(NoAvailabilityException.class, () -> {
            service.create(createRequest);
        });

        verify(restaurantClient).checkAvailability(any(CheckAvailabilityRequest.class));
        verify(repository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldThrowExceptionWhenNoTablesAvailable() {
        AvailabilityResponse noAvailability = AvailabilityResponse.builder()
                .closed(false)
                .available(false)
                .availableTables(List.of())
                .message("No tables available")
                .build();

        when(restaurantClient.checkAvailability(any(CheckAvailabilityRequest.class)))
                .thenReturn(noAvailability);

        assertThrows(NoAvailabilityException.class, () -> {
            service.create(createRequest);
        });

        verify(restaurantClient).checkAvailability(any(CheckAvailabilityRequest.class));
        verify(repository, never()).save(any(Reservation.class));
    }

    @Test
    void shouldGetReservationById() {
        when(repository.findById(1L)).thenReturn(Optional.of(reservation));
        when(mapper.toDTO(reservation)).thenReturn(reservationDTO);

        ReservationDTO result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenReservationNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ReservationNotFoundException.class, () -> {
            service.getById(999L);
        });
    }

    @Test
    void shouldConfirmReservation() {
        when(repository.findById(1L)).thenReturn(Optional.of(reservation));
        when(repository.save(any(Reservation.class))).thenReturn(reservation);
        when(mapper.toDTO(any(Reservation.class))).thenReturn(reservationDTO);

        ReservationDTO result = service.confirmReservation(1L);

        assertNotNull(result);
        verify(repository).save(argThat(r -> r.getStatus() == ReservationStatus.CONFIRMED));
    }

    @Test
    void shouldThrowExceptionWhenConfirmingNonPendingReservation() {
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(repository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThrows(InvalidStatusTransitionException.class, () -> {
            service.confirmReservation(1L);
        });
    }

    @Test
    void shouldCancelReservation() {
        when(repository.findById(1L)).thenReturn(Optional.of(reservation));
        when(repository.save(any(Reservation.class))).thenReturn(reservation);
        when(mapper.toDTO(any(Reservation.class))).thenReturn(reservationDTO);

        ReservationDTO result = service.cancelReservation(1L);

        assertNotNull(result);
        verify(repository).save(argThat(r ->
                r.getStatus() == ReservationStatus.CANCELED && r.getCanceledAt() != null
        ));
    }

    @Test
    void shouldGetReservationsByEmail() {
        when(repository.findByCustomerEmailOrderByDateDesc("john@example.com"))
                .thenReturn(List.of(reservation));
        when(mapper.toDTO(any(Reservation.class))).thenReturn(reservationDTO);

        List<ReservationDTO> result = service.getReservationsByCustomerEmail("john@example.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository).findByCustomerEmailOrderByDateDesc("john@example.com");
        verify(mapper).toDTO(any(Reservation.class));
    }
}