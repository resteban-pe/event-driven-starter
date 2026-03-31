package pe.resteban.events.model;

import java.time.Instant;

public record OrderEvent(String orderId, String status, Instant timestamp) {
}
