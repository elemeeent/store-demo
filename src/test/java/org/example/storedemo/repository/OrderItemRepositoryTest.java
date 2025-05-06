package org.example.storedemo.repository;

import org.example.storedemo.StoreDemoApplicationTests;
import org.example.storedemo.entity.OrderEntity;
import org.example.storedemo.entity.OrderItemEntity;
import org.example.storedemo.entity.OrderStatus;
import org.example.storedemo.entity.ProductEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class OrderItemRepositoryTest extends StoreDemoApplicationTests {

	@Test
	@DisplayName("Should save and find OrderItemEntity by ID")
	void saveAndFindById() {
		ProductEntity product = new ProductEntity();
		product.setId(UUID.randomUUID());
		product.setName("Test Product");
		product.setPrice(new BigDecimal("2.99"));
		product.setStockQuantity(100);
		productRepository.save(product);

		OrderEntity order = new OrderEntity();
		order.setId(UUID.randomUUID());
		order.setStatus(OrderStatus.CREATED);
		order.setCreatedAt(LocalDateTime.now(clock));
		order.setExpiresAt(LocalDateTime.now(clock).plusMinutes(30));
		orderRepository.save(order);

		OrderItemEntity item = new OrderItemEntity();
		item.setId(UUID.randomUUID());
		item.setOrderEntity(order);
		item.setProductEntity(product);
		item.setQuantity(3);
		item.setPriceSnapshot(new BigDecimal("2.99"));

		orderItemRepository.save(item);

		Optional<OrderItemEntity> found = orderItemRepository.findById(item.getId());
		assertThat(found).isPresent();
		assertThat(found.get().getQuantity()).isEqualTo(3);
		assertThat(found.get().getProductEntity().getId()).isEqualTo(product.getId());
	}

	@Test
	@DisplayName("Should delete OrderItemEntity")
	void deleteItem() {
		ProductEntity product = new ProductEntity();
		product.setId(UUID.randomUUID());
		product.setName("Delete Test Product");
		product.setPrice(new BigDecimal("3.99"));
		product.setStockQuantity(50);
		productRepository.save(product);

		OrderEntity order = new OrderEntity();
		order.setId(UUID.randomUUID());
		order.setStatus(OrderStatus.CREATED);
		order.setCreatedAt(LocalDateTime.now(clock));
		order.setExpiresAt(LocalDateTime.now(clock).plusMinutes(30));
		orderRepository.save(order);

		OrderItemEntity item = new OrderItemEntity();
		item.setId(UUID.randomUUID());
		item.setOrderEntity(order);
		item.setProductEntity(product);
		item.setQuantity(1);
		item.setPriceSnapshot(new BigDecimal("3.99"));
		orderItemRepository.save(item);

		orderItemRepository.deleteById(item.getId());
		Optional<OrderItemEntity> found = orderItemRepository.findById(item.getId());

		assertThat(found).isNotPresent();
	}

	@Test
	@DisplayName("Should return true if product exists in active orders (CREATED or PAID)")
	void existsByProductIdInActiveOrders_shouldReturnTrue() {
		UUID productId = UUID.randomUUID();
		ProductEntity product = new ProductEntity();
		product.setId(productId);
		product.setName("Active Order Product");
		product.setPrice(new BigDecimal("1.99"));
		product.setStockQuantity(20);
		productRepository.save(product);

		OrderEntity order = new OrderEntity();
		order.setId(UUID.randomUUID());
		order.setStatus(OrderStatus.CREATED);
		order.setCreatedAt(LocalDateTime.now(clock));
		order.setExpiresAt(LocalDateTime.now(clock).plusMinutes(30));
		orderRepository.save(order);

		OrderItemEntity item = new OrderItemEntity();
		item.setId(UUID.randomUUID());
		item.setOrderEntity(order);
		item.setProductEntity(product);
		item.setQuantity(2);
		item.setPriceSnapshot(product.getPrice());
		orderItemRepository.save(item);

		boolean exists = orderItemRepository.existsByProductIdInActiveOrders(productId);
		assertThat(exists).isTrue();
	}

	@Test
	@DisplayName("Should return false if product is not in any active orders")
	void existsByProductIdInActiveOrders_shouldReturnFalse() {
		UUID productId = UUID.randomUUID();
		ProductEntity product = new ProductEntity();
		product.setId(productId);
		product.setName("Non-active Order Product");
		product.setPrice(new BigDecimal("2.99"));
		product.setStockQuantity(50);
		productRepository.save(product);

		boolean exists = orderItemRepository.existsByProductIdInActiveOrders(productId);
		assertThat(exists).isFalse();
	}
}
