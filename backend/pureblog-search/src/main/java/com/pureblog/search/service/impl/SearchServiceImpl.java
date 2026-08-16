package com.pureblog.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pureblog.article.entity.ArticleContentDO;
import com.pureblog.article.entity.ArticleDO;
import com.pureblog.article.entity.ArticleTagDO;
import com.pureblog.article.entity.TagDO;
import com.pureblog.article.mapper.ArticleContentMapper;
import com.pureblog.article.mapper.ArticleMapper;
import com.pureblog.article.mapper.ArticleTagMapper;
import com.pureblog.article.mapper.TagMapper;
import com.pureblog.auth.entity.UserDO;
import com.pureblog.auth.mapper.UserMapper;
import com.pureblog.article.entity.CategoryDO;
import com.pureblog.article.mapper.CategoryMapper;
import com.pureblog.common.enums.ArticleStatus;
import com.pureblog.article.event.ArticleEvent;
import com.pureblog.search.document.ArticleDocument;
import com.pureblog.search.dto.SearchRequest;
import com.pureblog.search.service.SearchService;
import com.pureblog.search.vo.SearchResult;
import com.pureblog.search.vo.SearchVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient esClient;
    private final ArticleMapper articleMapper;
    private final ArticleContentMapper contentMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    private static final String INDEX_NAME = "pureblog_articles";

    @Override
    public SearchResult search(SearchRequest request) {
        long start = System.currentTimeMillis();
        try {
            ensureIndexExists();

            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
                String kw = request.getKeyword();
                boolQuery.must(m -> m.bool(b -> b
                        .should(s -> s.match(mt -> mt.field("title").query(kw).boost(3.0f)))
                        .should(s -> s.match(mt -> mt.field("summary").query(kw).boost(2.0f)))
                        .should(s -> s.match(mt -> mt.field("tagNames").query(kw).boost(2.5f)))
                        .should(s -> s.match(mt -> mt.field("content").query(kw)))
                        .minimumShouldMatch("1")
                ));
            }

            if (request.getCategoryId() != null) {
                boolQuery.filter(f -> f.term(t -> t.field("categoryId").value(request.getCategoryId())));
            }

            if (request.getTagId() != null) {
                boolQuery.filter(f -> f.term(t -> t.field("tagIds").value(request.getTagId())));
            }

            boolQuery.filter(f -> f.term(t -> t.field("status").value("PUBLISHED")));

            String sortField = switch (request.getSortBy()) {
                case "viewCount" -> "viewCount";
                case "likeCount" -> "likeCount";
                case "publishedAt" -> "publishedAt";
                default -> "_score";
            };

            co.elastic.clients.elasticsearch.core.SearchRequest.Builder searchBuilder = new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                    .index(INDEX_NAME)
                    .query(q -> q.bool(boolQuery.build()))
                    .from((request.getPage() - 1) * request.getSize())
                    .size(request.getSize())
                    .highlight(h -> h
                            .fields("title", HighlightField.of(f -> f.preTags("<em>").postTags("</em>")))
                            .fields("summary", HighlightField.of(f -> f.preTags("<em>").postTags("</em>")))
                            .fields("content", HighlightField.of(f -> f.preTags("<em>").postTags("</em>").fragmentSize(150).numberOfFragments(3)))
                    );

            if (!"_score".equals(sortField)) {
                searchBuilder.sort(s -> s.field(f -> f.field(sortField)
                        .order("desc".equals(request.getSortOrder()) ? SortOrder.Desc : SortOrder.Asc)));
            }

            co.elastic.clients.elasticsearch.core.SearchResponse<ArticleDocument> response =
                    esClient.search(searchBuilder.build(), ArticleDocument.class);

            List<SearchVO> results = new ArrayList<>();
            for (Hit<ArticleDocument> hit : response.hits().hits()) {
                ArticleDocument doc = hit.source();
                if (doc == null) continue;

                List<String> highlights = new ArrayList<>();
                if (hit.highlight() != null) {
                    hit.highlight().values().forEach(highlights::addAll);
                }

                results.add(SearchVO.builder()
                        .articleId(doc.getArticleId())
                        .title(highlights.isEmpty() || hit.highlight().get("title") == null ? doc.getTitle() :
                                String.join("", hit.highlight().get("title")))
                        .summary(doc.getSummary())
                        .authorName(doc.getAuthorNickname())
                        .authorAvatar(doc.getAuthorAvatar())
                        .authorId(doc.getAuthorId())
                        .categoryName(doc.getCategoryName())
                        .tagNames(doc.getTagNames())
                        .viewCount(doc.getViewCount())
                        .likeCount(doc.getLikeCount())
                        .publishedAt(doc.getPublishedAt() != null ? doc.getPublishedAt().toString() : null)
                        .highlights(highlights)
                        .build());
            }

            long total = response.hits().total() != null ? response.hits().total().value() : 0;
            long took = System.currentTimeMillis() - start;

            return SearchResult.builder()
                    .articles(results)
                    .total(total)
                    .page(request.getPage())
                    .size(request.getSize())
                    .totalPages(total % request.getSize() == 0 ? total / request.getSize() : total / request.getSize() + 1)
                    .tookMs(took)
                    .build();

        } catch (IOException e) {
            log.error("Search failed", e);
            return SearchResult.builder()
                    .articles(Collections.emptyList())
                    .total(0)
                    .page(request.getPage())
                    .size(request.getSize())
                    .totalPages(0)
                    .tookMs(System.currentTimeMillis() - start)
                    .build();
        }
    }

    @Override
    public void indexArticle(ArticleEvent event) {
        try {
            ensureIndexExists();
            ArticleDocument doc = buildDocument(event);
            IndexResponse response = esClient.index(i -> i
                    .index(INDEX_NAME)
                    .id(String.valueOf(event.getArticleId()))
                    .document(doc)
            );
            log.info("Article indexed: id={}, result={}", event.getArticleId(), response.result());
        } catch (IOException e) {
            log.error("Failed to index article: {}", event.getArticleId(), e);
            throw new RuntimeException("Failed to index article", e);
        }
    }

    @Override
    public void deleteArticleIndex(Long articleId) {
        try {
            esClient.delete(d -> d.index(INDEX_NAME).id(String.valueOf(articleId)));
            log.info("Article index deleted: {}", articleId);
        } catch (IOException e) {
            log.error("Failed to delete article index: {}", articleId, e);
        }
    }

    @Override
    public void rebuildArticleIndex(ArticleEvent event) {
        indexArticle(event);
    }

    @Override
    public void createIndex() {
        try {
            boolean exists = esClient.indices().exists(ExistsRequest.of(e -> e.index(INDEX_NAME))).value();
            if (exists) {
                log.info("Index {} already exists", INDEX_NAME);
                return;
            }

            String mappings = """
                {
                  "mappings": {
                    "properties": {
                      "id": { "type": "keyword" },
                      "articleId": { "type": "long" },
                      "authorId": { "type": "long" },
                      "authorName": { "type": "text" },
                      "authorNickname": { "type": "text" },
                      "authorAvatar": { "type": "keyword", "index": false },
                      "categoryId": { "type": "long" },
                      "categoryName": { "type": "text" },
                      "title": { "type": "text", "analyzer": "ik_max_word" },
                      "summary": { "type": "text", "analyzer": "ik_max_word" },
                      "content": { "type": "text", "analyzer": "ik_max_word" },
                      "tagNames": { "type": "text" },
                      "tagSlugs": { "type": "keyword" },
                      "tagIds": { "type": "long" },
                      "viewCount": { "type": "integer" },
                      "likeCount": { "type": "integer" },
                      "commentCount": { "type": "integer" },
                      "isFeatured": { "type": "boolean" },
                      "status": { "type": "keyword" },
                      "publishedAt": { "type": "date" },
                      "createdAt": { "type": "date" }
                    }
                  },
                  "settings": {
                    "number_of_shards": 1,
                    "number_of_replicas": 0
                  }
                }
                """;

            esClient.indices().create(CreateIndexRequest.of(c -> c
                    .index(INDEX_NAME)
                    .withJson(new StringReader(mappings))
            ));
            log.info("Index {} created successfully", INDEX_NAME);
        } catch (IOException e) {
            log.error("Failed to create index", e);
            throw new RuntimeException("Failed to create ES index", e);
        }
    }

    @Override
    public void rebuildAllIndex() {
        createIndex();

        LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED.getCode());
        List<ArticleDO> articles = articleMapper.selectList(wrapper);

        for (ArticleDO article : articles) {
            try {
                ArticleContentDO content = contentMapper.selectOne(new LambdaQueryWrapper<ArticleContentDO>()
                        .eq(ArticleContentDO::getArticleId, article.getId()));
                List<Long> tagIds = articleTagMapper.selectList(
                                new LambdaQueryWrapper<ArticleTagDO>().eq(ArticleTagDO::getArticleId, article.getId()))
                        .stream().map(ArticleTagDO::getTagId).collect(Collectors.toList());

                ArticleEvent event = ArticleEvent.builder()
                        .eventType("PUBLISHED")
                        .articleId(article.getId())
                        .authorId(article.getAuthorId())
                        .title(article.getTitle())
                        .summary(article.getSummary())
                        .content(content != null ? content.getContent() : null)
                        .htmlContent(content != null ? content.getHtmlContent() : null)
                        .categoryId(article.getCategoryId())
                        .tagIds(tagIds)
                        .publishedAt(article.getPublishedAt())
                        .build();

                indexArticle(event);
            } catch (Exception e) {
                log.error("Failed to rebuild index for article: {}", article.getId(), e);
            }
        }
        log.info("Rebuilt all article indexes: total={}", articles.size());
    }

    private ArticleDocument buildDocument(ArticleEvent event) {
        UserDO author = userMapper.selectById(event.getAuthorId());
        CategoryDO category = categoryMapper.selectById(event.getCategoryId());

        List<String> tagNames = new ArrayList<>();
        List<String> tagSlugs = new ArrayList<>();
        List<Long> tagIds = new ArrayList<>();

        if (event.getTagIds() != null) {
            for (Long tagId : event.getTagIds()) {
                TagDO tag = tagMapper.selectById(tagId);
                if (tag != null) {
                    tagNames.add(tag.getName());
                    tagSlugs.add(tag.getSlug());
                    tagIds.add(tag.getId());
                }
            }
        }

        return ArticleDocument.builder()
                .id(String.valueOf(event.getArticleId()))
                .articleId(event.getArticleId())
                .authorId(event.getAuthorId())
                .authorName(author != null ? author.getUsername() : null)
                .authorNickname(author != null ? author.getNickname() : null)
                .authorAvatar(author != null ? author.getAvatarUrl() : null)
                .categoryId(event.getCategoryId())
                .categoryName(category != null ? category.getName() : null)
                .title(event.getTitle())
                .summary(event.getSummary())
                .content(event.getContent())
                .tagNames(tagNames)
                .tagSlugs(tagSlugs)
                .tagIds(tagIds)
                .viewCount(0)
                .likeCount(0)
                .commentCount(0)
                .isFeatured(false)
                .status("PUBLISHED")
                .publishedAt(event.getPublishedAt())
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }

    private void ensureIndexExists() {
        try {
            boolean exists = esClient.indices().exists(ExistsRequest.of(e -> e.index(INDEX_NAME))).value();
            if (!exists) {
                createIndex();
            }
        } catch (IOException e) {
            log.error("Failed to check index existence", e);
        }
    }

    @EventListener
    public void onArticleEvent(ArticleEvent event) {
        try {
            log.info("Received article event: type={}, articleId={}", event.getEventType(), event.getArticleId());
            switch (event.getEventType()) {
                case "PUBLISHED" -> indexArticle(event);
                case "OFFLINE", "DELETED" -> deleteArticleIndex(event.getArticleId());
                case "REBUILD_INDEX" -> rebuildArticleIndex(event);
                default -> log.warn("Unknown event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process article event: articleId={}", event.getArticleId(), e);
        }
    }
}
