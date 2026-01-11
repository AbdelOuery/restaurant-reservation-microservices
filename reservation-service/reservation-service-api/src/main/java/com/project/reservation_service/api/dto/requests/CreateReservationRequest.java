package com.project.reservation_service.api.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
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
@Schema(description = "Request to create a new reservation")
public class CreateReservationRequest {

    @NotNull(message = "restaurantId is required")
    @Schema(description = "ID of the restaurant", example = "1")
    private Long restaurantId;

    @NotBlank(message = "Customer name is required")
    @Schema(description = "Customer full name", example = "Jean Dupont")
    private String customerName;

    @Email(message = "Invalid email")
    @Schema(description = "Customer email address", example = "jean.dupont@example.fr")
    private String customerEmail;

    @NotBlank(message = "Customer phone is required")
    @Pattern(
            regexp = "^(0|\\+33)[1-9]([-. ]?[0-9]{2}){4}$",
            message = "Invalid French phone number. Use format: 0612345678 or +33612345678"
    )
    @Schema(
            description = "Customer phone number (French format)",
            example = "0612345678",
            pattern = "^(0|\\+33)[1-9]([-. ]?[0-9]{2}){4}$"
    )
    private String customerPhone;

    @NotNull(message = "Reservation date is required")
    @FutureOrPresent(message = "Reservation date cannot be in the past")
    @Schema(description = "Date of reservation", example = "2026-01-15")
    private LocalDate date;

    @NotNull(message = "Reservation time is required")
    @Schema(description = "Time of reservation", example = "19:00")
    private LocalTime time;

    @NotNull(message = "Number of people is required")
    @Min(value = 1, message = "At least 1 person is required")
    @Max(value = 20, message = "Only 20 guests maximum")
    @Schema(description = "Number of people", example = "4", minimum = "1", maximum = "20")
    private Integer numberOfPeople;
}
