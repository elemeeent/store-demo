package org.example.storedemo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class NoStockAvailableException extends BaseApiException {
	public NoStockAvailableException(String message) {
		super(message);
	}
}
