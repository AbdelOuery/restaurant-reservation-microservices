package com.project.restaurant_service.app.table;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<TableEntity, Long> {
    List<TableEntity> findByRestaurantId(Long restaurantId);
    List<TableEntity> findByRestaurantIdAndCapacityGreaterThanEqual(Long restaurantId, Integer numberOfPeople);
}
