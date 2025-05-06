package org.example.storedemo.controller;

import com.slmdev.jsonapi.simple.response.Data;
import com.slmdev.jsonapi.simple.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.example.storedemo.dto.request.OrderCreateItemDto;
import org.example.storedemo.dto.response.OrderSummaryDto;
import org.example.storedemo.service.OrderService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Validated
@AllArgsConstructor
@RestController
@RequestMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
public class OrderController {

	private final OrderService orderService;

	@Operation(summary = "Create order with specified UUID and quantity via body")
	@PostMapping()
	public Response<Data<OrderSummaryDto>> createOrder(
			@Parameter(description = "List of items to create")
			@RequestBody List<@Valid OrderCreateItemDto> items
	) {
		return new Response.ResponseBuilder<Data<OrderSummaryDto>, OrderSummaryDto>()
				.data(orderService.createOrder(items))
				.build();
	}

	@Operation(summary = "Delete order with specified UUID")
	@DeleteMapping("/{id}")
	public Response<Data<OrderSummaryDto>> cancelOrder(
			@Parameter(description = "Id path variable to remove order")
			@PathVariable @NonNull UUID id
	) {
		return new Response.ResponseBuilder<Data<OrderSummaryDto>, OrderSummaryDto>()
				.data(orderService.cancelOrder(id))
				.build();
	}

	@Operation(summary = "Fetch order with specified UUID")
	@GetMapping("/{id}")
	public Response<Data<OrderSummaryDto>> getOrderSummary(
			@Parameter(description = "Id path variable for order info")
			@PathVariable @NonNull UUID id
	) {
		return new Response.ResponseBuilder<Data<OrderSummaryDto>, OrderSummaryDto>()
				.data(orderService.getOrderSummary(id))
				.build();
	}

}
