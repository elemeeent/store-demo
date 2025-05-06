package org.example.storedemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.storedemo.config.SecurityConfig;
import org.example.storedemo.dto.ProductDto;
import org.example.storedemo.dto.request.ProductRequest;
import org.example.storedemo.dto.response.OrderSummaryDto;
import org.example.storedemo.dto.response.ProductCreateResponse;
import org.example.storedemo.entity.OrderStatus;
import org.example.storedemo.service.OrderService;
import org.example.storedemo.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ProductService productService;
	@MockitoBean
	private OrderService orderService;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@WithMockUser(username = "admin", roles = {"ADMIN"})
	@DisplayName("POST /admin/products should create product with authorization")
	void createProduct_withAuth_shouldSucceed() throws Exception {
		ProductRequest request = new ProductRequest("Apple", new BigDecimal("1.99"), 100);
		ProductDto productDto = new ProductDto(UUID.randomUUID(), "Apple", new BigDecimal("1.99"), 100);
		ProductCreateResponse response = new ProductCreateResponse(List.of(productDto), List.of());

		when(productService.createProduct(request)).thenReturn(response);

		mockMvc.perform(MockMvcRequestBuilders.post("/admin/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.attributes.createdProducts[0].name").value("Apple"));
	}

	@Test
	@DisplayName("POST /admin/products should be unauthorized without login")
	void createProduct_withoutAuth_shouldFail() throws Exception {
		ProductRequest request = new ProductRequest("Apple", new BigDecimal("1.99"), 100);

		mockMvc.perform(MockMvcRequestBuilders.post("/admin/products")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("DELETE /admin/products/{id} should delete product with auth")
	void deleteProduct_withAuth_shouldSucceed() throws Exception {
		UUID id = UUID.randomUUID();
		ProductDto dto = new ProductDto(id, "Banana", new BigDecimal("2.50"), 10, null);

		when(productService.deleteProduct(id)).thenReturn(dto);

		mockMvc.perform(MockMvcRequestBuilders.delete("/admin/products/{productId}", id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.attributes.name").value("Banana"));
	}

	@Test
	@DisplayName("DELETE /admin/products/{id} should be unauthorized without auth")
	void deleteProduct_withoutAuth_shouldFail() throws Exception {
		UUID id = UUID.randomUUID();
		mockMvc.perform(MockMvcRequestBuilders.delete("/admin/products/{productId}", id))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = {"ADMIN"})
	@DisplayName("GET /admin/orders should return list of orders")
	void getAllOrders_withAuth_shouldSucceed() throws Exception {
		OrderSummaryDto order = new OrderSummaryDto(UUID.randomUUID(), OrderStatus.CREATED, null, null, null, null);
		Page<OrderSummaryDto> page = new PageImpl<>(List.of(order), PageRequest.of(0, 25), 1);

		when(orderService.getAllOrders(PageRequest.of(0, 8, Sort.by("id")))).thenReturn(page);

		mockMvc.perform(get("/admin/orders"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].attributes.status").value(OrderStatus.CREATED.name()));
	}

	@Test
	@DisplayName("GET /admin/orders should be unauthorized without auth")
	void getAllOrders_withoutAuth_shouldFail() throws Exception {
		mockMvc.perform(get("/admin/orders"))
				.andExpect(status().isUnauthorized());
	}

}
