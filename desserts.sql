/*
 Navicat Premium Dump SQL

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : desserts

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 26/05/2026 11:58:46
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `dessert_id` int NOT NULL,
  `quantity` int NULL DEFAULT 1,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_dessert`(`user_id` ASC, `dessert_id` ASC) USING BTREE,
  INDEX `dessert_id`(`dessert_id` ASC) USING BTREE,
  CONSTRAINT `cart_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `cart_ibfk_2` FOREIGN KEY (`dessert_id`) REFERENCES `dessert` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of cart
-- ----------------------------
INSERT INTO `cart` VALUES (13, 2, 1, 1, '2026-05-26 11:35:51', '2026-05-26 11:35:51');
INSERT INTO `cart` VALUES (14, 2, 4, 1, '2026-05-26 11:35:53', '2026-05-26 11:35:53');
INSERT INTO `cart` VALUES (15, 2, 5, 1, '2026-05-26 11:35:54', '2026-05-26 11:35:54');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `descp` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类描述',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '传统甜品', '精致到不忍下嘴的中国传统甜品，个个都经典', '2026-05-18 10:56:05');
INSERT INTO `category` VALUES (2, '凉粉系列', '创意凉粉系列，将凉粉这一种民间小吃融入菜肴', '2026-05-18 10:56:05');
INSERT INTO `category` VALUES (3, '雪山系列', '造型高颜值，口感绵软，越嚼越有嚼劲', '2026-05-18 10:56:05');
INSERT INTO `category` VALUES (4, '蛋糕系列', '松软香甜，每一口都是幸福的味道', '2026-05-18 10:56:05');
INSERT INTO `category` VALUES (5, '冰淇淋', '清凉解暑，夏日必备', '2026-05-18 10:56:05');
INSERT INTO `category` VALUES (6, '布丁系列', '口感细腻，入口即化', '2026-05-18 10:56:05');
INSERT INTO `category` VALUES (7, '饮品系列', '搭配甜点，完美享受', '2026-05-18 10:56:05');
INSERT INTO `category` VALUES (8, '烘焙系列', '新鲜出炉，香气四溢', '2026-05-18 10:56:05');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `dessert_id` int NOT NULL,
  `user_id` int NOT NULL,
  `order_id` int NULL DEFAULT NULL COMMENT '订单ID',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评价内容',
  `rating` int NULL DEFAULT 5 COMMENT '评分 1-5',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 1-显示 0-隐藏',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `dessert_id`(`dessert_id` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `comment_ibfk_1` FOREIGN KEY (`dessert_id`) REFERENCES `dessert` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `comment_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, 5, 2, 8, '味道好，分量足，会回购。味道好，分量足。味道好', 5, '2026-05-24 18:46:22', 1);
INSERT INTO `comment` VALUES (2, 5, 2, 7, '味道好，会回购，包装精美。味道好，会回购。味道好', 5, '2026-05-24 20:08:03', 1);
INSERT INTO `comment` VALUES (3, 15, 2, 9, '味道好', 3, '2026-05-24 20:32:57', 1);

-- ----------------------------
-- Table structure for dessert
-- ----------------------------
DROP TABLE IF EXISTS `dessert`;
CREATE TABLE `dessert`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '甜点ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '甜点名称',
  `descp` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '甜点描述',
  `photoUrl` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片URL',
  `price` decimal(10, 2) NOT NULL COMMENT '单价',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `discount` decimal(3, 2) NULL DEFAULT 1.00 COMMENT '折扣率',
  `release_date` date NULL DEFAULT NULL COMMENT '发布日期',
  `cat_id` bigint NULL DEFAULT NULL COMMENT '所属分类ID',
  `stock` int NULL DEFAULT 0 COMMENT '库存数量',
  `min_stock` int NULL DEFAULT 5 COMMENT '最低库存预警',
  `sales` int NULL DEFAULT 0 COMMENT '累计销量',
  `rating` decimal(2, 1) NULL DEFAULT 5.0 COMMENT '评分',
  `review_count` int NULL DEFAULT 0 COMMENT '评价数量',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0下架 1上架 2缺货',
  `is_hot` tinyint NULL DEFAULT 0 COMMENT '是否热门',
  `is_recommend` tinyint NULL DEFAULT 0 COMMENT '是否推荐',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `cat_id`(`cat_id` ASC) USING BTREE,
  CONSTRAINT `dessert_ibfk_1` FOREIGN KEY (`cat_id`) REFERENCES `category` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dessert
-- ----------------------------
INSERT INTO `dessert` VALUES (1, '生磨黑芝麻加糯米', '满满一碗芝麻糊中央放了一个圆圆的黑糯米球，入口醇香柔滑', '/photo/001.jpg', 19.99, 20.00, 1.00, '2024-02-01', 1, 99, 5, 257, 5.0, 0, 1, 0, 1, '2026-05-18 10:56:23', '2026-05-26 11:35:15');
INSERT INTO `dessert` VALUES (2, '鲜杂果凉粉(椰汁芒汁)', '新鲜水果料足量多，如艺术品般令人赞叹的造型', '/photo/002.jpg', 20.00, 28.00, 1.00, '2024-02-03', 2, 80, 5, 189, 5.0, 0, 1, 0, 1, '2026-05-18 10:56:23', '2026-05-18 10:56:23');
INSERT INTO `dessert` VALUES (3, '雪山芒果捞', '绵密雪山下藏着新鲜芒果，口感层次丰富', '/photo/003.jpg', 28.00, 38.00, 1.00, '2024-02-05', 3, 58, 5, 147, 5.0, 0, 1, 0, 1, '2026-05-18 10:56:23', '2026-05-26 11:35:15');
INSERT INTO `dessert` VALUES (4, '提拉米苏', '意大利经典甜点，咖啡与奶酪的完美结合', '/photo/004.jpg', 32.00, 45.00, 1.00, '2024-02-10', 4, 48, 5, 322, 5.0, 0, 1, 0, 1, '2026-05-18 10:56:23', '2026-05-26 11:35:15');
INSERT INTO `dessert` VALUES (5, '抹茶冰淇淋', '日式抹茶风味，清新不腻', '/photo/005.jpg', 18.00, 25.00, 1.00, '2024-02-12', 5, 196, 5, 416, 5.0, 2, 1, 0, 1, '2026-05-18 10:56:23', '2026-05-26 11:35:15');
INSERT INTO `dessert` VALUES (6, '焦糖布丁', '焦糖香甜，布丁嫩滑', '/photo/006.jpg', 12.00, 18.00, 1.00, '2024-02-15', 6, 150, 5, 289, 5.0, 0, 1, 0, 0, '2026-05-18 10:56:23', '2026-05-18 10:56:23');
INSERT INTO `dessert` VALUES (7, '杨枝甘露', '芒果西柚的完美搭配', '/photo/007.jpg', 22.00, 30.00, 1.00, '2024-02-18', 7, 90, 5, 178, 5.0, 0, 1, 0, 0, '2026-05-18 10:56:23', '2026-05-18 10:56:23');
INSERT INTO `dessert` VALUES (8, '榴莲班戟', '榴莲爱好者必点', '/photo/008.jpg', 25.00, 35.00, 1.00, '2024-02-20', 4, 40, 5, 98, 5.0, 0, 1, 0, 0, '2026-05-18 10:56:23', '2026-05-18 10:56:23');
INSERT INTO `dessert` VALUES (9, '双皮奶', '顺德传统名点，奶香浓郁', '/photo/009.jpg', 10.00, 15.00, 1.00, '2024-02-22', 1, 119, 5, 235, 5.0, 0, 1, 0, 1, '2026-05-18 10:56:23', '2026-05-26 11:35:15');
INSERT INTO `dessert` VALUES (10, '芒果布丁', '新鲜芒果制作', '/photo/010.jpg', 15.00, 22.00, 1.00, '2024-02-25', 6, 100, 5, 167, 5.0, 0, 1, 0, 0, '2026-05-18 10:56:23', '2026-05-18 10:56:23');
INSERT INTO `dessert` VALUES (11, '红豆沙汤圆', '暖心甜品，冬日必备', '/photo/011.jpg', 18.00, 25.00, 1.00, '2024-03-01', 1, 80, 5, 145, 5.0, 0, 1, 0, 0, '2026-05-18 10:56:23', '2026-05-18 10:56:23');
INSERT INTO `dessert` VALUES (12, '芋圆仙草', 'Q弹芋圆配仙草', '/photo/012.jpg', 20.00, 28.00, 1.00, '2024-03-05', 2, 70, 5, 123, 5.0, 0, 1, 0, 0, '2026-05-18 10:56:23', '2026-05-18 10:56:23');
INSERT INTO `dessert` VALUES (13, '巧克力熔岩蛋糕', '切开流心，浓郁巧克力', '/photo/013.jpg', 35.00, 48.00, 1.00, '2024-03-10', 4, 29, 5, 90, 5.0, 0, 1, 0, 1, '2026-05-18 10:56:23', '2026-05-23 20:49:42');
INSERT INTO `dessert` VALUES (14, '草莓慕斯', '少女心爆棚的甜品', '/photo/014.jpg', 28.00, 38.00, 1.00, '2024-03-15', 4, 45, 5, 156, 5.0, 0, 1, 0, 0, '2026-05-18 10:56:23', '2026-05-18 10:56:23');
INSERT INTO `dessert` VALUES (15, '香草冰淇淋', '经典香草口味', '/photo/015.jpg', 15.00, 20.00, 1.00, '2024-03-20', 5, 200, 5, 279, 3.0, 1, 1, 0, 0, '2026-05-18 10:56:23', '2026-05-24 20:53:02');
INSERT INTO `dessert` VALUES (18, 'AA', 'AA', '/photo/74d8e1dcfea94a10a41fa3c6a2156f22.png', 8.00, 8.00, 1.00, '2026-05-26', 1, 999, 5, 0, 5.0, 0, 1, 0, 0, '2026-05-26 11:26:07', '2026-05-26 11:26:07');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号',
  `user_id` int NOT NULL,
  `total_amount` decimal(10, 2) NOT NULL COMMENT '总金额',
  `discount_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '优惠金额',
  `actual_amount` decimal(10, 2) NOT NULL COMMENT '实付金额',
  `status` tinyint NULL DEFAULT 1 COMMENT '1待付款 2已付款 3配送中 4已完成 5已取消',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货地址',
  `receiver_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货人',
  `receiver_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货电话',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '配送时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `reviewed` tinyint(1) NULL DEFAULT 0 COMMENT '评价状态',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `order_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (1, 'ORD202402010001', 2, 47.00, 0.00, 47.00, 4, '北京市朝阳区xxx路1号', NULL, '13800000002', NULL, '2024-02-01 10:30:00', NULL, NULL, '2024-02-01 10:25:00', 0);
INSERT INTO `order` VALUES (2, 'ORD202402030002', 2, 32.00, 0.00, 32.00, 4, '北京市朝阳区xxx路1号', NULL, '13800000002', NULL, '2024-02-03 14:20:00', NULL, NULL, '2024-02-03 14:15:00', 0);
INSERT INTO `order` VALUES (3, 'ORD202402050003', 3, 50.00, 0.00, 50.00, 4, '上海市浦东新区xxx路2号', NULL, '13800000003', NULL, '2024-02-05 18:30:00', NULL, NULL, '2024-02-05 18:20:00', 0);
INSERT INTO `order` VALUES (4, 'ORD202402100004', 3, 32.00, 0.00, 32.00, 1, '上海市浦东新区xxx路2号', NULL, '13800000003', NULL, '2024-02-10 12:00:00', NULL, NULL, '2024-02-10 11:55:00', 0);
INSERT INTO `order` VALUES (5, 'ORD202402150005', 4, 28.00, 0.00, 28.00, 5, '广州市天河区xxx路3号', NULL, '13800000004', NULL, NULL, NULL, NULL, '2024-02-15 09:30:00', 0);
INSERT INTO `order` VALUES (6, '20260523204942F3AD', 4, 53.00, NULL, 53.00, 2, 'ABNIOSN', 'ddd', '15811798753', '', '2026-05-23 20:49:54', NULL, NULL, '2026-05-23 20:49:42', 0);
INSERT INTO `order` VALUES (7, '2026052321262732D4', 2, 18.00, NULL, 18.00, 4, 'AAA', 'vkw;m', '15979995976', '', '2026-05-23 21:26:30', NULL, '2026-05-23 21:26:30', '2026-05-23 21:26:27', 0);
INSERT INTO `order` VALUES (8, '2026052416580026FB', 2, 18.00, NULL, 18.00, 4, 'AAA', 'AAA', '13734565407', '', '2026-05-24 16:58:05', NULL, '2026-05-24 16:58:05', '2026-05-24 16:58:00', 0);
INSERT INTO `order` VALUES (9, '202605242015252AE2', 2, 15.00, NULL, 15.00, 4, 'AAAAAA', '小明', '17198760987', '', '2026-05-24 20:15:33', NULL, '2026-05-24 20:15:33', '2026-05-24 20:15:25', 0);
INSERT INTO `order` VALUES (10, '20260526112119C0E9', 2, 32.00, NULL, 32.00, 2, 'AAAA', 'AA', '15322462879', '', NULL, NULL, NULL, '2026-05-26 11:21:19', 0);
INSERT INTO `order` VALUES (11, '20260526113515CE03', 2, 135.99, NULL, 135.99, 2, 'ghh', 'aa', '13456789032', '', NULL, NULL, NULL, '2026-05-26 11:35:15', 0);

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `dessert_id` int NOT NULL,
  `dessert_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `dessert_image` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10, 2) NOT NULL,
  `quantity` int NOT NULL,
  `subtotal` decimal(10, 2) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `order_id`(`order_id` ASC) USING BTREE,
  INDEX `dessert_id`(`dessert_id` ASC) USING BTREE,
  CONSTRAINT `order_item_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `order_item_ibfk_2` FOREIGN KEY (`dessert_id`) REFERENCES `dessert` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of order_item
-- ----------------------------
INSERT INTO `order_item` VALUES (1, 1, 1, '生磨芝麻糊加黑糯米', NULL, 15.00, 1, 15.00);
INSERT INTO `order_item` VALUES (2, 1, 2, '鲜杂果凉粉(椰汁芒汁)', NULL, 20.00, 1, 20.00);
INSERT INTO `order_item` VALUES (3, 1, 6, '焦糖布丁', NULL, 12.00, 1, 12.00);
INSERT INTO `order_item` VALUES (4, 2, 4, '提拉米苏', NULL, 32.00, 1, 32.00);
INSERT INTO `order_item` VALUES (5, 3, 1, '生磨芝麻糊加黑糯米', NULL, 15.00, 2, 30.00);
INSERT INTO `order_item` VALUES (6, 3, 5, '抹茶冰淇淋', NULL, 18.00, 1, 18.00);
INSERT INTO `order_item` VALUES (7, 3, 12, '芋圆仙草', NULL, 20.00, 1, 20.00);
INSERT INTO `order_item` VALUES (8, 4, 4, '提拉米苏', NULL, 32.00, 1, 32.00);
INSERT INTO `order_item` VALUES (9, 5, 7, '杨枝甘露', NULL, 22.00, 1, 22.00);
INSERT INTO `order_item` VALUES (10, 5, 9, '双皮奶', NULL, 10.00, 1, 10.00);
INSERT INTO `order_item` VALUES (11, 6, 5, '抹茶冰淇淋', '/photo/005.jpg', 18.00, 1, 18.00);
INSERT INTO `order_item` VALUES (12, 6, 13, '巧克力熔岩蛋糕', '/photo/013.jpg', 35.00, 1, 35.00);
INSERT INTO `order_item` VALUES (13, 7, 5, '抹茶冰淇淋', '/photo/005.jpg', 18.00, 1, 18.00);
INSERT INTO `order_item` VALUES (14, 8, 5, '抹茶冰淇淋', '/photo/005.jpg', 18.00, 1, 18.00);
INSERT INTO `order_item` VALUES (15, 9, 15, '香草冰淇淋', '/photo/015.jpg', 15.00, 1, 15.00);
INSERT INTO `order_item` VALUES (16, 10, 4, '提拉米苏', '/photo/004.jpg', 32.00, 1, 32.00);
INSERT INTO `order_item` VALUES (17, 11, 3, '雪山芒果捞', '/photo/003.jpg', 28.00, 2, 56.00);
INSERT INTO `order_item` VALUES (18, 11, 5, '抹茶冰淇淋', '/photo/005.jpg', 18.00, 1, 18.00);
INSERT INTO `order_item` VALUES (19, 11, 1, '生磨黑芝麻加糯米', '/photo/001.jpg', 19.99, 1, 19.99);
INSERT INTO `order_item` VALUES (20, 11, 9, '双皮奶', '/photo/009.jpg', 10.00, 1, 10.00);
INSERT INTO `order_item` VALUES (21, 11, 4, '提拉米苏', '/photo/004.jpg', 32.00, 1, 32.00);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `role` tinyint NULL DEFAULT 0 COMMENT '角色：0普通用户 1管理员',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', 'admin123', '系统管理员', NULL, 'admin@dessert.com', '13800000001', 1, 1, '2026-05-26 11:35:36', '2026-05-18 10:55:34', '2026-05-26 11:35:36');
INSERT INTO `user` VALUES (2, 'user1', '123456', '甜点爱好者', NULL, 'user1@qq.com', '13800000002', 0, 1, '2026-05-26 11:53:44', '2026-05-18 10:55:50', '2026-05-26 11:53:44');
INSERT INTO `user` VALUES (3, 'user2', '123456', '美食达人', NULL, 'user2@qq.com', '13800000003', 0, 1, NULL, '2026-05-18 10:55:50', '2026-05-18 10:55:50');
INSERT INTO `user` VALUES (4, 'user3', '123456', '甜品控', NULL, 'user3@qq.com', '13800000004', 0, 1, NULL, '2026-05-18 10:55:50', '2026-05-18 10:55:50');
INSERT INTO `user` VALUES (5, 'zhangwei', '123456', '张伟', NULL, 'zhangwei@qq.com', '13800000005', 0, 1, NULL, '2026-05-18 10:55:50', '2026-05-18 10:55:50');
INSERT INTO `user` VALUES (6, 'lili', '123456', '莉莉', NULL, 'lili@qq.com', '13800000006', 0, 1, NULL, '2026-05-18 10:55:50', '2026-05-18 10:55:50');

SET FOREIGN_KEY_CHECKS = 1;
