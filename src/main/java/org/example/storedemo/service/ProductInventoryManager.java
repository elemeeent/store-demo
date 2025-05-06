package org.example.storedemo.service;

import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storedemo.entity.ProductEntity;
import org.example.storedemo.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductInventoryManager {

	private final ProductRepository productRepository;
	private final ProductService productService;
	private static final int MAX_RETRIES = 3;

	public void adjustStockWithRetry(Map<UUID, Integer> quantityChanges, boolean releaseStock) {
		for (Map.Entry<UUID, Integer> entry : quantityChanges.entrySet()) {
			UUID productId = entry.getKey();
			int delta = entry.getValue() * (releaseStock ? 1 : -1);
			int retries = 0;
			boolean success = false;

			while (!success && retries < MAX_RETRIES) {
				try {
					ProductEntity product = productService.findProductById(productId);
					product.setStockQuantity(product.getStockQuantity() + delta);
					productRepository.saveAndFlush(product); // Optimistic lock trigger
					success = true;
				} catch (OptimisticLockException e) {
					log.warn("Optimistic lock on product {} (attempt {})", productId, retries + 1);
					retries++;
				}
			}

			if (!success) {
				throw new IllegalStateException("Failed to update stock for product " + productId + " after retries");
			}
		}
	}

}
