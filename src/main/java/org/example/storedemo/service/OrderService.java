package org.example.storedemo.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.storedemo.dto.request.OrderCreateItemDto;
import org.example.storedemo.dto.response.OrderSummaryDto;
import org.example.storedemo.entity.*;
import org.example.storedemo.exception.BadRequestException;
import org.example.storedemo.exception.NoStockAvailableException;
import org.example.storedemo.exception.NotFoundException;
import org.example.storedemo.mapper.OrderMapper;
import org.example.storedemo.repository.OrderRepository;
import org.example.storedemo.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@AllArgsConstructor
public class OrderService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final PaymentService paymentService;
	private final ProductInventoryManager productInventoryManager;
	private final OrderMapper orderMapper;
	private final Clock clock;

	public Page<OrderSummaryDto> getAllOrders(Pageable pageable) {
		log.info("Requesting all orders with params: {}", pageable);
		return orderRepository.findAll(pageable).map(orderMapper::toSummaryDto);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
	public OrderSummaryDto payOrder(UUID orderId) {
		log.info("Attempting to pay order: {}", orderId);
		OrderEntity order = getOrderById(orderId);

		if (order.getStatus() != OrderStatus.CREATED) {
			throw new BadRequestException("Order: " + orderId + " cannot be paid in its current state: " + order.getStatus());
		}
		if (order.getStatus() == OrderStatus.PAID) {
			throw new BadRequestException("Order: " + orderId + " was already paid");
		}
		if (order.getExpiresAt().isBefore(LocalDateTime.now(clock))) {
			throw new BadRequestException("Order has expired");
		}

		paymentService.pay(order.toString()); // mock implementation
		order.setStatus(OrderStatus.PAID);
		order.setExpiresAt(null);
		order.setPaidAt(LocalDateTime.now(clock));

		OrderEntity savedOrder = orderRepository.save(order);
		log.info("Successfully paid order: {}", orderId);
		return orderMapper.toSummaryDto(savedOrder);
	}

	@Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
	public OrderSummaryDto createOrder(List<OrderCreateItemDto> items) {
		validateItemsNotEmpty(items);

		log.info("Creating new order with items: {}", items);
		Map<UUID, Integer> quantityMap = toQuantityMap(items);

		List<ProductEntity> products = fetchProductsAndValidateQuantity(quantityMap);
		productInventoryManager.adjustStockWithRetry(quantityMap, false);

		OrderEntity order = buildOrderFromProducts(products, quantityMap);
		OrderEntity saved = orderRepository.save(order);

		log.info("Created order {} with {} items", saved.getId(), saved.getItems().size());
		return orderMapper.toSummaryDto(saved);
	}

	@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
	public OrderSummaryDto cancelOrder(UUID orderId) {
		log.info("Attempting to cancel order: {}", orderId);
		OrderEntity order = getOrderById(orderId);

		if (order.getStatus() != OrderStatus.CREATED) {
			throw new BadRequestException("Order " + orderId + " cannot be cancelled");
		}

		updateProductStockForCancel(order);
		order.setStatus(OrderStatus.CANCELED);
		order.setExpiresAt(null);

		OrderEntity saved = orderRepository.save(order);
		log.info("Order {} canceled", saved.getId());
		return orderMapper.toSummaryDto(saved);
	}

	public OrderSummaryDto getOrderSummary(UUID orderId) {
		log.info("Fetching summary for order: {}", orderId);
		return orderMapper.toSummaryDto(getOrderById(orderId));
	}

	private void validateItemsNotEmpty(List<OrderCreateItemDto> items) {
		if (items == null || items.isEmpty()) {
			throw new BadRequestException("Order must contain at least one item.");
		}
	}

	private OrderEntity getOrderById(UUID orderId) {
		return orderRepository.findById(orderId)
				.orElseThrow(() -> new NotFoundException("Order not found"));
	}

	private Map<UUID, Integer> toQuantityMap(List<OrderCreateItemDto> items) {
		return items.stream()
				.collect(Collectors.toMap(
						OrderCreateItemDto::getProductId,
						OrderCreateItemDto::getQuantity,
						Integer::sum));
	}

	private List<ProductEntity> fetchProductsAndValidateQuantity(Map<UUID, Integer> quantityMap) {
		List<ProductEntity> products = productRepository.findAllById(quantityMap.keySet());

		Set<UUID> foundIds = products.stream()
				.map(ProductEntity::getId)
				.collect(Collectors.toSet());

		List<UUID> missing = quantityMap.keySet().stream()
				.filter(id -> !foundIds.contains(id))
				.toList();

		if (!missing.isEmpty()) {
			throw new NotFoundException("Products not found: " + missing.size() + " item(s): " + missing);
		}

		Set<ProductAmountToRequested> outOfStock = products.stream()
				.filter(p -> p.getStockQuantity() < quantityMap.get(p.getId()))
				.map(p -> new ProductAmountToRequested(p.getName(), p.getStockQuantity(), quantityMap.get(p.getId())))
				.collect(Collectors.toSet());

		if (!outOfStock.isEmpty()) {
			throw new NoStockAvailableException("Insufficient stock for: " + outOfStock);
		}

		return products;
	}

	private void updateProductStockForCancel(OrderEntity order) {
		Map<UUID, Integer> quantityMap = order.getItems().stream()
				.collect(Collectors.toMap(
						i -> i.getProductEntity().getId(),
						OrderItemEntity::getQuantity));

		List<ProductEntity> products = order.getItems().stream()
				.map(OrderItemEntity::getProductEntity)
				.toList();

		productInventoryManager.adjustStockWithRetry(quantityMap, true);
	}

	private OrderEntity buildOrderFromProducts(List<ProductEntity> products, Map<UUID, Integer> quantityMap) {
		LocalDateTime now = LocalDateTime.now(clock);
		OrderEntity order = new OrderEntity();
		order.setId(UUID.randomUUID());
		order.setStatus(OrderStatus.CREATED);
		order.setCreatedAt(now);
		order.setExpiresAt(now.plusMinutes(30));

		List<OrderItemEntity> items = products.stream()
				.map(p -> OrderItemEntity.builder()
						.id(UUID.randomUUID())
						.productEntity(p)
						.quantity(quantityMap.get(p.getId()))
						.priceSnapshot(p.getPrice().multiply(BigDecimal.valueOf(quantityMap.get(p.getId()))))
						.orderEntity(order)
						.build())
				.toList();

		order.setItems(items);
		return order;
	}

	@Data
	@AllArgsConstructor
	private static class ProductAmountToRequested {
		private String productName;
		private int quantity;
		private int requestedQuantity;
	}
}