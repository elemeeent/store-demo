package org.example.storedemo.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storedemo.dto.ProductDto;
import org.example.storedemo.dto.request.ProductRequest;
import org.example.storedemo.dto.response.ProductCreateResponse;
import org.example.storedemo.dto.response.ProductCreationError;
import org.example.storedemo.entity.ProductEntity;
import org.example.storedemo.exception.BadRequestException;
import org.example.storedemo.exception.NotFoundException;
import org.example.storedemo.mapper.ProductMapper;
import org.example.storedemo.repository.OrderItemRepository;
import org.example.storedemo.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductMapper productMapper;

	public Page<ProductDto> getAllProducts(Pageable pageable) {
		log.info("Requesting all products with params: {}", pageable);
		return productRepository.findAll(pageable).map(productMapper::toDto);
	}

	public Page<ProductDto> getProductIdOrByName(UUID productId, String productName, Pageable pageable) {
		if (productId != null) {
			log.info("Requesting product by id: {}", productId);
			ProductEntity productEntity = findProductById(productId);
			return new PageImpl<>(List.of(productMapper.toDto(productEntity)), pageable, 1);
		}
		if (productName != null) {
			log.info("Requesting product by name: {}", productId);
			return findProductsContainsName(productName, pageable).map(productMapper::toDto);
		} else {
			throw new BadRequestException("Product id or name is required");
		}
	}

	private ProductEntity findProductByName(String productName) {
		return productRepository.findByNameIgnoreCase(productName)
				.orElseThrow(() -> new NotFoundException("Product with name " + productName + " not found"));
	}

	private Page<ProductEntity> findProductsContainsName(String name, Pageable pageable) {
		Page<ProductEntity> productsByName = productRepository.findByNameContainingIgnoreCase(name, pageable);
		if (productsByName.isEmpty()) {
			throw new NotFoundException("Products containing name " + name + " not found");
		}
		return productsByName;
	}

	@Transactional(rollbackFor = Exception.class)
	public ProductCreateResponse createProduct(ProductRequest productToCreate) {
		if (productToCreate == null) {
			throw new BadRequestException("Could not create product because there are no new products provided");
		}

		log.info("Attempting to create product: {}", productToCreate);

		List<ProductDto> created = new ArrayList<>();
		List<ProductCreationError> errors = new ArrayList<>();

		handleSingleProductCreation(productToCreate, created, errors);

		log.info("Created {} products, {} failed", created.size(), errors.size());
		return new ProductCreateResponse(created, errors);
	}

	@Transactional(rollbackFor = Exception.class)
	public ProductDto deleteProduct(UUID productId) {
		ProductEntity productToDelete = findProductById(productId);
		log.info("Attempting to delete product id: {}, name: {}", productToDelete.getId(), productToDelete.getName());

		boolean productInActiveOrders = orderItemRepository.existsByProductIdInActiveOrders(productToDelete.getId());
		if (productInActiveOrders) {
			throw new BadRequestException("Product with id " + productToDelete.getId() + " is already in active orders");
		}

		productRepository.delete(productToDelete);
		log.info("Product with id: {}, name: {} was deleted", productToDelete.getId(), productToDelete.getName());
		return productMapper.toDto(productToDelete);
	}

	@Transactional(rollbackFor = Exception.class)
	public List<ProductDto> updateProduct(Map<UUID, ProductRequest> productRequests) {
		if (productRequests == null || productRequests.isEmpty()) {
			throw new BadRequestException("Could not update products because there are no updates provided");
		}

		log.info("Attempting to update {} products", productRequests.size());
		return productRequests.entrySet().stream()
				.map(this::updateSingleProduct)
				.collect(Collectors.toList());
	}

	private void handleSingleProductCreation(
			ProductRequest request,
			List<ProductDto> created,
			List<ProductCreationError> errors
	) {
		try {
			if (productRepository.existsByName(request.getName())) {
				log.warn("Product with name '{}' already exists", request.getName());
				errors.add(new ProductCreationError(request.getName(), "Product with this name already exists"));
				return;
			}

			ProductEntity entity = productMapper.toEntity(request);
			ProductEntity saved = productRepository.save(entity);
			created.add(productMapper.toDto(saved));
		} catch (Exception ex) {
			log.error("Error while creating product '{}': {}", request.getName(), ex.getMessage(), ex);
			errors.add(new ProductCreationError(request.getName(), "Unexpected error: " + ex.getMessage()));
		}
	}

	private ProductDto updateSingleProduct(Map.Entry<UUID, ProductRequest> entry) {
		UUID uuid = entry.getKey();
		ProductRequest request = entry.getValue();
		ProductEntity product = findProductById(uuid);

		if (isUnchanged(product, request)) {
			log.info("Product with id {} is equal to new data. No update needed", uuid);
			return productMapper.toDto(product);
		}

		product.setName(request.getName());
		product.setPrice(request.getPrice());
		product.setStockQuantity(request.getStockQuantity());
		ProductEntity updated = productRepository.save(product);

		log.info("Product with id: {}, name: {} was updated", uuid, updated.getName());
		return productMapper.toDto(updated);
	}

	public ProductEntity findProductById(UUID id) {
		return productRepository.findById(id).orElseThrow(
				() -> new NotFoundException("Product with id " + id + " not found"));
	}

	private boolean isUnchanged(ProductEntity existing, ProductRequest incoming) {
		return existing.getName().equals(incoming.getName()) &&
				existing.getPrice().equals(incoming.getPrice()) &&
				existing.getStockQuantity() == incoming.getStockQuantity();
	}
}
