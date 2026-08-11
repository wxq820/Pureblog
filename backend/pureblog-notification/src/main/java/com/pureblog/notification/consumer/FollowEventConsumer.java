package com.pureblog.notification.consumer;

import com.pureblog.notification.event.FollowEvent;
import com.pureblog.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "pureblog-follow-events",
            groupId = "pureblog-notification-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeFollowEvent(@Payload FollowEvent event, Acknowledgment ack) {
        try {
            log.info("Received follow event: follower={}, following={}", event.getFollowerId(), event.getFollowingId());
            notificationService.handleFollowEvent(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process follow event", e);
            throw e;
        }
    }
}
