package org.example.storedemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items", indexes = {
		@Index(name = "idx_order_id", columnList = "order_id"),
		@Index(name = "idx_product_id", columnList = "product_id")
})
public class OrderItemEntity {
	@Id
	private UUID id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id")
	private OrderEntity orderEntity;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private ProductEntity productEntity;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false)
	private BigDecimal priceSnapshot;
}
