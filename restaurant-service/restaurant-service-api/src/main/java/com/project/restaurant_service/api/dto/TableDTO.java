package com.project.restaurant_service.api.dto;

import lombok.*;
import org.springframework.hateoas.server.core.Relation;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Relation(collectionRelation = "tables", itemRelation = "table")
public class TableDTO {
    private Long id;
    private String tableNumber;
    private Integer capacity;
    private Long restaurantId;
}
