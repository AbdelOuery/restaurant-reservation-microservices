package com.project.restaurant_service.app.table;

import com.project.restaurant_service.api.dto.TableDTO;
import com.project.restaurant_service.api.dto.requests.CreateTableRequest;
import com.project.restaurant_service.api.exception.RestaurantNotFoundException;
import com.project.restaurant_service.api.exception.TableNotFoundException;
import com.project.restaurant_service.app.restaurant.Restaurant;
import com.project.restaurant_service.app.restaurant.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository repository;
    private final RestaurantRepository restaurantRepository;
    private final TableMapper mapper;

    @Transactional
    public TableDTO getById(Long id) {
        TableEntity entity = repository.findById(id)
                .orElseThrow(TableNotFoundException::new);
        return mapper.toDTO(entity);
    }

    @Transactional
    public List<TableDTO> getAllForRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(RestaurantNotFoundException::new);

        return repository.findByRestaurantId(restaurantId)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public TableDTO create(CreateTableRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(RestaurantNotFoundException::new);

        TableEntity entity = mapper.toEntity(request, restaurant);
        TableEntity saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Transactional
    public TableDTO update(Long id, CreateTableRequest request) {
        TableEntity current = repository.findById(id)
                .orElseThrow(TableNotFoundException::new);

        if (!restaurantRepository.existsById(request.getRestaurantId())) {
            throw new RestaurantNotFoundException();
        }

        mapper.updateEntity(current, request);
        TableEntity updated = repository.save(current);
        return mapper.toDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new TableNotFoundException();
        }
        repository.deleteById(id);
    }
}
