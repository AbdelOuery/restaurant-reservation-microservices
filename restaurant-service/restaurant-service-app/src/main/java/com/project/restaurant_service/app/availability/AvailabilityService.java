package com.project.restaurant_service.app.availability;

import com.project.reservation_service.api.dto.ReservationDTO;
import com.project.reservation_service.api.dto.requests.SearchReservationsRequest;
import com.project.reservation_service.client.ReservationServiceClient;
import com.project.restaurant_service.api.dto.TableDTO;
import com.project.restaurant_service.api.dto.response.AvailabilityResponse;
import com.project.restaurant_service.api.exception.RestaurantNotFoundException;
import com.project.restaurant_service.app.restaurant.Restaurant;
import com.project.restaurant_service.app.restaurant.RestaurantRepository;
import com.project.restaurant_service.app.table.TableEntity;
import com.project.restaurant_service.app.table.TableMapper;
import com.project.restaurant_service.app.table.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final RestaurantRepository restaurantRepository;
    private final TableRepository tableRepository;
    private final TableMapper tableMapper;
    private final ReservationServiceClient reservationServiceClient;

    public AvailabilityResponse checkAvailability(Long restaurantId, LocalDate date, LocalTime time, Integer numberOfPeople) {
        log.info("Checking availability for restaurant id {} on the {} {}", restaurantId, date, time);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(RestaurantNotFoundException::new);

        // Check if the restaurant is currently closed
        if (restaurant.getIsClosed()) {
            return AvailabilityResponse.builder()
                    .closed(true)
                    .available(false)
                    .availableTables(List.of())
                    .message("Restaurant is currently closed")
                    .build();
        }

        // Find suitable tables for the requested number of people
        List<TableEntity> tables = tableRepository.findByRestaurantIdAndCapacityGreaterThanEqual(restaurantId, numberOfPeople);

        log.info("Found {} corresponding tables with capacity >= {}", tables.size(), numberOfPeople);

        if (tables.isEmpty()) {
            return AvailabilityResponse.builder()
                    .closed(false)
                    .available(false)
                    .availableTables(List.of())
                    .message(String.format("No tables available for %d people", numberOfPeople))
                    .build();
        }

        // Check if the corresponding tables are already booked by calling reservation microservice
        List<ReservationDTO> reservations;
        try {
            SearchReservationsRequest request = SearchReservationsRequest.builder()
                    .restaurantId(restaurantId)
                    .date(date)
                    .time(time)
                    .build();

            reservations = reservationServiceClient.getReservationsByRestaurantAndDateAndTime(request);
            log.info("Found {} already booked reservations for this date and time!", reservations);
        } catch (Exception e) {
            log.error("Error calling Reservation Service Client: {}", e.getMessage());
            reservations = List.of();
        }

        // Collects ids of already taken tables
        Set<Long> bookedTableIds = reservations
                .stream()
                .map(ReservationDTO::getTableId)
                .collect(Collectors.toSet());

        // Aggregate the available ones
        List<TableEntity> availableTables = tables
                .stream()
                .filter(table -> !bookedTableIds.contains(table.getId()))
                .toList();

        if (availableTables.isEmpty()) {
            return AvailabilityResponse.builder()
                    .closed(false)
                    .available(false)
                    .availableTables(List.of())
                    .message("All suitable tables are booked for this time")
                    .build();
        }

        List<TableDTO> availableTablesDTOs = availableTables
                .stream()
                .map(tableMapper::toDTO)
                .toList();

        return AvailabilityResponse.builder()
                .closed(false)
                .available(true)
                .availableTables(availableTablesDTOs)
                .message(String.format("%d tables available", availableTablesDTOs.size()))
                .build();
    }
}
