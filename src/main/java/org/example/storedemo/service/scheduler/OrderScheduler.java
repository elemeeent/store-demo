package org.example.storedemo.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.storedemo.entity.OrderEntity;
import org.example.storedemo.entity.OrderStatus;
import org.example.storedemo.repository.OrderRepository;
import org.example.storedemo.service.FakeRedisService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderScheduler {

	private final OrderRepository orderRepository;
	private final Clock clock;
	private final OrderSchedulerExpireService orderSchedulerExpireService;
	private final FakeRedisService fakeRedisService;

	@Scheduled(initialDelay = 60_000, fixedDelay = 60_000)
	public void invalidateExpiredOrders() {
		log.info("Starting to expire out-of-date orders");
		if (fakeRedisService.tryLock("invalidateExpiredOrders")) {
			try {
				Instant cleanupStartTime = clock.instant();

				final int PAGE_SIZE = 100;
				LocalDateTime cutoffTime = LocalDateTime.now(clock);
				int page = 0;
				int totalOrders = 0;
				int totalProducts = 0;
				Page<OrderEntity> resultPage;

				do {
					resultPage = orderRepository.findByStatusAndExpiresAtBeforeWithItems(
							OrderStatus.CREATED,
							cutoffTime,
							PageRequest.of(page, PAGE_SIZE, Sort.by("id"))
					);

					OrderSchedulerExpireService.OrderProcessingResult result =
							orderSchedulerExpireService.processExpiredOrders(resultPage.getContent());
					totalOrders += result.expiredOrders();
					totalProducts += result.releasedProducts();
					page++;

				} while (!resultPage.isLast());

				Instant cleanupFinishTime = clock.instant();
				log.info(
						"Finished. Orders expired: {}, products released: {}. Processing took: {} ms",
						totalOrders, totalProducts, Duration.between(cleanupStartTime, cleanupFinishTime).toMillis()
				);
			} catch (Exception ex) {
				log.error("An error occurred while processing order invalidation", ex);
			} finally {
				fakeRedisService.unlock("invalidateExpiredOrders");
			}
		} else {
			log.info("Another instance is already running this task");
		}
	}

}
