package com.pureblog.notification.consumer;

import com.pureblog.notification.event.CommentEvent;
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
public class CommentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "pureblog-comment-events",
            groupId = "pureblog-notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeCommentEvent(
            @Payload CommentEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        try {
            log.info("Received comment event: type={}, commentId={}, partition={}, offset={}",
                    event.getEventType(), event.getCommentId(), partition, offset);

            switch (event.getEventType()) {
                case "COMMENT_CREATED" -> notificationService.handleCommentCreated(event);
                default -> log.warn("Unknown comment event type: {}", event.getEventType());
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process comment event", e);
            throw e;
        }
    }
}
