package org.example.storedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSummaryItemDto {
	private UUID productId;
	private String name;
	private BigDecimal unitPrice;
	private int quantity;
	private BigDecimal totalPrice;
}
