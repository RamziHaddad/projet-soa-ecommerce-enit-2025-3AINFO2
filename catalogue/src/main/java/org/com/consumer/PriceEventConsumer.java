package org.com.consumer;

import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;
import org.com.service.InboxService;

import java.math.BigDecimal;

@ApplicationScoped
public class PriceEventConsumer {

    private static final Logger LOG = Logger.getLogger(PriceEventConsumer.class);

    @Inject
    InboxService inboxService;

    @Incoming("price-events")
    @Blocking
    public void onPriceChanged(JsonObject event) {
        LOG.infof("Received event: %s", event);

        try {
            String eventId = event.getString("eventId");
            String eventType = event.getString("eventType");
            String productId = event.getString("productId");
            BigDecimal newPrice = event.getJsonNumber("basePrice").bigDecimalValue();

            inboxService.processPriceEvent(eventId, eventType, productId, newPrice);

        } catch (Exception e) {
            LOG.errorf("Failed to process event: %s", e.getMessage());
            throw e;
        }
    }
}