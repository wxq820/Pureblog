package com.pureblog.article.service.impl;

import com.pureblog.article.dto.SearchRequest;
import com.pureblog.article.mapper.SearchMapper;
import com.pureblog.article.mapper.SearchRow;
import com.pureblog.article.service.SearchService;
import com.pureblog.article.vo.SearchResult;
import com.pureblog.article.vo.SearchVO;
import com.pureblog.common.utils.DateUtils;
import com.pureblog.common.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * MySQL LIKE 版全文搜索。
 *
 * <p>替代原 Elasticsearch 实现: 个人博客量级（当前 4 篇文章）无需全文索引，
 * {@code %keyword%} 前缀通配在数据量上万后才会明显劣化。升级路径：MySQL {@code FULLTEXT} + ngram 分词器。
 *
 * <p><b>安全要点</b>: 前端 SearchPage.vue 用 {@code v-html} 渲染 {@code title} 与
 * {@code highlights}，因此服务端必须先 HTML 转义原文再插入 {@code <em>} 标签，
 * 否则用户提交的标题中的 HTML 会在所有搜索到该文章的用户浏览器里执行。
 * {@code summary} 由前端 {@code {{ }}} 插值渲染，无需转义。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchMapper searchMapper;

    /** 正文高亮片段的最大长度 (字符), 与原 ES 实现 fragmentSize=150 对齐 */
    private static final int FRAGMENT_SIZE = 150;

    /** 正文高亮片段的最大条数, 与原 ES 实现 numberOfFragments=3 对齐 */
    private static final int MAX_FRAGMENTS = 3;

    @Override
    public SearchResult search(SearchRequest request) {
        long start = System.currentTimeMillis();

        int page = request.getPage() == null || request.getPage() < 1 ? 1 : request.getPage();
        int size = request.getSize() == null || request.getSize() < 1 ? 10 : request.getSize();
        String rawKeyword = request.getKeyword();
        boolean hasKeyword = rawKeyword != null && !rawKeyword.trim().isEmpty();

        // LIKE 通配符按字面量处理: ! -> !!, % -> !%, _ -> !_
        String likeKeyword = hasKeyword ? StringUtils.escapeLike(rawKeyword.trim()) : null;
        String sortBy = request.getSortBy() == null ? "relevance" : request.getSortBy();
        String sortOrder = request.getSortOrder() == null ? "desc" : request.getSortOrder();

        long total = searchMapper.count(likeKeyword, request.getCategoryId(), request.getTagId());
        List<SearchRow> rows = total == 0
                ? List.of()
                : searchMapper.search(likeKeyword, request.getCategoryId(), request.getTagId(),
                        sortBy, sortOrder, (long) (page - 1) * size, size);

        List<SearchVO> articles = new ArrayList<>(rows.size());
        for (SearchRow row : rows) {
            articles.add(toVO(row, hasKeyword ? rawKeyword.trim() : null));
        }

        return SearchResult.builder()
                .articles(articles)
                .total(total)
                .page(page)
                .size(size)
                .totalPages(total % size == 0 ? total / size : total / size + 1)
                .tookMs(System.currentTimeMillis() - start)
                .build();
    }

    private SearchVO toVO(SearchRow row, String keyword) {
        String title = row.getTitle();
        List<String> highlights = new ArrayList<>();

        if (keyword != null) {
            // title 走 v-html: 转义后插入 <em>
            title = highlight(row.getTitle(), keyword);
            // 正文片段同样走 v-html
            highlights.addAll(contentFragments(row.getContent(), keyword));
        }

        return SearchVO.builder()
                .articleId(row.getArticleId())
                .title(title)
                // summary 由前端 {{ }} 渲染, 返回原文即可, 转义会导致双重转义
                .summary(row.getSummary())
                .authorName(row.getAuthorName())
                .authorAvatar(row.getAuthorAvatar())
                .authorId(row.getAuthorId())
                .categoryName(row.getCategoryName())
                .tagNames(splitTagNames(row.getTagNames()))
                .viewCount(row.getViewCount())
                .likeCount(row.getLikeCount())
                .publishedAt(row.getPublishedAt() != null ? DateUtils.format(row.getPublishedAt()) : null)
                .highlights(highlights)
                .build();
    }

    /**
     * 转义原文后用 &lt;em&gt; 包裹关键词命中片段.
     *
     * <p>顺序至关重要: 先转义, 再插标签。反过来做, 用户输入的 {@code <em>} 会被
     * 当成高亮标签的一部分, 且未转义的原始 HTML 会被浏览器执行。
     */
    private String highlight(String text, String keyword) {
        if (text == null) return null;
        String escaped = escapeHtml(text);
        if (keyword == null || keyword.isBlank()) return escaped;
        return wrapMatches(escaped, escapeHtml(keyword));
    }

    /**
     * 从正文中截取包含关键词的高亮片段, 最多 {@link #MAX_FRAGMENTS} 条.
     */
    private List<String> contentFragments(String content, String keyword) {
        List<String> fragments = new ArrayList<>();
        if (content == null || content.isBlank() || keyword == null || keyword.isBlank()) {
            return fragments;
        }

        String escapedContent = escapeHtml(content);
        String escapedKeyword = escapeHtml(keyword);
        Matcher matcher = Pattern.compile(Pattern.quote(escapedKeyword), Pattern.CASE_INSENSITIVE)
                .matcher(escapedContent);

        while (matcher.find() && fragments.size() < MAX_FRAGMENTS) {
            int start = Math.max(0, matcher.start() - FRAGMENT_SIZE / 2);
            int end = Math.min(escapedContent.length(), matcher.end() + FRAGMENT_SIZE / 2);
            String fragment = escapedContent.substring(start, end);
            fragments.add(wrapMatches(fragment, escapedKeyword));
        }
        return fragments;
    }

    /**
     * 把 fragment 中所有 escapedKeyword 的出现位置用 &lt;em&gt; 包裹.
     */
    private String wrapMatches(String fragment, String escapedKeyword) {
        return Pattern.compile(Pattern.quote(escapedKeyword), Pattern.CASE_INSENSITIVE)
                .matcher(fragment)
                .replaceAll(m -> "<em>" + m.group() + "</em>");
    }

    /**
     * HTML 转义: &amp; &lt; &gt; " ' 五个字符.
     *
     * <p>不用 Spring 的 {@code HtmlUtils.htmlEscape}: 它默认按 ISO-8859-1 转义,
     * 中文会变成 {@code &#x4E2D;} 这类数字实体, JSON 响应可读性很差。
     */
    private String escapeHtml(String text) {
        if (text == null) return null;
        StringBuilder sb = new StringBuilder(text.length() + 16);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private List<String> splitTagNames(String tagNames) {
        if (tagNames == null || tagNames.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tagNames.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
