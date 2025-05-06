package org.example.storedemo.repository;

import org.example.storedemo.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
	boolean existsByName(String name);

	Optional<ProductEntity> findByNameIgnoreCase(String name);

	Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
}