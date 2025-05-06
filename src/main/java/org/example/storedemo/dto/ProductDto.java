package org.example.storedemo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.slmdev.jsonapi.simple.annotation.JsonApiId;
import com.slmdev.jsonapi.simple.annotation.JsonApiType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonApiType("products")
public class ProductDto {
	@JsonApiId
	private UUID id;
	private String name;
	private BigDecimal price;
	private int stockQuantity;
	private String errorMessage;

	public ProductDto(UUID id, String name, BigDecimal price, int stockQuantity) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.stockQuantity = stockQuantity;
	}
}
