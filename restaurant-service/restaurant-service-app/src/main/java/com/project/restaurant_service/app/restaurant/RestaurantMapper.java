package com.project.restaurant_service.app.restaurant;

import com.project.restaurant_service.api.dto.RestaurantDTO;
import com.project.restaurant_service.api.dto.requests.CreateRestaurantRequest;
import org.springframework.stereotype.Component;

@Component
public class RestaurantMapper {
    public RestaurantDTO toDTO(Restaurant entity) {
        RestaurantDTO dto = new RestaurantDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setIsClosed(entity.getIsClosed());
        return dto;
    }

    public Restaurant toEntity(CreateRestaurantRequest request) {
        Restaurant entity = new Restaurant();
        entity.setName(request.getName());
        entity.setAddress(request.getAddress());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setIsClosed(request.getIsClosed());
        return entity;
    }

    public void updateEntity(Restaurant entity, CreateRestaurantRequest request) {
        if (request == null || entity == null) {
            return;
        }

        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getAddress() != null) {
            entity.setAddress(request.getAddress());
        }
        if (request.getPhone() != null) {
            entity.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            entity.setEmail(request.getEmail());
        }
        if (request.getIsClosed() != null) {
            entity.setIsClosed(request.getIsClosed());
        }
    }
}
