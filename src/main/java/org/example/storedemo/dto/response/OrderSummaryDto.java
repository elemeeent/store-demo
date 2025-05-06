package org.example.storedemo.dto.response;

import com.slmdev.jsonapi.simple.annotation.JsonApiId;
import com.slmdev.jsonapi.simple.annotation.JsonApiType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.storedemo.dto.OrderSummaryItemDto;
import org.example.storedemo.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonApiType("orders")
public class OrderSummaryDto {
	@JsonApiId
	private UUID orderId;
	private OrderStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime expiresAt;
	private LocalDateTime paidAt;
	private List<OrderSummaryItemDto> products;
}
