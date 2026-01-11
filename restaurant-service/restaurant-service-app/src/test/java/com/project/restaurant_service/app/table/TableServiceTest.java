package com.project.restaurant_service.app.table;

import com.project.restaurant_service.api.dto.TableDTO;
import com.project.restaurant_service.api.dto.requests.CreateTableRequest;
import com.project.restaurant_service.api.exception.RestaurantNotFoundException;
import com.project.restaurant_service.api.exception.TableNotFoundException;
import com.project.restaurant_service.app.restaurant.Restaurant;
import com.project.restaurant_service.app.restaurant.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private TableRepository tableRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private TableMapper tableMapper;

    @InjectMocks
    private TableService tableService;

    private Restaurant restaurant;
    private TableEntity tableEntity;
    private TableDTO tableDTO;
    private CreateTableRequest createRequest;

    @BeforeEach
    void setUp() {
        restaurant = Restaurant.builder()
                .id(1L)
                .name("Test Restaurant")
                .address("123 Test St")
                .phone("+33123456789")
                .email("test@restaurant.fr")
                .isClosed(false)
                .build();

        tableEntity = TableEntity.builder()
                .id(1L)
                .restaurant(restaurant)
                .tableNumber("T1")
                .capacity(4)
                .build();

        tableDTO = TableDTO.builder()
                .id(1L)
                .restaurantId(1L)
                .tableNumber("T1")
                .capacity(4)
                .build();

        createRequest = CreateTableRequest.builder()
                .restaurantId(1L)
                .tableNumber("T1")
                .capacity(4)
                .build();
    }

    @Test
    void shouldCreateTableWhenRestaurantExists() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));
        when(tableMapper.toEntity(createRequest, restaurant)).thenReturn(tableEntity);
        when(tableRepository.save(any(TableEntity.class))).thenReturn(tableEntity);
        when(tableMapper.toDTO(tableEntity)).thenReturn(tableDTO);

        TableDTO result = tableService.create(createRequest);

        assertNotNull(result);
        assertEquals("T1", result.getTableNumber());
        assertEquals(4, result.getCapacity());
        verify(restaurantRepository).findById(1L);
        verify(tableRepository).save(any(TableEntity.class));
    }

    @Test
    void shouldThrowExceptionWhenRestaurantNotFound() {
        when(restaurantRepository.findById(999L)).thenReturn(Optional.empty());

        CreateTableRequest invalidRequest = CreateTableRequest.builder()
                .restaurantId(999L)
                .tableNumber("T1")
                .capacity(4)
                .build();

        assertThrows(RestaurantNotFoundException.class, () -> {
            tableService.create(invalidRequest);
        });

        verify(restaurantRepository).findById(999L);
        verify(tableRepository, never()).save(any(TableEntity.class));
    }

    @Test
    void shouldGetTableById() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(tableEntity));
        when(tableMapper.toDTO(tableEntity)).thenReturn(tableDTO);

        TableDTO result = tableService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("T1", result.getTableNumber());
        verify(tableRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenTableNotFound() {
        when(tableRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TableNotFoundException.class, () -> {
            tableService.getById(999L);
        });
    }

    @Test
    void shouldGetTablesByRestaurantId() {
        when(restaurantRepository.findById(1L)).thenReturn(Optional.of(restaurant));

        when(tableRepository.findByRestaurantId(1L)).thenReturn(List.of(tableEntity));
        when(tableMapper.toDTO(any(TableEntity.class))).thenReturn(tableDTO);

        List<TableDTO> result = tableService.getAllForRestaurant(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("T1", result.get(0).getTableNumber());
        verify(tableRepository).findByRestaurantId(1L);
    }
}