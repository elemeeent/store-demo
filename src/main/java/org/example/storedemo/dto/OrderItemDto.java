package org.example.storedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storedemo.entity.OrderEntity;
import org.example.storedemo.entity.ProductEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
	private UUID id;
	private OrderEntity orderEntity;
	private ProductEntity productEntity;
	private int quantity;
	private BigDecimal priceSnapshot;
}
