package org.example.storedemo.controller;

import com.slmdev.jsonapi.simple.response.Data;
import com.slmdev.jsonapi.simple.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.example.storedemo.dto.response.OrderSummaryDto;
import org.example.storedemo.service.OrderService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/payments", produces = MediaType.APPLICATION_JSON_VALUE)
public class PaymentController {

	private final OrderService orderService;

	@Operation(summary = "Payment request for specified order id")
	@PostMapping("/{orderId}")
	public Response<Data<OrderSummaryDto>> payOrder(
			@Parameter(description = "Order id pay for")
			@PathVariable @NonNull UUID orderId
	) {
		return new Response.ResponseBuilder<Data<OrderSummaryDto>, OrderSummaryDto>()
				.data(orderService.payOrder(orderId))
				.build();
	}
}
