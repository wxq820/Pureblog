USE pureblog;
INSERT INTO pb_article (author_id, category_id, title, summary, status, view_count, like_count, comment_count, is_featured, is_top, published_at) VALUES
(1, 1, '从零开始搭建Spring Boot博客项目', '本文记录如何用Spring Boot + MyBatis-Plus从零搭建一个博客后端，涵盖多模块、缓存、鉴权等核心设计。', 1, 234, 18, 5, 1, 1, NOW()),
(1, 1, 'MyBatis-Plus与Spring Boot 3的最佳实践', '分享在Spring Boot 3.2中使用MyBatis-Plus 3.5.11时遇到的兼容性问题及解决方案。', 1, 567, 42, 12, 1, 0, NOW()),
(1, 3, '《代码大全》读书笔记', '软件构建的核心实践与原则：从需求分析到代码评审，每个环节都值得关注。', 1, 89, 7, 2, 0, 0, NOW()),
(1, 2, '周末随想：写作的意义', '为什么坚持写技术博客？分享三点真实感受：沉淀知识、结识同好、影响他人。', 1, 156, 11, 4, 0, 0, NOW());

INSERT INTO pb_article_content (article_id, content, word_count)
SELECT id, CONCAT('# ', title, '\n\n', summary, '\n\n这是占位内容，用于演示前端渲染。'), 200 FROM pb_article;
SELECT id, title, view_count FROM pb_article WHERE status=1 ORDER BY id;
