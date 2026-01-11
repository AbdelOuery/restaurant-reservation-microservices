package com.project.restaurant_service.app.restaurant;

import com.project.restaurant_service.api.dto.RestaurantDTO;
import com.project.restaurant_service.api.dto.requests.CreateRestaurantRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/restaurant")
@RequiredArgsConstructor
@Tag(name = "Restaurants", description = "Restaurant Management API")
public class RestaurantController {

    private final RestaurantService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve all restaurants")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved restaurants"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public CollectionModel<EntityModel<RestaurantDTO>> getAll() {
        List<EntityModel<RestaurantDTO>> restaurants = service.getAll().stream()
                .map(this::toHateoasEntityModel)
                .toList();

        return CollectionModel.of(restaurants,
                linkTo(methodOn(RestaurantController.class).getAll()).withSelfRel());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve a single restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurant successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public EntityModel<RestaurantDTO> getOne(@PathVariable Long id) {
        return toHateoasEntityModel(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Restaurant successfully created"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public EntityModel<RestaurantDTO> create(@Valid @RequestBody CreateRestaurantRequest request) {
        return toHateoasEntityModel(service.create(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update a restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurant successfully updated"),
            @ApiResponse(responseCode = "404", description = "Restaurant Not Found")
    })
    public EntityModel<RestaurantDTO> update(@PathVariable Long id,
                                             @Valid @RequestBody CreateRestaurantRequest request) {
        return toHateoasEntityModel(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content, restaurant successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Restaurant Not Found")
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /**
     * Generates HATEOAS links inside the entity model object
     *
     * @param  restaurantDTO DTO Object to return
     * @return The entity model containing the DTO with the links
     */
    private EntityModel<RestaurantDTO> toHateoasEntityModel(RestaurantDTO restaurantDTO) {
        Link selfLink = linkTo(methodOn(RestaurantController.class).getOne(restaurantDTO.getId())).withSelfRel();
        Link allRestaurantsLink = linkTo(methodOn(RestaurantController.class).getAll()).withRel("all-restaurants");
        return EntityModel.of(restaurantDTO, selfLink, allRestaurantsLink);
    }
}