package com.project.restaurant_service.api.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class CheckAvailabilityRequest {

    @NotNull(message = "restaurantId is required")
    @Schema(description = "ID of the restaurant", example = "1")
    private Long restaurantId;

    @NotNull(message = "date is required")
    @Schema(description = "Date of reservation", example = "2026-01-15")
    private LocalDate date;

    @NotNull(message = "time is required")
    @Schema(description = "Time of reservation", example = "19:00")
    private LocalTime time;

    @NotNull (message = "numberOfPeople is required")
    @Min(value = 1, message = "At least 1 guest required")
    @Max(value = 20, message = "Only 20 guests maximum")
    @Schema(description = "Number of people", example = "4", minimum = "1", maximum = "20")
    private Integer numberOfPeople;
}
