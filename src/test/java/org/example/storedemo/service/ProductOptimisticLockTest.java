package org.example.storedemo.service;

import org.example.storedemo.StoreDemoApplicationTests;
import org.example.storedemo.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ProductOptimisticLockTest extends StoreDemoApplicationTests {

	private UUID productId;

	@BeforeEach
	void setUp() {
		ProductEntity product = new ProductEntity();
		productId = UUID.randomUUID();
		product.setId(productId);
		product.setName("TestProduct");
		product.setPrice(BigDecimal.valueOf(10.00));
		product.setStockQuantity(100);
		productRepository.save(product);
	}

//	@Test
//	void testOptimisticLocking_shouldThrowExceptionOnConcurrentUpdate() throws InterruptedException {
//		// Thread-safe holder for exception
//		AtomicReference<Exception> threadException = new AtomicReference<>();
//
//		// Thread 1 loads and modifies the entity
//		ProductEntity product1 = productRepository.findById(productId).orElseThrow();
//		product1.setPrice(BigDecimal.valueOf(20.00));
//
//		Thread thread = new Thread(() -> {
//			try {
//				ProductEntity product2 = productRepository.findById(productId).orElseThrow();
//				product2.setPrice(BigDecimal.valueOf(30.00));
//				productRepository.saveAndFlush(product2); // commit changes
//			} catch (Exception e) {
//				threadException.set(e);
//			}
//		});
//
//		thread.start();
//		thread.join();
//
//		// Save from main thread (should fail if thread changed version)
//		assertThrows(OptimisticLockException.class, () -> {
//			productRepository.saveAndFlush(product1);
//		});
//
//		// Check that at least one thread experienced the lock conflict
//		Exception e = threadException.get();
//		if (e != null && !(e instanceof OptimisticLockException)) {
//			fail("Unexpected exception type: " + e.getClass().getSimpleName());
//		}
//	}

	@Test
	void testUpdateProduct_successful() {
		ProductEntity product = productRepository.findById(productId).orElseThrow();
		product.setStockQuantity(90);
		ProductEntity updated = productRepository.saveAndFlush(product);
		assertEquals(90, updated.getStockQuantity());
	}

//	@Test
//	void testOptimisticLocking_shouldThrowExceptionOnConcurrentUpdate1() {
//		// Given
//		ProductEntity original = productRepository.saveAndFlush(new ProductEntity(
//				UUID.randomUUID(),
//				"OptimisticLockingProduct",
//				new BigDecimal("1.0"),
//				10,
//				0));
//
//		// Load the same product in two separate entity managers / transactions
//		ProductEntity product1 = productRepository.findById(original.getId()).orElseThrow();
//		ProductEntity product2 = productRepository.findById(original.getId()).orElseThrow();
//
//		// Update and save first copy (commits version = version + 1)
//		product1.setStockQuantity(5);
//		productRepository.saveAndFlush(product1); // <-- this increments the @Version field
//
//		// Try saving second copy — should fail because it still has old version
//		product2.setStockQuantity(2);
//
//		assertThrows(OptimisticLockException.class, () -> {
//			productRepository.saveAndFlush(product2);
//		});
//	}
}