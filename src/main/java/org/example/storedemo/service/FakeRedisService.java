package org.example.storedemo.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FakeRedisService {
	private final Map<String, Boolean> locks = new ConcurrentHashMap<>();

	public boolean tryLock(String key) {
		return locks.compute(key, (k, v) -> {
			if (v == null || !v) {
				return true; // acquire lock
			}
			return false; // already locked
		});
	}

	public void unlock(String key) {
		locks.remove(key);
	}
}
