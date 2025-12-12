/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80044 (8.0.44)
 Source Host           : localhost:3306
 Source Schema         : cshp

 Target Server Type    : MySQL
 Target Server Version : 80044 (8.0.44)
 File Encoding         : 65001

 Date: 09/12/2025 21:11:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_id` bigint NOT NULL,
  `product_title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `product_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NULL DEFAULT NULL,
  `quantity` int NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cart
-- ----------------------------

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sender_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `sender_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `receiver_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `receiver_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `type` tinyint NULL DEFAULT 0,
  `read_status` tinyint NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `product_id` bigint NULL DEFAULT NULL COMMENT '商品ID',
  `product_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品标题',
  `product_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session_id`(`session_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of chat_message
-- ----------------------------
INSERT INTO `chat_message` VALUES (3, '10000001_10000002', '10000002', '小何', '10000001', '小王', '能不能便宜一些', 0, 1, '2025-12-08 20:04:10', 8, '键盘', '/uploads/2025/12/08/6d565261-e301-4ac1-9f90-3f637e0354e4.jpg');
INSERT INTO `chat_message` VALUES (4, '10000001_10000002', '10000001', '小王', '10000002', '小何', '不能便宜了，不好意思', 0, 1, '2025-12-08 20:04:32', 8, '键盘', '/uploads/2025/12/08/6d565261-e301-4ac1-9f90-3f637e0354e4.jpg');
INSERT INTO `chat_message` VALUES (5, '10000001_10000002', '10000002', '小何', '10000001', '小王', '好吧', 0, 1, '2025-12-08 20:04:44', 8, '键盘', '/uploads/2025/12/08/6d565261-e301-4ac1-9f90-3f637e0354e4.jpg');

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `price` decimal(10, 2) NOT NULL,
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `seller_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `seller_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 0,
  `shipped` tinyint NULL DEFAULT 0,
  `view_count` int NULL DEFAULT 0,
  `condition` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '良好' COMMENT '商品成色：全新、几乎全新、良好、一般、较差',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES (7, '华为purax 12+512', '买了几天，挺新的', 6999.00, '电子产品', '2025/12/08/b7686407-6727-4460-afec-637076440dcf.jpg,2025/12/08/605b09b0-1326-49e5-84d6-58c234084172.jpg,2025/12/08/d2be61d6-4b11-4c86-892d-758384c6f5f2.jpg', '10000001', '小王', 0, 0, 2, '几乎全新', '2025-12-08 19:59:14', '2025-12-08 19:59:14');
INSERT INTO `product` VALUES (8, '键盘', '还不错', 99.00, '电子产品', '2025/12/08/6d565261-e301-4ac1-9f90-3f637e0354e4.jpg,2025/12/08/2746fc05-d76e-4e40-851c-2d2e4d95ae13.jpg', '10000001', '小王', 1, 1, 1, '良好', '2025-12-08 19:59:45', '2025-12-08 20:06:44');
INSERT INTO `product` VALUES (9, '修身牛仔裤', '太大了，穿不下，原价99买的，70出了', 70.00, '服装配饰', '2025/12/08/a6acec4d-cdf4-473e-ae3b-768b0e529c5d.jpg,2025/12/08/99f95dcb-b55c-4d37-8b4d-e423c28eed94.jpg', '10000001', '小王', 0, 0, 1, '几乎全新', '2025-12-08 20:00:38', '2025-12-08 20:00:38');
INSERT INTO `product` VALUES (10, '高等数学', '闲置出了', 9.99, '图书教材', '2025/12/08/491a102a-5f4d-4804-96cf-3b5052aa08ed.jpg', '10000002', '小何', 0, 0, 2, '良好', '2025-12-08 20:01:51', '2025-12-08 20:01:51');
INSERT INTO `product` VALUES (11, '纯白短袖xl码', '买小了，出了，很新', 69.00, '服装配饰', '2025/12/08/cde2851e-2485-4d20-b565-bd7012a81045.jpg', '10000002', '小何', 0, 0, 0, '良好', '2025-12-08 20:02:30', '2025-12-08 20:02:30');
INSERT INTO `product` VALUES (12, '时尚雨伞', '换新的，所以出了', 12.90, '生活用品', '2025/12/08/724b3fc0-addf-462c-ab00-74d37c08a14b.jpg', '10000002', '小何', 0, 0, 0, '良好', '2025-12-08 20:03:02', '2025-12-08 20:03:02');
INSERT INTO `product` VALUES (13, '品牌充电宝', '朋友送的，全新', 99.00, '电子产品', '2025/12/08/b5bb590c-761f-4001-99de-3d22847c99a4.jpg,2025/12/08/60c186ae-6d68-45f9-aa3a-0497015f9735.jpg', '10000002', '小何', 0, 0, 0, '全新', '2025-12-08 20:03:38', '2025-12-08 20:03:38');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `avatar` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (3, '10000001', '$2a$10$95Zlth3uVaCcUTCLeijSce2gVso0hu7RbCkd96BNooZiD2SnAmQsC', '小王', '', '', NULL, '2025-12-08 19:49:50', '2025-12-08 19:49:50');
INSERT INTO `user` VALUES (4, '10000002', '$2a$10$DUNDOJ14yeOKdDGouZBqT.RGSZgoEybzLZ.mVM5jOsKgCPI3nUrBu', '小何', '', '', NULL, '2025-12-08 20:01:06', '2025-12-08 20:01:06');
INSERT INTO `user` VALUES (5, '100000003', '$2a$10$YTYvhl6gP8jhGyXR0jGTiei3C3tMmbhayv8TUs0rs.aR.73Awf9tq', '小陈', '', '', NULL, '2025-12-08 21:21:28', '2025-12-08 21:21:28');

-- ----------------------------
-- Table structure for user_order
-- ----------------------------
DROP TABLE IF EXISTS `user_order`;
CREATE TABLE `user_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `product_id` bigint NOT NULL,
  `product_title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `product_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `price` decimal(10, 2) NULL DEFAULT NULL,
  `buyer_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `buyer_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `seller_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `seller_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NULL DEFAULT 0,
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `pay_time` datetime NULL DEFAULT NULL,
  `ship_time` datetime NULL DEFAULT NULL,
  `complete_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_buyer_id`(`buyer_id` ASC) USING BTREE,
  INDEX `idx_seller_id`(`seller_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user_order
-- ----------------------------
INSERT INTO `user_order` VALUES (2, 'ORD202512082005567406', 8, '键盘', '2025/12/08/6d565261-e301-4ac1-9f90-3f637e0354e4.jpg', 99.00, '10000002', '小何', '10000001', NULL, 3, '广东省中山市', '13800000002', '无', '2025-12-08 20:05:56', '2025-12-08 20:06:49', '2025-12-08 20:06:39', '2025-12-08 20:06:44', '2025-12-08 20:06:49');
INSERT INTO `user_order` VALUES (3, 'ORD202512082007207142', 7, '华为purax 12+512', '2025/12/08/b7686407-6727-4460-afec-637076440dcf.jpg', 6999.00, '10000002', '10000002', '10000001', NULL, 0, '请填写收货地址', '请填写联系方式', '', '2025-12-08 20:07:21', '2025-12-08 20:07:21', NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
