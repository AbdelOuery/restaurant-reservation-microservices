package com.project.restaurant_service.app.table;

import com.project.restaurant_service.api.dto.TableDTO;
import com.project.restaurant_service.api.dto.requests.CreateTableRequest;
import com.project.restaurant_service.app.restaurant.Restaurant;
import org.springframework.stereotype.Component;

@Component
public class TableMapper {

    public TableDTO toDTO(TableEntity entity) {
        return TableDTO.builder()
                .id(entity.getId())
                .tableNumber(entity.getTableNumber())
                .capacity(entity.getCapacity())
                .restaurantId(entity.getRestaurant().getId())
                .build();
    }

    public TableEntity toEntity(CreateTableRequest request, Restaurant restaurant) {
        return TableEntity.builder()
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity())
                .restaurant(restaurant)
                .build();
    }

    public void updateEntity(TableEntity entity, CreateTableRequest request) {
        if (request == null || entity == null) {
            return;
        }

        if (request.getTableNumber() != null) {
            entity.setTableNumber(request.getTableNumber());
        }
        if (request.getCapacity() != null) {
            entity.setCapacity(request.getCapacity());
        }
    }
}
