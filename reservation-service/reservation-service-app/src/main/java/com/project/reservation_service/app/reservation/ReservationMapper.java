package com.project.reservation_service.app.reservation;

import com.project.reservation_service.api.dto.ReservationDTO;
import com.project.reservation_service.api.dto.requests.CreateReservationRequest;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {
    public ReservationDTO toDTO(Reservation entity) {
        return ReservationDTO.builder()
                .id(entity.getId())
                .restaurantId(entity.getRestaurantId())
                .tableId(entity.getTableId())
                .customerName(entity.getCustomerName())
                .customerEmail(entity.getCustomerEmail())
                .customerPhone(entity.getCustomerPhone())
                .date(entity.getDate())
                .time(entity.getTime())
                .numberOfPeople(entity.getNumberOfPeople())
                .status(entity.getStatus().toString())
                .canceledAt(entity.getCanceledAt())
                .build();
    }

    public Reservation toEntity(CreateReservationRequest request) {
        return Reservation.builder()
                .restaurantId(request.getRestaurantId())
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .date(request.getDate())
                .time(request.getTime())
                .numberOfPeople(request.getNumberOfPeople())
                .build();
    }
}
