package com.project.restaurant_service.app.restaurant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.restaurant_service.api.dto.requests.CreateRestaurantRequest;
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
class RestaurantControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestaurantRepository repository;

    private Restaurant testRestaurant;

    @BeforeEach
    void setUp() {
        repository.deleteAll();

        testRestaurant = Restaurant.builder()
                .name("Test Restaurant")
                .address("123 Test Street")
                .phone("+33123456789")
                .email("test@restaurant.fr")
                .isClosed(false)
                .build();

        testRestaurant = repository.save(testRestaurant);
    }

    @Test
    void shouldCreateRestaurant() throws Exception {
        CreateRestaurantRequest request = CreateRestaurantRequest.builder()
                .name("New Restaurant")
                .address("456 New Street")
                .phone("+33987654321")
                .email("new@restaurant.fr")
                .isClosed(false)
                .build();

        mockMvc.perform(post("/api/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Restaurant"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links['all-restaurants'].href").exists());
    }

    @Test
    void shouldGetRestaurantById() throws Exception {
        mockMvc.perform(get("/api/restaurant/{id}", testRestaurant.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRestaurant.getId()))
                .andExpect(jsonPath("$.name").value("Test Restaurant"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links['all-restaurants'].href").exists());
    }

    @Test
    void shouldGetAllRestaurants() throws Exception {
        mockMvc.perform(get("/api/restaurant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.restaurantDTOList", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void shouldValidatePhoneNumber() throws Exception {
        CreateRestaurantRequest request = CreateRestaurantRequest.builder()
                .name("Test")
                .address("Address")
                .phone("invalid-phone")
                .email("test@test.fr")
                .isClosed(false)
                .build();

        mockMvc.perform(post("/api/restaurant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phone").exists());
    }
}