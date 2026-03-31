package pe.resteban.events.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class OrderEventTest {

    @Test
    void shouldConstructRecordWithCorrectValues() {
        Instant now = Instant.parse("2026-03-30T17:00:00Z");

        OrderEvent event = new OrderEvent("ORD-001", "CREATED", now);

        assertThat(event.orderId()).isEqualTo("ORD-001");
        assertThat(event.status()).isEqualTo("CREATED");
        assertThat(event.timestamp()).isEqualTo(now);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Instant now = Instant.parse("2026-03-30T17:00:00Z");

        OrderEvent a = new OrderEvent("ORD-001", "CREATED", now);
        OrderEvent b = new OrderEvent("ORD-001", "CREATED", now);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentOrderId() {
        Instant now = Instant.now();

        OrderEvent a = new OrderEvent("ORD-001", "CREATED", now);
        OrderEvent b = new OrderEvent("ORD-002", "CREATED", now);

        assertThat(a).isNotEqualTo(b);
    }
}
