USE pureblog;

INSERT INTO pb_user (username, email, password_hash, nickname, role, status) VALUES
('admin', 'admin@pureblog.com', '{bcrypt}$2b$10$n2RjfdG2uceykRrldR7mROLiWWbKAspeYmXSXXomSXxzP53BdCy9a', '管理员', 4, 1);

INSERT INTO pb_category (name, slug, description, sort_order) VALUES
('技术', 'tech', '技术相关文章', 1),
('生活', 'life', '生活随笔', 2),
('读书', 'reading', '读书笔记', 3),
('随想', 'thoughts', '随想杂谈', 4);

INSERT INTO pb_tag (name, slug) VALUES
('Java', 'java'),
('Spring Boot', 'spring-boot'),
('Redis', 'redis'),
('MySQL', 'mysql'),
('Elasticsearch', 'elasticsearch'),
('Kafka', 'kafka'),
('Docker', 'docker'),
('Vue', 'vue'),
('架构设计', 'architecture'),
('性能优化', 'performance');

UPDATE pb_article SET title = '从零开始搭建Spring Boot博客项目', summary = '本文记录如何用Spring Boot + MyBatis-Plus从零搭建一个博客后端，涵盖多模块、缓存、鉴权等核心设计。' WHERE id = 1;
UPDATE pb_article SET title = 'MyBatis-Plus与Spring Boot 3的最佳实践', summary = '分享在Spring Boot 3.2中使用MyBatis-Plus 3.5.11时遇到的兼容性问题及解决方案。' WHERE id = 2;
UPDATE pb_article SET title = '《代码大全》读书笔记', summary = '软件构建的核心实践与原则：从需求分析到代码评审，每个环节都值得关注。' WHERE id = 3;
UPDATE pb_article SET title = '周末随想：写作的意义', summary = '为什么坚持写技术博客？分享三点真实感受：沉淀知识、结识同好、影响他人。' WHERE id = 4;
