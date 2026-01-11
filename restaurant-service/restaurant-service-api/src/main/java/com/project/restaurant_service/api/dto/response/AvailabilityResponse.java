package com.project.restaurant_service.api.dto.response;

import com.project.restaurant_service.api.dto.TableDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AvailabilityResponse {
    private Boolean closed;
    private Boolean available;
    private String message;
    private List<TableDTO> availableTables;
}
