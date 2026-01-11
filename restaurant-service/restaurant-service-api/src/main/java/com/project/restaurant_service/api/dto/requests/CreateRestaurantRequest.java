package com.project.restaurant_service.api.dto.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "Request to create a new restaurant")
public class CreateRestaurantRequest {

    @NotBlank(message = "Name is required")
    @Schema(description = "Restaurant name", example = "Le Petit Bistro")
    private String name;

    @NotBlank(message = "Address is required")
    @Schema(description = "Restaurant address", example = "15 Rue de la Paix, 75002 Paris, France")
    private String address;

    @Pattern(regexp = "^(0|\\+33)[1-9]([-. ]?[0-9]{2}){4}$", message = "Invalid phone number")
    @Schema(
            description = "Restaurant phone number (French format)",
            example = "+33142968917",
            pattern = "^(0|\\+33)[1-9]([-. ]?[0-9]{2}){4}$"
    )
    private String phone;

    @Email(message = "Invalid email")
    @Schema(description = "Restaurant email", example = "contact@lepetitbistro.fr")
    private String email;

    @NotNull(message = "isClosed is required")
    @Schema(description = "Is the restaurant currently closed", example = "false")
    private Boolean isClosed;
}
