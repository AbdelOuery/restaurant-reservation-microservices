package com.project.restaurant_service.app.table;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.restaurant_service.api.dto.requests.CreateTableRequest;
import com.project.restaurant_service.app.restaurant.Restaurant;
import com.project.restaurant_service.app.restaurant.RestaurantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TableControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private Restaurant testRestaurant;
    private TableEntity testTable;

    @BeforeEach
    void setUp() {
        tableRepository.deleteAll();
        restaurantRepository.deleteAll();

        testRestaurant = Restaurant.builder()
                .name("Test Restaurant")
                .address("123 Test Street")
                .phone("+33123456789")
                .email("test@restaurant.fr")
                .isClosed(false)
                .build();
        testRestaurant = restaurantRepository.save(testRestaurant);

        testTable = TableEntity.builder()
                .restaurant(testRestaurant)
                .tableNumber("T1")
                .capacity(4)
                .build();
        testTable = tableRepository.save(testTable);
    }

    @Test
    void shouldCreateTable() throws Exception {
        CreateTableRequest request = CreateTableRequest.builder()
                .restaurantId(testRestaurant.getId())
                .tableNumber("T2")
                .capacity(6)
                .build();

        mockMvc.perform(post("/api/table")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tableNumber").value("T2"))
                .andExpect(jsonPath("$.capacity").value(6))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links['all-tables-for-a-restaurant'].href").exists());
    }

    @Test
    void shouldReturn404WhenCreatingTableForNonExistentRestaurant() throws Exception {
        CreateTableRequest request = CreateTableRequest.builder()
                .restaurantId(999L)
                .tableNumber("T2")
                .capacity(4)
                .build();

        mockMvc.perform(post("/api/table")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void shouldGetTableById() throws Exception {
        mockMvc.perform(get("/api/table/{id}", testTable.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTable.getId()))
                .andExpect(jsonPath("$.tableNumber").value("T1"))
                .andExpect(jsonPath("$.capacity").value(4))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links['all-tables-for-a-restaurant'].href").exists());
    }

    @Test
    void shouldReturn404WhenTableNotFound() throws Exception {
        mockMvc.perform(get("/api/table/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void shouldGetTablesByRestaurantId() throws Exception {
        mockMvc.perform(get("/api/table/restaurant/{restaurantId}", testRestaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.tableDTOList", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$._embedded.tableDTOList[0].tableNumber").value("T1"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void shouldUpdateTable() throws Exception {
        CreateTableRequest updateRequest = CreateTableRequest.builder()
                .restaurantId(testRestaurant.getId())
                .tableNumber("T1-Updated")
                .capacity(8)
                .build();

        mockMvc.perform(put("/api/table/{id}", testTable.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tableNumber").value("T1-Updated"))
                .andExpect(jsonPath("$.capacity").value(8))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links['all-tables-for-a-restaurant'].href").exists());
    }

    @Test
    void shouldDeleteTable() throws Exception {
        mockMvc.perform(delete("/api/table/{id}", testTable.getId()))
                .andExpect(status().isNoContent());

        // Verify table is deleted
        mockMvc.perform(get("/api/table/{id}", testTable.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldValidateTableCapacity() throws Exception {
        CreateTableRequest invalidRequest = CreateTableRequest.builder()
                .restaurantId(testRestaurant.getId())
                .tableNumber("T2")
                .capacity(0)
                .build();

        mockMvc.perform(post("/api/table")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.capacity").exists());
    }
}