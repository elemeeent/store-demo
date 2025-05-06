package org.example.storedemo.component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.storedemo.dto.request.OrderCreateItemDto;
import org.example.storedemo.dto.ProductDto;
import org.example.storedemo.dto.request.ProductRequest;
import org.example.storedemo.dto.response.ProductCreateResponse;
import org.example.storedemo.service.OrderService;
import org.example.storedemo.service.ProductService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer {

	private final ProductService productService;
	private final OrderService orderService;

	@PostConstruct
	public void init() {
		// add products
		List<ProductRequest> productRequests = readProductsFromClasspath("mockData/products.csv");
		List<ProductCreateResponse> createdProductsResponse = productRequests.stream().map(productService::createProduct).toList();
		List<ProductDto> createdProducts = createdProductsResponse.stream()
				.map(ProductCreateResponse::getCreatedProducts)
				.toList()
				.stream()
				.flatMap(List::stream)
				.toList();

		List<OrderCreateItemDto> onePositionOrderItems = List.of(
				new OrderCreateItemDto(createdProducts.stream().filter(it -> it.getName().equals("Milk")).findFirst().get().getId(), 5)
		);
		List<OrderCreateItemDto> twoPositionsOrderItems = List.of(
				new OrderCreateItemDto(createdProducts.stream().filter(it -> it.getName().equals("Apples")).findFirst().get().getId(), 10),
				new OrderCreateItemDto(createdProducts.stream().filter(it -> it.getName().equals("Bananas")).findFirst().get().getId(), 20)
		);
		List<OrderCreateItemDto> threePositionsOrderItems = List.of(
				new OrderCreateItemDto(createdProducts.stream().filter(it -> it.getName().equals("Milk")).findFirst().get().getId(), 5),
				new OrderCreateItemDto(createdProducts.stream().filter(it -> it.getName().equals("Bread")).findFirst().get().getId(), 2),
				new OrderCreateItemDto(createdProducts.stream().filter(it -> it.getName().equals("Eggs")).findFirst().get().getId(), 12)
		);

		orderService.createOrder(onePositionOrderItems);
		orderService.createOrder(twoPositionsOrderItems);
		orderService.createOrder(threePositionsOrderItems);
	}

	private List<ProductRequest> readProductsFromClasspath(String resourcePath) {
		List<ProductRequest> products = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(new ClassPathResource(resourcePath).getInputStream()))) {

			String line;
			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split(",");
				if (tokens.length != 3) continue;

				String name = tokens[0].trim();
				BigDecimal price = new BigDecimal(tokens[1].trim());
				int quantity = Integer.parseInt(tokens[2].trim());

				products.add(new ProductRequest(name, price, quantity));
			}

		} catch (Exception e) {
			System.err.println("Failed to read CSV from classpath: " + e.getMessage());
		}

		return products;
	}
}
