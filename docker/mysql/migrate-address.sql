-- 多地址支持：新建 user_address 表，订单增加 delivery_address，迁移旧 user.address 数据
USE ordersys;

CREATE TABLE IF NOT EXISTS `user_address` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `label`       VARCHAR(50),
  `detail`      VARCHAR(200) NOT NULL,
  `is_default`  TINYINT(1)   NOT NULL DEFAULT 0,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `order`
  ADD COLUMN `delivery_address` VARCHAR(200) NULL AFTER `remark`;

INSERT INTO `user_address` (`user_id`, `label`, `detail`, `is_default`)
SELECT `id`, '默认', `address`, 1
FROM `user`
WHERE `address` IS NOT NULL AND TRIM(`address`) <> ''
  AND NOT EXISTS (
    SELECT 1 FROM `user_address` ua WHERE ua.user_id = `user`.id
  );
