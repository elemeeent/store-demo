package org.example.storedemo.service;

import org.example.storedemo.StoreDemoApplicationTests;
import org.example.storedemo.dto.request.OrderCreateItemDto;
import org.example.storedemo.dto.response.OrderSummaryDto;
import org.example.storedemo.entity.OrderStatus;
import org.example.storedemo.entity.ProductEntity;
import org.example.storedemo.exception.BadRequestException;
import org.example.storedemo.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class OrderServiceTest extends StoreDemoApplicationTests {

	private UUID productId;

	@BeforeEach
	void setUp() {
		ProductEntity product = new ProductEntity();
		productId = UUID.randomUUID();
		product.setId(productId);
		product.setName("TestProduct" + UUID.randomUUID());
		product.setPrice(BigDecimal.valueOf(10));
		product.setStockQuantity(100);
		productRepository.save(product);
	}

	@Test
	@DisplayName("Should create order successfully")
	void testCreateOrder() {
		OrderCreateItemDto item = new OrderCreateItemDto(productId, 2);
		OrderSummaryDto order = orderService.createOrder(List.of(item));

		assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
		assertThat(order.getProducts().size()).isEqualTo(1);
	}

	@Test
	@DisplayName("Should throw on create order with nonexistent product")
	void testCreateOrderMissingProduct() {
		UUID nonexistentProductId = UUID.randomUUID();
		OrderCreateItemDto item = new OrderCreateItemDto(nonexistentProductId, 2);

		assertThatThrownBy(() -> orderService.createOrder(List.of(item)))
				.isInstanceOf(NotFoundException.class);
	}

	@Test
	@DisplayName("Should cancel order and restore stock")
	void testCancelOrder() {
		OrderCreateItemDto item = new OrderCreateItemDto(productId, 3);
		OrderSummaryDto createdOrder = orderService.createOrder(List.of(item));
		UUID orderId = createdOrder.getOrderId();

		OrderSummaryDto canceledOrder = orderService.cancelOrder(orderId);

		assertThat(canceledOrder.getStatus()).isEqualTo(OrderStatus.CANCELED);
		ProductEntity product = productRepository.findById(productId).orElseThrow();
		assertThat(product.getStockQuantity()).isEqualTo(100);
	}

	@Test
	@DisplayName("Should not cancel already paid order")
	void testCancelPaidOrder() {
		OrderCreateItemDto item = new OrderCreateItemDto(productId, 1);
		OrderSummaryDto order = orderService.createOrder(List.of(item));
		orderService.payOrder(order.getOrderId());

		assertThatThrownBy(() -> orderService.cancelOrder(order.getOrderId()))
				.isInstanceOf(BadRequestException.class);
	}

	@Test
	@DisplayName("Should throw on pay expired order")
	void testPayExpiredOrder() {
		OrderCreateItemDto item = new OrderCreateItemDto(productId, 1);
		OrderSummaryDto order = orderService.createOrder(List.of(item));
		var entity = orderRepository.findById(order.getOrderId()).orElseThrow();
		entity.setExpiresAt(entity.getExpiresAt().minusMinutes(31));
		orderRepository.save(entity);

		assertThatThrownBy(() -> orderService.payOrder(order.getOrderId()))
				.isInstanceOf(BadRequestException.class);
	}
}
