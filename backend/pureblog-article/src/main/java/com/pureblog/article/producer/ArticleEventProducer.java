package com.pureblog.article.producer;

import com.pureblog.article.event.ArticleEvent;
import com.pureblog.common.constant.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendArticleEvent(ArticleEvent event) {
        String topic = KafkaTopic.ARTICLE_EVENTS;
        String key = String.valueOf(event.getArticleId());
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, key, event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send article event: articleId={}, error={}", event.getArticleId(), ex.getMessage());
            } else {
                log.info("Article event sent: articleId={}, topic={}, partition={}, offset={}",
                        event.getArticleId(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
