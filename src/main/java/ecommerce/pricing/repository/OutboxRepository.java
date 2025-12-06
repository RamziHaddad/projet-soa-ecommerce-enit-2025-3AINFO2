package ecommerce.pricing.repository;

import ecommerce.pricing.entity.OutboxEvent;
import ecommerce.pricing.entity.OutboxEvent.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'PENDING' AND o.retryCount < 3 ORDER BY o.createdAt ASC")
    List<OutboxEvent> findPendingEvents();

    @Query("SELECT o FROM OutboxEvent o WHERE o.status = 'FAILED' AND o.retryCount < 5 ORDER BY o.createdAt ASC")
    List<OutboxEvent> findFailedEventsForRetry();

    List<OutboxEvent> findByStatusAndCreatedAtBefore(OutboxStatus status, LocalDateTime dateTime);
}