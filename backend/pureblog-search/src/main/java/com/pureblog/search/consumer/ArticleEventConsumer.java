package com.pureblog.search.consumer;

import com.pureblog.search.event.ArticleEvent;
import com.pureblog.search.service.SearchService;
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
public class ArticleEventConsumer {

    private final SearchService searchService;

    @KafkaListener(
            topics = "pureblog-article-events",
            groupId = "pureblog-search-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeArticleEvent(
            @Payload ArticleEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {
        try {
            log.info("Received article event: type={}, articleId={}, partition={}, offset={}",
                    event.getEventType(), event.getArticleId(), partition, offset);

            switch (event.getEventType()) {
                case "PUBLISHED" -> searchService.indexArticle(event);
                case "OFFLINE", "DELETED" -> searchService.deleteArticleIndex(event.getArticleId());
                case "REBUILD_INDEX" -> searchService.rebuildArticleIndex(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process article event: articleId={}", event.getArticleId(), e);
            throw e;
        }
    }
}
