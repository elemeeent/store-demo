package org.example.storedemo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.slmdev.jsonapi.simple.annotation.JsonApiId;
import com.slmdev.jsonapi.simple.annotation.JsonApiType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storedemo.dto.ProductDto;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonApiType("products")
public class ProductCreateResponse {
	@JsonApiId
	private UUID id = UUID.randomUUID();
	private List<ProductDto> createdProducts;
	private List<ProductCreationError> errorProducts;

	public ProductCreateResponse(List<ProductDto> createdProducts, List<ProductCreationError> errorProducts) {
		this.createdProducts = createdProducts;
		this.errorProducts = errorProducts;
	}
}
