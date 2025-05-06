package org.example.storedemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.storedemo.config.SecurityConfig;
import org.example.storedemo.dto.OrderSummaryItemDto;
import org.example.storedemo.dto.request.OrderCreateItemDto;
import org.example.storedemo.dto.response.OrderSummaryDto;
import org.example.storedemo.entity.OrderStatus;
import org.example.storedemo.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private final Clock clock = Clock.systemDefaultZone();

	@MockitoBean
	private OrderService orderService;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private final UUID orderId = UUID.randomUUID();

	private OrderSummaryDto sampleOrder() {
		LocalDateTime now = LocalDateTime.now(clock);
		return new OrderSummaryDto(
				orderId,
				OrderStatus.CREATED,
				now,
				now.plusMinutes(30),
				null,
				List.of(
						new OrderSummaryItemDto(UUID.randomUUID(), "Apple", new BigDecimal("1"), 5, new BigDecimal("3.98"))
				)
		);
	}

	@Test
	@DisplayName("GET /orders/{id} should return order summary")
	void getOrderSummary_shouldReturnData() throws Exception {
		when(orderService.getOrderSummary(orderId)).thenReturn(sampleOrder());

		mockMvc.perform(get("/orders/{id}", orderId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.attributes.products[0].totalPrice").value("3.98"))
				.andExpect(jsonPath("$.data.attributes.status").value("CREATED"));
	}

	@Test
	@DisplayName("POST /orders should create a new order")
	void createOrder_shouldReturnCreatedOrder() throws Exception {
		List<OrderCreateItemDto> items = List.of(new OrderCreateItemDto(UUID.randomUUID(), 2));
		when(orderService.createOrder(items)).thenReturn(sampleOrder());

		mockMvc.perform(MockMvcRequestBuilders.post("/orders")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(items)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.attributes.status").value("CREATED"))
				.andExpect(jsonPath("$.data.attributes.products[0].totalPrice").value("3.98"));
	}

	@Test
	@DisplayName("DELETE /orders?id={id} should cancel the order")
	void cancelOrder_shouldReturnCancelledOrder() throws Exception {
		when(orderService.cancelOrder(orderId)).thenReturn(sampleOrder());

		mockMvc.perform(MockMvcRequestBuilders.delete("/orders/{id}", orderId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.attributes.status").value("CREATED"));
	}
}
