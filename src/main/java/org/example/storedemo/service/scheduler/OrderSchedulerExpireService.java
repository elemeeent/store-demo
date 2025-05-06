package org.example.storedemo.service.scheduler;

import lombok.RequiredArgsConstructor;
import org.example.storedemo.entity.OrderEntity;
import org.example.storedemo.entity.OrderItemEntity;
import org.example.storedemo.entity.OrderStatus;
import org.example.storedemo.entity.ProductEntity;
import org.example.storedemo.repository.OrderRepository;
import org.example.storedemo.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderSchedulerExpireService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;

	@Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
	public OrderProcessingResult processExpiredOrders(List<OrderEntity> expiredOrders) {
		Set<ProductEntity> updatedProducts = new HashSet<>();
		int releasedProducts = 0;

		for (OrderEntity order : expiredOrders) {
			order.setStatus(OrderStatus.EXPIRED);
			for (OrderItemEntity item : order.getItems()) {
				ProductEntity product = item.getProductEntity();
				product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
				updatedProducts.add(product);
				releasedProducts += item.getQuantity();
			}
		}

		productRepository.saveAll(updatedProducts);
		orderRepository.saveAll(expiredOrders);
		return new OrderProcessingResult(expiredOrders.size(), releasedProducts);
	}

	public record OrderProcessingResult(int expiredOrders, int releasedProducts) {}
}
