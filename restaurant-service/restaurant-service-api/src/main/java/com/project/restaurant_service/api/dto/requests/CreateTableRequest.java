package com.project.restaurant_service.api.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateTableRequest {

    @NotBlank(message = "Table number is required")
    @Schema(description = "Number of table", example = "0001")
    private String tableNumber;

    @Positive(message = "Capacity is required and should be a positive number")
    @Schema(description = "Number of people the table can hold", example = "4")
    private Integer capacity;

    @NotNull(message = "restaurantId is required")
    @Schema(description = "ID of the restaurant", example = "1")
    private Long restaurantId;
}
