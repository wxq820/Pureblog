package com.pureblog.notification.consumer;

import com.pureblog.notification.event.StatsEvent;
import com.pureblog.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "pureblog-stats-events",
            groupId = "pureblog-notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeStatsEvent(
            @Payload StatsEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        try {
            log.info("Received stats event: type={}, articleId={}", event.getEventType(), event.getArticleId());
            notificationService.handleStatsEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process stats event", e);
            throw e;
        }
    }
}
