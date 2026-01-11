package com.project.restaurant_service.app.table;

import com.project.restaurant_service.api.dto.TableDTO;
import com.project.restaurant_service.api.dto.requests.CreateTableRequest;
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
@RequestMapping("/api/table")
@RequiredArgsConstructor
@Tag(name = "Tables", description = "Restaurant Tables Management API")
public class TableController {

    private final TableService service;

    @GetMapping("/restaurant/{restaurantId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Retrieve all tables of a restaurant",
            description = "Retrieves all tables of a restaurant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved tables"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public CollectionModel<EntityModel<TableDTO>> getAllForRestaurant(@PathVariable Long restaurantId) {
        List<EntityModel<TableDTO>> tables = service.getAllForRestaurant(restaurantId).stream()
                .map(this::toHateoasEntityModel)
                .toList();

        return CollectionModel.of(tables,
                linkTo(methodOn(TableController.class).getAllForRestaurant(restaurantId)).withSelfRel());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Retrieve a single table",
            description = "Retrieves an existing table"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Table successfully retrieved"),
            @ApiResponse(responseCode = "404", description = "Table not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public EntityModel<TableDTO> getOne(@PathVariable Long id) {
        return toHateoasEntityModel(service.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new table for a restaurant",
            description = "Creates a new table for a restaurant"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Table successfully created"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })
    public EntityModel<TableDTO> create(@Valid @RequestBody CreateTableRequest request) {
        return toHateoasEntityModel(service.create(request));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            summary = "Update a table",
            description = "Updates an existing table"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Table successfully updated"),
            @ApiResponse(responseCode = "404", description = "Table Not Found")
    })
    public EntityModel<TableDTO> update(@PathVariable Long id,
                                        @Valid @RequestBody CreateTableRequest request) {
        return toHateoasEntityModel(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a table",
            description = "Deletes an existing table"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "No Content, table successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Table Not Found")
    })
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    /**
     * Generates HATEOAS links inside the entity model object
     *
     * @param  tableDTO DTO Object to return
     * @return The entity model containing the DTO with the links
     */
    private EntityModel<TableDTO> toHateoasEntityModel(TableDTO tableDTO) {
        Link selfLink = linkTo(methodOn(TableController.class).getOne(tableDTO.getId())).withSelfRel();
        Link allTablesLink = linkTo(methodOn(TableController.class).getAllForRestaurant(tableDTO.getRestaurantId())).withRel("all-tables-for-a-restaurant");
        return EntityModel.of(tableDTO, selfLink, allTablesLink);
    }
}