package com.pureblog.article.producer;

import com.pureblog.article.event.ArticleEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleEventProducer {

    private final ApplicationEventPublisher eventPublisher;

    public void sendArticleEvent(ArticleEvent event) {
        log.info("Publishing article event: type={}, articleId={}", event.getEventType(), event.getArticleId());
        eventPublisher.publishEvent(event);
    }
}
