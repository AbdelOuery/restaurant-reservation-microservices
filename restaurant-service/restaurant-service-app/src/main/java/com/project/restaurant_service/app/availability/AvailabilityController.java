package com.project.restaurant_service.app.availability;

import com.project.restaurant_service.api.dto.requests.CheckAvailabilityRequest;
import com.project.restaurant_service.api.dto.response.AvailabilityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Table Availability API")
public class AvailabilityController {

    private final AvailabilityService service;

    @PostMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Check for table availability in a restaurant for a certain time and date")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability response"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public AvailabilityResponse checkAvailability(@Valid @RequestBody CheckAvailabilityRequest request) {
        return service.checkAvailability(
            request.getRestaurantId(),
            request.getDate(),
            request.getTime(),
            request.getNumberOfPeople()
        );
    }
}
