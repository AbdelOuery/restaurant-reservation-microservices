package com.project.reservation_service.api.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SearchReservationsRequest {

    @NotNull(message = "restaurantId is required")
    private Long restaurantId;

    @NotNull(message = "date is required")
    private LocalDate date;

    @NotNull(message = "time is required")
    private LocalTime time;
}
