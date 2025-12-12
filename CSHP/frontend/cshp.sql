CREATE DATABASE IF NOT EXISTS `cshp` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `cshp`;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `student_id` VARCHAR(32) NOT NULL UNIQUE,
  `password` VARCHAR(128) NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `phone` VARCHAR(32),
  `email` VARCHAR(64),
  `avatar` VARCHAR(255),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(128) NOT NULL,
  `description` TEXT,
  `price` DECIMAL(10,2) NOT NULL,
  `category` VARCHAR(32),
  `images` TEXT,
  `seller_id` VARCHAR(32) NOT NULL,
  `seller_name` VARCHAR(64),
  `status` TINYINT DEFAULT 0,
  `shipped` TINYINT DEFAULT 0,
  `view_count` INT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(64) NOT NULL,
  `product_id` BIGINT NOT NULL,
  `product_title` VARCHAR(128),
  `product_image` VARCHAR(255),
  `price` DECIMAL(10,2),
  `buyer_id` VARCHAR(32) NOT NULL,
  `buyer_name` VARCHAR(64),
  `seller_id` VARCHAR(32),
  `seller_name` VARCHAR(64),
  `status` TINYINT DEFAULT 0,
  `address` VARCHAR(255),
  `phone` VARCHAR(32),
  `remark` VARCHAR(255),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `pay_time` DATETIME,
  `ship_time` DATETIME,
  `complete_time` DATETIME,
  PRIMARY KEY (`id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` VARCHAR(32) NOT NULL,
  `product_id` BIGINT NOT NULL,
  `product_title` VARCHAR(128),
  `product_image` VARCHAR(255),
  `price` DECIMAL(10,2),
  `seller_name` VARCHAR(64),
  `quantity` INT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `session_id` VARCHAR(64) NOT NULL,
  `sender_id` VARCHAR(32) NOT NULL,
  `sender_name` VARCHAR(64),
  `receiver_id` VARCHAR(32) NOT NULL,
  `receiver_name` VARCHAR(64),
  `content` TEXT,
  `type` TINYINT DEFAULT 0,
  `read_status` TINYINT DEFAULT 0,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (`student_id`, `password`, `name`, `phone`, `email`)
VALUES ('20240001', '$2a$10$X/v0pQJrR1q9G0GgJfLpuO7oQFhkuGZ5VY.ksMvyZlYcivt6D7B6i', '张三', '13800000000', 'zhangsan@example.com');

