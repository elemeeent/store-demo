package org.example.storedemo.controller;

import com.slmdev.jsonapi.simple.response.Data;
import com.slmdev.jsonapi.simple.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.example.storedemo.dto.ProductDto;
import org.example.storedemo.dto.request.ProductRequest;
import org.example.storedemo.dto.response.OrderSummaryDto;
import org.example.storedemo.dto.response.ProductCreateResponse;
import org.example.storedemo.service.OrderService;
import org.example.storedemo.service.ProductService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

	private final ProductService productService;
	private final OrderService orderService;
	private static final String ADMIN_TAG = "admin";

	@Operation(summary = "Create new products via provided data", tags = ADMIN_TAG)
	@PostMapping("/products")
	public Response<Data<ProductCreateResponse>> createProduct(
			@Parameter(description = "List of products to create")
			@RequestBody @Valid ProductRequest productRequests
	) {
		return new Response.ResponseBuilder<Data<ProductCreateResponse>, ProductCreateResponse>()
				.data(productService.createProduct(productRequests))
				.build();
	}

	@Operation(summary = "Delete product by id", tags = ADMIN_TAG)
	@DeleteMapping("/products/{productId}")
	public Response<Data<ProductDto>> deleteProduct(
			@Parameter(description = "Product id")
			@PathVariable @NonNull UUID productId
	) {
		return new Response.ResponseBuilder<Data<ProductDto>, ProductDto>()
				.data(productService.deleteProduct(productId))
				.build();
	}

	@Operation(summary = "Update products via provided data with comparison", tags = ADMIN_TAG)
	@PatchMapping("/products")
	public Response<Data<List<ProductDto>>> updateProduct(
			@Parameter(description = "Map of product ids and updated product, related to specified id")
			@RequestBody Map<@NonNull UUID, @Valid ProductRequest> productRequests
	) {
		return new Response.ResponseBuilder<Data<List<ProductDto>>, List<ProductDto>>()
				.data(productService.updateProduct(productRequests))
				.build();
	}

	@Operation(summary = "Get product by id or name", tags = ADMIN_TAG)
	@GetMapping("/products/search")
	public Response<Data<ProductDto>> getProductByIdOrName(
			@Parameter(description = "Product id to fetch")
			@RequestParam(required = false) @Valid UUID productId,
			@Parameter(description = "Product name to fetch")
			@RequestParam(required = false) @Valid String productName,
			@Parameter(description = "Pageable parameters to request products")
			@ParameterObject @PageableDefault(size = 8, sort = "name") Pageable pageable
	) {
		return new Response.ResponseBuilder<Data<ProductDto>, ProductDto>()
				.data(productService.getProductIdOrByName(productId, productName, pageable).getContent())
				.build();
	}

	@Operation(summary = "Return pageable list of all orders", tags = ADMIN_TAG)
	@GetMapping("/orders")
	public Response<Data<List<OrderSummaryDto>>> getAllOrders(
			@Parameter(description = "Pageable parameters to request orders")
			@ParameterObject @PageableDefault(size = 8, sort = "id") Pageable pageable
	) {
		return new Response.ResponseBuilder<Data<List<OrderSummaryDto>>, List<OrderSummaryDto>>()
				.data(orderService.getAllOrders(pageable).getContent())
				.build();
	}

}
