-- 更新chat_message表，添加商品相关字段
ALTER TABLE `chat_message` 
ADD COLUMN `product_id` BIGINT DEFAULT NULL COMMENT '关联的商品ID',
ADD COLUMN `product_title` VARCHAR(128) DEFAULT NULL COMMENT '商品标题',
ADD COLUMN `product_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片';

