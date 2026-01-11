package com.project.restaurant_service.app.availability;

import com.project.reservation_service.api.dto.ReservationDTO;
import com.project.reservation_service.client.ReservationServiceClient;
import com.project.restaurant_service.api.dto.TableDTO;
import com.project.restaurant_service.api.dto.response.AvailabilityResponse;
import com.project.restaurant_service.api.exception.RestaurantNotFoundException;
import com.project.restaurant_service.app.restaurant.Restaurant;
import com.project.restaurant_service.app.restaurant.RestaurantRepository;
import com.project.restaurant_service.app.table.TableEntity;
import com.project.restaurant_service.app.table.TableMapper;
import com.project.restaurant_service.app.table.TableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private TableRepository tableRepository;

    @Mock
    private ReservationServiceClient reservationClient;

    @Mock
    private TableMapper tableMapper;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Restaurant restaurant;
    private TableEntity table1;
    private TableEntity table2;
    private TableDTO tableDTO1;
    private TableDTO tableDTO2;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .isClosed(false)
                .build();

        table1 = TableEntity.builder()
                .id(1L)
                .restaurant(restaurant)
                .tableNumber("T1")
                .capacity(4)
                .build();

        table2 = TableEntity.builder()
                .id(2L)
                .restaurant(restaurant)
                .tableNumber("T2")
                .capacity(6)
                .build();

        tableDTO1 = TableDTO.builder()
                .id(1L)
                .tableNumber("T1")
                .capacity(4)
                .build();

        tableDTO2 = TableDTO.builder()
                .id(2L)
                .tableNumber("T2")
                .capacity(6)
                .build();
    }

    @Test
    void shouldReturnAvailableTablesWhenNoReservations() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(tableRepository.findByRestaurantIdAndCapacityGreaterThanEqual(1L, 4))
                .thenReturn(List.of(table1, table2));
        when(reservationClient.getReservationsByRestaurantAndDateAndTime(any())).thenReturn(List.of());
        when(tableMapper.toDTO(table1)).thenReturn(tableDTO1);
        when(tableMapper.toDTO(table2)).thenReturn(tableDTO2);

        AvailabilityResponse response = availabilityService.checkAvailability(
                1L,
                LocalDate.of(2026, 1, 15),
                LocalTime.of(19, 0),
                4
        );

        assertTrue(response.getAvailable());
        assertFalse(response.getClosed());
        assertEquals(2, response.getAvailableTables().size());
        verify(reservationClient).getReservationsByRestaurantAndDateAndTime(any());
        verify(tableMapper).toDTO(table1);
        verify(tableMapper).toDTO(table2);
    }

    @Test
    void shouldFilterOutBookedTables() {
        ReservationDTO bookedReservation = ReservationDTO.builder()
                .tableId(1L)
                .build();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(tableRepository.findByRestaurantIdAndCapacityGreaterThanEqual(1L, 4))
                .thenReturn(List.of(table1, table2));
        when(reservationClient.getReservationsByRestaurantAndDateAndTime(any()))
                .thenReturn(List.of(bookedReservation));
        when(tableMapper.toDTO(table2)).thenReturn(tableDTO2);

        AvailabilityResponse response = availabilityService.checkAvailability(
                1L,
                LocalDate.of(2026, 1, 15),
                LocalTime.of(19, 0),
                4
        );

        assertTrue(response.getAvailable());
        assertEquals(1, response.getAvailableTables().size());
        assertEquals(2L, response.getAvailableTables().get(0).getId());
        verify(tableMapper, times(1)).toDTO(any(TableEntity.class));
    }

    @Test
    void shouldReturnUnavailableWhenAllTablesBooked() {
        ReservationDTO reservation1 = ReservationDTO.builder().tableId(1L).build();
        ReservationDTO reservation2 = ReservationDTO.builder().tableId(2L).build();

        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(tableRepository.findByRestaurantIdAndCapacityGreaterThanEqual(1L, 4))
                .thenReturn(List.of(table1, table2));
        when(reservationClient.getReservationsByRestaurantAndDateAndTime(any()))
                .thenReturn(List.of(reservation1, reservation2));

        AvailabilityResponse response = availabilityService.checkAvailability(
                1L,
                LocalDate.of(2026, 1, 15),
                LocalTime.of(19, 0),
                4
        );

        assertFalse(response.getAvailable());
        assertEquals(0, response.getAvailableTables().size());
        assertTrue(response.getMessage().contains("booked"));
        verify(tableMapper, never()).toDTO(any(TableEntity.class));
    }

    @Test
    void shouldReturnUnavailableWhenRestaurantIsClosed() {
        restaurant.setIsClosed(true);
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        AvailabilityResponse response = availabilityService.checkAvailability(
                1L,
                LocalDate.of(2026, 1, 15),
                LocalTime.of(19, 0),
                4
        );

        assertFalse(response.getAvailable());
        assertTrue(response.getMessage().contains("Restaurant is currently closed"));
    }

    @Test
    void shouldReturnUnavailableWhenNoSuitableTables() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(tableRepository.findByRestaurantIdAndCapacityGreaterThanEqual(1L, 10))
                .thenReturn(List.of());

        AvailabilityResponse response = availabilityService.checkAvailability(
                1L,
                LocalDate.of(2026, 1, 15),
                LocalTime.of(19, 0),
                10
        );

        assertFalse(response.getAvailable());
        assertEquals(0, response.getAvailableTables().size());
        assertTrue(response.getMessage().contains("No tables available"));
    }

    @Test
    void shouldThrowExceptionWhenRestaurantNotFound() {
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> {
            availabilityService.checkAvailability(
                    999L,
                    LocalDate.of(2026, 1, 15),
                    LocalTime.of(19, 0),
                    4
            );
        });
    }
}