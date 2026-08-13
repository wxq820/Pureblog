package com.pureblog.stats.consumer;

import com.pureblog.stats.event.StatsEvent;
import com.pureblog.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component("statsEventConsumerForStats")
@RequiredArgsConstructor
public class StatsEventConsumer {

    private final StatsService statsService;

    @KafkaListener(
            topics = "pureblog-stats-events",
            groupId = "pureblog-stats-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeStatsEvent(@Payload StatsEvent event, Acknowledgment ack) {
        try {
            log.info("Received stats event: type={}, articleId={}", event.getEventType(), event.getArticleId());
            statsService.handleStatsEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process stats event", e);
            throw e;
        }
    }
}
