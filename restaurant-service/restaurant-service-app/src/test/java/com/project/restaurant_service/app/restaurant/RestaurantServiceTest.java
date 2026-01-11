package com.project.restaurant_service.app.restaurant;

import com.project.restaurant_service.api.dto.RestaurantDTO;
import com.project.restaurant_service.api.dto.requests.CreateRestaurantRequest;
import com.project.restaurant_service.api.exception.RestaurantNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @Mock
    private RestaurantRepository repository;

    @Mock
    private RestaurantMapper mapper;

    @InjectMocks
    private RestaurantService service;

    private CreateRestaurantRequest createRequest;
    private Restaurant restaurant;
    private RestaurantDTO restaurantDTO;

    @BeforeEach
    void setUp() {
        createRequest = CreateRestaurantRequest.builder()
                .name("Test Restaurant")
                .address("123 Test St")
                .phone("+33123456789")
                .email("test@restaurant.fr")
                .isClosed(false)
                .build();

        restaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .address("123 Test St")
                .phone("+33123456789")
                .email("test@restaurant.fr")
                .isClosed(false)
                .build();

        restaurantDTO = RestaurantDTO.builder()
                .id(1L)
                .name("Test Restaurant")
                .address("123 Test St")
                .build();
    }

    @Test
    void shouldCreateRestaurant() {
        when(mapper.toEntity(any(CreateRestaurantRequest.class))).thenReturn(restaurant);
        when(repository.save(any(Restaurant.class))).thenReturn(restaurant);
        when(mapper.toDTO(any(Restaurant.class))).thenReturn(restaurantDTO);

        RestaurantDTO result = service.create(createRequest);

        assertNotNull(result);
        assertEquals("Test Restaurant", result.getName());
        verify(repository).save(any(Restaurant.class));
    }

    @Test
    void shouldGetRestaurantById() {
        when(repository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(mapper.toDTO(restaurant)).thenReturn(restaurantDTO);

        RestaurantDTO result = service.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowExceptionWhenRestaurantNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RestaurantNotFoundException.class, () -> {
            service.getById(999L);
        });
    }
}