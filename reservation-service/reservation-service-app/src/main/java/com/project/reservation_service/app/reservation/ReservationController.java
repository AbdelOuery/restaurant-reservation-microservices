package com.project.reservation_service.app.reservation;

import com.project.reservation_service.api.dto.ReservationDTO;
import com.project.reservation_service.api.dto.requests.CreateReservationRequest;
import com.project.reservation_service.api.dto.requests.SearchReservationsRequest;
import com.project.reservation_service.api.enums.ReservationStatus;
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

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
@Tag(name = "Reservations", description = "Reservation Management API")
public class ReservationController {

    private final ReservationService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve all reservations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reservations"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public CollectionModel<EntityModel<ReservationDTO>> getAll() {
        List<EntityModel<ReservationDTO>> reservations = service.getAll().stream()
                .map(this::toEntityModel)
                .toList();

        return CollectionModel.of(reservations,
                linkTo(methodOn(ReservationController.class).getAll()).withSelfRel());
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve a reservation by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reservation"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public EntityModel<ReservationDTO> getById(@PathVariable Long id) {
        return toEntityModel(service.getById(id));
    }

    @GetMapping("/customer/phone/{phone}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve reservations by customer phone number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reservations"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public CollectionModel<EntityModel<ReservationDTO>> getAllForCustomerByPhone(@PathVariable String phone) {
        List<EntityModel<ReservationDTO>> reservations = service.getReservationsByCustomerPhone(phone).stream()
                .map(this::toEntityModel)
                .toList();

        return CollectionModel.of(reservations,
                linkTo(methodOn(ReservationController.class).getAllForCustomerByPhone(phone)).withSelfRel(),
                linkTo(methodOn(ReservationController.class).getAll()).withRel("all-reservations"));
    }

    @GetMapping("/customer/email/{email}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve reservations by customer email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reservations"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public CollectionModel<EntityModel<ReservationDTO>> getAllForCustomerByEmail(@PathVariable String email) {
        List<EntityModel<ReservationDTO>> reservations = service.getReservationsByCustomerEmail(email).stream()
                .map(this::toEntityModel)
                .toList();

        return CollectionModel.of(reservations,
                linkTo(methodOn(ReservationController.class).getAllForCustomerByEmail(email)).withSelfRel(),
                linkTo(methodOn(ReservationController.class).getAll()).withRel("all-reservations"));
    }

    @GetMapping("/restaurant/{restaurantId}/status/{status}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Retrieve reservations by restaurant and status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reservations"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public CollectionModel<EntityModel<ReservationDTO>> getForRestaurantByStatus(
            @PathVariable Long restaurantId,
            @PathVariable ReservationStatus status) {

        List<EntityModel<ReservationDTO>> reservations = service.getReservationsByRestaurantAndStatus(restaurantId, status).stream()
                .map(this::toEntityModel)
                .toList();

        return CollectionModel.of(reservations,
                linkTo(methodOn(ReservationController.class).getForRestaurantByStatus(restaurantId, status)).withSelfRel(),
                linkTo(methodOn(ReservationController.class).getAll()).withRel("all-reservations"));
    }

    @PostMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Search reservations by restaurant, date, and optionally time")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved reservations"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public CollectionModel<EntityModel<ReservationDTO>> search(@Valid @RequestBody SearchReservationsRequest request) {
        List<EntityModel<ReservationDTO>> reservations = service.getReservationsByRestaurantAndDateAndTime(
                        request.getRestaurantId(),
                        request.getDate(),
                        request.getTime()
                ).stream()
                .map(this::toEntityModel)
                .toList();

        return CollectionModel.of(reservations,
                linkTo(methodOn(ReservationController.class).search(request)).withSelfRel(),
                linkTo(methodOn(ReservationController.class).getAll()).withRel("all-reservations"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation successfully created"),
            @ApiResponse(responseCode = "400", description = "Bad Request - Validation failed"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
            @ApiResponse(responseCode = "409", description = "No availability")
    })
    public EntityModel<ReservationDTO> create(@Valid @RequestBody CreateReservationRequest request) {
        return toEntityModel(service.create(request));
    }

    @PatchMapping("/{id}/confirm")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Confirm a pending reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation successfully confirmed"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public EntityModel<ReservationDTO> confirm(@PathVariable Long id) {
        return toEntityModel(service.confirmReservation(id));
    }

    @PatchMapping("/{id}/reject")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reject a pending reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation successfully rejected"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public EntityModel<ReservationDTO> reject(@PathVariable Long id) {
        return toEntityModel(service.rejectReservation(id));
    }

    @PatchMapping("/{id}/check-in")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Check in a confirmed reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation successfully checked in"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public EntityModel<ReservationDTO> checkIn(@PathVariable Long id) {
        return toEntityModel(service.checkInReservation(id));
    }

    @PatchMapping("/{id}/complete")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Complete a checked-in reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation successfully completed"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "400", description = "Invalid status transition"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public EntityModel<ReservationDTO> complete(@PathVariable Long id) {
        return toEntityModel(service.completeReservation(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Cancel a reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation successfully canceled"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "400", description = "Cannot cancel completed or already canceled reservation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token")
    })
    public EntityModel<ReservationDTO> cancel(@PathVariable Long id) {
        return toEntityModel(service.cancelReservation(id));
    }

    /**
     * Generates HATEOAS links based on reservation status.
     * Following HATEOAS principles: links guide what actions are available.
     *
     * @param reservationDTO DTO object to enrich with links
     * @return EntityModel with state-based hypermedia links
     */
    private EntityModel<ReservationDTO> toEntityModel(ReservationDTO reservationDTO) {
        List<Link> links = new ArrayList<>();

        // Self link (always present)
        links.add(linkTo(methodOn(ReservationController.class)
                .getById(reservationDTO.getId()))
                .withSelfRel());

        // Customer reservations links
        links.add(linkTo(methodOn(ReservationController.class)
                .getAllForCustomerByEmail(reservationDTO.getCustomerEmail()))
                .withRel("customer-reservations-by-email"));

        links.add(linkTo(methodOn(ReservationController.class)
                .getAllForCustomerByPhone(reservationDTO.getCustomerPhone()))
                .withRel("customer-reservations-by-phone"));

        // All reservations link
        links.add(linkTo(methodOn(ReservationController.class)
                .getAll())
                .withRel("all-reservations"));

        // State-based action links (HATEOAS principle)
        addStateBasedLinks(links, reservationDTO);

        return EntityModel.of(reservationDTO, links);
    }

    /**
     * Add state-based action links.
     * This is a key HATEOAS principle: only show actions that are valid for the current state.
     *
     * @param links List of links
     * @param reservationDTO Current reservation
     */
    private void addStateBasedLinks(List<Link> links, ReservationDTO reservationDTO) {
        ReservationStatus status = ReservationStatus.valueOf(reservationDTO.getStatus());

        switch (status) {
            case PENDING:
                links.add(linkTo(methodOn(ReservationController.class)
                        .confirm(reservationDTO.getId()))
                        .withRel("confirm"));

                links.add(linkTo(methodOn(ReservationController.class)
                        .reject(reservationDTO.getId()))
                        .withRel("reject"));

                links.add(linkTo(methodOn(ReservationController.class)
                        .cancel(reservationDTO.getId()))
                        .withRel("cancel"));
                break;

            case CONFIRMED:
                links.add(linkTo(methodOn(ReservationController.class)
                        .checkIn(reservationDTO.getId()))
                        .withRel("check-in"));

                links.add(linkTo(methodOn(ReservationController.class)
                        .cancel(reservationDTO.getId()))
                        .withRel("cancel"));
                break;

            case CHECKED_IN:
                // CHECKED_IN → can only complete
                links.add(linkTo(methodOn(ReservationController.class)
                        .complete(reservationDTO.getId()))
                        .withRel("complete"));
                break;

            case COMPLETED:
            case CANCELED:
                break;
        }
    }
}