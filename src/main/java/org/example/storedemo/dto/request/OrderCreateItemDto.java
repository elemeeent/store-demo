package org.example.storedemo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Data
@AllArgsConstructor
public class OrderCreateItemDto {
	@NotNull(message = "Product ID must not be null")
	private UUID productId;

	@Min(value = 1, message = "Quantity must be at least 1")
	private int quantity;
}
