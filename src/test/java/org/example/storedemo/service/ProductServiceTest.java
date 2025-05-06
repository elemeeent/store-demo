package org.example.storedemo.service;

import org.example.storedemo.StoreDemoApplicationTests;
import org.example.storedemo.dto.ProductDto;
import org.example.storedemo.dto.request.ProductRequest;
import org.example.storedemo.dto.response.ProductCreateResponse;
import org.example.storedemo.exception.BadRequestException;
import org.example.storedemo.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProductServiceTest extends StoreDemoApplicationTests {

	@Test
	@DisplayName("Should create new product")
	void testCreateProduct() {
		ProductRequest request = new ProductRequest("New Test Product", BigDecimal.valueOf(10.99), 100);
		ProductCreateResponse response = productService.createProduct(request);

		assertThat(response.getCreatedProducts().size()).isEqualTo(1);
		assertThat(response.getErrorProducts()).isEmpty();
		assertThat(response.getCreatedProducts().get(0).getName()).isEqualTo("New Test Product");
	}

	@Test
	@DisplayName("Should not create product with existing name")
	void testCreateProductDuplicateName() {
		ProductRequest request = new ProductRequest("Duplicate Product", BigDecimal.valueOf(5.0), 10);
		productService.createProduct(request);

		ProductCreateResponse response = productService.createProduct(request);

		assertThat(response.getCreatedProducts()).isEmpty();
		assertThat(response.getErrorProducts()).hasSize(1);
		assertThat(response.getErrorProducts().get(0).getErrorMessage()).contains("already exists");
	}

	@Test
	@DisplayName("Should delete existing product")
	void testDeleteProduct() {
		ProductRequest request = new ProductRequest("Deletable Product", BigDecimal.valueOf(8.0), 10);
		ProductCreateResponse response = productService.createProduct(request);
		ProductDto dto = response.getCreatedProducts().get(0);

		ProductDto deleted = productService.deleteProduct(dto.getId());
		assertThat(deleted.getId()).isEqualTo(dto.getId());
	}

	@Test
	@DisplayName("Should throw exception if product does not exist when deleting")
	void testDeleteNonExistingProduct() {
		assertThrows(NotFoundException.class, () -> productService.deleteProduct(UUID.randomUUID()));
	}

	@Test
	@DisplayName("Should update products")
	void testUpdateProducts() {
		ProductRequest request = new ProductRequest("Updatable Product", BigDecimal.valueOf(12.0), 10);
		ProductCreateResponse response = productService.createProduct(request);
		ProductDto created = response.getCreatedProducts().get(0);

		ProductRequest update = new ProductRequest("Updatable Product", BigDecimal.valueOf(20.0), 5);
		Map<UUID, ProductRequest> updates = new HashMap<>();
		updates.put(created.getId(), update);

		List<ProductDto> updated = productService.updateProduct(updates);
		assertThat(updated).hasSize(1);
		assertThat(updated.get(0).getPrice()).isEqualTo(BigDecimal.valueOf(20.0));
	}

	@Test
	@DisplayName("Should throw error for empty update map")
	void testUpdateEmptyMap() {
		assertThrows(BadRequestException.class, () -> productService.updateProduct(Map.of()));
	}


}
