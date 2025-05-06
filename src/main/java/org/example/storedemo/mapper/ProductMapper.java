package org.example.storedemo.mapper;


import org.example.storedemo.dto.ProductDto;
import org.example.storedemo.dto.request.ProductRequest;
import org.example.storedemo.entity.ProductEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.UUID;

@Mapper(componentModel = "spring",
		unmappedTargetPolicy = ReportingPolicy.ERROR,
		imports = { BigDecimal.class, UUID.class }
)
public interface ProductMapper {

	@Mapping(target = "errorMessage", ignore = true)
	@Mapping(target = "id", source = "entity.id")
	@Mapping(target = "name", source = "entity.name")
	@Mapping(target = "price", source = "entity.price")
	@Mapping(target = "stockQuantity", source = "entity.stockQuantity")
	ProductDto toDto(ProductEntity entity);

	@Mapping(target = "id", expression = "java(UUID.randomUUID())")
	@Mapping(target = "version", ignore = true)
	ProductEntity toEntity(ProductRequest request);

}
