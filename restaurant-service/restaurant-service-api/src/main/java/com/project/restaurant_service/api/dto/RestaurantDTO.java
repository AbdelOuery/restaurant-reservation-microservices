package com.project.restaurant_service.api.dto;

import lombok.*;
import org.springframework.hateoas.server.core.Relation;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Relation(collectionRelation = "restaurants", itemRelation = "restaurant")
public class RestaurantDTO {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private Boolean isClosed;
}
