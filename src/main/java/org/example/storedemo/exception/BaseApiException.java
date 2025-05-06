package org.example.storedemo.exception;

public abstract class BaseApiException extends RuntimeException {
	public BaseApiException(String message) {
		super(message);
	}
}
