package com.project.reservation_service.api.dto;

import lombok.*;
import org.springframework.hateoas.server.core.Relation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Relation(collectionRelation = "reservations", itemRelation = "reservation")
public class ReservationDTO {
    private Long id;
    private Long restaurantId;
    private Long tableId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private LocalDate date;
    private LocalTime time;
    private Integer numberOfPeople;
    private String status;
    private LocalDateTime canceledAt;
}
