package com.project.reservation_service.app.reservation;

import com.project.reservation_service.api.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByRestaurantIdAndDateAndTime(Long restaurantId, LocalDate date, LocalTime time);
    List<Reservation> findByCustomerPhoneOrderByDateDesc(String customerPhone);
    List<Reservation> findByCustomerEmailOrderByDateDesc(String customerEmail);
    List<Reservation> findByRestaurantIdAndStatus(Long restaurantId, ReservationStatus status);

}
