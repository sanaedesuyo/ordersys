# OrderSys — 外卖订单系统

OrderSys 是一个外卖订单后端项目，使用了四种经典 GoF 设计模式：**状态模式、建造者模式、策略模式、工厂模式**。项目采用 Spring Boot 3 + MyBatis-Plus + MySQL，配套 Vue 3 前端。

---

## 目录

- [OrderSys — 外卖订单系统](#ordersys--外卖订单系统)
  - [目录](#目录)
  - [技术栈与快速启动](#技术栈与快速启动)
    - [1. 启动数据库](#1-启动数据库)
    - [2. 启动后端](#2-启动后端)
    - [3. 启动前端（可选）](#3-启动前端可选)
  - [项目结构](#项目结构)
  - [业务全景：订单生命周期](#业务全景订单生命周期)
  - [设计模式详解](#设计模式详解)
    - [1. 状态模式（State）— 订单状态机](#1-状态模式state-订单状态机)
      - [要解决的问题](#要解决的问题)
      - [设计思路](#设计思路)
      - [关键实现](#关键实现)
      - [状态迁移表](#状态迁移表)
      - [为何这样设计](#为何这样设计)
    - [2. 建造者模式（Builder）— 定制订单项](#2-建造者模式builder-定制订单项)
      - [要解决的问题](#要解决的问题-1)
      - [设计思路](#设计思路-1)
      - [关键实现](#关键实现-1)
      - [为何这样设计](#为何这样设计-1)
    - [3. 策略模式（Strategy）— 多渠道支付](#3-策略模式strategy-多渠道支付)
      - [要解决的问题](#要解决的问题-2)
      - [设计思路](#设计思路-2)
      - [关键实现](#关键实现-2)
      - [为何这样设计](#为何这样设计-2)
    - [4. 工厂模式（Factory）— 菜品创建](#4-工厂模式factory-菜品创建)
      - [要解决的问题](#要解决的问题-3)
      - [设计思路](#设计思路-3)
      - [关键实现](#关键实现-3)
      - [为何这样设计](#为何这样设计-3)
  - [模式协作关系](#模式协作关系)
  - [数据模型](#数据模型)
  - [API 概览](#api-概览)
  - [测试](#测试)
  - [扩展建议](#扩展建议)
  - [相关文档](#相关文档)

---

## 技术栈与快速启动

| 层级 | 技术 |
|------|------|
| 后端 | Java 17、Spring Boot 3.3、MyBatis-Plus 3.5 |
| 数据库 | MySQL 8.0（Docker） |
| 前端 | Vue 3、Vite（端口固定 `5173`） |

### 1. 启动数据库

```bash
cd docker
docker compose up -d
```

数据库初始化脚本 `docker/mysql/init.sql` 会自动建表并插入测试用户与菜品。

默认连接信息：

| 项 | 值 |
|----|-----|
| 地址 | `localhost:3306` |
| 库名 | `ordersys` |
| 用户名 | `ordersys` |
| 密码 | `ordersys123` |

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

服务监听 `http://localhost:8080`。

### 3. 启动前端（可选）

```bash
cd frontend
npm install
npm run dev
```

访问 `http://localhost:5173`，API 请求通过 Vite 代理转发到后端。

---

## 项目结构

后端按**领域包**组织，每个领域内部再分层：

```
backend/src/main/java/com/ordersys/
├── common/                  # 通用组件（统一响应 Result、时间戳自动填充）
├── order/
│   ├── builder/             # 建造者模式
│   ├── state/               # 状态模式
│   ├── controller/
│   ├── service/
│   ├── entity/
│   └── mapper/
├── payment/
│   ├── strategy/            # 策略模式
│   ├── controller/
│   ├── service/
│   ├── entity/
│   └── mapper/
├── product/
│   ├── factory/             # 工厂模式
│   ├── controller/
│   ├── service/
│   ├── entity/
│   └── mapper/
└── user/
    ├── controller/
    ├── service/
    ├── entity/
    └── mapper/
```

**分层约定：**

| 层 | 职责 |
|----|------|
| `controller` | 接收 HTTP 请求，解析参数，返回 `Result<T>` |
| `service` | 业务逻辑、事务边界、设计模式编排 |
| `mapper` | MyBatis-Plus 数据访问（`BaseMapper<T>`） |
| `entity` | 与数据库表映射的 POJO |
| `state` / `builder` / `strategy` / `factory` | 各模式的具体实现，与领域逻辑解耦 |

---

## 业务全景：订单生命周期

一笔订单从创建到完成，会依次经过以下状态：

```
待支付 → 已支付 → 制作中 → 配送中 → 已完成
  ↕ cancel（待支付 / 已支付 / 制作中 / 配送中 均可取消 → 已取消）
```

对应代码中的状态枚举与类：

| 状态 | 数据库值 | 状态类 |
|------|----------|--------|
| 待支付 | `PENDING_PAYMENT` | `PendingPaymentState` |
| 已支付 | `PAID` | `PaidState` |
| 制作中 | `PREPARING` | `PreparingState` |
| 配送中 | `DELIVERING` | `DeliveringState` |
| 已完成 | `COMPLETED` | `CompletedState` |
| 已取消 | `CANCELLED` | `CancelledState` |

完整调用链：

```
创建菜品（工厂）→ 下单（建造者 + 状态）→ 支付（策略 + 状态）→ 接单 / 配送 / 完成（状态）
```

---

## 设计模式详解

### 1. 状态模式（State）— 订单状态机

#### 要解决的问题

订单在不同阶段允许的操作不同：

- 「待支付」可以支付、取消，但不能接单
- 「已支付」可以接单、取消，但不能直接完成
- 「已完成」「已取消」是终态，任何操作都应拒绝

如果用 `if-else` 或 `switch` 把规则写在 `OrderService` 里，会出现两个问题：

1. **规则分散**：每新增一个状态或操作，都要改多处判断
2. **非法迁移难防**：例如「待支付」直接调用 `complete()`，很容易在重构时漏掉校验

#### 设计思路

把「当前状态下能做什么」封装到**状态对象**里，`Order` 只负责持有状态并委托调用：

```
Order（上下文）  ──委托──▶  OrderState（接口）
                              ├── PendingPaymentState
                              ├── PaidState
                              ├── PreparingState
                              ├── DeliveringState
                              ├── CompletedState
                              └── CancelledState
```

核心约定：

- 接口 `OrderState` 为每个操作提供 **default 实现**，默认抛出 `IllegalStateException`
- 各具体状态类**只重写自己允许的操作**
- 状态迁移通过 `order.setState(new XxxState())` 完成

#### 关键实现

**状态接口**（`order/state/OrderState.java`）：

```java
public interface OrderState {
    default void pay(Order order) {
        throw new IllegalStateException("当前状态[" + getStatusName() + "]不允许支付操作");
    }
    // accept / startDelivery / complete / cancel 同理……
    String getStatusName();
}
```

**具体状态示例**（`PendingPaymentState`）：

```java
public class PendingPaymentState implements OrderState {
    @Override public void pay(Order order)    { order.setState(new PaidState()); }
    @Override public void cancel(Order order) { order.setState(new CancelledState()); }
    @Override public String getStatusName()   { return "PENDING_PAYMENT"; }
}
```

**上下文对象**（`order/entity/Order.java`）：

```java
@TableField(exist = false)
private OrderState state;

public void setState(OrderState state) {
    this.state = state;
    this.status = state.getStatusName();  // 同步写入 DB 字段
}
```

这里有一个重要工程决策：**状态对象不持久化**，数据库只存 `status` 字符串。每次从 DB 加载订单时，由 `OrderService.resolveState()` 根据字符串重建对应的状态对象。这样避免了 ORM 映射状态类的复杂性，同时保留了运行时的多态行为。

**服务层编排**（`OrderService`）：

```java
// 加载时恢复状态对象
public Order getOrderWithState(Long orderId) {
    Order order = orderMapper.selectById(orderId);
    order.setState(resolveState(order.getStatus()));
    return order;
}

// 商家操作统一入口
public Order transition(Long orderId, String action) {
    Order order = getOrderWithState(orderId);
    switch (action) {
        case "accept"   -> order.getState().accept(order);
        case "deliver"  -> order.getState().startDelivery(order);
        case "complete" -> order.getState().complete(order);
        case "cancel"   -> order.getState().cancel(order);
    }
    orderMapper.updateById(order);
    return order;
}
```

注意：`transition()` 里的 `switch` 只做「**操作名 → 状态方法**」的路由，**不包含任何业务规则**。规则全在状态类内部。

#### 状态迁移表

| 当前状态 | 允许的操作 | 迁移目标 |
|----------|-----------|----------|
| `PendingPaymentState` | `pay`, `cancel` | `PaidState`, `CancelledState` |
| `PaidState` | `accept`, `cancel` | `PreparingState`, `CancelledState` |
| `PreparingState` | `startDelivery`, `cancel` | `DeliveringState`, `CancelledState` |
| `DeliveringState` | `complete`, `cancel` | `CompletedState`, `CancelledState` |
| `CompletedState` | 无 | 终态 |
| `CancelledState` | 无 | 终态 |

#### 为何这样设计

- **开闭原则**：新增状态（如「退款中」）只需新增一个 `OrderState` 实现类，不必改动其他状态的逻辑
- **单一职责**：`OrderService` 管持久化和流程编排，状态类管规则
- **防御式编程**：非法操作在状态层直接拒绝，调用方无需重复校验

---

### 2. 建造者模式（Builder）— 定制订单项

#### 要解决的问题

用户下单时，每个菜品可以有不同的组合：

- 份量：大份 / 小份
- 加料：加辣、加蛋……
- 数量、备注

如果为 `CustomDish` 写多个构造函数（`(id, name, price)`、`(id, name, price, size)`、`(id, name, price, size, extras)`……），会产生**构造函数爆炸**，且难以保证对象创建后的一致性。

#### 设计思路

用 **Builder** 提供流式 API，分步设置可选参数，最后 `build()` 产出不可变产品对象：

```
CustomDishBuilder  ──build()──▶  CustomDish（值对象）
   .size(LARGE)
   .addExtra("加辣")
   .note("不要香菜")
```

#### 关键实现

**建造者**（`order/builder/CustomDishBuilder.java`）：

```java
public CustomDishBuilder quantity(int quantity) {
    if (quantity < 1) throw new IllegalArgumentException("quantity 必须 >= 1");
    this.quantity = quantity;
    return this;  // 链式调用
}

public CustomDish build() {
    return new CustomDish(dishId, dishName, unitPrice, quantity, size, extras, note);
}
```

**产品（值对象）**（`order/builder/CustomDish.java`）：

- 构造函数为**包私有**，外部无法 `new CustomDish(...)`，只能通过 Builder 创建
- 字段全部 `final`，`extras` 用 `List.copyOf()` 防止外部修改
- `extrasToJson()` 将加料序列化后写入 `order_item.extras` 列

**调用入口**（`OrderController.createOrder()`）：

```java
CustomDishBuilder builder = new CustomDishBuilder(dishId, name, price)
    .quantity(qty).size(size);
extrasList.forEach(e -> builder.addExtra(e.toString()));
if (item.containsKey("note")) builder.note((String) item.get("note"));
return builder.build();
```

**消费方**（`OrderService.createOrder()`）：

```java
order.setState(new PendingPaymentState());  // 初始状态
// 将 CustomDish 快照写入 order_item（菜名、单价、规格、加料）
```

#### 为何这样设计

- **可读性**：`.size(LARGE).addExtra("加辣").note("不要香菜")` 比一长串构造参数更清晰
- **校验前置**：`quantity` 合法性在 Builder 阶段就拦截，不会产出非法对象
- **与菜单解耦**：`CustomDish` 是订单快照的值对象，不映射数据库表；即使日后菜单改价，历史订单数据不受影响

---

### 3. 策略模式（Strategy）— 多渠道支付

#### 要解决的问题

系统需要支持微信、支付宝等多种支付方式。它们的调用流程、签名规则、回调处理各不相同。如果把所有逻辑塞进 `PaymentService` 的一个大 `if-else`，会导致：

1. 每新增一种支付方式都要改 `PaymentService`
2. 支付逻辑与订单逻辑耦合
3. 难以单独测试某一种支付渠道

#### 设计思路

把每种支付方式封装为独立策略，由上下文统一调度：

```
PaymentService
    └── PaymentContext（上下文）
            └── PaymentStrategy（接口）
                    ├── WechatPayStrategy
                    └── AlipayStrategy
```

调用方只关心「用什么方式付多少钱」，不关心具体实现。

#### 关键实现

**策略接口**（`payment/strategy/PaymentStrategy.java`）：

```java
public interface PaymentStrategy {
    PaymentResult pay(BigDecimal amount, Long orderId);
    String getMethodName();
}
```

**具体策略**（`WechatPayStrategy`，Mock 实现）：

```java
public PaymentResult pay(BigDecimal amount, Long orderId) {
    String txId = "WECHAT_" + UUID.randomUUID()...;
    return PaymentResult.success(txId, getMethodName());
}
```

**上下文**（`payment/strategy/PaymentContext.java`）：

```java
public PaymentResult pay(BigDecimal amount, Long orderId) {
    return strategy.pay(amount, orderId);
}
```

**服务层选择策略**（`PaymentService.pay()`）：

```java
PaymentStrategy strategy = switch (method.toUpperCase()) {
    case "WECHAT" -> new WechatPayStrategy();
    case "ALIPAY" -> new AlipayStrategy();
    default -> throw new IllegalArgumentException("不支持的支付方式: " + method);
};
PaymentContext ctx = new PaymentContext(strategy);
PaymentResult result = ctx.pay(amount, orderId);
```

支付成功后，通过 `orderService.markPaid(orderId)` **桥接状态模式**，触发 `PendingPaymentState.pay()` → `PaidState`。

#### 为何这样设计

- **开闭原则**：接入银联、Apple Pay 只需新增一个 `PaymentStrategy` 实现
- **职责分离**：`PaymentService` 管持久化与事务；策略类管渠道逻辑
- **可测试性**：`PaymentStrategyTest` 可独立验证各策略行为，无需启动完整支付链路

> 当前实现为 Mock（生成假流水号），便于本地开发与教学演示。接入真实网关时，只需替换策略实现，上层代码不变。

---

### 4. 工厂模式（Factory）— 菜品创建

#### 要解决的问题

菜品分为主食（`MAIN_DISH`）、饮品（`BEVERAGE`）、甜点（`DESSERT`）三类。调用方在创建菜品时，不应关心具体实例化哪个子类，也不应绕过公共校验逻辑。

#### 设计思路

采用**静态工厂方法**（Static Factory Method）：调用方传入 `DishType`，工厂负责选择具体类、校验、转换为持久化实体。

```
DishController → DishService → DishFactory.create(type, ...)
                                    ├── MainDish
                                    ├── Beverage
                                    └── Dessert
                                        └── validate() → toDishEntity() → Dish
```

#### 关键实现

**抽象产品**（`product/factory/AbstractDish.java`）：

```java
public abstract class AbstractDish {
    public void validate() {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException(type.name() + " 名称不能为空");
        if (price.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("价格必须大于0");
    }

    public Dish toDishEntity() {
        Dish dish = new Dish();
        dish.setName(name);
        dish.setType(type.name());
        dish.setStatus(1);  // 默认上架
        return dish;
    }
}
```

**具体产品**（以 `MainDish` 为例）：

```java
public class MainDish extends AbstractDish {
    public MainDish(String name, String description, double price) {
        super(name, description, price, DishType.MAIN_DISH);
    }
}
```

**工厂**（`product/factory/DishFactory.java`）：

```java
public class DishFactory {
    private DishFactory() {}  // 禁止实例化

    public static AbstractDish create(DishType type, String name, String description, double price) {
        AbstractDish dish = switch (type) {
            case MAIN_DISH -> new MainDish(name, description, price);
            case BEVERAGE  -> new Beverage(name, description, price);
            case DESSERT   -> new Dessert(name, description, price);
        };
        dish.validate();
        return dish;
    }
}
```

**服务层**（`DishService.createDish()`）：

```java
AbstractDish abstractDish = DishFactory.create(dishType, name, description, price);
Dish dish = abstractDish.toDishEntity();
this.baseMapper.insert(dish);
```

#### 为何这样设计

- **封装创建细节**：Controller 只传 `type` 字符串，不需要 `new MainDish(...)`
- **统一校验**：`validate()` 在抽象类中定义，所有子类创建后必经校验
- **扩展方便**：若饮品需要额外字段（容量、温度），可在 `Beverage` 子类中扩展，不影响主食和甜点

> 这里用的是**简单工厂 / 静态工厂方法**，不是抽象工厂（Abstract Factory）。菜品类型之间没有「产品族」的强关联，简单工厂足够。

---

## 模式协作关系

四种模式在一条业务链上串联，但各自职责清晰、互不侵入：

```
┌─────────────────────────────────────────────────────────────────────┐
│                         完整下单流程                                  │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  ① 菜品管理                                                         │
│     DishController ──▶ DishService ──▶ DishFactory（工厂）           │
│                                        └── AbstractDish.validate()  │
│                                                                     │
│  ② 创建订单                                                         │
│     OrderController ──▶ CustomDishBuilder（建造者）──▶ CustomDish   │
│                      ──▶ OrderService.createOrder()                 │
│                              └── setState(PendingPaymentState) 状态  │
│                                                                     │
│  ③ 支付                                                             │
│     PaymentController ──▶ PaymentService                            │
│                              ├── PaymentContext + Strategy（策略）   │
│                              └── markPaid() ──▶ State.pay()（状态）  │
│                                                                     │
│  ④ 履约                                                             │
│     OrderController ──▶ OrderService.transition()                     │
│                              └── State.accept / deliver / complete  │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

**跨模式桥接点：**

| 桥接 | 说明 |
|------|------|
| `PaymentService` → `OrderService.markPaid()` | 支付成功后触发状态迁移，支付层不直接操作 `status` 字段 |
| `OrderController` → `CustomDishBuilder` → `OrderService` | Controller 负责组装 Builder，Service 负责持久化与初始状态 |
| `DishService` → `DishFactory` → `AbstractDish.toDishEntity()` | 工厂产出领域对象，再转为 MyBatis 实体入库 |

---

## 数据模型

```
user ──1:N──▶ order ──1:N──▶ order_item
                │
                └──1:1──▶ payment

dish ◀──（逻辑引用，order_item 存快照）── order_item
```

| 表 | 说明 |
|----|------|
| `user` | 用户（姓名、手机、地址） |
| `dish` | 菜品目录（名称、价格、类型、上下架状态） |
| `order` | 订单主表（用户、总金额、状态、备注） |
| `order_item` | 订单明细（菜品快照：名称、单价、份量、加料 JSON） |
| `payment` | 支付记录（金额、方式、流水号、结果），`order_id` 唯一 |

**设计要点：**

- `order_item` 对菜品信息做**快照**（`dish_name`、`unit_price`），避免菜单变更影响历史订单
- `order.status` 存枚举字符串，运行时由 `resolveState()` 还原为状态对象
- `payment.order_id` 有唯一约束，一笔订单对应一条支付记录

---

## API 概览

完整接口文档见 [`docs/api.md`](docs/api.md)。

| 模块 | 方法 | 路径 | 关联模式 |
|------|------|------|----------|
| 菜品 | `POST` | `/api/dish` | 工厂 |
| 菜品 | `GET` | `/api/dish` | — |
| 订单 | `POST` | `/api/order?userId=` | 建造者 + 状态 |
| 订单 | `GET` | `/api/order` | — |
| 订单 | `GET` | `/api/order/{id}` | 状态（重 hydration） |
| 订单 | `PUT` | `/api/order/{id}/accept` | 状态 |
| 订单 | `PUT` | `/api/order/{id}/deliver` | 状态 |
| 订单 | `PUT` | `/api/order/{id}/complete` | 状态 |
| 订单 | `PUT` | `/api/order/{id}/cancel` | 状态 |
| 支付 | `POST` | `/api/payment` | 策略 + 状态 |
| 用户 | `POST` | `/api/user` | — |
| 用户 | `GET` | `/api/user/{id}` | — |

统一响应格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

---

## 测试

后端为每种设计模式编写了单元测试：

```bash
cd backend
mvn test
```

| 测试类 | 覆盖模式 |
|--------|----------|
| `OrderStateTest` | 状态迁移规则、非法操作拒绝 |
| `OrderServiceTest` | 下单初始状态、金额计算 |
| `CustomDishBuilderTest` | 建造者链式调用、参数校验 |
| `PaymentStrategyTest` | 各支付策略行为 |
| `DishFactoryTest` | 工厂创建、校验逻辑 |

---

## 扩展建议

若在此基础上继续演进，可参考以下方向，且大多符合现有模式结构：

| 需求 | 建议做法 |
|------|----------|
| 新增订单状态（如「退款中」） | 新增 `OrderState` 实现类 + `resolveState()` 分支 |
| 新增支付方式 | 新增 `PaymentStrategy` 实现，在 `PaymentService` 的 `switch` 中注册 |
| 新增菜品类型 | 新增 `AbstractDish` 子类 + `DishFactory` 分支 |
| 订单项更复杂（套餐、组合） | 扩展 `CustomDishBuilder` 或引入 Director 协调多步构建 |
| 全局异常处理 | 添加 `@ControllerAdvice`，将 `IllegalStateException` 映射为 4xx 响应 |

---

## 相关文档

- [API 接口文档](docs/api.md)
- [后端设计规格](docs/superpowers/specs/2026-06-14-ordersys-backend-design.md)
- [前端说明](frontend/README.md)
