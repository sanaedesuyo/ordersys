# OrderSys API 接口文档

**版本：** v0.0.1  
**基础地址：** `http://localhost:8080`  
**Content-Type：** `application/json`（除特别说明外）

---

## 1. 通用说明

### 1.1 统一响应格式

所有接口均返回 `Result<T>` 包装对象：

```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 业务状态码。成功为 `200`，失败为 `500` |
| `message` | string | 提示信息。成功时为 `"success"`，失败时为具体错误描述 |
| `data` | T \| null | 业务数据。失败时通常为 `null` |

### 1.2 错误处理

- 控制器内参数校验失败：返回 HTTP 200，响应体 `code=500`，`message` 为错误说明。
- 未捕获异常（如订单不存在、非法状态迁移）：返回 HTTP 500，Spring Boot 默认错误响应（非 `Result` 格式）。

### 1.3 认证

当前版本无认证机制。下单时通过查询参数传入 `userId`。

### 1.4 时间格式

`LocalDateTime` 字段序列化为 ISO-8601 格式，例如：`"2026-06-14T18:30:00"`。

---

## 2. 用户模块

**基础路径：** `/api/user`

### 2.1 创建用户

```
POST /api/user
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | 用户姓名 |
| `phone` | string | 否 | 手机号 |
| `address` | string | 否 | 收货地址 |

**请求示例**

```json
{
  "name": "张三",
  "phone": "13800138000",
  "address": "北京市朝阳区xxx路1号"
}
```

**响应 `data`（User）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 用户 ID（自增） |
| `name` | string | 用户姓名 |
| `phone` | string | 手机号 |
| `address` | string | 收货地址 |
| `createTime` | string | 创建时间 |
| `updateTime` | string | 更新时间 |

**响应示例**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "张三",
    "phone": "13800138000",
    "address": "北京市朝阳区xxx路1号",
    "createTime": "2026-06-14T10:00:00",
    "updateTime": "2026-06-14T10:00:00"
  }
}
```

---

### 2.2 查询用户

```
GET /api/user/{id}
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 用户 ID |

**响应 `data`（User）**

字段同 [2.1 创建用户](#21-创建用户)。用户不存在时 `data` 为 `null`。

---

## 3. 菜品模块

**基础路径：** `/api/dish`

### 3.1 创建菜品

使用工厂模式按类型创建菜品。

```
POST /api/dish
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `type` | string | 是 | 菜品类型，见下方枚举 |
| `name` | string | 是 | 菜品名称 |
| `price` | number | 是 | 价格 |
| `description` | string | 否 | 描述，默认空字符串 |

**菜品类型 `type` 枚举**

| 值 | 说明 |
|----|------|
| `MAIN_DISH` | 主食 |
| `BEVERAGE` | 饮料 |
| `DESSERT` | 甜点 |

**请求示例**

```json
{
  "type": "MAIN_DISH",
  "name": "红烧肉盖饭",
  "description": "经典红烧肉配米饭",
  "price": 28.0
}
```

**错误响应示例**

```json
{
  "code": 500,
  "message": "type、name、price 不能为空",
  "data": null
}
```

```json
{
  "code": 500,
  "message": "不支持的菜品类型: SNACK",
  "data": null
}
```

**响应 `data`（Dish）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 菜品 ID |
| `name` | string | 菜品名称 |
| `description` | string | 描述 |
| `price` | number | 价格 |
| `type` | string | 菜品类型 |
| `status` | int | 状态，`1` 表示上架可售 |
| `createTime` | string | 创建时间 |
| `updateTime` | string | 更新时间 |

---

### 3.2 菜品列表

返回所有上架（`status=1`）的菜品。

```
GET /api/dish
```

**响应 `data`（Dish[]）**

数组元素字段同 [3.1 创建菜品](#31-创建菜品)。

---

### 3.3 查询菜品

```
GET /api/dish/{id}
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 菜品 ID |

**响应 `data`（Dish）**

字段同 [3.1 创建菜品](#31-创建菜品)。菜品不存在时 `data` 为 `null`。

---

## 4. 订单模块

**基础路径：** `/api/order`

### 4.1 创建订单

使用建造者模式组装每个订单项的规格与加料。创建后订单状态为 `PENDING_PAYMENT`（待支付）。

```
POST /api/order?userId={userId}
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `userId` | long | 是 | 下单用户 ID |

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `remark` | string | 否 | 订单备注，默认空字符串 |
| `items` | array | 是 | 订单项列表，至少一项 |

**订单项 `items[]` 字段**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `dishId` | long | 是 | 菜品 ID |
| `dishName` | string | 是 | 菜品名称 |
| `price` | number | 是 | 单价 |
| `quantity` | int | 否 | 数量，默认 `1` |
| `size` | string | 否 | 规格，默认 `LARGE` |
| `extras` | string[] | 否 | 加料列表，如 `["加辣","加蛋"]` |
| `note` | string | 否 | 单项备注 |

**规格 `size` 枚举**

| 值 | 说明 |
|----|------|
| `SMALL` | 小份 |
| `LARGE` | 大份 |

**请求示例**

```json
{
  "remark": "不要辣",
  "items": [
    {
      "dishId": 1,
      "dishName": "红烧肉盖饭",
      "price": 28.0,
      "quantity": 1,
      "size": "LARGE",
      "extras": ["加辣", "加蛋"],
      "note": "不要香菜"
    }
  ]
}
```

**错误响应示例**

```json
{
  "code": 500,
  "message": "订单中至少需要一个菜品",
  "data": null
}
```

**响应 `data`（Order）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 订单 ID |
| `userId` | long | 用户 ID |
| `totalAmount` | number | 订单总金额（各订单项 `price × quantity` 之和） |
| `status` | string | 订单状态，创建时为 `PENDING_PAYMENT` |
| `remark` | string | 订单备注 |
| `createTime` | string | 创建时间 |
| `updateTime` | string | 更新时间 |
| `state` | object | 状态对象（运行时字段，一般不序列化输出） |

---

### 4.2 查询订单

```
GET /api/order/{id}
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `id` | long | 订单 ID |

**响应 `data`（Order）**

字段同 [4.1 创建订单](#41-创建订单)。

订单不存在时抛出异常，返回 HTTP 500：

```
IllegalArgumentException: 订单不存在: {id}
```

---

### 4.3 商家接单

将订单从 `PAID` 迁移至 `PREPARING`。

```
PUT /api/order/{id}/accept
```

**前置状态：** `PAID`

---

### 4.4 开始配送

将订单从 `PREPARING` 迁移至 `DELIVERING`。

```
PUT /api/order/{id}/deliver
```

**前置状态：** `PREPARING`

---

### 4.5 完成订单

将订单从 `DELIVERING` 迁移至 `COMPLETED`。

```
PUT /api/order/{id}/complete
```

**前置状态：** `DELIVERING`

---

### 4.6 取消订单

将订单迁移至 `CANCELLED`。

```
PUT /api/order/{id}/cancel
```

**允许的前置状态：** `PENDING_PAYMENT`、`PAID`、`PREPARING`、`DELIVERING`  
**不允许：** `COMPLETED`（已完成订单不可取消）

**状态流转接口响应 `data`（Order）**

字段同 [4.1 创建订单](#41-创建订单)，`status` 为迁移后的新状态。

非法状态迁移时抛出 `IllegalStateException`，例如：

```
当前状态[COMPLETED]不允许取消操作
```

---

### 4.7 订单状态说明

| 状态 | 说明 | 触发方式 |
|------|------|----------|
| `PENDING_PAYMENT` | 待支付 | 创建订单 |
| `PAID` | 已支付，待接单 | 支付成功 |
| `PREPARING` | 制作中 | 商家接单 `accept` |
| `DELIVERING` | 配送中 | 开始配送 `deliver` |
| `COMPLETED` | 已完成 | 完成订单 `complete` |
| `CANCELLED` | 已取消 | 取消订单 `cancel` |

**状态流转图**

```
PENDING_PAYMENT ──支付成功──▶ PAID ──接单──▶ PREPARING ──配送──▶ DELIVERING ──完成──▶ COMPLETED
       │                        │                │                    │
       └──────── cancel ────────┴────────────────┴────────────────────┘
                                        │
                                        ▼
                                   CANCELLED
```

---

## 5. 支付模块

**基础路径：** `/api/payment`

使用策略模式处理不同支付方式（当前为 Mock 实现）。

### 5.1 发起支付

```
POST /api/payment
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | long | 是 | 订单 ID |
| `amount` | number | 是 | 支付金额，必须大于 0 |
| `method` | string | 是 | 支付方式，见下方枚举 |

**支付方式 `method` 枚举**

| 值 | 说明 |
|----|------|
| `WECHAT` | 微信支付（Mock） |
| `ALIPAY` | 支付宝（Mock） |

**请求示例**

```json
{
  "orderId": 1,
  "amount": 28.00,
  "method": "WECHAT"
}
```

**错误响应示例**

```json
{
  "code": 500,
  "message": "orderId、amount、method 均为必填项",
  "data": null
}
```

```json
{
  "code": 500,
  "message": "不支持的支付方式: CASH",
  "data": null
}
```

**响应 `data`（Payment）**

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 支付记录 ID |
| `orderId` | long | 订单 ID |
| `amount` | number | 支付金额 |
| `method` | string | 支付方式（大写） |
| `status` | string | 支付状态：`SUCCESS` 或 `FAILED` |
| `transactionId` | string | 第三方交易号（Mock 生成） |
| `createTime` | string | 创建时间 |
| `updateTime` | string | 更新时间 |

**副作用**

支付成功（`status=SUCCESS`）时，自动将对应订单状态从 `PENDING_PAYMENT` 迁移至 `PAID`。

---

### 5.2 查询订单支付记录

```
GET /api/payment/order/{orderId}
```

**路径参数**

| 参数 | 类型 | 说明 |
|------|------|------|
| `orderId` | long | 订单 ID |

**响应 `data`（Payment[]）**

数组元素字段同 [5.1 发起支付](#51-发起支付)。同一订单可有多条支付记录。

---

## 6. 完整业务流程示例

以下演示从创建用户到订单完成的典型调用顺序。

### Step 1：创建用户

```http
POST /api/user
Content-Type: application/json

{"name": "张三", "phone": "13800138000", "address": "北京市朝阳区"}
```

### Step 2：创建菜品

```http
POST /api/dish
Content-Type: application/json

{"type": "MAIN_DISH", "name": "红烧肉盖饭", "price": 28.0}
```

### Step 3：下单

```http
POST /api/order?userId=1
Content-Type: application/json

{
  "remark": "不要辣",
  "items": [
    {"dishId": 1, "dishName": "红烧肉盖饭", "price": 28.0, "quantity": 1, "size": "LARGE"}
  ]
}
```

### Step 4：支付

```http
POST /api/payment
Content-Type: application/json

{"orderId": 1, "amount": 28.00, "method": "WECHAT"}
```

### Step 5：商家操作（接单 → 配送 → 完成）

```http
PUT /api/order/1/accept
PUT /api/order/1/deliver
PUT /api/order/1/complete
```

---

## 7. 接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/user` | 创建用户 |
| GET | `/api/user/{id}` | 查询用户 |
| POST | `/api/dish` | 创建菜品 |
| GET | `/api/dish` | 上架菜品列表 |
| GET | `/api/dish/{id}` | 查询菜品 |
| POST | `/api/order?userId=` | 创建订单 |
| GET | `/api/order/{id}` | 查询订单 |
| PUT | `/api/order/{id}/accept` | 商家接单 |
| PUT | `/api/order/{id}/deliver` | 开始配送 |
| PUT | `/api/order/{id}/complete` | 完成订单 |
| PUT | `/api/order/{id}/cancel` | 取消订单 |
| POST | `/api/payment` | 发起支付 |
| GET | `/api/payment/order/{orderId}` | 查询订单支付记录 |
