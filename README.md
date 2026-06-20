# OrderSys — 外卖订单系统

OrderSys 是一个前后端分离的外卖订单系统，包含**用户点餐端**与**商家后台**两个独立前端，后端基于 Spring Boot 3 提供 JWT 鉴权与角色隔离的 REST API。

后端在订单、支付、菜品等核心流程中运用了四种经典 GoF 设计模式：**状态模式、建造者模式、策略模式、工厂模式**。

---

## 目录

- [应用一览](#应用一览)
- [功能概览](#功能概览)
- [快速启动](#快速启动)
- [演示账号](#演示账号)
- [项目结构](#项目结构)
- [架构说明](#架构说明)
- [数据模型](#数据模型)
- [API 概览](#api-概览)
- [数据库迁移](#数据库迁移)
- [设计模式](#设计模式)
- [测试](#测试)
- [相关文档](#相关文档)

---

## 应用一览

| 应用 | 目录 | 端口 | 角色 | 说明 |
|------|------|------|------|------|
| 后端 API | `backend/` | 8080 | — | Spring Boot 3 + JWT |
| 用户点餐端 | `frontend-client/` | 5173 | `USER` | 浏览菜单、下单、支付、管理地址与账户 |
| 商家后台 | `frontend-admin/` | 5174 | `MERCHANT` | 订单看板、菜品管理、支付流水 |

```
┌─────────────────┐     ┌─────────────────┐
│ frontend-client │     │ frontend-admin  │
│   :5173         │     │   :5174         │
└────────┬────────┘     └────────┬────────┘
         │  /api/client/*        │  /api/admin/*
         └──────────┬────────────┘
                    ▼
            ┌───────────────┐
            │ backend :8080 │
            │ JWT + 角色隔离 │
            └───────┬───────┘
                    ▼
            ┌───────────────┐
            │  MySQL :3306  │
            └───────────────┘
```

> 旧版单体前端 `frontend/` 仍保留作参考，日常开发请使用 `frontend-client` 与 `frontend-admin`。  
> 拆分设计详见 [`docs/split-design.md`](docs/split-design.md)。

---

## 功能概览

### 用户点餐端（`frontend-client`）

| 功能 | 说明 |
|------|------|
| 注册 / 登录 | 手机号注册，支持确认密码；JWT 持久化 |
| 菜单浏览 | 按分类筛选、关键词搜索（仅上架菜品） |
| 购物车 & 结算 | 选择收货地址后下单，订单记录配送地址快照 |
| 我的地址 | 多地址管理，支持默认地址 |
| 账户设置 | 修改姓名、手机号、密码 |
| 我的订单 | 查看订单状态、支付、取消 |
| 支付 | 微信 / 支付宝（Mock 策略） |

### 商家后台（`frontend-admin`）

| 功能 | 说明 |
|------|------|
| 登录 | 商家账号（`MERCHANT` 角色） |
| 订单看板 | 按状态分组展示全部订单，含收货地址 |
| 履约操作 | 接单 → 配送 → 完成；支持取消 |
| 菜品管理 | 新增、搜索、编辑、上下架；6 种菜品类型 |
| 支付流水 | 查看订单支付记录 |

---

## 快速启动

### 环境要求

- Java 17+
- Maven 3.8+
- Node.js 18+
- Docker（用于 MySQL）

### 1. 启动数据库

```bash
cd docker
docker compose up -d
```

`docker/mysql/init.sql` 会自动建表并插入演示用户、地址与菜品。

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

服务地址：`http://localhost:8080`

### 3. 启动用户点餐端

```bash
cd frontend-client
npm install
npm run dev
```

访问 `http://localhost:5173`

### 4. 启动商家后台

```bash
cd frontend-admin
npm install
npm run dev
```

访问 `http://localhost:5174`

---

## 演示账号

密码均为 `password123`：

| 角色 | 手机号 | 用途 |
|------|--------|------|
| 用户 | `13800138001` | 点餐端登录（已有默认地址） |
| 用户 | `13800138002` | 点餐端登录（已有默认地址） |
| 商家 | `18888888888` | 商家后台登录 |

也可在点餐端自行注册新用户。若演示账号密码不匹配（BCrypt 与运行环境不一致），后端启动时会通过 `DemoAccountInitializer` 尝试同步；亦可执行 `docker/mysql/migrate-auth.sql` 修复。

---

## 项目结构

```
ordersys/
├── backend/                  # Spring Boot 后端
│   └── src/main/java/com/ordersys/
│       ├── auth/             # JWT 认证（登录、注册、过滤器）
│       ├── config/           # SecurityConfig、演示账号初始化
│       ├── order/            # 订单（状态模式 + 建造者模式）
│       ├── payment/          # 支付（策略模式）
│       ├── product/          # 菜品（工厂模式）
│       ├── user/             # 用户、多地址
│       └── legacy/           # 旧版 API（过渡期兼容）
├── frontend-client/          # 用户点餐端 Vue 3
├── frontend-admin/           # 商家后台 Vue 3
├── frontend/                 # 旧版单体前端（已弃用）
├── docker/                   # Docker Compose + MySQL 脚本
└── docs/                     # 设计文档
```

**后端分层约定：**

| 层 | 职责 |
|----|------|
| `controller` | HTTP 入口，解析参数，返回 `Result<T>` |
| `service` | 业务逻辑、事务、设计模式编排 |
| `mapper` | MyBatis-Plus 数据访问 |
| `entity` | 数据库映射 POJO |
| `state` / `builder` / `strategy` / `factory` | 各设计模式实现 |

---

## 架构说明

### 认证与鉴权

- 登录 / 注册接口独立过滤链，无需 Token
- 业务 API 通过 JWT 鉴权，按路径前缀区分角色：
  - `/api/client/**` → `USER`
  - `/api/admin/**` → `MERCHANT`
- 客户端订单、地址等接口按 `userId` 做数据隔离

### 订单生命周期

```
待支付 → 已支付 → 制作中 → 配送中 → 已完成
  ↕ cancel（待支付 / 已支付 / 制作中 / 配送中 可取消）
```

| 状态 | 数据库值 | 用户端 | 商家端 |
|------|----------|--------|--------|
| 待支付 | `PENDING_PAYMENT` | 支付、取消 | 取消 |
| 已支付 | `PAID` | 取消 | 接单、取消 |
| 制作中 | `PREPARING` | 取消 | 开始配送、取消 |
| 配送中 | `DELIVERING` | 取消 | 完成、取消 |
| 已完成 | `COMPLETED` | — | — |
| 已取消 | `CANCELLED` | — | — |

### 菜品类型

`MAIN_DISH`（主食）、`BEVERAGE`（饮品）、`DESSERT`（甜点）、`SNACK`（小吃）、`SIDE_DISH`（小菜）、`SOUP`（汤品）

---

## 数据模型

```
user ──1:N──▶ user_address
  │
  └──1:N──▶ order ──1:N──▶ order_item
                │
                └──1:1──▶ payment

dish ◀──（逻辑引用，order_item 存快照）
```

| 表 | 说明 |
|----|------|
| `user` | 用户（姓名、手机、角色、密码） |
| `user_address` | 收货地址（标签、详情、是否默认） |
| `dish` | 菜品目录（名称、价格、类型、上下架） |
| `order` | 订单（用户、金额、状态、备注、配送地址快照） |
| `order_item` | 订单明细（菜品快照：名称、单价、份量、加料） |
| `payment` | 支付记录（金额、方式、流水号），`order_id` 唯一 |

**设计要点：**

- `order_item` 对菜品信息做快照，菜单改价不影响历史订单
- `order.delivery_address` 下单时写入地址文本快照
- `order.status` 存枚举字符串，运行时由 `resolveState()` 还原为状态对象
- 状态对象不持久化，避免 ORM 映射复杂性

---

## API 概览

统一响应格式：

```json
{ "code": 200, "message": "success", "data": {} }
```

### 认证（无需 Token）

| 方法 | 路径 | 说明 |
|------|------|------|
| `POST` | `/api/client/auth/register` | 用户注册 |
| `POST` | `/api/client/auth/login` | 用户登录 |
| `POST` | `/api/admin/auth/login` | 商家登录 |

### 用户端 `/api/client/*`（需 `USER` Token）

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 菜品 | `GET` | `/dish` | 浏览菜单（`keyword`、`type` 可选，仅上架） |
| 菜品 | `GET` | `/dish/{id}` | 菜品详情 |
| 订单 | `POST` | `/order` | 下单（需 `addressId`） |
| 订单 | `GET` | `/order` | 我的订单 |
| 订单 | `GET` | `/order/{id}` | 订单详情 |
| 订单 | `PUT` | `/order/{id}/cancel` | 取消订单 |
| 支付 | `POST` | `/payment` | 发起支付 |
| 支付 | `GET` | `/payment/order/{orderId}` | 支付记录 |
| 用户 | `GET` | `/user/me` | 当前用户信息 |
| 用户 | `PUT` | `/user/me` | 更新姓名、手机号 |
| 用户 | `PUT` | `/user/me/password` | 修改密码 |
| 地址 | `GET` | `/address` | 地址列表 |
| 地址 | `POST` | `/address` | 新增地址 |
| 地址 | `PUT` | `/address/{id}` | 编辑地址 |
| 地址 | `DELETE` | `/address/{id}` | 删除地址 |
| 地址 | `PUT` | `/address/{id}/default` | 设为默认 |

> `GET /api/client/dish` 无需登录即可访问。

### 商家端 `/api/admin/*`（需 `MERCHANT` Token）

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 菜品 | `POST` | `/dish` | 新增菜品 |
| 菜品 | `GET` | `/dish` | 菜品列表（含下架，`keyword`、`type` 可选） |
| 菜品 | `GET` | `/dish/{id}` | 菜品详情 |
| 菜品 | `PUT` | `/dish/{id}` | 编辑菜品 |
| 订单 | `GET` | `/order` | 全部订单看板 |
| 订单 | `GET` | `/order/{id}` | 订单详情 |
| 订单 | `PUT` | `/order/{id}/accept` | 接单 |
| 订单 | `PUT` | `/order/{id}/deliver` | 开始配送 |
| 订单 | `PUT` | `/order/{id}/complete` | 完成订单 |
| 订单 | `PUT` | `/order/{id}/cancel` | 取消订单 |
| 支付 | `GET` | `/payment/order/{orderId}` | 支付流水 |

### 旧版 API（过渡期，无鉴权）

`/api/dish`、`/api/order`、`/api/user`、`/api/payment` 保留在 `legacy/` 包中，供旧版 `frontend/` 兼容使用。

完整接口说明见 [`docs/api.md`](docs/api.md)（部分内容可能尚未同步最新拆分）。

---

## 数据库迁移

全新部署直接使用 `docker/mysql/init.sql` 即可。

已有数据库需按需执行迁移脚本：

```bash
# 认证字段（role、password）
docker exec -i ordersys-mysql mysql -uroot -proot123 ordersys < docker/mysql/migrate-auth.sql

# 扩展菜品类型
docker exec -i ordersys-mysql mysql -uroot -proot123 ordersys < docker/mysql/migrate-dish-types.sql

# 多地址 + 订单配送地址
docker exec -i ordersys-mysql mysql -uroot -proot123 ordersys < docker/mysql/migrate-address.sql
```

---

## 设计模式

后端在领域层落地了四种模式，各自职责清晰、互不侵入：

```
菜品管理  → 工厂模式（DishFactory）
创建订单  → 建造者模式（CustomDishBuilder）+ 状态模式（PendingPaymentState）
支付      → 策略模式（WechatPay / AlipayPay）→ 桥接状态模式（markPaid）
履约      → 状态模式（accept / deliver / complete / cancel）
```

| 模式 | 位置 | 解决的问题 |
|------|------|-----------|
| **状态模式** | `order/state/` | 不同订单状态允许不同操作，避免 `if-else` 散落 |
| **建造者模式** | `order/builder/` | 订单项多可选参数（份量、加料、备注），避免构造函数爆炸 |
| **策略模式** | `payment/strategy/` | 多渠道支付逻辑解耦，便于扩展与单独测试 |
| **工厂模式** | `product/factory/` | 按类型创建菜品，统一校验后转为持久化实体 |

**跨模式桥接：**

- `PaymentService.markPaid()` → 触发 `PendingPaymentState.pay()`，支付层不直接改 `status` 字段
- `OrderController` 组装 `CustomDishBuilder`，`OrderService` 负责持久化与初始状态
- `DishFactory.create()` → `AbstractDish.validate()` → `toDishEntity()` 入库

> 支付策略当前为 Mock 实现（生成假流水号），接入真实网关时只需替换策略类，上层不变。

---

## 测试

```bash
cd backend
mvn test
```

| 测试类 | 覆盖 |
|--------|------|
| `OrderStateTest` | 状态迁移规则、非法操作拒绝 |
| `OrderServiceTest` | 下单初始状态、金额计算 |
| `CustomDishBuilderTest` | 建造者链式调用、参数校验 |
| `PaymentStrategyTest` | 各支付策略行为 |
| `DishFactoryTest` | 工厂创建、校验逻辑 |

---

## 相关文档

- [双端拆分设计](docs/split-design.md)
- [API 接口文档](docs/api.md)
- [后端设计规格](docs/superpowers/specs/2026-06-14-ordersys-backend-design.md)

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17、Spring Boot 3.3、MyBatis-Plus 3.5、Spring Security、JJWT |
| 数据库 | MySQL 8.0（Docker） |
| 前端 | Vue 3、Vite、Pinia、Axios |
