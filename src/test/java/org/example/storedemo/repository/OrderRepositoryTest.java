package org.example.storedemo.repository;

import org.example.storedemo.StoreDemoApplicationTests;
import org.example.storedemo.entity.OrderEntity;
import org.example.storedemo.entity.OrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryTest extends StoreDemoApplicationTests {

	@Test
	@DisplayName("Should save and find OrderEntity by ID")
	void saveAndFindById() {
		OrderEntity order = new OrderEntity();
		order.setId(UUID.randomUUID());
		order.setStatus(OrderStatus.CREATED);
		order.setCreatedAt(LocalDateTime.now());
		order.setExpiresAt(LocalDateTime.now().plusMinutes(30));

		orderRepository.save(order);

		Optional<OrderEntity> found = orderRepository.findById(order.getId());
		assertThat(found).isPresent();
		assertThat(found.get().getStatus()).isEqualTo(OrderStatus.CREATED);
	}

	@Test
	@DisplayName("Should delete OrderEntity")
	void deleteOrder() {
		OrderEntity order = new OrderEntity();
		order.setId(UUID.randomUUID());
		order.setStatus(OrderStatus.CREATED);
		order.setCreatedAt(LocalDateTime.now());
		order.setExpiresAt(LocalDateTime.now().plusMinutes(30));

		orderRepository.save(order);
		orderRepository.deleteById(order.getId());

		Optional<OrderEntity> deleted = orderRepository.findById(order.getId());
		assertThat(deleted).isNotPresent();
	}

	@Test
	@DisplayName("Should find expired orders with specific status")
	void findByStatusAndExpiresAtBefore() {
		OrderEntity expiredOrder = new OrderEntity();
		expiredOrder.setId(UUID.randomUUID());
		expiredOrder.setStatus(OrderStatus.CREATED);
		expiredOrder.setCreatedAt(LocalDateTime.now().minusHours(1));
		expiredOrder.setExpiresAt(LocalDateTime.now().minusMinutes(10));

		OrderEntity validOrder = new OrderEntity();
		validOrder.setId(UUID.randomUUID());
		validOrder.setStatus(OrderStatus.CREATED);
		validOrder.setCreatedAt(LocalDateTime.now());
		validOrder.setExpiresAt(LocalDateTime.now().plusMinutes(30));

		orderRepository.saveAll(List.of(expiredOrder, validOrder));

		Page<OrderEntity> result = orderRepository.findByStatusAndExpiresAtBeforeWithItems(
				OrderStatus.CREATED,
				LocalDateTime.now(),
				Pageable.ofSize(10)
		);

		assertThat(result).isNotEmpty();
		List<UUID> resultIds = result.stream().map(OrderEntity::getId).toList();
		assertThat(resultIds).contains(expiredOrder.getId());
		assertThat(resultIds).doesNotContain(validOrder.getId());
		OrderEntity resultExpiredOrder = result.stream()
				.filter(orderEntity -> orderEntity.getId().equals(expiredOrder.getId()))
				.findFirst()
				.get();
		assertThat(resultExpiredOrder.getExpiresAt()).isEqualTo(expiredOrder.getExpiresAt());
		assertThat(resultExpiredOrder.getCreatedAt()).isEqualTo(expiredOrder.getCreatedAt());
		assertThat(resultExpiredOrder.getStatus()).isEqualTo(expiredOrder.getStatus());
		assertThat(resultExpiredOrder.getPaidAt()).isEqualTo(expiredOrder.getPaidAt());
	}
}
