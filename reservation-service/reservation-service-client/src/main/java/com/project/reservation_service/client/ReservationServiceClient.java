package com.project.reservation_service.client;

import com.project.reservation_service.api.dto.ReservationDTO;
import com.project.reservation_service.api.dto.requests.SearchReservationsRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "reservation-service")
public interface ReservationServiceClient {

    @PostMapping("/api/reservation/search")
    List<ReservationDTO> getReservationsByRestaurantAndDateAndTime(SearchReservationsRequest request);
}
