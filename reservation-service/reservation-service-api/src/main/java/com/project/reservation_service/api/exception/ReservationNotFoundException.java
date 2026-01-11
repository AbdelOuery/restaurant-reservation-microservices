package com.project.reservation_service.api.exception;

public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException() {
        super("Reservation not found!");
    }
}
