package org.example.storedemo.dto.request;

import lombok.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
	@NotBlank(message = "Name must not be blank")
	private String name;

	@NotNull(message = "Price must not be null")
	@DecimalMin(value = "0.0", message = "Price must be greater than or equal to 0")
	private BigDecimal price;

	@Min(value = 0, message = "Stock quantity must not be negative")
	private int stockQuantity;

}
