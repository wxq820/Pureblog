package com.pureblog.stats.listener;

import com.pureblog.common.event.StatsLikeArticleEvent;
import com.pureblog.common.event.StatsPvEvent;
import com.pureblog.stats.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsEventListener {

    private final StatsService statsService;

    @EventListener
    public void onPv(StatsPvEvent event) {
        statsService.incrementPv(event.articleId(), event.ip());
    }

    @EventListener
    public void onLike(StatsLikeArticleEvent event) {
        statsService.recordLikeHotScore(event.articleId());
    }
}