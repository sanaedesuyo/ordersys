SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS ordersys DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ordersys;

CREATE TABLE IF NOT EXISTS `dish` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(100) NOT NULL,
  `description` VARCHAR(500),
  `price`       DECIMAL(10,2) NOT NULL,
  `type`        ENUM('MAIN_DISH','BEVERAGE','DESSERT') NOT NULL,
  `status`      TINYINT(1)   NOT NULL DEFAULT 1,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50)  NOT NULL,
  `phone`       VARCHAR(20)  NOT NULL,
  `address`     VARCHAR(200),
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT        NOT NULL,
  `total_amount` DECIMAL(10,2) NOT NULL,
  `status`       ENUM('PENDING_PAYMENT','PAID','PREPARING','DELIVERING','COMPLETED','CANCELLED')
                               NOT NULL DEFAULT 'PENDING_PAYMENT',
  `remark`       VARCHAR(200),
  `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `order_item` (
  `id`         BIGINT        NOT NULL AUTO_INCREMENT,
  `order_id`   BIGINT        NOT NULL,
  `dish_id`    BIGINT        NOT NULL,
  `dish_name`  VARCHAR(100)  NOT NULL,
  `quantity`   INT           NOT NULL DEFAULT 1,
  `unit_price` DECIMAL(10,2) NOT NULL,
  `size`       ENUM('SMALL','LARGE') NOT NULL DEFAULT 'LARGE',
  `extras`     VARCHAR(500),
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `payment` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `order_id`       BIGINT        NOT NULL,
  `amount`         DECIMAL(10,2) NOT NULL,
  `method`         ENUM('WECHAT','ALIPAY') NOT NULL,
  `status`         ENUM('PENDING','SUCCESS','FAILED') NOT NULL DEFAULT 'PENDING',
  `transaction_id` VARCHAR(100),
  `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Seed data
INSERT IGNORE INTO `user` (`name`, `phone`, `address`) VALUES
  ('张三', '13800138001', '北京市朝阳区XXX街道1号'),
  ('李四', '13800138002', '上海市浦东新区YYY路2号');

INSERT IGNORE INTO `dish` (`name`, `description`, `price`, `type`) VALUES
  ('红烧肉盖饭',   '经典红烧肉搭配米饭',   28.00, 'MAIN_DISH'),
  ('宫保鸡丁盖饭', '鲜嫩鸡丁，香脆花生',   25.00, 'MAIN_DISH'),
  ('珍珠奶茶',     '台式手工珍珠奶茶',      12.00, 'BEVERAGE'),
  ('西瓜汁',       '新鲜现榨西瓜汁',        10.00, 'BEVERAGE'),
  ('芒果布丁',     '香甜芒果布丁',          15.00, 'DESSERT'),
  ('提拉米苏',     '经典意式甜点',          18.00, 'DESSERT');
