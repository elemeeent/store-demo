package org.example.storedemo.repository;

import org.example.storedemo.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {

	@Query("""
      SELECT COUNT(oi) > 0 FROM OrderItemEntity oi
      JOIN oi.orderEntity o
      WHERE oi.productEntity.id = :productId
      AND o.status IN ('CREATED', 'PAID')
      """)
	boolean existsByProductIdInActiveOrders(@Param("productId") UUID productId);

}