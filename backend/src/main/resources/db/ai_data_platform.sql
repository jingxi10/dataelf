/*
 Navicat Premium Data Transfer

 Source Server         : datalefl
 Source Server Type    : MySQL
 Source Server Version : 50744
 Source Host           : 59.110.123.64:3306
 Source Schema         : ai_data_platform

 Target Server Type    : MySQL
 Target Server Version : 50744
 File Encoding         : 65001

 Date: 30/12/2025 22:31:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for categories
-- ----------------------------
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `level` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `parent_id` bigint(20) DEFAULT NULL,
  `sort_order` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`)
) ENGINE=MyISAM AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of categories
-- ----------------------------
BEGIN;
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (1, '2025-12-24 10:54:07', '', 1, '数据科学', NULL, 2);
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (2, '2025-12-24 10:54:24', '', 1, '科技', NULL, 1);
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (3, '2025-12-24 10:54:33', '', 1, '区块链', NULL, 3);
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (4, '2025-12-24 10:55:42', '', 1, '数据管理', NULL, 4);
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (5, '2025-12-24 10:56:05', '', 1, '人工智能', NULL, 5);
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (6, '2025-12-24 10:56:12', '', 1, '云计算', NULL, 6);
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (7, '2025-12-24 10:56:29', '', 1, '产品评测', NULL, 7);
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (8, '2025-12-24 10:57:30', '', 1, '案例研究', NULL, 8);
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (9, '2025-12-24 11:01:09', '', 1, '数据报告', NULL, 1);
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (10, '2025-12-24 11:01:28', '', 1, '调查报告', NULL, 1);
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (11, '2025-12-24 11:01:36', '', 1, '行业分析', NULL, 1);
INSERT INTO `categories` (`id`, `created_at`, `description`, `level`, `name`, `parent_id`, `sort_order`) VALUES (12, '2025-12-24 11:01:52', '', 1, '产品分析', NULL, 0);
COMMIT;

-- ----------------------------
-- Table structure for comments
-- ----------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment_text` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `content_id` bigint(20) NOT NULL,
  `created_at` datetime NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `is_pinned` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=MyISAM AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of comments
-- ----------------------------
BEGIN;
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (1, '你好', 5, '2025-12-24 12:48:32', 1, b'1', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (2, '测试你好哈', 3, '2025-12-24 13:19:45', 1, b'0', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (3, '加油', 7, '2025-12-24 13:42:14', 1, b'0', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (4, '我是的', 10, '2025-12-24 15:38:05', 4, b'0', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (5, '我', 7, '2025-12-25 21:09:18', 4, b'0', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (6, '我额额', 7, '2025-12-25 21:09:23', 4, b'1', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (7, '我我', 5, '2025-12-25 21:31:14', 1, b'1', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (8, '我我我', 5, '2025-12-25 21:31:28', 1, b'0', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (9, '我', 15, '2025-12-25 21:40:15', 1, b'1', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (10, 'w', 15, '2025-12-25 21:40:35', 6, b'1', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (11, '我', 15, '2025-12-25 21:41:00', 7, b'0', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (12, 'w w w', 15, '2025-12-25 21:41:17', 6, b'0', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (13, '我', 15, '2025-12-25 21:41:49', 1, b'0', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (14, '好多好', 17, '2025-12-25 23:40:36', 5, b'0', b'0');
INSERT INTO `comments` (`id`, `comment_text`, `content_id`, `created_at`, `user_id`, `is_deleted`, `is_pinned`) VALUES (15, 'w w', 17, '2025-12-25 23:40:59', 8, b'0', b'0');
COMMIT;

-- ----------------------------
-- Table structure for content_categories
-- ----------------------------
DROP TABLE IF EXISTS `content_categories`;
CREATE TABLE `content_categories` (
  `content_id` bigint(20) NOT NULL,
  `category_id` bigint(20) NOT NULL,
  PRIMARY KEY (`content_id`,`category_id`),
  KEY `FK6eh12wo661in1jxfjlm2a8lm3` (`category_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of content_categories
-- ----------------------------
BEGIN;
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (3, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (5, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (6, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (7, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (8, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (9, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (10, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (11, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (12, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (13, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (15, 2);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (16, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (17, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (18, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (19, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (20, 2);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (21, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (22, 1);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (22, 2);
INSERT INTO `content_categories` (`content_id`, `category_id`) VALUES (23, 3);
COMMIT;

-- ----------------------------
-- Table structure for content_tags
-- ----------------------------
DROP TABLE IF EXISTS `content_tags`;
CREATE TABLE `content_tags` (
  `content_id` bigint(20) NOT NULL,
  `tag_id` bigint(20) NOT NULL,
  PRIMARY KEY (`content_id`,`tag_id`),
  KEY `FKcr5rprg24ccjyjsnc7cac859f` (`tag_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of content_tags
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for contents
-- ----------------------------
DROP TABLE IF EXISTS `contents`;
CREATE TABLE `contents` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `author_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content_source` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `copyright_notice` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL,
  `html_output` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `integrity_score` decimal(3,2) DEFAULT NULL,
  `is_original` bit(1) NOT NULL,
  `json_ld` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `markdown_output` text COLLATE utf8mb4_unicode_ci,
  `published_at` datetime DEFAULT NULL,
  `reject_reason` text COLLATE utf8mb4_unicode_ci,
  `reviewed_at` datetime DEFAULT NULL,
  `reviewed_by` bigint(20) DEFAULT NULL,
  `status` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `structured_data` json NOT NULL,
  `submitted_at` datetime DEFAULT NULL,
  `template_id` bigint(20) NOT NULL,
  `title` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `view_count` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_status` (`status`),
  KEY `idx_published_at` (`published_at`)
) ENGINE=MyISAM AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of contents
-- ----------------------------
BEGIN;
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (10, '测试新增管理员功能', '测试新增管理员功能', '测试新增管理员功能', '2025-12-24 15:18:05', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>测试新增管理员功能</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试新增管理员功能\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试新增管理员功能\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"测试新增管理员功能测试新增管理员功能\",\n  \"keywords\" : \"\",\n  \"image\" : \"\",\n  \"copyrightNotice\" : \"测试新增管理员功能\",\n  \"sourceOrganization\" : \"测试新增管理员功能\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">测试新增管理员功能</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">测试新增管理员功能</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：测试新增管理员功能</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">测试新增管理员功能</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"10\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.67, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试新增管理员功能\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试新增管理员功能\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"测试新增管理员功能测试新增管理员功能\",\n  \"keywords\" : \"\",\n  \"image\" : \"\",\n  \"copyrightNotice\" : \"测试新增管理员功能\",\n  \"sourceOrganization\" : \"测试新增管理员功能\",\n  \"isOriginalContent\" : true\n}', '# 测试新增管理员功能\n\n---\n\n## 版权信息\n\n**作者：** 测试新增管理员功能\n\n**来源：** 测试新增管理员功能\n\n**版权声明：** 测试新增管理员功能\n\n**原创：** 是\n\n', '2025-12-24 15:36:29', NULL, '2025-12-24 15:34:03', 5, 'PUBLISHED', '{\"image\": \"\", \"author\": \"测试新增管理员功能\", \"headline\": \"测试新增管理员功能\", \"keywords\": \"\", \"articleBody\": \"测试新增管理员功能测试新增管理员功能\", \"datePublished\": \"2025-12-23T16:00:00.000Z\"}', '2025-12-24 15:18:05', 1, '测试新增管理员功能', '2025-12-25 19:45:30', 4, 5);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (9, '用户测试2-发布成功', '用户测试2-发布成功', '用户测试2-发布成功', '2025-12-24 14:56:45', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>用户测试2-发布成功</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"用户测试2-发布成功\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"用户测试2-发布成功\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"我我我我我链接点击查看<a href=\\\"https://www.douyin.com/jingxuan?modal_id=7585822793170734363\\\" target=\\\"_blank\\\" rel=\\\"noopener noreferrer\\\">11</a>用户测试2-发布成功用户测试2-发布成功<img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/24/27690125-3a0d-4a9f-b15d-3b9367bddbc4.png\\\" alt=\\\"图片主体抠取与背景替换 (1).png\\\">\",\n  \"keywords\" : \"用户测试2-发布成功\",\n  \"image\" : \"用户测试2-发布成功\",\n  \"copyrightNotice\" : \"用户测试2-发布成功\",\n  \"sourceOrganization\" : \"用户测试2-发布成功\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">用户测试2-发布成功</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">用户测试2-发布成功</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：用户测试2-发布成功</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">用户测试2-发布成功</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"9\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 1.00, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"用户测试2-发布成功\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"用户测试2-发布成功\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"我我我我我链接点击查看<a href=\\\"https://www.douyin.com/jingxuan?modal_id=7585822793170734363\\\" target=\\\"_blank\\\" rel=\\\"noopener noreferrer\\\">11</a>用户测试2-发布成功用户测试2-发布成功<img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/24/27690125-3a0d-4a9f-b15d-3b9367bddbc4.png\\\" alt=\\\"图片主体抠取与背景替换 (1).png\\\">\",\n  \"keywords\" : \"用户测试2-发布成功\",\n  \"image\" : \"用户测试2-发布成功\",\n  \"copyrightNotice\" : \"用户测试2-发布成功\",\n  \"sourceOrganization\" : \"用户测试2-发布成功\",\n  \"isOriginalContent\" : true\n}', '# 用户测试2-发布成功\n\n---\n\n## 版权信息\n\n**作者：** 用户测试2-发布成功\n\n**来源：** 用户测试2-发布成功\n\n**版权声明：** 用户测试2-发布成功\n\n**原创：** 是\n\n', '2025-12-24 15:07:41', NULL, '2025-12-24 15:05:52', 1, 'PUBLISHED', '{\"image\": \"用户测试2-发布成功\", \"author\": \"用户测试2-发布成功\", \"headline\": \"用户测试2-发布成功\", \"keywords\": \"用户测试2-发布成功\", \"articleBody\": \"我我我我我链接点击查看<a href=\\\"https://www.douyin.com/jingxuan?modal_id=7585822793170734363\\\" target=\\\"_blank\\\" rel=\\\"noopener noreferrer\\\">11</a>用户测试2-发布成功用户测试2-发布成功<img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/24/27690125-3a0d-4a9f-b15d-3b9367bddbc4.png\\\" alt=\\\"图片主体抠取与背景替换 (1).png\\\">\", \"datePublished\": \"2025-12-23T16:00:00.000Z\"}', '2025-12-24 14:56:45', 1, '用户测试2-发布成功', '2025-12-26 14:16:19', 4, 14);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (3, '测试3', '测试3', '测试3', '2025-12-24 12:30:48', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>测试3发布</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试3发布\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试3\"\n  },\n  \"datePublished\" : \"2025-12-22T16:00:00.000Z\",\n  \"articleBody\" : \"发布成功测试2测试2测试2测试2\",\n  \"keywords\" : \"测试3\",\n  \"image\" : \"测试3\",\n  \"copyrightNotice\" : \"测试3\",\n  \"sourceOrganization\" : \"测试3\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">测试3发布</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">测试3</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：测试3</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">测试3</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"null\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 1.00, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试3发布\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试3\"\n  },\n  \"datePublished\" : \"2025-12-22T16:00:00.000Z\",\n  \"articleBody\" : \"发布成功测试2测试2测试2测试2\",\n  \"keywords\" : \"测试3\",\n  \"image\" : \"测试3\",\n  \"copyrightNotice\" : \"测试3\",\n  \"sourceOrganization\" : \"测试3\",\n  \"isOriginalContent\" : true\n}', '# 测试3发布\n\n---\n\n## 版权信息\n\n**作者：** 测试3\n\n**来源：** 测试3\n\n**版权声明：** 测试3\n\n**原创：** 是\n\n', '2025-12-24 13:19:27', '管理员下架', '2025-12-24 12:43:59', 1, 'REJECTED', '{\"image\": \"测试3\", \"author\": \"测试3\", \"headline\": \"测试3\", \"keywords\": \"测试3\", \"articleBody\": \"发布成功测试2测试2测试2测试2\", \"datePublished\": \"2025-12-22T16:00:00.000Z\"}', '2025-12-24 12:32:36', 1, '测试3发布', '2025-12-25 21:22:56', 1, 9);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (5, '测试4', '测试4', '测试4', '2025-12-24 12:39:44', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>测试4-退回从新编辑</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试4-退回从新编辑\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试4\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"测试4测试4测试4<div>测试4测试4测试4</div><div><br></div>\",\n  \"keywords\" : \"测试4\",\n  \"image\" : \"测试4\",\n  \"copyrightNotice\" : \"测试4\",\n  \"sourceOrganization\" : \"测试4\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">测试4-退回从新编辑</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">测试4</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：测试4</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">测试4</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"null\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 1.00, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试4-退回从新编辑\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试4\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"测试4测试4测试4<div>测试4测试4测试4</div><div><br></div>\",\n  \"keywords\" : \"测试4\",\n  \"image\" : \"测试4\",\n  \"copyrightNotice\" : \"测试4\",\n  \"sourceOrganization\" : \"测试4\",\n  \"isOriginalContent\" : true\n}', '# 测试4-退回从新编辑\n\n---\n\n## 版权信息\n\n**作者：** 测试4\n\n**来源：** 测试4\n\n**版权声明：** 测试4\n\n**原创：** 是\n\n', '2025-12-25 21:30:48', NULL, '2025-12-25 21:30:48', 1, 'PUBLISHED', '{\"image\": \"测试4\", \"author\": \"测试4\", \"headline\": \"测试4\", \"keywords\": \"测试4\", \"articleBody\": \"测试4测试4测试4<div>测试4测试4测试4</div><div><br></div>\", \"datePublished\": \"2025-12-23T16:00:00.000Z\"}', '2025-12-25 21:29:55', 1, '测试4-退回从新编辑', '2025-12-25 21:32:07', 1, 31);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (8, '用户测试1', '用户测试1', '用户测试1', '2025-12-24 14:54:09', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>用户测试1</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"用户测试1\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"用户测试1\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"用户测试1用户测试1用户测试1<img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/24/96bd6ee0-bb99-4853-a3dd-25285ddecdc5.jpg\\\" alt=\\\"图片主体抠取与背景替换 (1)副本.jpg\\\">\",\n  \"keywords\" : \"用户测试1\",\n  \"image\" : \"用户测试1\",\n  \"copyrightNotice\" : \"用户测试1\",\n  \"sourceOrganization\" : \"用户测试1\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">用户测试1</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">用户测试1</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：用户测试1</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">用户测试1</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"8\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 1.00, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"用户测试1\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"用户测试1\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"用户测试1用户测试1用户测试1<img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/24/96bd6ee0-bb99-4853-a3dd-25285ddecdc5.jpg\\\" alt=\\\"图片主体抠取与背景替换 (1)副本.jpg\\\">\",\n  \"keywords\" : \"用户测试1\",\n  \"image\" : \"用户测试1\",\n  \"copyrightNotice\" : \"用户测试1\",\n  \"sourceOrganization\" : \"用户测试1\",\n  \"isOriginalContent\" : true\n}', '# 用户测试1\n\n---\n\n## 版权信息\n\n**作者：** 用户测试1\n\n**来源：** 用户测试1\n\n**版权声明：** 用户测试1\n\n**原创：** 是\n\n', NULL, '修改', '2025-12-24 15:06:07', 1, 'REJECTED', '{\"image\": \"用户测试1\", \"author\": \"用户测试1\", \"headline\": \"用户测试1\", \"keywords\": \"用户测试1\", \"articleBody\": \"用户测试1用户测试1用户测试1<img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/24/96bd6ee0-bb99-4853-a3dd-25285ddecdc5.jpg\\\" alt=\\\"图片主体抠取与背景替换 (1)副本.jpg\\\">\", \"datePublished\": \"2025-12-23T16:00:00.000Z\"}', '2025-12-24 14:54:09', 1, '用户测试1', '2025-12-24 15:39:57', 4, 1);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (6, '测试管理员审核端', '测试管理员审核端', '测试管理员审核端', '2025-12-24 13:33:15', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>测试管理员审核端</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试管理员审核端\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试管理员审核端\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"测试管理员审核端测试管理员审核端测试管理员审核端测试管理员审核端<div><span style=\\\"color: var(--el-text-color-primary); font-family: var(--font-family-base); font-size: var(--font-size-base); font-weight: var(--font-weight-normal);\\\">测试管理员审核端测试管理员审核端</span></div>\",\n  \"keywords\" : \"测试管理员审核端\",\n  \"image\" : \"测试管理员审核端\",\n  \"copyrightNotice\" : \"测试管理员审核端\",\n  \"sourceOrganization\" : \"测试管理员审核端\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">测试管理员审核端</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">测试管理员审核端</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：测试管理员审核端</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">测试管理员审核端</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"6\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 1.00, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试管理员审核端\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试管理员审核端\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"测试管理员审核端测试管理员审核端测试管理员审核端测试管理员审核端<div><span style=\\\"color: var(--el-text-color-primary); font-family: var(--font-family-base); font-size: var(--font-size-base); font-weight: var(--font-weight-normal);\\\">测试管理员审核端测试管理员审核端</span></div>\",\n  \"keywords\" : \"测试管理员审核端\",\n  \"image\" : \"测试管理员审核端\",\n  \"copyrightNotice\" : \"测试管理员审核端\",\n  \"sourceOrganization\" : \"测试管理员审核端\",\n  \"isOriginalContent\" : true\n}', '# 测试管理员审核端\n\n---\n\n## 版权信息\n\n**作者：** 测试管理员审核端\n\n**来源：** 测试管理员审核端\n\n**版权声明：** 测试管理员审核端\n\n**原创：** 是\n\n', NULL, NULL, '2025-12-24 13:35:03', 1, 'APPROVED', '{\"image\": \"测试管理员审核端\", \"author\": \"测试管理员审核端\", \"headline\": \"测试管理员审核端\", \"keywords\": \"测试管理员审核端\", \"articleBody\": \"测试管理员审核端测试管理员审核端测试管理员审核端测试管理员审核端<div><span style=\\\"color: var(--el-text-color-primary); font-family: var(--font-family-base); font-size: var(--font-size-base); font-weight: var(--font-weight-normal);\\\">测试管理员审核端测试管理员审核端</span></div>\", \"datePublished\": \"2025-12-23T16:00:00.000Z\"}', '2025-12-24 13:33:15', 1, '测试管理员审核端', '2025-12-26 11:29:38', 1, 1);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (7, '图片测试发布效果', '图片测试发布效果', '图片测试发布效果', '2025-12-24 13:40:42', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>图片测试发布效果</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"图片测试发布效果\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"图片测试发布效果\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"图片测试发布效果图片测试发布效果<img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/24/fdf46bdf-a957-4cd0-90c6-88bb373e8a32.JPG\\\" alt=\\\"DSC00040.JPG\\\">\",\n  \"keywords\" : \"图片测试发布效果\",\n  \"image\" : \"图片测试发布效果\",\n  \"copyrightNotice\" : \"图片测试发布效果\",\n  \"sourceOrganization\" : \"图片测试发布效果\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">图片测试发布效果</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">图片测试发布效果</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：图片测试发布效果</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">图片测试发布效果</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"7\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 1.00, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"图片测试发布效果\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"图片测试发布效果\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"图片测试发布效果图片测试发布效果<img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/24/fdf46bdf-a957-4cd0-90c6-88bb373e8a32.JPG\\\" alt=\\\"DSC00040.JPG\\\">\",\n  \"keywords\" : \"图片测试发布效果\",\n  \"image\" : \"图片测试发布效果\",\n  \"copyrightNotice\" : \"图片测试发布效果\",\n  \"sourceOrganization\" : \"图片测试发布效果\",\n  \"isOriginalContent\" : true\n}', '# 图片测试发布效果\n\n---\n\n## 版权信息\n\n**作者：** 图片测试发布效果\n\n**来源：** 图片测试发布效果\n\n**版权声明：** 图片测试发布效果\n\n**原创：** 是\n\n', '2025-12-24 13:41:46', NULL, '2025-12-24 13:41:46', 1, 'PUBLISHED', '{\"image\": \"图片测试发布效果\", \"author\": \"图片测试发布效果\", \"headline\": \"图片测试发布效果\", \"keywords\": \"图片测试发布效果\", \"articleBody\": \"图片测试发布效果图片测试发布效果<img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/24/fdf46bdf-a957-4cd0-90c6-88bb373e8a32.JPG\\\" alt=\\\"DSC00040.JPG\\\">\", \"datePublished\": \"2025-12-23T16:00:00.000Z\"}', '2025-12-24 13:40:42', 1, '图片测试发布效果', '2025-12-26 14:16:25', 1, 12);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (11, '测试会员草稿箱功能', '测试会员草稿箱功能', '测试会员草稿箱功能测试会员草稿箱功能', '2025-12-24 15:18:59', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>测试会员草稿箱功能</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试会员草稿箱功能\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试会员草稿箱功能\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"测试会员草稿箱功能测试会员草稿箱功能\",\n  \"keywords\" : \"测试会员草稿箱功能\",\n  \"image\" : \"测试会员草稿箱功能\",\n  \"copyrightNotice\" : \"测试会员草稿箱功能测试会员草稿箱功能\",\n  \"sourceOrganization\" : \"测试会员草稿箱功能\",\n  \"isOriginalContent\" : false\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">测试会员草稿箱功能</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">测试会员草稿箱功能</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：测试会员草稿箱功能测试会员草稿箱功能</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">测试会员草稿箱功能</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"null\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 1.00, b'0', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试会员草稿箱功能\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试会员草稿箱功能\"\n  },\n  \"datePublished\" : \"2025-12-23T16:00:00.000Z\",\n  \"articleBody\" : \"测试会员草稿箱功能测试会员草稿箱功能\",\n  \"keywords\" : \"测试会员草稿箱功能\",\n  \"image\" : \"测试会员草稿箱功能\",\n  \"copyrightNotice\" : \"测试会员草稿箱功能测试会员草稿箱功能\",\n  \"sourceOrganization\" : \"测试会员草稿箱功能\",\n  \"isOriginalContent\" : false\n}', '# 测试会员草稿箱功能\n\n---\n\n## 版权信息\n\n**作者：** 测试会员草稿箱功能\n\n**来源：** 测试会员草稿箱功能\n\n**版权声明：** 测试会员草稿箱功能测试会员草稿箱功能\n\n**原创：** 否\n\n', NULL, NULL, '2025-12-25 21:20:57', 1, 'APPROVED', '{\"image\": \"测试会员草稿箱功能\", \"author\": \"测试会员草稿箱功能\", \"headline\": \"测试会员草稿箱功能\", \"keywords\": \"测试会员草稿箱功能\", \"articleBody\": \"测试会员草稿箱功能测试会员草稿箱功能\", \"datePublished\": \"2025-12-23T16:00:00.000Z\"}', '2025-12-25 21:19:17', 1, '测试会员草稿箱功能', '2025-12-25 21:20:57', 4, 0);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (12, '新版测试-1', '新版测试-1', '新版测试-1', '2025-12-25 21:17:41', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>新版测试-1</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"新版测试-1\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"新版测试-1\"\n  },\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"111111111<div>新版测试-1新版测试-1新版测试-1新版测试-1新版测试-1新版测试-1<br><img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/25/55e805e0-c226-41e5-877b-bf182b8e4dad.jpg\\\" alt=\\\"侧-1.jpg\\\"></div>\",\n  \"keywords\" : \"\",\n  \"image\" : \"\",\n  \"about\" : \"新版测试-1\",\n  \"copyrightNotice\" : \"新版测试-1\",\n  \"sourceOrganization\" : \"新版测试-1\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">新版测试-1</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">新版测试-1</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：新版测试-1</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">新版测试-1</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"12\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.83, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"新版测试-1\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"新版测试-1\"\n  },\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"111111111<div>新版测试-1新版测试-1新版测试-1新版测试-1新版测试-1新版测试-1<br><img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/25/55e805e0-c226-41e5-877b-bf182b8e4dad.jpg\\\" alt=\\\"侧-1.jpg\\\"></div>\",\n  \"keywords\" : \"\",\n  \"image\" : \"\",\n  \"about\" : \"新版测试-1\",\n  \"copyrightNotice\" : \"新版测试-1\",\n  \"sourceOrganization\" : \"新版测试-1\",\n  \"isOriginalContent\" : true\n}', '# 新版测试-1\n\n---\n\n## 版权信息\n\n**作者：** 新版测试-1\n\n**来源：** 新版测试-1\n\n**版权声明：** 新版测试-1\n\n**原创：** 是\n\n', NULL, NULL, NULL, NULL, 'DRAFT', '{\"about\": \"新版测试-1\", \"image\": \"\", \"author\": \"新版测试-1\", \"headline\": \"新版测试-1\", \"keywords\": \"\", \"articleBody\": \"111111111<div>新版测试-1新版测试-1新版测试-1新版测试-1新版测试-1新版测试-1<br><img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/25/55e805e0-c226-41e5-877b-bf182b8e4dad.jpg\\\" alt=\\\"侧-1.jpg\\\"></div>\", \"datePublished\": \"2025-12-24T16:00:00.000Z\"}', NULL, 2, '新版测试-1', '2025-12-25 21:19:41', 4, 1);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (13, '新版测试-2', '新版测试-2', '新版测试-2', '2025-12-25 21:18:54', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>新版测试-2</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"AnalysisNewsArticle\",\n  \"headline\" : \"新版测试-2\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"新版测试-2\"\n  },\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"新版测试-2新版测试-2新版测试-2\",\n  \"about\" : \"新版测试-2\",\n  \"keywords\" : \"\",\n  \"name\" : \"\",\n  \"text\" : \"\",\n  \"reviewBody\" : \"\",\n  \"itemReviewed\" : \"\",\n  \"reviewRating\" : \"\",\n  \"image\" : \"\",\n  \"copyrightNotice\" : \"新版测试-2\",\n  \"sourceOrganization\" : \"新版测试-2\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/AnalysisNewsArticle\">\n    <h1 itemprop=\"headline\">新版测试-2</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">新版测试-2</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：新版测试-2</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">新版测试-2</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"13\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.83, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"AnalysisNewsArticle\",\n  \"headline\" : \"新版测试-2\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"新版测试-2\"\n  },\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"新版测试-2新版测试-2新版测试-2\",\n  \"about\" : \"新版测试-2\",\n  \"keywords\" : \"\",\n  \"name\" : \"\",\n  \"text\" : \"\",\n  \"reviewBody\" : \"\",\n  \"itemReviewed\" : \"\",\n  \"reviewRating\" : \"\",\n  \"image\" : \"\",\n  \"copyrightNotice\" : \"新版测试-2\",\n  \"sourceOrganization\" : \"新版测试-2\",\n  \"isOriginalContent\" : true\n}', '# 新版测试-2\n\n---\n\n## 版权信息\n\n**作者：** 新版测试-2\n\n**来源：** 新版测试-2\n\n**版权声明：** 新版测试-2\n\n**原创：** 是\n\n', NULL, '不通过', '2025-12-25 21:21:18', 1, 'REJECTED', '{\"name\": \"\", \"text\": \"\", \"about\": \"新版测试-2\", \"image\": \"\", \"author\": \"新版测试-2\", \"headline\": \"新版测试-2\", \"keywords\": \"\", \"reviewBody\": \"\", \"articleBody\": \"新版测试-2新版测试-2新版测试-2\", \"itemReviewed\": \"\", \"reviewRating\": \"\", \"datePublished\": \"2025-12-24T16:00:00.000Z\"}', '2025-12-25 21:18:54', 6, '新版测试-2', '2025-12-25 21:21:18', 4, 1);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (17, '测试文章视频是否能打开', '测试文章视频是否能打开', '测试文章视频是否能打开', '2025-12-25 23:38:14', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>测试文章视频是否能打开</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试文章视频是否能打开\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试文章视频是否能打开\"\n  },\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"<div><br></div><video src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/videos/2025/12/25/d54c4b36-1430-4d5e-b67a-12202e8aaa54.mp4\\\" controls=\\\"\\\" preload=\\\"metadata\\\" style=\\\"max-width: 100%; height: auto; display: block; margin-top: 16px; margin-bottom: 16px; border-radius: 8px; background-color: rgb(0, 0, 0);\\\"></video>测试文章视频是否能打开测试文章视频是否能打开<div>测试文章视频是否能打开</div><img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/25/c811ace7-8bb4-4562-8b90-d4cf58b3a643.png\\\" alt=\\\"大雪.png\\\"><div><video src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/videos/2025/12/25/351a15c6-3f14-4615-b4f5-098b6ae66634.mp4\\\" controls=\\\"\\\" preload=\\\"metadata\\\" style=\\\"max-width: 100%; height: auto; display: block; margin-top: 16px; margin-bottom: 16px; border-radius: 8px; background-color: rgb(0, 0, 0);\\\"></video></div>\",\n  \"keywords\" : \"测试文章视频是否能打开\",\n  \"image\" : \"\",\n  \"copyrightNotice\" : \"测试文章视频是否能打开\",\n  \"sourceOrganization\" : \"测试文章视频是否能打开\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">测试文章视频是否能打开</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">测试文章视频是否能打开</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：测试文章视频是否能打开</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">测试文章视频是否能打开</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"17\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.83, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试文章视频是否能打开\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试文章视频是否能打开\"\n  },\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"<div><br></div><video src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/videos/2025/12/25/d54c4b36-1430-4d5e-b67a-12202e8aaa54.mp4\\\" controls=\\\"\\\" preload=\\\"metadata\\\" style=\\\"max-width: 100%; height: auto; display: block; margin-top: 16px; margin-bottom: 16px; border-radius: 8px; background-color: rgb(0, 0, 0);\\\"></video>测试文章视频是否能打开测试文章视频是否能打开<div>测试文章视频是否能打开</div><img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/25/c811ace7-8bb4-4562-8b90-d4cf58b3a643.png\\\" alt=\\\"大雪.png\\\"><div><video src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/videos/2025/12/25/351a15c6-3f14-4615-b4f5-098b6ae66634.mp4\\\" controls=\\\"\\\" preload=\\\"metadata\\\" style=\\\"max-width: 100%; height: auto; display: block; margin-top: 16px; margin-bottom: 16px; border-radius: 8px; background-color: rgb(0, 0, 0);\\\"></video></div>\",\n  \"keywords\" : \"测试文章视频是否能打开\",\n  \"image\" : \"\",\n  \"copyrightNotice\" : \"测试文章视频是否能打开\",\n  \"sourceOrganization\" : \"测试文章视频是否能打开\",\n  \"isOriginalContent\" : true\n}', '# 测试文章视频是否能打开\n\n---\n\n## 版权信息\n\n**作者：** 测试文章视频是否能打开\n\n**来源：** 测试文章视频是否能打开\n\n**版权声明：** 测试文章视频是否能打开\n\n**原创：** 是\n\n', '2025-12-25 23:39:58', NULL, '2025-12-25 23:39:58', 1, 'PUBLISHED', '{\"image\": \"\", \"author\": \"测试文章视频是否能打开\", \"headline\": \"测试文章视频是否能打开\", \"keywords\": \"测试文章视频是否能打开\", \"articleBody\": \"<div><br></div><video src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/videos/2025/12/25/d54c4b36-1430-4d5e-b67a-12202e8aaa54.mp4\\\" controls=\\\"\\\" preload=\\\"metadata\\\" style=\\\"max-width: 100%; height: auto; display: block; margin-top: 16px; margin-bottom: 16px; border-radius: 8px; background-color: rgb(0, 0, 0);\\\"></video>测试文章视频是否能打开测试文章视频是否能打开<div>测试文章视频是否能打开</div><img src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/images/2025/12/25/c811ace7-8bb4-4562-8b90-d4cf58b3a643.png\\\" alt=\\\"大雪.png\\\"><div><video src=\\\"https://datalef.oss-cn-hangzhou.aliyuncs.com/dataelf/videos/2025/12/25/351a15c6-3f14-4615-b4f5-098b6ae66634.mp4\\\" controls=\\\"\\\" preload=\\\"metadata\\\" style=\\\"max-width: 100%; height: auto; display: block; margin-top: 16px; margin-bottom: 16px; border-radius: 8px; background-color: rgb(0, 0, 0);\\\"></video></div>\", \"datePublished\": \"2025-12-24T16:00:00.000Z\"}', '2025-12-25 23:38:15', 1, '测试文章视频是否能打开', '2025-12-26 14:16:03', 1, 17);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (15, '测试发布成功的文章删除', '测试发布成功的文章删除', '测试发布成功的文章删除', '2025-12-25 21:38:07', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>测试发布成功的文章删除</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试发布成功的文章删除\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试发布成功的文章删除\"\n  },\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"测试发布成功的文章删除<div>测试发布成功的文章删除</div><div>测试发布成功的文章删除</div>\",\n  \"keywords\" : \"\",\n  \"image\" : \"\",\n  \"copyrightNotice\" : \"测试发布成功的文章删除\",\n  \"sourceOrganization\" : \"测试发布成功的文章删除\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">测试发布成功的文章删除</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">测试发布成功的文章删除</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：测试发布成功的文章删除</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">测试发布成功的文章删除</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"15\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.67, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试发布成功的文章删除\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"测试发布成功的文章删除\"\n  },\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"测试发布成功的文章删除<div>测试发布成功的文章删除</div><div>测试发布成功的文章删除</div>\",\n  \"keywords\" : \"\",\n  \"image\" : \"\",\n  \"copyrightNotice\" : \"测试发布成功的文章删除\",\n  \"sourceOrganization\" : \"测试发布成功的文章删除\",\n  \"isOriginalContent\" : true\n}', '# 测试发布成功的文章删除\n\n---\n\n## 版权信息\n\n**作者：** 测试发布成功的文章删除\n\n**来源：** 测试发布成功的文章删除\n\n**版权声明：** 测试发布成功的文章删除\n\n**原创：** 是\n\n', '2025-12-25 21:38:24', NULL, '2025-12-25 21:38:24', 6, 'PUBLISHED', '{\"image\": \"\", \"author\": \"测试发布成功的文章删除\", \"headline\": \"测试发布成功的文章删除\", \"keywords\": \"\", \"articleBody\": \"测试发布成功的文章删除<div>测试发布成功的文章删除</div><div>测试发布成功的文章删除</div>\", \"datePublished\": \"2025-12-24T16:00:00.000Z\"}', '2025-12-25 21:38:07', 1, '测试发布成功的文章删除', '2025-12-26 14:16:10', 1, 13);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (16, '主管理员测试', '主管理员测试', '主管理员测试', '2025-12-25 21:52:48', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>主管理员测试</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"主管理员测试\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"主管理员测试\"\n  },\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"主管理员测试主管理员测试主管理员测试\",\n  \"keywords\" : \"主管理员测试\",\n  \"image\" : \"\",\n  \"copyrightNotice\" : \"主管理员测试\",\n  \"sourceOrganization\" : \"主管理员测试\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">主管理员测试</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">主管理员测试</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：主管理员测试</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">主管理员测试</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"16\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.83, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"主管理员测试\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"主管理员测试\"\n  },\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"主管理员测试主管理员测试主管理员测试\",\n  \"keywords\" : \"主管理员测试\",\n  \"image\" : \"\",\n  \"copyrightNotice\" : \"主管理员测试\",\n  \"sourceOrganization\" : \"主管理员测试\",\n  \"isOriginalContent\" : true\n}', '# 主管理员测试\n\n---\n\n## 版权信息\n\n**作者：** 主管理员测试\n\n**来源：** 主管理员测试\n\n**版权声明：** 主管理员测试\n\n**原创：** 是\n\n', NULL, '位置', '2025-12-25 23:43:26', 5, 'REJECTED', '{\"image\": \"\", \"author\": \"主管理员测试\", \"headline\": \"主管理员测试\", \"keywords\": \"主管理员测试\", \"articleBody\": \"主管理员测试主管理员测试主管理员测试\", \"datePublished\": \"2025-12-24T16:00:00.000Z\"}', '2025-12-25 21:52:48', 1, '主管理员测试', '2025-12-25 23:43:26', 5, 0);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (18, '', '', '', '2025-12-25 23:44:44', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>测试内容审核页的模板类目</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试内容审核页的模板类目\",\n  \"author\" : \"测试内容审核页的模板类目\",\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"测试内容审核页的模板类目<div>测试内容审核页的模板类目</div>\",\n  \"about\" : \"测试内容审核页的模板类目\",\n  \"keywords\" : \"\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">测试内容审核页的模板类目</h1>\n    <footer class=\"copyright-section\">\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"18\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.83, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"测试内容审核页的模板类目\",\n  \"author\" : \"测试内容审核页的模板类目\",\n  \"datePublished\" : \"2025-12-24T16:00:00.000Z\",\n  \"articleBody\" : \"测试内容审核页的模板类目<div>测试内容审核页的模板类目</div>\",\n  \"about\" : \"测试内容审核页的模板类目\",\n  \"keywords\" : \"\",\n  \"isOriginalContent\" : true\n}', '# 测试内容审核页的模板类目\n\n---\n\n## 版权信息\n\n**原创：** 是\n\n', NULL, NULL, '2025-12-26 00:04:48', 1, 'APPROVED', '{\"about\": \"测试内容审核页的模板类目\", \"author\": \"测试内容审核页的模板类目\", \"headline\": \"测试内容审核页的模板类目\", \"keywords\": \"\", \"articleBody\": \"测试内容审核页的模板类目<div>测试内容审核页的模板类目</div>\", \"datePublished\": \"2025-12-24T16:00:00.000Z\"}', '2025-12-25 23:45:00', 2, '测试内容审核页的模板类目', '2025-12-26 00:04:48', 8, 0);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (19, '', '', '', '2025-12-26 00:06:23', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>自己写的自己是否可删除测试</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"自己写的自己是否可删除测试\",\n  \"author\" : \"自己写的自己是否可删除测试\",\n  \"datePublished\" : \"2025-12-25T16:00:00.000Z\",\n  \"articleBody\" : \"自己写的自己是否可删除测试\",\n  \"keywords\" : \"\",\n  \"image\" : \"\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">自己写的自己是否可删除测试</h1>\n    <footer class=\"copyright-section\">\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"19\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.67, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"自己写的自己是否可删除测试\",\n  \"author\" : \"自己写的自己是否可删除测试\",\n  \"datePublished\" : \"2025-12-25T16:00:00.000Z\",\n  \"articleBody\" : \"自己写的自己是否可删除测试\",\n  \"keywords\" : \"\",\n  \"image\" : \"\",\n  \"isOriginalContent\" : true\n}', '# 自己写的自己是否可删除测试\n\n---\n\n## 版权信息\n\n**原创：** 是\n\n', '2025-12-26 00:06:43', NULL, '2025-12-26 00:06:35', 1, 'PUBLISHED', '{\"image\": \"\", \"author\": \"自己写的自己是否可删除测试\", \"headline\": \"自己写的自己是否可删除测试\", \"keywords\": \"\", \"articleBody\": \"自己写的自己是否可删除测试\", \"datePublished\": \"2025-12-25T16:00:00.000Z\"}', '2025-12-26 00:06:24', 1, '自己写的自己是否可删除测试', '2025-12-26 14:15:57', 11, 4);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (20, '', '', '', '2025-12-26 00:20:07', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>审核页面类目显示测试</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Report\",\n  \"name\" : \"审核页面类目显示测试\",\n  \"author\" : \"审核页面类目显示测试\",\n  \"datePublished\" : \"2025-12-25T16:00:00.000Z\",\n  \"text\" : \"审核页面类目显示测试\",\n  \"about\" : \"审核页面类目显示测试\",\n  \"keywords\" : \"\",\n  \"articleBody\" : \"审核页面类目显示测试\",\n  \"headline\" : \"审核页面类目显示测试\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Report\">\n    <h1 itemprop=\"headline\">审核页面类目显示测试</h1>\n    <footer class=\"copyright-section\">\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"20\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.83, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Report\",\n  \"name\" : \"审核页面类目显示测试\",\n  \"author\" : \"审核页面类目显示测试\",\n  \"datePublished\" : \"2025-12-25T16:00:00.000Z\",\n  \"text\" : \"审核页面类目显示测试\",\n  \"about\" : \"审核页面类目显示测试\",\n  \"keywords\" : \"\",\n  \"articleBody\" : \"审核页面类目显示测试\",\n  \"headline\" : \"审核页面类目显示测试\",\n  \"isOriginalContent\" : true\n}', '# 审核页面类目显示测试\n\n---\n\n## 版权信息\n\n**原创：** 是\n\n', '2025-12-26 09:44:48', NULL, '2025-12-26 09:44:48', 1, 'PUBLISHED', '{\"name\": \"审核页面类目显示测试\", \"text\": \"审核页面类目显示测试\", \"about\": \"审核页面类目显示测试\", \"author\": \"审核页面类目显示测试\", \"keywords\": \"\", \"articleBody\": \"审核页面类目显示测试\", \"datePublished\": \"2025-12-25T16:00:00.000Z\"}', '2025-12-26 00:20:07', 3, '审核页面类目显示测试', '2025-12-26 14:15:46', 11, 5);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (21, '', '', '', '2025-12-26 00:32:11', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>技术文章模板标签审核员测试</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"技术文章模板标签审核员测试\",\n  \"author\" : \"技术文章模板标签审核员测试\",\n  \"datePublished\" : \"2025-12-25T16:00:00.000Z\",\n  \"articleBody\" : \"技术文章模板标签审核员测试\",\n  \"keywords\" : \"\",\n  \"image\" : \"\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Article\">\n    <h1 itemprop=\"headline\">技术文章模板标签审核员测试</h1>\n    <footer class=\"copyright-section\">\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"21\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.67, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Article\",\n  \"headline\" : \"技术文章模板标签审核员测试\",\n  \"author\" : \"技术文章模板标签审核员测试\",\n  \"datePublished\" : \"2025-12-25T16:00:00.000Z\",\n  \"articleBody\" : \"技术文章模板标签审核员测试\",\n  \"keywords\" : \"\",\n  \"image\" : \"\",\n  \"isOriginalContent\" : true\n}', '# 技术文章模板标签审核员测试\n\n---\n\n## 版权信息\n\n**原创：** 是\n\n', NULL, NULL, NULL, NULL, 'PENDING_REVIEW', '{\"image\": \"\", \"author\": \"技术文章模板标签审核员测试\", \"headline\": \"技术文章模板标签审核员测试\", \"keywords\": \"\", \"articleBody\": \"技术文章模板标签审核员测试\", \"datePublished\": \"2025-12-25T16:00:00.000Z\"}', '2025-12-26 00:32:11', 1, '技术文章模板标签审核员测试', '2025-12-26 00:32:11', 11, 0);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (22, '是的', '是的', '是的', '2025-12-26 00:33:44', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>是的</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Review\",\n  \"name\" : \"是\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"是的\"\n  },\n  \"datePublished\" : \"2025-12-25T16:00:00.000Z\",\n  \"reviewBody\" : \"是的\",\n  \"itemReviewed\" : \"是的\",\n  \"reviewRating\" : \"\",\n  \"articleBody\" : \"是的是\",\n  \"headline\" : \"是的\",\n  \"copyrightNotice\" : \"是的\",\n  \"sourceOrganization\" : \"是的\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Review\">\n    <h1 itemprop=\"headline\">是的</h1>\n    <footer class=\"copyright-section\">\n      <div itemprop=\"author\" itemscope itemtype=\"https://schema.org/Person\">\n        <span>作者：</span><span itemprop=\"name\">是的</span>\n      </div>\n      <div itemprop=\"copyrightNotice\">版权声明：是的</div>\n      <div>来源：<span itemprop=\"sourceOrganization\">是的</span></div>\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"null\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.83, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Review\",\n  \"name\" : \"是\",\n  \"author\" : {\n    \"@type\" : \"Person\",\n    \"name\" : \"是的\"\n  },\n  \"datePublished\" : \"2025-12-25T16:00:00.000Z\",\n  \"reviewBody\" : \"是的\",\n  \"itemReviewed\" : \"是的\",\n  \"reviewRating\" : \"\",\n  \"articleBody\" : \"是的是\",\n  \"headline\" : \"是的\",\n  \"copyrightNotice\" : \"是的\",\n  \"sourceOrganization\" : \"是的\",\n  \"isOriginalContent\" : true\n}', '# 是的\n\n---\n\n## 版权信息\n\n**作者：** 是的\n\n**来源：** 是的\n\n**版权声明：** 是的\n\n**原创：** 是\n\n', NULL, NULL, NULL, NULL, 'PENDING_REVIEW', '{\"name\": \"是\", \"author\": \"是的\", \"reviewBody\": \"是的\", \"articleBody\": \"是的是\", \"itemReviewed\": \"是的\", \"reviewRating\": \"\", \"datePublished\": \"2025-12-25T16:00:00.000Z\"}', '2025-12-26 00:33:47', 4, '是的', '2025-12-26 00:34:05', 11, 1);
INSERT INTO `contents` (`id`, `author_name`, `content_source`, `copyright_notice`, `created_at`, `html_output`, `integrity_score`, `is_original`, `json_ld`, `markdown_output`, `published_at`, `reject_reason`, `reviewed_at`, `reviewed_by`, `status`, `structured_data`, `submitted_at`, `template_id`, `title`, `updated_at`, `user_id`, `view_count`) VALUES (23, '', '', '', '2025-12-26 09:44:12', '<!DOCTYPE html>\n<html lang=\"zh-CN\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>测试首页展示8篇以上内容折叠</title>\n  <script type=\"application/ld+json\">\n{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Review\",\n  \"name\" : \"测试首页展示8篇以上内容折叠\",\n  \"author\" : \"测试首页展示8篇以上内容折叠\",\n  \"datePublished\" : \"2025-12-25T16:00:00.000Z\",\n  \"reviewBody\" : \"测试首页展示8篇以上内容折叠\",\n  \"itemReviewed\" : \"测试首页展示8篇以上内容折叠\",\n  \"reviewRating\" : \"\",\n  \"articleBody\" : \"测试首页展示8篇以上内容折叠\",\n  \"headline\" : \"测试首页展示8篇以上内容折叠\",\n  \"isOriginalContent\" : true\n}\n  </script>\n  <link rel=\"alternate\" type=\"application/ld+json\" href=\"/api/ai/data/null\">\n</head>\n<body>\n  <main itemscope itemtype=\"https://schema.org/Review\">\n    <h1 itemprop=\"headline\">测试首页展示8篇以上内容折叠</h1>\n    <footer class=\"copyright-section\">\n    </footer>\n  </main>\n\n  <!-- User interactions loaded via JavaScript -->\n  <div id=\"user-interactions\" data-content-id=\"23\"></div>\n\n  <script defer src=\"/static/js/interactions.js\"></script>\n</body>\n</html>', 0.83, b'1', '{\n  \"@context\" : \"https://schema.org\",\n  \"@type\" : \"Review\",\n  \"name\" : \"测试首页展示8篇以上内容折叠\",\n  \"author\" : \"测试首页展示8篇以上内容折叠\",\n  \"datePublished\" : \"2025-12-25T16:00:00.000Z\",\n  \"reviewBody\" : \"测试首页展示8篇以上内容折叠\",\n  \"itemReviewed\" : \"测试首页展示8篇以上内容折叠\",\n  \"reviewRating\" : \"\",\n  \"articleBody\" : \"测试首页展示8篇以上内容折叠\",\n  \"headline\" : \"测试首页展示8篇以上内容折叠\",\n  \"isOriginalContent\" : true\n}', '# 测试首页展示8篇以上内容折叠\n\n---\n\n## 版权信息\n\n**原创：** 是\n\n', '2025-12-26 09:44:40', NULL, '2025-12-26 09:44:40', 1, 'PUBLISHED', '{\"name\": \"测试首页展示8篇以上内容折叠\", \"author\": \"测试首页展示8篇以上内容折叠\", \"reviewBody\": \"测试首页展示8篇以上内容折叠\", \"articleBody\": \"测试首页展示8篇以上内容折叠\", \"itemReviewed\": \"测试首页展示8篇以上内容折叠\", \"reviewRating\": \"\", \"datePublished\": \"2025-12-25T16:00:00.000Z\"}', '2025-12-26 09:44:12', 4, '测试首页展示8篇以上内容折叠', '2025-12-26 14:15:52', 11, 8);
COMMIT;

-- ----------------------------
-- Table structure for data_sources
-- ----------------------------
DROP TABLE IF EXISTS `data_sources`;
CREATE TABLE `data_sources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cleaning_rules` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL,
  `description` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `error_count` int(11) DEFAULT NULL,
  `fetch_count` int(11) DEFAULT NULL,
  `fetch_interval` int(11) DEFAULT NULL,
  `last_error` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_fetch_time` datetime DEFAULT NULL,
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `next_fetch_time` datetime DEFAULT NULL,
  `selector_config` text COLLATE utf8mb4_unicode_ci,
  `source_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `success_count` int(11) DEFAULT NULL,
  `template_mapping` text COLLATE utf8mb4_unicode_ci,
  `updated_at` datetime DEFAULT NULL,
  `url` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of data_sources
-- ----------------------------
BEGIN;
INSERT INTO `data_sources` (`id`, `cleaning_rules`, `created_at`, `description`, `enabled`, `error_count`, `fetch_count`, `fetch_interval`, `last_error`, `last_fetch_time`, `name`, `next_fetch_time`, `selector_config`, `source_type`, `status`, `success_count`, `template_mapping`, `updated_at`, `url`) VALUES (1, NULL, '2025-12-24 14:21:52', '1', b'0', 0, 1, 24, NULL, '2025-12-24 14:22:08', '测试1', '2025-12-25 14:22:08', NULL, 'HTML', 'DISABLED', 0, NULL, '2025-12-24 14:33:09', 'https://baijiahao.baidu.com/s?id=1852309743097503385&wfr=spider&for=pc');
INSERT INTO `data_sources` (`id`, `cleaning_rules`, `created_at`, `description`, `enabled`, `error_count`, `fetch_count`, `fetch_interval`, `last_error`, `last_fetch_time`, `name`, `next_fetch_time`, `selector_config`, `source_type`, `status`, `success_count`, `template_mapping`, `updated_at`, `url`) VALUES (2, NULL, '2025-12-24 14:23:23', '', b'0', 0, 1, 1, NULL, '2025-12-24 14:23:31', '测试2', '2025-12-24 15:23:31', NULL, 'RSS', 'DISABLED', 0, NULL, '2025-12-24 14:33:10', 'https://news.163.com/');
COMMIT;

-- ----------------------------
-- Table structure for login_attempts
-- ----------------------------
DROP TABLE IF EXISTS `login_attempts`;
CREATE TABLE `login_attempts` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `successful` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=MyISAM AUTO_INCREMENT=82 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of login_attempts
-- ----------------------------
BEGIN;
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (1, '2025-12-23 04:25:18', 'admin@dataelf.com', '111.208.79.19', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (2, '2025-12-23 04:25:24', 'admin@dataelf.com', '111.208.80.65', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (3, '2025-12-23 04:26:26', 'admin@dataelf.com', '111.208.81.238', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (4, '2025-12-23 09:34:48', '1195964119@qq.com', '180.172.233.167', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (5, '2025-12-23 09:34:49', '1195964119@qq.com', '180.172.233.167', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (6, '2025-12-23 09:35:42', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (7, '2025-12-23 20:42:55', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (8, '2025-12-23 20:45:41', 'lixiaofei@weihezao.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (9, '2025-12-23 23:09:05', 'admin@dataelf.com', '223.167.145.118', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (10, '2025-12-24 10:05:59', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (11, '2025-12-24 10:07:48', '46813336@qq.com', '180.172.233.167', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (12, '2025-12-24 10:08:05', 'lixiaofei@weihezao.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (13, '2025-12-24 10:10:17', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (14, '2025-12-24 10:34:44', 'admin@dataelf.com', '49.77.143.209', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (15, '2025-12-24 11:18:36', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (16, '2025-12-24 11:24:30', 'lixiaofei@weihezao.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (17, '2025-12-24 11:38:39', 'admin@dataelf.com', '49.77.143.209', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (18, '2025-12-24 12:24:16', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (19, '2025-12-24 13:25:04', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (20, '2025-12-24 14:41:50', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (21, '2025-12-24 14:50:46', 'oyjf@weihezao.com', '180.172.233.167', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (22, '2025-12-24 14:52:31', 'oyjf@weihezao.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (23, '2025-12-24 15:31:08', '46813336@qq.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (24, '2025-12-24 15:31:08', '46813336@qq.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (25, '2025-12-24 16:17:42', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (26, '2025-12-24 18:35:01', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (27, '2025-12-24 20:59:07', 'admin@dataelf.com', '223.167.145.118', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (28, '2025-12-25 09:37:29', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (29, '2025-12-25 12:25:44', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (30, '2025-12-25 13:41:22', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (31, '2025-12-25 19:39:55', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (32, '2025-12-25 19:44:51', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (33, '2025-12-25 19:45:52', 'admin@dataelf.com', '111.208.78.122', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (34, '2025-12-25 19:46:04', 'admin@dataelf.com', '111.208.78.122', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (35, '2025-12-25 19:47:05', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (36, '2025-12-25 19:48:00', 'oyjf@weihezao.com', '180.172.233.167', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (37, '2025-12-25 20:57:38', 'oyjf@weihezao.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (38, '2025-12-25 20:58:37', 'oyjf@weihezao.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (39, '2025-12-25 21:06:27', 'admin@dataelf.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (40, '2025-12-25 21:25:14', 'yangfengzy@126.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (41, '2025-12-25 21:28:07', 'oyjf@weihezao.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (42, '2025-12-25 21:49:57', '46813336@qq.com', '58.247.236.45', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (43, '2025-12-25 21:50:00', '46813336@qq.com', '58.247.236.45', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (44, '2025-12-25 21:50:06', '46813336@qq.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (45, '2025-12-25 22:17:32', 'oyjf@weihezao.com', '58.247.236.45', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (46, '2025-12-25 22:18:01', '46813336@qq.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (47, '2025-12-25 22:18:40', 'yangfengzy@126.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (48, '2025-12-25 22:20:09', 'oyjf@weihezao.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (49, '2025-12-25 22:20:29', 'admin@dataelf.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (50, '2025-12-25 23:30:33', '1195964119@qq.com', '58.247.236.45', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (51, '2025-12-25 23:30:46', 'admin@dataelf.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (52, '2025-12-25 23:38:35', '46813336@qq.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (53, '2025-12-25 23:40:48', 'oyjf@weihezao.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (54, '2025-12-25 23:59:24', 'admin@dataelf.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (55, '2025-12-26 00:03:02', 'oyjf@weihezao.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (56, '2025-12-26 00:04:57', 'oyjf@weihezao.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (57, '2025-12-26 00:19:09', 'admin@dataelf.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (58, '2025-12-26 00:19:19', 'oyjf@weihezao.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (59, '2025-12-26 00:27:07', 'oyjf@weihezao.com', '111.208.78.122', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (60, '2025-12-26 00:27:26', 'oyjf@weihezao.com', '111.208.78.122', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (61, '2025-12-26 00:27:40', 'oyjf@weihezao.com', '111.208.78.122', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (62, '2025-12-26 00:34:57', 'admin@dataelf.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (63, '2025-12-26 00:35:37', '46813336@qq.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (64, '2025-12-26 00:38:25', 'admin@dataelf.com', '111.208.80.122', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (65, '2025-12-26 00:38:41', 'oyjf@weihezao.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (66, '2025-12-26 00:46:15', 'admin@dataelf.com', '111.208.80.122', b'0');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (67, '2025-12-26 00:46:21', 'admin@dataelf.com', '111.208.80.122', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (68, '2025-12-26 00:51:54', 'admin@dataelf.com', '111.208.80.122', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (69, '2025-12-26 01:35:50', 'admin@dataelf.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (70, '2025-12-26 01:36:31', 'oyjf@weihezao.com', '58.247.236.45', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (71, '2025-12-26 01:53:49', 'oyjf@weihezao.com', '111.208.80.122', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (72, '2025-12-26 09:42:22', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (73, '2025-12-26 09:42:59', 'oyjf@weihezao.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (74, '2025-12-26 10:17:36', 'lixiaofei@weihezao.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (75, '2025-12-26 10:26:59', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (76, '2025-12-26 11:06:51', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (77, '2025-12-26 11:10:08', 'admin@dataelf.com', '111.208.78.122', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (78, '2025-12-26 11:27:20', 'admin@dataelf.com', '111.208.80.122', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (79, '2025-12-26 11:29:49', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (80, '2025-12-26 13:41:54', 'admin@dataelf.com', '180.172.233.167', b'1');
INSERT INTO `login_attempts` (`id`, `created_at`, `email`, `ip_address`, `successful`) VALUES (81, '2025-12-28 10:55:12', 'admin@dataelf.com', '58.247.236.45', b'1');
COMMIT;

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `is_read` bit(1) NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `related_content_id` bigint(20) DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=MyISAM AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of notifications
-- ----------------------------
BEGIN;
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (1, '2025-12-23 09:44:08', b'0', '您的账号已通过审核，有效期为 1110 天。', NULL, '账号审核通过', 'ACCOUNT_APPROVED', 2);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (2, '2025-12-23 20:45:26', b'1', '您的账号已通过审核，有效期为 1130 天。', NULL, '账号审核通过', 'ACCOUNT_APPROVED', 3);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (3, '2025-12-24 11:42:06', b'1', '您的内容《我我》审核未通过，原因：修改', NULL, '内容审核未通过', 'CONTENT_REJECTED', 1);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (4, '2025-12-24 12:43:59', b'1', '您的内容《测试3发布》已通过审核，可以发布了', NULL, '内容审核通过', 'CONTENT_APPROVED', 1);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (5, '2025-12-24 12:44:13', b'1', '您的内容《测试4》已由管理员直接发布', NULL, '内容审核通过', 'CONTENT_APPROVED', 1);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (6, '2025-12-24 12:45:50', b'1', '您的内容《测试1》审核未通过，原因：测试哈', NULL, '内容审核未通过', 'CONTENT_REJECTED', 1);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (7, '2025-12-24 13:35:03', b'1', '您的内容《测试管理员审核端》已通过审核，可以发布了', NULL, '内容审核通过', 'CONTENT_APPROVED', 1);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (8, '2025-12-24 13:41:46', b'1', '您的内容《图片测试发布效果》已由管理员直接发布', NULL, '内容审核通过', 'CONTENT_APPROVED', 1);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (9, '2025-12-24 14:42:22', b'0', '您的账号使用时长已延长 1 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 3);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (10, '2025-12-24 14:42:57', b'0', '您的账号使用时长已延长 31 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 3);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (11, '2025-12-24 14:43:02', b'0', '您的账号使用时长已延长 30 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 3);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (12, '2025-12-24 14:43:10', b'0', '您的账号使用时长已延长 1 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 3);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (13, '2025-12-24 14:51:34', b'1', '您的账号已通过审核，有效期为 1 天。', NULL, '账号审核通过', 'ACCOUNT_APPROVED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (14, '2025-12-24 15:05:52', b'1', '您的内容《用户测试2-发布成功》已通过审核，可以发布了', NULL, '内容审核通过', 'CONTENT_APPROVED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (15, '2025-12-24 15:06:07', b'1', '您的内容《用户测试1》审核未通过，原因：修改', NULL, '内容审核未通过', 'CONTENT_REJECTED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (16, '2025-12-24 15:15:01', b'1', '您的账号使用时长已延长 130 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 5);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (17, '2025-12-24 15:34:03', b'1', '您的内容《测试新增管理员功能》已通过审核，可以发布了', NULL, '内容审核通过', 'CONTENT_APPROVED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (18, '2025-12-25 19:52:50', b'0', '您的账号使用时长已延长 30 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (19, '2025-12-25 19:53:15', b'0', '您的账号使用时长已延长 30 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (20, '2025-12-25 19:56:18', b'0', '您的账号使用时长已延长 30 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (21, '2025-12-25 19:56:28', b'1', '您的账号使用时长已延长 22 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 5);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (22, '2025-12-25 19:56:43', b'0', '您的账号使用时长已延长 28 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (23, '2025-12-25 19:56:50', b'0', '您的账号使用时长已延长 27 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (24, '2025-12-25 19:56:55', b'0', '您的账号使用时长已延长 1 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (25, '2025-12-25 19:57:01', b'0', '您的账号使用时长已延长 1 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (26, '2025-12-25 21:20:57', b'0', '您的内容《测试会员草稿箱功能》已通过审核，可以发布了', NULL, '内容审核通过', 'CONTENT_APPROVED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (27, '2025-12-25 21:21:18', b'0', '您的内容《新版测试-2》审核未通过，原因：不通过', NULL, '内容审核未通过', 'CONTENT_REJECTED', 4);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (28, '2025-12-25 21:30:48', b'1', '您的内容《测试4-退回从新编辑》已由管理员直接发布', NULL, '内容审核通过', 'CONTENT_APPROVED', 1);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (29, '2025-12-25 21:36:54', b'1', '您的内容《测试-2新版测试审核模块》审核未通过，原因：测试拒绝', NULL, '内容审核未通过', 'CONTENT_REJECTED', 7);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (30, '2025-12-25 21:38:24', b'1', '您的内容《测试发布成功的文章删除》已由管理员直接发布', NULL, '内容审核通过', 'CONTENT_APPROVED', 1);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (31, '2025-12-25 22:19:57', b'1', '您的账号已通过审核，有效期为 30 天。', NULL, '账号审核通过', 'ACCOUNT_APPROVED', 8);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (32, '2025-12-25 23:32:27', b'1', '您的账号使用时长已延长 30 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 8);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (33, '2025-12-25 23:39:58', b'1', '您的内容《测试文章视频是否能打开》已由管理员直接发布', NULL, '内容审核通过', 'CONTENT_APPROVED', 1);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (34, '2025-12-25 23:43:26', b'1', '您的内容《主管理员测试》审核未通过，原因：位置', NULL, '内容审核未通过', 'CONTENT_REJECTED', 5);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (35, '2025-12-25 23:56:00', b'0', '您的账号已通过审核，有效期为 30 天。', NULL, '账号审核通过', 'ACCOUNT_APPROVED', 10);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (36, '2025-12-25 23:58:21', b'1', '您的账号已通过审核，有效期为 30 天。', NULL, '账号审核通过', 'ACCOUNT_APPROVED', 11);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (37, '2025-12-26 00:04:48', b'0', '您的内容《测试内容审核页的模板类目》已通过审核，可以发布了', NULL, '内容审核通过', 'CONTENT_APPROVED', 8);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (38, '2025-12-26 00:06:35', b'1', '您的内容《自己写的自己是否可删除测试》已通过审核，可以发布了', NULL, '内容审核通过', 'CONTENT_APPROVED', 11);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (39, '2025-12-26 09:44:40', b'0', '您的内容《测试首页展示8篇以上内容折叠》已由管理员直接发布', NULL, '内容审核通过', 'CONTENT_APPROVED', 11);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (40, '2025-12-26 09:44:48', b'0', '您的内容《审核页面类目显示测试》已由管理员直接发布', NULL, '内容审核通过', 'CONTENT_APPROVED', 11);
INSERT INTO `notifications` (`id`, `created_at`, `is_read`, `message`, `related_content_id`, `title`, `type`, `user_id`) VALUES (41, '2025-12-26 10:48:48', b'0', '您的账号使用时长已延长 365 天。', NULL, '账号时长延长', 'ACCOUNT_EXTENDED', 12);
COMMIT;

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `config_value` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of system_config
-- ----------------------------
BEGIN;
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (1, 'site.name', '数流精灵', '网站名称', '2025-12-22 17:48:56');
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (2, 'site.description', '我们让AI可以高效抓取、理解和引用您在这里创作的结构化内容，为企业在AI智能化时代获得更多的认可和关注！', '网站描述', '2025-12-24 10:31:23');
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (3, 'site.hero.title', '去伪存真、建立AI秩序', '首页标题', '2025-12-22 17:48:56');
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (4, 'site.hero.subtitle', '专为AI优化的结构化数据平台', '首页副标题', '2025-12-22 17:48:56');
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (5, 'site.logo.url', '', '网站Logo URL', '2025-12-22 17:48:56');
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (6, 'site.hero.bgColor', 'rgb(251, 250, 250)', 'Hero背景颜色', '2025-12-23 09:50:08');
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (7, 'site.hero.textColor', '#fffefe', 'Hero文字颜色', '2025-12-24 10:43:08');
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (8, 'site.footer.bottomLinks', '[{\"name\":\"网站地图\",\"url\":\"#\"},{\"name\":\"AI数据接口\",\"url\":\"#\"},{\"name\":\"机器人协议\",\"url\":\"#\"}]', '页脚底部链接配置', '2025-12-24 14:10:29');
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (9, 'site.footer.links', '[{\"group\":\"产品功能\",\"links\":[{\"name\":\"结构化模板\",\"url\":\"#\"},{\"name\":\"内容编辑器\",\"url\":\"#\"},{\"name\":\"数据探索\",\"url\":\"#\"},{\"name\":\"API文档\",\"url\":\"#\"}]},{\"group\":\"资源中心\",\"links\":[{\"name\":\"帮助文档\",\"url\":\"#\"},{\"name\":\"教程指南\",\"url\":\"#\"},{\"name\":\"博客文章\",\"url\":\"#\"},{\"name\":\"常见问题\",\"url\":\"\"}]},{\"group\":\"关于我们\",\"links\":[{\"name\":\"公司介绍\",\"url\":\"#\"},{\"name\":\"联系我们\",\"url\":\"https://m.ebrun.com/604826.html\"},{\"name\":\"隐私政策\",\"url\":\"#\"},{\"name\":\"服务条款\",\"url\":\"#\"}]}]', '页脚链接配置', '2025-12-25 23:03:39');
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (10, 'site.footer.icp', '沪ICP备2025108954号-2', '备案号', '2025-12-25 19:51:46');
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (11, 'site.footer.copyright', '© 2025 数流精灵. 保留所有权利', '版权信息', '2025-12-25 23:02:43');
INSERT INTO `system_config` (`id`, `config_key`, `config_value`, `description`, `updated_at`) VALUES (12, 'mail.from.name', '数流精灵', '邮件发件人名称', '2025-12-25 23:54:32');
COMMIT;

-- ----------------------------
-- Table structure for tags
-- ----------------------------
DROP TABLE IF EXISTS `tags`;
CREATE TABLE `tags` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `usage_count` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_t48xdq560gs3gap9g7jg36kgc` (`name`),
  KEY `idx_name` (`name`),
  KEY `idx_usage_count` (`usage_count`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of tags
-- ----------------------------
BEGIN;
INSERT INTO `tags` (`id`, `created_at`, `name`, `usage_count`) VALUES (1, '2025-12-24 11:02:48', '科技类', 0);
INSERT INTO `tags` (`id`, `created_at`, `name`, `usage_count`) VALUES (2, '2025-12-24 11:03:07', '数据类', 0);
INSERT INTO `tags` (`id`, `created_at`, `name`, `usage_count`) VALUES (3, '2025-12-24 11:03:18', '报告类', 0);
INSERT INTO `tags` (`id`, `created_at`, `name`, `usage_count`) VALUES (4, '2025-12-24 11:03:26', '分析类', 0);
COMMIT;

-- ----------------------------
-- Table structure for templates
-- ----------------------------
DROP TABLE IF EXISTS `templates`;
CREATE TABLE `templates` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `created_by` bigint(20) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `is_system` bit(1) NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `schema_definition` json NOT NULL,
  `schema_org_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=MyISAM AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of templates
-- ----------------------------
BEGIN;
INSERT INTO `templates` (`id`, `created_at`, `created_by`, `description`, `is_system`, `name`, `schema_definition`, `schema_org_type`, `type`, `updated_at`) VALUES (1, '2025-12-22 17:48:53', NULL, '技术类文章模板', b'1', '技术文章', '{\"@type\": \"Article\", \"fields\": [{\"name\": \"headline\", \"type\": \"string\", \"label\": \"标题\", \"required\": true}, {\"name\": \"author\", \"type\": \"Person\", \"label\": \"作者\", \"required\": true}, {\"name\": \"datePublished\", \"type\": \"date\", \"label\": \"发布日期\", \"required\": true}, {\"name\": \"articleBody\", \"type\": \"text\", \"label\": \"正文\", \"required\": true}, {\"name\": \"keywords\", \"type\": \"array\", \"label\": \"关键词\", \"required\": false}, {\"name\": \"image\", \"type\": \"url\", \"label\": \"配图\", \"required\": false}], \"@context\": \"https://schema.org\"}', 'Article', 'TechArticle', '2025-12-22 17:48:53');
INSERT INTO `templates` (`id`, `created_at`, `created_by`, `description`, `is_system`, `name`, `schema_definition`, `schema_org_type`, `type`, `updated_at`) VALUES (2, '2025-12-22 17:48:53', NULL, '案例研究模板', b'1', '案例研究', '{\"@type\": \"Article\", \"fields\": [{\"name\": \"headline\", \"type\": \"string\", \"label\": \"案例标题\", \"required\": true}, {\"name\": \"author\", \"type\": \"Person\", \"label\": \"作者\", \"required\": true}, {\"name\": \"datePublished\", \"type\": \"date\", \"label\": \"发布日期\", \"required\": true}, {\"name\": \"articleBody\", \"type\": \"text\", \"label\": \"案例内容\", \"required\": true}, {\"name\": \"about\", \"type\": \"Thing\", \"label\": \"案例主题\", \"required\": true}, {\"name\": \"keywords\", \"type\": \"array\", \"label\": \"关键词\", \"required\": false}], \"@context\": \"https://schema.org\"}', 'Article', 'CaseStudy', '2025-12-22 17:48:53');
INSERT INTO `templates` (`id`, `created_at`, `created_by`, `description`, `is_system`, `name`, `schema_definition`, `schema_org_type`, `type`, `updated_at`) VALUES (3, '2025-12-22 17:48:53', NULL, '数据报告模板', b'1', '数据报告', '{\"@type\": \"Report\", \"fields\": [{\"name\": \"name\", \"type\": \"string\", \"label\": \"报告名称\", \"required\": true}, {\"name\": \"author\", \"type\": \"Person\", \"label\": \"作者\", \"required\": true}, {\"name\": \"datePublished\", \"type\": \"date\", \"label\": \"发布日期\", \"required\": true}, {\"name\": \"text\", \"type\": \"text\", \"label\": \"报告内容\", \"required\": true}, {\"name\": \"about\", \"type\": \"Thing\", \"label\": \"报告主题\", \"required\": true}, {\"name\": \"keywords\", \"type\": \"array\", \"label\": \"关键词\", \"required\": false}], \"@context\": \"https://schema.org\"}', 'Report', 'DataReport', '2025-12-22 17:48:53');
INSERT INTO `templates` (`id`, `created_at`, `created_by`, `description`, `is_system`, `name`, `schema_definition`, `schema_org_type`, `type`, `updated_at`) VALUES (4, '2025-12-22 17:48:53', NULL, '产品评测模板', b'1', '产品评测', '{\"@type\": \"Review\", \"fields\": [{\"name\": \"name\", \"type\": \"string\", \"label\": \"评测标题\", \"required\": true}, {\"name\": \"author\", \"type\": \"Person\", \"label\": \"作者\", \"required\": true}, {\"name\": \"datePublished\", \"type\": \"date\", \"label\": \"发布日期\", \"required\": true}, {\"name\": \"reviewBody\", \"type\": \"text\", \"label\": \"评测内容\", \"required\": true}, {\"name\": \"itemReviewed\", \"type\": \"Product\", \"label\": \"评测产品\", \"required\": true}, {\"name\": \"reviewRating\", \"type\": \"Rating\", \"label\": \"评分\", \"required\": false}], \"@context\": \"https://schema.org\"}', 'Review', 'ProductReview', '2025-12-22 17:48:53');
INSERT INTO `templates` (`id`, `created_at`, `created_by`, `description`, `is_system`, `name`, `schema_definition`, `schema_org_type`, `type`, `updated_at`) VALUES (5, '2025-12-22 17:48:53', NULL, '调查报告模板', b'1', '调查报告', '{\"@type\": \"Report\", \"fields\": [{\"name\": \"name\", \"type\": \"string\", \"label\": \"调查报告名称\", \"required\": true}, {\"name\": \"author\", \"type\": \"Person\", \"label\": \"作者\", \"required\": true}, {\"name\": \"datePublished\", \"type\": \"date\", \"label\": \"发布日期\", \"required\": true}, {\"name\": \"text\", \"type\": \"text\", \"label\": \"调查内容\", \"required\": true}, {\"name\": \"about\", \"type\": \"Thing\", \"label\": \"调查主题\", \"required\": true}, {\"name\": \"keywords\", \"type\": \"array\", \"label\": \"关键词\", \"required\": false}], \"@context\": \"https://schema.org\"}', 'Report', 'SurveyReport', '2025-12-22 17:48:53');
INSERT INTO `templates` (`id`, `created_at`, `created_by`, `description`, `is_system`, `name`, `schema_definition`, `schema_org_type`, `type`, `updated_at`) VALUES (6, '2025-12-22 17:48:53', NULL, '行业分析模板', b'1', '行业分析', '{\"@type\": \"AnalysisNewsArticle\", \"fields\": [{\"name\": \"headline\", \"type\": \"string\", \"label\": \"分析标题\", \"required\": true}, {\"name\": \"author\", \"type\": \"Person\", \"label\": \"作者\", \"required\": true}, {\"name\": \"datePublished\", \"type\": \"date\", \"label\": \"发布日期\", \"required\": true}, {\"name\": \"articleBody\", \"type\": \"text\", \"label\": \"分析内容\", \"required\": true}, {\"name\": \"about\", \"type\": \"Thing\", \"label\": \"分析主题\", \"required\": true}, {\"name\": \"keywords\", \"type\": \"array\", \"label\": \"关键词\", \"required\": false}], \"@context\": \"https://schema.org\"}', 'AnalysisNewsArticle', 'IndustryAnalysis', '2025-12-22 17:48:53');
COMMIT;

-- ----------------------------
-- Table structure for user_interactions
-- ----------------------------
DROP TABLE IF EXISTS `user_interactions`;
CREATE TABLE `user_interactions` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `content_id` bigint(20) NOT NULL,
  `created_at` datetime NOT NULL,
  `interaction_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_interaction` (`user_id`,`content_id`,`interaction_type`),
  KEY `idx_content_id` (`content_id`),
  KEY `idx_interaction_type` (`interaction_type`)
) ENGINE=MyISAM AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of user_interactions
-- ----------------------------
BEGIN;
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (1, 1, '2025-12-24 12:14:41', 'LIKE', 1);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (2, 1, '2025-12-24 12:14:43', 'FAVORITE', 1);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (20, 5, '2025-12-25 21:31:09', 'LIKE', 1);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (21, 15, '2025-12-25 21:40:09', 'FAVORITE', 1);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (26, 7, '2025-12-26 11:26:15', 'FAVORITE', 1);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (13, 9, '2025-12-24 15:34:55', 'FAVORITE', 4);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (10, 9, '2025-12-24 14:57:17', 'LIKE', 4);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (11, 9, '2025-12-24 15:09:28', 'LIKE', 1);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (12, 9, '2025-12-24 15:09:30', 'FAVORITE', 1);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (14, 10, '2025-12-25 19:45:32', 'LIKE', 1);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (15, 10, '2025-12-25 19:45:32', 'FAVORITE', 1);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (16, 7, '2025-12-25 21:09:11', 'LIKE', 4);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (17, 7, '2025-12-25 21:09:12', 'FAVORITE', 4);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (18, 12, '2025-12-25 21:19:46', 'FAVORITE', 4);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (19, 12, '2025-12-25 21:19:47', 'LIKE', 4);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (22, 15, '2025-12-25 21:40:10', 'LIKE', 1);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (23, 19, '2025-12-26 00:06:59', 'LIKE', 11);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (24, 19, '2025-12-26 00:07:00', 'FAVORITE', 11);
INSERT INTO `user_interactions` (`id`, `content_id`, `created_at`, `interaction_type`, `user_id`) VALUES (25, 7, '2025-12-26 11:26:14', 'LIKE', 1);
COMMIT;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `approved_at` datetime DEFAULT NULL,
  `approved_by` bigint(20) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `expires_at` datetime DEFAULT NULL,
  `password_hash` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` enum('PENDING','APPROVED','REJECTED','EXPIRED') COLLATE utf8mb4_unicode_ci NOT NULL,
  `admin_permissions` json DEFAULT NULL,
  `admin_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=MyISAM AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Records of users
-- ----------------------------
BEGIN;
INSERT INTO `users` (`id`, `approved_at`, `approved_by`, `created_at`, `email`, `expires_at`, `password_hash`, `nickname`, `phone`, `role`, `status`, `admin_permissions`, `admin_type`) VALUES (1, '2025-12-22 17:48:56', NULL, '2025-12-22 17:48:56', 'admin@dataelf.com', '2037-12-31 23:59:59', '$2a$10$f7APg4f2vci1jcBHVzq4c.unEOXqbhFQvhTH8yZZUmo4.PygCZZnu', NULL, '13800000000', 'ADMIN', 'APPROVED', NULL, 'MAIN_ADMIN');
INSERT INTO `users` (`id`, `approved_at`, `approved_by`, `created_at`, `email`, `expires_at`, `password_hash`, `nickname`, `phone`, `role`, `status`, `admin_permissions`, `admin_type`) VALUES (11, '2025-12-25 23:58:21', 1, '2025-12-25 23:58:09', 'oyjf@weihezao.com', '2026-01-24 23:58:21', '$2a$12$FkfoEeScDCt9qqjvyqgNkeJrLjtzoFec15ha3TUa1jtTuJoBwkMzC', NULL, '15601873151', 'USER', 'APPROVED', NULL, NULL);
INSERT INTO `users` (`id`, `approved_at`, `approved_by`, `created_at`, `email`, `expires_at`, `password_hash`, `nickname`, `phone`, `role`, `status`, `admin_permissions`, `admin_type`) VALUES (9, '2025-12-25 23:31:26', 1, '2025-12-25 23:31:26', '1195964119@qq.com', '2026-01-25 23:31:26', '$2a$12$kPuu5/KZ8Oiye13Kdy0.sOTeC1qrI1uBha7FMQrFH0vH5OI8Sdc8y', NULL, '18301932103', 'ADMIN', 'APPROVED', '[\"user_approve\", \"content_review\", \"content_view_own\", \"content_unpublish_own\", \"template_manage\"]', 'NORMAL_ADMIN');
INSERT INTO `users` (`id`, `approved_at`, `approved_by`, `created_at`, `email`, `expires_at`, `password_hash`, `nickname`, `phone`, `role`, `status`, `admin_permissions`, `admin_type`) VALUES (5, '2025-12-24 15:14:38', 1, '2025-12-24 15:14:38', '46813336@qq.com', '2026-06-25 15:14:38', '$2a$12$Ndf7o1OqafEhgxzomIUjAOZm2z36GmL8XzjqlnEOYF8T5q/vZKJVG', NULL, '17031136161', 'ADMIN', 'APPROVED', NULL, 'MAIN_ADMIN');
INSERT INTO `users` (`id`, `approved_at`, `approved_by`, `created_at`, `email`, `expires_at`, `password_hash`, `nickname`, `phone`, `role`, `status`, `admin_permissions`, `admin_type`) VALUES (10, '2025-12-25 23:55:59', 1, '2025-12-25 23:55:43', 'limengfei@weihezao.com', '2026-01-24 23:55:59', '$2a$12$eNs.yDVZTvlDYULxg4TssOJtNUA2aWZTc32ELybKiUJxXqs5eqzvq', NULL, '15800417830', 'USER', 'APPROVED', NULL, NULL);
INSERT INTO `users` (`id`, `approved_at`, `approved_by`, `created_at`, `email`, `expires_at`, `password_hash`, `nickname`, `phone`, `role`, `status`, `admin_permissions`, `admin_type`) VALUES (12, '2025-12-26 10:16:35', 1, '2025-12-26 10:16:35', 'lixiaofei@weihezao.com', '2027-01-26 10:16:35', '$2a$12$eSAU8luk4pQHlyrWWPK0BucdkvZTqYZuqo6/xkkczN3GtWrFph1EC', NULL, '13651688127', 'ADMIN', 'APPROVED', '[\"user_approve\", \"content_review\", \"content_view_own\", \"content_unpublish_own\", \"user_delete\", \"content_delete\"]', 'NORMAL_ADMIN');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
