package com.project.restaurant_service.client;

import com.project.restaurant_service.api.dto.requests.CheckAvailabilityRequest;
import com.project.restaurant_service.api.dto.response.AvailabilityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name ="restaurant-service")
public interface RestaurantServiceClient {

    @PostMapping("/api/availability/check")
    AvailabilityResponse checkAvailability(CheckAvailabilityRequest request);
}
