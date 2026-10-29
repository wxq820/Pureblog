package com.pureblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pureblog.article.dto.*;
import com.pureblog.article.entity.*;
import com.pureblog.article.manager.ArticleCacheManager;
import com.pureblog.article.mapper.*;
import com.pureblog.article.service.ArticleService;
import com.pureblog.article.vo.*;
import com.pureblog.article.vo.ArticleDetailVO.*;
import com.pureblog.auth.entity.UserDO;
import com.pureblog.auth.mapper.UserMapper;
import com.pureblog.common.context.LoginUserHolder;
import com.pureblog.common.enums.ArticleStatus;
import com.pureblog.common.enums.ErrorCode;
import com.pureblog.common.exception.BusinessException;
import com.pureblog.common.result.PageResult;
import com.pureblog.common.utils.DateUtils;
import com.pureblog.common.utils.StringUtils;
import com.pureblog.tree.entity.TreeNodeDO;
import com.pureblog.tree.service.TreeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ArticleContentMapper contentMapper;
    private final ArticleTagMapper articleTagMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final UserMapper userMapper;
    private final ArticleCacheManager cacheManager;
    private final TreeService treeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleDetailVO createArticle(ArticleCreateDTO dto) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        CategoryDO category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);

        treeService.assertIsLeaf(dto.getTreeNodeId());
        TreeNodeDO treeNode = treeService.getNodeById(dto.getTreeNodeId());

        ArticleDO article = new ArticleDO();
        article.setAuthorId(userId);
        article.setCategoryId(dto.getCategoryId());
        article.setTreeId(treeNode.getTreeId());
        article.setTreeNodeId(dto.getTreeNodeId());
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setCoverUrl(dto.getCoverUrl());
        article.setStatus(ArticleStatus.DRAFT.getCode());
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setCommentCount(0);
        article.setCollectCount(0);
        article.setIsFeatured(0);
        article.setIsTop(0);
        articleMapper.insert(article);

        saveContent(article.getId(), dto.getContent(), dto.getHtmlContent());
        saveTags(article.getId(), dto.getTagIds());
        treeService.incrementArticleCount(dto.getTreeNodeId());

        log.info("Article created: id={}, author={}", article.getId(), userId);
        return getArticleDetail(article.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleDetailVO updateArticle(ArticleUpdateDTO dto) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        ArticleDO article = articleMapper.selectByIdSimple(dto.getId());
        if (article == null) throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        if (!article.getAuthorId().equals(userId)) throw new BusinessException(ErrorCode.NOT_AUTHOR);

        treeService.assertIsLeaf(dto.getTreeNodeId());
        TreeNodeDO treeNode = treeService.getNodeById(dto.getTreeNodeId());

        Long previousNodeId = article.getTreeNodeId();

        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setCoverUrl(dto.getCoverUrl());
        article.setCategoryId(dto.getCategoryId());
        article.setTreeId(treeNode.getTreeId());
        article.setTreeNodeId(dto.getTreeNodeId());
        articleMapper.updateById(article);

        updateContent(dto.getId(), dto.getContent(), dto.getHtmlContent());
        updateTags(dto.getId(), dto.getTagIds());
        cacheManager.evictArticleDetail(dto.getId());

        if (previousNodeId != null && !previousNodeId.equals(dto.getTreeNodeId())) {
            treeService.decrementArticleCount(previousNodeId);
            treeService.incrementArticleCount(dto.getTreeNodeId());
        }

        log.info("Article updated: id={}", dto.getId());
        return getArticleDetail(dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(ArticlePublishDTO dto) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        ArticleDO article = articleMapper.selectByIdSimple(dto.getId());
        if (article == null) throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        if (!article.getAuthorId().equals(userId)) throw new BusinessException(ErrorCode.NOT_AUTHOR);

        article.setStatus(ArticleStatus.PUBLISHED.getCode());
        article.setPublishedAt(java.time.LocalDateTime.now());
        if (dto.getIsFeatured() != null) article.setIsFeatured(dto.getIsFeatured());
        if (dto.getIsTop() != null) article.setIsTop(dto.getIsTop());
        articleMapper.updateById(article);

        cacheManager.evictArticleDetail(dto.getId());
        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId).setSql("article_count = article_count + 1"));
        categoryMapper.update(null, new LambdaUpdateWrapper<CategoryDO>()
                .eq(CategoryDO::getId, article.getCategoryId()).setSql("article_count = article_count + 1"));

        log.info("Article published: id={}", dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineArticle(Long articleId) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        ArticleDO article = articleMapper.selectByIdSimple(articleId);
        if (article == null) throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        if (!article.getAuthorId().equals(userId)) throw new BusinessException(ErrorCode.NOT_AUTHOR);

        article.setStatus(ArticleStatus.OFFLINE.getCode());
        articleMapper.updateById(article);

        cacheManager.evictArticleDetail(articleId);
        log.info("Article offline: id={}", articleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long articleId) {
        Long userId = LoginUserHolder.getUserId();
        if (userId == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);

        ArticleDO article = articleMapper.selectByIdSimple(articleId);
        if (article == null) throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        if (!article.getAuthorId().equals(userId)) throw new BusinessException(ErrorCode.NOT_AUTHOR);

        articleMapper.deleteById(articleId);
        contentMapper.delete(new LambdaQueryWrapper<ArticleContentDO>().eq(ArticleContentDO::getArticleId, articleId));
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTagDO>().eq(ArticleTagDO::getArticleId, articleId));

        cacheManager.evictArticleDetail(articleId);
        userMapper.update(null, new LambdaUpdateWrapper<UserDO>()
                .eq(UserDO::getId, userId).setSql("article_count = GREATEST(article_count - 1, 0)"));
        categoryMapper.update(null, new LambdaUpdateWrapper<CategoryDO>()
                .eq(CategoryDO::getId, article.getCategoryId()).setSql("article_count = GREATEST(article_count - 1, 0)"));
        treeService.decrementArticleCount(article.getTreeNodeId());

        log.info("Article deleted: id={}", articleId);
    }

    @Override
    public ArticleDetailVO getArticleDetail(Long articleId) {
        ArticleDetailVO cached = cacheManager.getArticleDetail(articleId);
        if (cached != null) {
            cacheManager.incrementPv(articleId);
            return cached;
        }

        ArticleDO article = articleMapper.selectByIdSimple(articleId);
        if (article == null || article.getStatus() != ArticleStatus.PUBLISHED.getCode()) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }

        ArticleDetailVO vo = buildDetailVO(article);
        cacheManager.cacheArticleDetail(articleId, vo);
        cacheManager.incrementPv(articleId);
        return vo;
    }

    @Override
    public PageResult<ArticleListVO> getArticleList(ArticleQueryDTO query) {
        LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getCategoryId() != null, ArticleDO::getCategoryId, query.getCategoryId());
        wrapper.eq(query.getAuthorId() != null, ArticleDO::getAuthorId, query.getAuthorId());
        wrapper.eq(query.getStatus() != null, ArticleDO::getStatus, query.getStatus());
        wrapper.eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED.getCode());
        wrapper.eq(query.getTreeNodeId() != null, ArticleDO::getTreeNodeId, query.getTreeNodeId());
        wrapper.eq(query.getTreeId() != null && query.getTreeNodeId() == null, ArticleDO::getTreeId, query.getTreeId());
        if (StringUtils.isNotBlank(query.getKeyword())) {
            // 关键词匹配范围与 /api/search 保持一致: 标题 OR 摘要 OR 正文 OR 标签名
            // 用 apply 而非 like(): MyBatis-Plus 的 like() 不带 ESCAPE 子句,
            // 用户搜 "100%" 会被当成通配符; 这里显式用 ESCAPE '!' 处理字面量.
            String kw = StringUtils.escapeLike(query.getKeyword().trim());
            wrapper.and(w -> w
                    .apply("pb_article.title LIKE CONCAT('%', {0}, '%') ESCAPE '!'", kw)
                    .or().apply("pb_article.summary LIKE CONCAT('%', {0}, '%') ESCAPE '!'", kw)
                    .or().apply("EXISTS (SELECT 1 FROM pb_article_content c "
                            + "WHERE c.article_id = pb_article.id "
                            + "AND c.content LIKE CONCAT('%', {0}, '%') ESCAPE '!')", kw)
                    .or().apply("EXISTS (SELECT 1 FROM pb_article_tag at_k "
                            + "JOIN pb_tag t_k ON t_k.id = at_k.tag_id AND t_k.deleted = 0 "
                            + "WHERE at_k.article_id = pb_article.id "
                            + "AND t_k.name LIKE CONCAT('%', {0}, '%') ESCAPE '!')", kw));
        }

        // 排序：置顶优先 → 按 sortBy 指定的字段 → asc/desc
        wrapper.orderByDesc(ArticleDO::getIsTop);
        boolean asc = query.getSortOrder() != null && query.getSortOrder().equalsIgnoreCase("asc");
        switch (query.getSortBy() == null ? "" : query.getSortBy()) {
            case "viewCount" -> {
                if (asc) wrapper.orderByAsc(ArticleDO::getViewCount);
                else wrapper.orderByDesc(ArticleDO::getViewCount);
            }
            case "likeCount" -> {
                if (asc) wrapper.orderByAsc(ArticleDO::getLikeCount);
                else wrapper.orderByDesc(ArticleDO::getLikeCount);
            }
            case "commentCount" -> {
                if (asc) wrapper.orderByAsc(ArticleDO::getCommentCount);
                else wrapper.orderByDesc(ArticleDO::getCommentCount);
            }
            default -> {
                if (asc) wrapper.orderByAsc(ArticleDO::getPublishedAt);
                else wrapper.orderByDesc(ArticleDO::getPublishedAt);
            }
        }

        Page<ArticleDO> page = new Page<>((long) query.getPage(), (long) query.getSize());
        Page<ArticleDO> result = articleMapper.selectPage(page, wrapper);

        List<ArticleListVO> voList = result.getRecords().stream()
                .map(this::buildListVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public List<ArticleListVO> getHotArticles(int limit) {
        List<Long> hotIds = cacheManager.getHotArticleIds(limit);
        if (hotIds.isEmpty()) {
            LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED.getCode())
                    .orderByDesc(ArticleDO::getViewCount, ArticleDO::getLikeCount)
                    .last("LIMIT " + limit);
            return articleMapper.selectList(wrapper).stream().map(this::buildListVO).collect(Collectors.toList());
        }
        List<ArticleDO> articles = new ArrayList<>();
        for (Long id : hotIds) {
            ArticleDO a = articleMapper.selectByIdSimple(id);
            if (a != null && a.getStatus() == ArticleStatus.PUBLISHED.getCode()) {
                articles.add(a);
            }
        }
        return articles.stream().map(this::buildListVO).collect(Collectors.toList());
    }

    @Override
    public List<ArticleListVO> getFeaturedArticles(int limit) {
        LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleDO::getStatus, ArticleStatus.PUBLISHED.getCode())
                .eq(ArticleDO::getIsFeatured, 1)
                .orderByDesc(ArticleDO::getPublishedAt)
                .last("LIMIT " + limit);
        return articleMapper.selectList(wrapper).stream().map(this::buildListVO).collect(Collectors.toList());
    }

    @Override
    public List<ArticleListVO> getAuthorArticles(Long authorId, int page, int size) {
        int offset = (page - 1) * size;
        LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleDO::getAuthorId, authorId)
                .ne(ArticleDO::getStatus, ArticleStatus.OFFLINE.getCode())
                .orderByDesc(ArticleDO::getCreatedAt)
                .last("LIMIT " + offset + "," + size);
        return articleMapper.selectList(wrapper).stream().map(this::buildListVO).collect(Collectors.toList());
    }

    @Override
    public List<com.pureblog.article.vo.CategoryVO> getAllCategories() {
        List<CategoryDO> categories = categoryMapper.selectList(new LambdaQueryWrapper<CategoryDO>()
                .eq(CategoryDO::getDeleted, 0).orderByAsc(CategoryDO::getSortOrder));
        return categories.stream().map(c -> com.pureblog.article.vo.CategoryVO.builder()
                .id(c.getId()).name(c.getName()).slug(c.getSlug())
                .description(c.getDescription()).articleCount(c.getArticleCount()).build())
                .collect(Collectors.toList());
    }

    @Override
    public List<com.pureblog.article.vo.TagVO> getAllTags() {
        List<TagDO> tags = tagMapper.selectList(new LambdaQueryWrapper<TagDO>()
                .eq(TagDO::getDeleted, 0).orderByDesc(TagDO::getArticleCount));
        return tags.stream().map(t -> com.pureblog.article.vo.TagVO.builder()
                .id(t.getId()).name(t.getName()).slug(t.getSlug()).articleCount(t.getArticleCount()).build())
                .collect(Collectors.toList());
    }

    @Override
    public void createCategory(CategoryDO category) {
        if (StringUtils.isBlank(category.getName())) throw new BusinessException(ErrorCode.PARAM_INVALID);
        categoryMapper.insert(category);
        log.info("Category created: id={}, name={}", category.getId(), category.getName());
    }

    @Override
    public void updateCategory(CategoryDO category) {
        categoryMapper.updateById(category);
        log.info("Category updated: id={}", category.getId());
    }

    @Override
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
        log.info("Category deleted: id={}", id);
    }

    @Override
    public void createTag(TagDO tag) {
        if (StringUtils.isBlank(tag.getName())) throw new BusinessException(ErrorCode.PARAM_INVALID);
        tagMapper.insert(tag);
        log.info("Tag created: id={}, name={}", tag.getId(), tag.getName());
    }

    @Override
    public void updateTag(TagDO tag) {
        tagMapper.updateById(tag);
        log.info("Tag updated: id={}", tag.getId());
    }

    @Override
    public void deleteTag(Long id) {
        tagMapper.deleteById(id);
        log.info("Tag deleted: id={}", id);
    }

    @Override
    public void incrementViewCount(Long articleId) {
        cacheManager.incrementPv(articleId);
    }

    private void saveContent(Long articleId, String content, String htmlContent) {
        ArticleContentDO ac = new ArticleContentDO();
        ac.setArticleId(articleId);
        ac.setContent(content);
        ac.setHtmlContent(htmlContent);
        ac.setWordCount(content != null ? content.length() : 0);
        // createdAt / updatedAt 由 MybatisPlusConfig 自动填充
        contentMapper.insert(ac);
    }

    private void updateContent(Long articleId, String content, String htmlContent) {
        LambdaUpdateWrapper<ArticleContentDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ArticleContentDO::getArticleId, articleId)
                .set(ArticleContentDO::getContent, content)
                .set(ArticleContentDO::getHtmlContent, htmlContent)
                .set(ArticleContentDO::getWordCount, content != null ? content.length() : 0)
                .set(ArticleContentDO::getUpdatedAt, java.time.LocalDateTime.now());
        // LambdaUpdateWrapper.update(null, wrapper) 的 entity 为 null,
        // MetaObjectHandler.updateFill 不会跑,这里保留显式赋值最稳.
        contentMapper.update(null, wrapper);
    }

    private void saveTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return;
        for (Long tagId : tagIds) {
            ArticleTagDO at = new ArticleTagDO();
            at.setArticleId(articleId);
            at.setTagId(tagId);
            // createdAt 由自动填充写入
            articleTagMapper.insert(at);
            tagMapper.update(null, new LambdaUpdateWrapper<TagDO>()
                    .eq(TagDO::getId, tagId).setSql("article_count = article_count + 1"));
        }
    }

    private void updateTags(Long articleId, List<Long> newTagIds) {
        List<ArticleTagDO> oldTags = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTagDO>().eq(ArticleTagDO::getArticleId, articleId));
        for (ArticleTagDO old : oldTags) {
            tagMapper.update(null, new LambdaUpdateWrapper<TagDO>()
                    .eq(TagDO::getId, old.getTagId()).setSql("article_count = GREATEST(article_count - 1, 0)"));
        }
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTagDO>().eq(ArticleTagDO::getArticleId, articleId));
        if (newTagIds != null && !newTagIds.isEmpty()) {
            saveTags(articleId, newTagIds);
        }
    }

    private ArticleDetailVO buildDetailVO(ArticleDO article) {
        UserDO author = userMapper.selectById(article.getAuthorId());
        CategoryDO category = categoryMapper.selectById(article.getCategoryId());
        ArticleContentDO content = contentMapper.selectOne(new LambdaQueryWrapper<ArticleContentDO>()
                .eq(ArticleContentDO::getArticleId, article.getId()));
        List<ArticleTagDO> tagRelations = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTagDO>().eq(ArticleTagDO::getArticleId, article.getId()));
        List<TagDO> tags = tagRelations.isEmpty() ? Collections.emptyList() :
                tagMapper.selectBatchIds(tagRelations.stream().map(ArticleTagDO::getTagId).collect(Collectors.toList()));

        Long currentUserId = LoginUserHolder.getUserId();

        return ArticleDetailVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .coverUrl(article.getCoverUrl())
                .content(content != null ? content.getContent() : null)
                .htmlContent(content != null ? content.getHtmlContent() : null)
                .wordCount(content != null ? content.getWordCount() : 0)
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .collectCount(article.getCollectCount())
                .isFeatured(article.getIsFeatured() == 1)
                .isTop(article.getIsTop() == 1)
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .author(author != null ? AuthorVO.builder()
                        .userId(author.getId()).username(author.getUsername())
                        .nickname(author.getNickname()).avatarUrl(author.getAvatarUrl())
                        .followerCount(author.getFollowerCount()).build() : null)
                .category(category != null ? ArticleDetailVO.CategoryVO.builder()
                        .id(category.getId()).name(category.getName()).slug(category.getSlug()).build() : null)
                .tags(tags.stream().map(t -> ArticleDetailVO.TagVO.builder()
                        .id(t.getId()).name(t.getName()).slug(t.getSlug()).build()).collect(Collectors.toList()))
                .isLiked(false)
                .isCollected(false)
                .build();
    }

    private ArticleListVO buildListVO(ArticleDO article) {
        UserDO author = userMapper.selectById(article.getAuthorId());
        CategoryDO category = categoryMapper.selectById(article.getCategoryId());
        List<ArticleTagDO> tagRelations = articleTagMapper.selectList(
                new LambdaQueryWrapper<ArticleTagDO>().eq(ArticleTagDO::getArticleId, article.getId()));
        List<String> tagNames = new ArrayList<>();
        for (ArticleTagDO tr : tagRelations) {
            TagDO t = tagMapper.selectById(tr.getTagId());
            if (t != null) tagNames.add(t.getName());
        }

        return ArticleListVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .coverUrl(article.getCoverUrl())
                .viewCount(article.getViewCount())
                .likeCount(article.getLikeCount())
                .commentCount(article.getCommentCount())
                .isFeatured(article.getIsFeatured() == 1)
                .isTop(article.getIsTop() == 1)
                .publishedAt(article.getPublishedAt())
                .authorName(author != null ? author.getNickname() : null)
                .authorAvatar(author != null ? author.getAvatarUrl() : null)
                .authorId(article.getAuthorId())
                .categoryName(category != null ? category.getName() : null)
                .tagNames(tagNames)
                .build();
    }
}
