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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {

    private final ReservationRepository repository;
    private final ReservationMapper mapper;
    private final RestaurantServiceClient restaurantServiceClient;

    @Transactional
    public ReservationDTO getById(Long id) {
        Reservation entity = repository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);
        return mapper.toDTO(entity);
    }

    @Transactional
    public List<ReservationDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public List<ReservationDTO> getReservationsByRestaurantAndDateAndTime(
        Long restaurantId,
        LocalDate date,
        LocalTime time
    ) {
        return repository.findByRestaurantIdAndDateAndTime(restaurantId, date, time)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public List<ReservationDTO> getReservationsByCustomerPhone(String customerPhone) {
        return repository.findByCustomerPhoneOrderByDateDesc(customerPhone)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public List<ReservationDTO> getReservationsByCustomerEmail(String customerEmail) {
        return repository.findByCustomerEmailOrderByDateDesc(customerEmail)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public List<ReservationDTO> getReservationsByRestaurantAndStatus(Long restaurantId, ReservationStatus status) {
        return repository.findByRestaurantIdAndStatus(restaurantId, status)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public ReservationDTO create(CreateReservationRequest request) {
        log.info("Attempting to create a reservation for restaurant {} on {} {}", request.getRestaurantId(), request.getDate(), request.getTime());

        // Call availability service to check available tables for requested criteria
        CheckAvailabilityRequest availabilityRequest = CheckAvailabilityRequest.builder()
                .restaurantId(request.getRestaurantId())
                .date(request.getDate())
                .time(request.getTime())
                .numberOfPeople(request.getNumberOfPeople())
                .build();

        AvailabilityResponse availabilityResponse = restaurantServiceClient.checkAvailability(availabilityRequest);

        // Is restaurant closed
        if(availabilityResponse.getClosed()) {
            throw new NoAvailabilityException(
                String.format("Restaurant %d is currently closed", request.getRestaurantId())
            );
        }

        // No tables available ?
        if (!availabilityResponse.getAvailable() || availabilityResponse.getAvailableTables().isEmpty()) {
            throw new NoAvailabilityException(
                String.format("No tables available for %d people on %s %s",
                    request.getNumberOfPeople(),
                    request.getDate(),
                    request.getTime()
                )
            );
        }

        log.info("Retrieved {} available tables", availabilityResponse.getAvailableTables().size());

        // Select the first free table
        TableDTO selectedTable = availabilityResponse.getAvailableTables().get(0);
        log.info("Selecting table {} for reservation", selectedTable.getTableNumber());

        Reservation entity = mapper.toEntity(request);

        // Set the tableId & the status to PENDING (awaiting confirmation from restaurant)
        entity.setTableId(selectedTable.getId());
        entity.setStatus(ReservationStatus.PENDING);

        Reservation saved = repository.save(entity);

        log.info("Reservation successfully created");
        return mapper.toDTO(saved);
    }

    @Transactional
    public ReservationDTO confirmReservation(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);

        // Validate status transition
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                "Can only confirm PENDING reservations. Current status: " + reservation.getStatus()
            );
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation updated = repository.save(reservation);

        log.info("Reservation {} confirmed", id);

        return mapper.toDTO(updated);
    }

    @Transactional
    public ReservationDTO rejectReservation(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new InvalidStatusTransitionException(
                "Can only reject PENDING reservations. Current status: " + reservation.getStatus()
            );
        }

        reservation.setStatus(ReservationStatus.CANCELED);
        reservation.setCanceledAt(LocalDateTime.now());
        Reservation updated = repository.save(reservation);

        log.info("Reservation {} rejected by restaurant", id);

        return mapper.toDTO(updated);
    }

    @Transactional
    public ReservationDTO checkInReservation(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new InvalidStatusTransitionException(
                "Can only checkIn CONFIRMED reservations. Current status: " + reservation.getStatus()
            );
        }

        reservation.setStatus(ReservationStatus.CHECKED_IN);
        Reservation updated = repository.save(reservation);

        log.info("Reservation {} marked as checked in", id);

        return mapper.toDTO(updated);
    }

    @Transactional
    public ReservationDTO completeReservation(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);

        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new InvalidStatusTransitionException(
                "Can only complete CHECKED_IN reservations. Current status: " + reservation.getStatus()
            );
        }

        reservation.setStatus(ReservationStatus.COMPLETED);
        Reservation updated = repository.save(reservation);

        log.info("Reservation {} marked as completed", id);

        return mapper.toDTO(updated);
    }

    @Transactional
    public ReservationDTO cancelReservation(Long id) {
        Reservation reservation = repository.findById(id)
                .orElseThrow(ReservationNotFoundException::new);

        reservation.setStatus(ReservationStatus.CANCELED);
        reservation.setCanceledAt(LocalDateTime.now());
        Reservation updated = repository.save(reservation);

        log.info("Reservation {} canceled", id);

        return mapper.toDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ReservationNotFoundException();
        }
        repository.deleteById(id);
    }
}
