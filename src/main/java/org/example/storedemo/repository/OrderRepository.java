package org.example.storedemo.repository;

import org.example.storedemo.entity.OrderEntity;
import org.example.storedemo.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

	Page<OrderEntity> findByStatusAndExpiresAtBefore(
			OrderStatus status,
			LocalDateTime expiresAt,
			Pageable pageable
	);

	@Query("SELECT DISTINCT o FROM OrderEntity o " +
			"LEFT JOIN FETCH o.items i " +
			"LEFT JOIN FETCH i.productEntity " +
			"WHERE o.status = :status AND o.expiresAt < :cutoffTime")
	Page<OrderEntity> findByStatusAndExpiresAtBeforeWithItems(
			@Param("status") OrderStatus status,
			@Param("cutoffTime") LocalDateTime cutoffTime,
			Pageable pageable
	);


}
