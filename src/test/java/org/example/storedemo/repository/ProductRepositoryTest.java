package org.example.storedemo.repository;

import org.example.storedemo.StoreDemoApplicationTests;
import org.example.storedemo.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductRepositoryTest extends StoreDemoApplicationTests {

	private final UUID productId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		ProductEntity product = new ProductEntity();
		product.setId(productId);
		product.setName("TestProduct");
		product.setPrice(BigDecimal.valueOf(9.99));
		product.setStockQuantity(100);
		productRepository.save(product);
	}

	@Test
	@DisplayName("Should return true if product exists by name, false otherwise")
	void testExistsByName() {
		assertThat(productRepository.existsByName("TestProduct")).isTrue();
		assertThat(productRepository.existsByName("NonExisting")).isFalse();
	}

	@Test
	@DisplayName("Should find product by name")
	void testFindByName() {
		Optional<ProductEntity> product = productRepository.findByNameIgnoreCase("TestProduct");
		assertThat(product).isPresent();
		assertThat(product.get().getName()).isEqualTo("TestProduct");
	}

	@Test
	@DisplayName("Should find product by ID")
	void testFindById() {
		Optional<ProductEntity> product = productRepository.findById(productId);
		assertThat(product).isPresent();
		assertThat(product.get().getId()).isEqualTo(productId);
	}

	@Test
	@DisplayName("Should delete product by ID")
	void testDeleteById() {
		productRepository.deleteById(productId);
		assertThat(productRepository.findById(productId)).isNotPresent();
	}

	@Test
	@DisplayName("Should return all products")
	void testFindAll() {
		assertThat(productRepository.findAll()).isNotEmpty();
	}
}