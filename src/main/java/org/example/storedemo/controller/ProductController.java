package org.example.storedemo.controller;

import com.slmdev.jsonapi.simple.response.Data;
import com.slmdev.jsonapi.simple.response.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.example.storedemo.dto.ProductDto;
import org.example.storedemo.service.ProductService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

	private final ProductService productService;

	@Operation(summary = "Return pageable list of all products")
	@GetMapping()
	public Response<Data<List<ProductDto>>> getAllProducts(
			@Parameter(description = "Pageable parameters for products request")
			@ParameterObject @PageableDefault(size = 8, sort = "name") Pageable pageable
	) {
		return new Response.ResponseBuilder<Data<List<ProductDto>>, List<ProductDto>>()
				.data(productService.getAllProducts(pageable).getContent())
				.build();
	}

}
