package org.example.storedemo;

import org.example.storedemo.mapper.ProductMapper;
import org.example.storedemo.repository.OrderItemRepository;
import org.example.storedemo.repository.OrderRepository;
import org.example.storedemo.repository.ProductRepository;
import org.example.storedemo.service.OrderService;
import org.example.storedemo.service.ProductInventoryManager;
import org.example.storedemo.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Clock;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class StoreDemoApplicationTests {

	@Autowired
	public ProductRepository productRepository;

	@Autowired
	public OrderRepository orderRepository;

	@Autowired
	public OrderItemRepository orderItemRepository;

	@Autowired
	public OrderService orderService;

	@Autowired
	public ProductService productService;

	@Autowired
	public ProductInventoryManager productInventoryManager;

	@Autowired
	public ProductMapper productMapper;

	@Autowired
	public Clock clock;

	@Test
	void contextLoads() {
	}

}
