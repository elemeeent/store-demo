package org.example.storedemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storedemo.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
	private UUID id;
	private OrderStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime expiresAt;
	private LocalDateTime paidAt;
	private List<OrderItemDto> items;
}
