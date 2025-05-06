package org.example.storedemo.mapper;

import org.example.storedemo.dto.OrderSummaryItemDto;
import org.example.storedemo.dto.response.OrderSummaryDto;
import org.example.storedemo.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR, imports = { LocalDateTime.class })
public interface OrderMapper {

	@Mappings({})
	default OrderSummaryDto toSummaryDto(OrderEntity order) {
		if (order == null){
			return new OrderSummaryDto();
		}

		List<OrderSummaryItemDto> items = order.getItems().stream()
				.map(item -> new OrderSummaryItemDto(
						item.getProductEntity().getId(),
						item.getProductEntity().getName(),
						item.getProductEntity().getPrice(),
						item.getQuantity(),
						item.getPriceSnapshot()
				)).toList();

		return new OrderSummaryDto(
				order.getId(),
				order.getStatus(),
				order.getCreatedAt(),
				order.getExpiresAt(),
				order.getPaidAt(),
				items
		);
	}
}
