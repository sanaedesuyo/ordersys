# 订餐系统后端设计文档

**日期：** 2026-06-14  
**状态：** 已确认  

---

## 1. 技术栈

| 项目 | 选型 |
|---|---|
| 框架 | Spring Boot 3.x |
| ORM | MyBatis-Plus |
| 数据库 | MySQL（Docker 容器化） |
| 语言 | Java 17+ |
| 认证 | 无（userId 通过请求参数传入） |
| 基础包名 | `com.ordersys` |

---

## 2. 目录结构

```
ordersys/
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/ordersys/
│       │   ├── OrdersysApplication.java
│       │   ├── common/
│       │   │   └── Result.java
│       │   ├── product/
│       │   │   ├── entity/Dish.java
│       │   │   ├── mapper/DishMapper.java
│       │   │   ├── service/DishService.java
│       │   │   ├── controller/DishController.java
│       │   │   └── factory/
│       │   │       ├── AbstractDish.java
│       │   │       ├── MainDish.java
│       │   │       ├── Beverage.java
│       │   │       ├── Dessert.java
│       │   │       └── DishFactory.java
│       │   ├── user/
│       │   │   ├── entity/User.java
│       │   │   ├── mapper/UserMapper.java
│       │   │   ├── service/UserService.java
│       │   │   └── controller/UserController.java
│       │   ├── order/
│       │   │   ├── entity/Order.java
│       │   │   ├── entity/OrderItem.java
│       │   │   ├── mapper/OrderMapper.java
│       │   │   ├── mapper/OrderItemMapper.java
│       │   │   ├── service/OrderService.java
│       │   │   ├── controller/OrderController.java
│       │   │   ├── builder/
│       │   │   │   ├── CustomDish.java
│       │   │   │   └── CustomDishBuilder.java
│       │   │   └── state/
│       │   │       ├── OrderState.java
│       │   │       ├── PendingPaymentState.java
│       │   │       ├── PaidState.java
│       │   │       ├── PreparingState.java
│       │   │       ├── DeliveringState.java
│       │   │       ├── CompletedState.java
│       │   │       └── CancelledState.java
│       │   └── payment/
│       │       ├── entity/Payment.java
│       │       ├── mapper/PaymentMapper.java
│       │       ├── service/PaymentService.java
│       │       ├── controller/PaymentController.java
│       │       └── strategy/
│       │           ├── PaymentStrategy.java
│       │           ├── WechatPayStrategy.java
│       │           ├── AlipayStrategy.java
│       │           └── PaymentContext.java
│       └── resources/
│           └── application.yml
└── docker/
    ├── docker-compose.yml
    └── mysql/
        └── init.sql
```

---

## 3. 数据模型

### 表关系

```
user (用户)
  └── order (订单)  1:N
        └── order_item (订单项)  1:N  →  dish (菜品)
order (订单)
  └── payment (支付记录)  1:1
```

### 表结构

**dish（菜品）**
```sql
id, name, description, price, type ENUM('MAIN_DISH','BEVERAGE','DESSERT'),
status TINYINT(1) DEFAULT 1, create_time, update_time
```

**user（用户）**
```sql
id, name, phone, address, create_time, update_time
```

**`order`（订单）**
```sql
id, user_id, total_amount, remark,
status ENUM('PENDING_PAYMENT','PAID','PREPARING','DELIVERING','COMPLETED','CANCELLED'),
create_time, update_time
```

**order_item（订单项）**
```sql
id, order_id, dish_id, dish_name, quantity, unit_price,
size ENUM('SMALL','LARGE'), extras VARCHAR(500)  -- JSON字符串存加料
```

**payment（支付记录）**
```sql
id, order_id, amount, method ENUM('WECHAT','ALIPAY'),
status ENUM('PENDING','SUCCESS','FAILED'),
transaction_id, create_time, update_time
```

---

## 4. 设计模式

### 4.1 工厂模式 — 菜品创建

**触发时机：** 后台创建菜品时  
**参与类：** `AbstractDish`、`MainDish`、`Beverage`、`Dessert`、`DishFactory`

```
DishFactory.create(DishType.MAIN_DISH, dto) → MainDish（设置主食默认逻辑）
DishFactory.create(DishType.BEVERAGE, dto)  → Beverage（设置饮料默认逻辑）
DishFactory.create(DishType.DESSERT, dto)   → Dessert（设置甜点默认逻辑）
```

各子类继承 `AbstractDish`，覆盖 `validate()` 方法做类型专属校验，最终持久化到同一张 `dish` 表。

---

### 4.2 建造者模式 — 定制化菜品

**触发时机：** 用户下单，为每一个菜品条目选规格和加料  
**参与类：** `CustomDish`（产品类）、`CustomDishBuilder`（建造者）

```java
CustomDish customDish = new CustomDishBuilder(baseDish)
    .size(Size.LARGE)
    .addExtra("加辣")
    .addExtra("加蛋")
    .note("不要香菜")
    .build();
```

`CustomDish` 是值对象（非数据库实体），最终将 `size` 和 `extras` 序列化写入 `order_item` 表。

---

### 4.3 策略模式 — 支付方式

**触发时机：** 用户选择支付方式并发起支付  
**参与类：** `PaymentStrategy`（接口）、`WechatPayStrategy`、`AlipayStrategy`、`PaymentContext`

```java
PaymentContext ctx = new PaymentContext(PaymentMethod.WECHAT);
PaymentResult result = ctx.pay(amount, orderId);
```

`WechatPayStrategy` 和 `AlipayStrategy` 均为 Mock 实现，返回模拟的 `transaction_id` 和支付状态。  
`PaymentContext` 通过 `PaymentMethod` 枚举动态选择实现。

---

### 4.4 状态模式 — 订单状态流转

**触发时机：** 订单全生命周期  
**参与类：** `OrderState`（接口）+ 6个状态实现类，Order 持有当前状态引用

**状态迁移：**
```
PENDING_PAYMENT → pay()       → PAID
PAID            → accept()    → PREPARING
PREPARING       → startDelivery() → DELIVERING
DELIVERING      → complete()  → COMPLETED
*任意状态        → cancel()    → CANCELLED（COMPLETED除外）
```

**核心约束：** 状态迁移逻辑全部封装在状态类中，`Order` 实体不含任何 if-else / switch-case。非法操作（如对已完成订单调用 cancel）由状态类抛出 `IllegalStateException`。

---

## 5. 完整下单支付流程

```
1. [菜品创建]  POST /dish        → DishFactory.create() → 持久化
2. [用户下单]  POST /order
   - 遍历请求中的菜品列表
   - CustomDishBuilder 构建每项 CustomDish
   - OrderService 创建 Order（status=PENDING_PAYMENT）
   - 写入 OrderItem（含 size + extras）
3. [发起支付]  POST /payment/{orderId}
   - PaymentContext 执行 Mock 支付
   - 支付成功 → 写入 Payment 记录
   - 调用 order.getState().pay(order) → 状态迁移至 PAID
4. [商家接单]  PUT /order/{id}/accept
   - order.getState().accept(order) → 状态迁移至 PREPARING
5. [后续流转]  配送/完成/取消 同上，全部由状态对象处理
```

---

## 6. Docker 部署

- `docker/docker-compose.yml`：仅部署 MySQL 容器
- `docker/mysql/init.sql`：建库建表 + 测试数据
- 启动：`docker compose -f docker/docker-compose.yml up -d`
- 停止：`docker compose -f docker/docker-compose.yml down`
- 后端本地运行（`mvn spring-boot:run`），连接容器内的 MySQL

---

## 7. API 概览

| 方法 | 路径 | 描述 |
|---|---|---|
| POST | `/api/dish` | 创建菜品（工厂模式） |
| GET | `/api/dish` | 菜品列表 |
| POST | `/api/user` | 创建用户 |
| POST | `/api/order?userId=` | 下单（建造者模式） |
| GET | `/api/order/{id}` | 订单详情 |
| PUT | `/api/order/{id}/accept` | 商家接单 |
| PUT | `/api/order/{id}/deliver` | 开始配送 |
| PUT | `/api/order/{id}/complete` | 完成订单 |
| PUT | `/api/order/{id}/cancel` | 取消订单 |
| POST | `/api/payment/{orderId}` | 发起支付（策略模式） |
