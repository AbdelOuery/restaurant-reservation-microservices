package com.project.restaurant_service.app.restaurant;

import com.project.restaurant_service.api.dto.RestaurantDTO;
import com.project.restaurant_service.api.dto.requests.CreateRestaurantRequest;
import com.project.restaurant_service.api.exception.RestaurantNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository repository;
    private final RestaurantMapper mapper;

    @Transactional
    public RestaurantDTO getById(Long id) {
        Restaurant entity = repository.findById(id)
                .orElseThrow(RestaurantNotFoundException::new);
        return mapper.toDTO(entity);
    }

    @Transactional
    public List<RestaurantDTO> getAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public RestaurantDTO create(CreateRestaurantRequest request) {
        Restaurant entity = mapper.toEntity(request);
        Restaurant saved = repository.save(entity);
        return mapper.toDTO(saved);
    }

    @Transactional
    public RestaurantDTO update(Long id, CreateRestaurantRequest request) {
        Restaurant current = repository.findById(id)
            .orElseThrow(RestaurantNotFoundException::new);
        mapper.updateEntity(current, request);
        Restaurant updated = repository.save(current);
        return mapper.toDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RestaurantNotFoundException();
        }
        repository.deleteById(id);
    }
}
