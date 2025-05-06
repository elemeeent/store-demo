package org.example.storedemo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductCreationError {
	private String productName;
	private String errorMessage;
}
