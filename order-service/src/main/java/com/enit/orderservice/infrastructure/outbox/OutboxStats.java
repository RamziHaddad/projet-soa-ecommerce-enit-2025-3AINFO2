package com.enit.orderservice.infrastructure.outbox;

import lombok.Builder;
import lombok.Data;

/**
 * Statistics for outbox events
 */
@Data
@Builder
public class OutboxStats {
    private long pendingCount;
    private long publishedCount;
    private long failedCount;
}
