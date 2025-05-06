package org.example.storedemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final RestTemplate paymentRestTemplate;

	public void pay(String somePaymentData) {
		// TODO: implement payment
	}
}
