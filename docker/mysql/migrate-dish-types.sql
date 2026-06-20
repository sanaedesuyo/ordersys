-- 扩展菜品类型枚举并补充种子数据
USE ordersys;

ALTER TABLE `dish`
  MODIFY COLUMN `type` ENUM('MAIN_DISH','BEVERAGE','DESSERT','SNACK','SIDE_DISH','SOUP') NOT NULL;

INSERT IGNORE INTO `dish` (`name`, `description`, `price`, `type`) VALUES
  ('炸鸡块',       '外酥里嫩，现炸现卖',     16.00, 'SNACK'),
  ('薯条',         '金黄酥脆薯条',           8.00, 'SNACK'),
  ('凉拌黄瓜',     '清爽开胃小菜',           6.00, 'SIDE_DISH'),
  ('酸辣土豆丝',   '爽脆酸辣，下饭必备',     8.00, 'SIDE_DISH'),
  ('紫菜蛋花汤',   '家常暖胃汤品',           9.00, 'SOUP'),
  ('番茄牛腩汤',   '浓郁番茄配软烂牛腩',    22.00, 'SOUP');
