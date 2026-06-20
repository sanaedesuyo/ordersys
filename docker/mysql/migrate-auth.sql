-- 为已有数据库补充认证字段
-- 用法：docker compose exec -T mysql mysql -uordersys -pordersys123 ordersys < docker/mysql/migrate-auth.sql
USE ordersys;

ALTER TABLE `user`
  ADD COLUMN `role` ENUM('USER','MERCHANT') NOT NULL DEFAULT 'USER' AFTER `address`;

ALTER TABLE `user`
  ADD COLUMN `password` VARCHAR(100) NOT NULL DEFAULT '' AFTER `role`;

-- 若 uk_phone 已存在可忽略报错
ALTER TABLE `user` ADD UNIQUE KEY `uk_phone` (`phone`);

UPDATE `user` SET `role` = 'USER' WHERE `phone` IN ('13800138001', '13800138002');

INSERT IGNORE INTO `user` (`name`, `phone`, `address`, `role`, `password`) VALUES
  ('店长', '18888888888', '本店', 'MERCHANT', '');

UPDATE `user` SET `role` = 'MERCHANT' WHERE `phone` = '18888888888';
