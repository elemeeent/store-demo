package org.example.storedemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.storedemo.config.SecurityConfig;
import org.example.storedemo.dto.ProductDto;
import org.example.storedemo.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductService productService;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("GET /products should return list of products (public)")
	void getAllProducts_shouldReturnList() throws Exception {
		// given
		List<ProductDto> products = List.of(
				new ProductDto(UUID.randomUUID(), "Apple", new BigDecimal("1.00"), 100, null),
				new ProductDto(UUID.randomUUID(), "Banana", new BigDecimal("2.00"), 50, null)
		);
		Pageable pageable = PageRequest.of(0, 8, Sort.by("name").ascending());
		Page<ProductDto> page = new PageImpl<>(products, pageable, products.size());

		when(productService.getAllProducts(pageable)).thenReturn(page);

		// when / then
		mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data").exists())
				.andExpect(jsonPath("$.data[0].attributes.name").value("Apple"))
				.andExpect(jsonPath("$.data[0].attributes.price").value("1.0"))
				.andExpect(jsonPath("$.data[0].attributes.stockQuantity").value("100"))
				.andExpect(jsonPath("$.data[1].attributes.name").value("Banana"))
				.andExpect(jsonPath("$.data[1].attributes.price").value("2.0"))
				.andExpect(jsonPath("$.data[1].attributes.stockQuantity").value("50"));
	}

}
