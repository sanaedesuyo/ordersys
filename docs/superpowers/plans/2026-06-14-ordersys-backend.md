# Ordersys Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a food ordering system backend with Spring Boot 3.x + MyBatis-Plus + MySQL demonstrating Factory, Builder, Strategy, and State design patterns.

**Architecture:** Layered monolith packaged by feature (`product`, `user`, `order`, `payment`). Design pattern classes live inside their respective feature packages. MySQL runs in Docker; backend runs locally connecting to the container. No auth — `userId` is passed as a request parameter.

**Tech Stack:** Java 17, Spring Boot 3.3.x, MyBatis-Plus 3.5.7, MySQL 8.0, JUnit 5 + Mockito, Maven

---

## File Map

```
ordersys/
├── docker/
│   ├── docker-compose.yml
│   └── mysql/init.sql
└── backend/
    ├── pom.xml
    └── src/
        ├── main/java/com/ordersys/
        │   ├── OrdersysApplication.java
        │   ├── common/
        │   │   ├── Result.java
        │   │   └── MybatisPlusMetaHandler.java
        │   ├── product/
        │   │   ├── entity/Dish.java
        │   │   ├── mapper/DishMapper.java
        │   │   ├── service/DishService.java
        │   │   ├── controller/DishController.java
        │   │   └── factory/
        │   │       ├── DishType.java       (enum)
        │   │       ├── AbstractDish.java   (abstract base)
        │   │       ├── MainDish.java
        │   │       ├── Beverage.java
        │   │       ├── Dessert.java
        │   │       └── DishFactory.java    (factory class)
        │   ├── user/
        │   │   ├── entity/User.java
        │   │   ├── mapper/UserMapper.java
        │   │   ├── service/UserService.java
        │   │   └── controller/UserController.java
        │   ├── order/
        │   │   ├── entity/Order.java
        │   │   ├── entity/OrderItem.java
        │   │   ├── mapper/OrderMapper.java
        │   │   ├── mapper/OrderItemMapper.java
        │   │   ├── service/OrderService.java
        │   │   ├── controller/OrderController.java
        │   │   ├── builder/
        │   │   │   ├── Size.java           (enum)
        │   │   │   ├── CustomDish.java     (product)
        │   │   │   └── CustomDishBuilder.java
        │   │   └── state/
        │   │       ├── OrderState.java     (interface)
        │   │       ├── PendingPaymentState.java
        │   │       ├── PaidState.java
        │   │       ├── PreparingState.java
        │   │       ├── DeliveringState.java
        │   │       ├── CompletedState.java
        │   │       └── CancelledState.java
        │   └── payment/
        │       ├── entity/Payment.java
        │       ├── mapper/PaymentMapper.java
        │       ├── service/PaymentService.java
        │       ├── controller/PaymentController.java
        │       └── strategy/
        │           ├── PaymentStrategy.java    (interface)
        │           ├── PaymentResult.java
        │           ├── WechatPayStrategy.java
        │           ├── AlipayStrategy.java
        │           └── PaymentContext.java
        ├── main/resources/application.yml
        └── test/java/com/ordersys/
            ├── product/DishFactoryTest.java
            ├── order/CustomDishBuilderTest.java
            ├── order/OrderStateTest.java
            ├── order/OrderServiceTest.java
            └── payment/PaymentStrategyTest.java
```

---

### Task 1: Docker MySQL Setup

**Files:**
- Create: `docker/docker-compose.yml`
- Create: `docker/mysql/init.sql`

- [ ] **Step 1: Create `docker/docker-compose.yml`**

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: ordersys-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: ordersys
      MYSQL_USER: ordersys
      MYSQL_PASSWORD: ordersys123
    ports:
      - "3306:3306"
    volumes:
      - ./mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
      - ordersys-mysql-data:/var/lib/mysql
    command: --default-authentication-plugin=mysql_native_password
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-proot123"]
      interval: 10s
      timeout: 5s
      retries: 5

volumes:
  ordersys-mysql-data:
```

- [ ] **Step 2: Create `docker/mysql/init.sql`**

```sql
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
INSERT INTO `user` (`name`, `phone`, `address`) VALUES
  ('张三', '13800138001', '北京市朝阳区XXX街道1号'),
  ('李四', '13800138002', '上海市浦东新区YYY路2号');

INSERT INTO `dish` (`name`, `description`, `price`, `type`) VALUES
  ('红烧肉盖饭',   '经典红烧肉搭配米饭',   28.00, 'MAIN_DISH'),
  ('宫保鸡丁盖饭', '鲜嫩鸡丁，香脆花生',   25.00, 'MAIN_DISH'),
  ('珍珠奶茶',     '台式手工珍珠奶茶',      12.00, 'BEVERAGE'),
  ('西瓜汁',       '新鲜现榨西瓜汁',        10.00, 'BEVERAGE'),
  ('芒果布丁',     '香甜芒果布丁',          15.00, 'DESSERT'),
  ('提拉米苏',     '经典意式甜点',          18.00, 'DESSERT');
```

- [ ] **Step 3: Start container and verify**

```bash
docker compose -f docker/docker-compose.yml up -d
sleep 15
docker compose -f docker/docker-compose.yml ps
# Expected: ordersys-mysql   Up (healthy)
docker exec ordersys-mysql mysql -u ordersys -pordersys123 ordersys -e "SHOW TABLES;"
# Expected: dish  order  order_item  payment  user
```

- [ ] **Step 4: Commit**

```bash
git add docker/
git commit -m "feat: add Docker MySQL with schema and seed data"
```

---

### Task 2: Spring Boot Project Scaffold

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/ordersys/OrdersysApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/java/com/ordersys/common/Result.java`
- Create: `backend/src/main/java/com/ordersys/common/MybatisPlusMetaHandler.java`

- [ ] **Step 1: Create `backend/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.0</version>
    <relativePath/>
  </parent>

  <groupId>com.ordersys</groupId>
  <artifactId>ordersys</artifactId>
  <version>0.0.1-SNAPSHOT</version>
  <name>ordersys</name>

  <properties>
    <java.version>17</java.version>
    <mybatis-plus.version>3.5.7</mybatis-plus.version>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>com.baomidou</groupId>
      <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
      <version>${mybatis-plus.version}</version>
    </dependency>
    <dependency>
      <groupId>com.mysql</groupId>
      <artifactId>mysql-connector-j</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
          <excludes>
            <exclude>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
            </exclude>
          </excludes>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

- [ ] **Step 2: Create `OrdersysApplication.java`**

```java
package com.ordersys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ordersys.*.mapper")
public class OrdersysApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrdersysApplication.class, args);
    }
}
```

- [ ] **Step 3: Create `backend/src/main/resources/application.yml`**

```yaml
spring:
  application:
    name: ordersys
  datasource:
    url: jdbc:mysql://localhost:3306/ordersys?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: ordersys
    password: ordersys123
    driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
  global-config:
    db-config:
      id-type: auto

server:
  port: 8080
```

- [ ] **Step 4: Create `Result.java`**

```java
package com.ordersys.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "success";
        r.data = data;
        return r;
    }

    public static <T> Result<T> error(String message) {
        Result<T> r = new Result<>();
        r.code = 500;
        r.message = message;
        r.data = null;
        return r;
    }
}
```

- [ ] **Step 5: Create `MybatisPlusMetaHandler.java`**

```java
package com.ordersys.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class MybatisPlusMetaHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

- [ ] **Step 6: Verify compilation**

```bash
cd backend && mvn compile -q
# Expected: BUILD SUCCESS
```

- [ ] **Step 7: Commit**

```bash
git add backend/
git commit -m "feat: scaffold Spring Boot 3 + MyBatis-Plus project"
```

---

### Task 3: User Module

**Files:**
- Create: `backend/src/main/java/com/ordersys/user/entity/User.java`
- Create: `backend/src/main/java/com/ordersys/user/mapper/UserMapper.java`
- Create: `backend/src/main/java/com/ordersys/user/service/UserService.java`
- Create: `backend/src/main/java/com/ordersys/user/controller/UserController.java`

- [ ] **Step 1: Create `User.java`**

```java
package com.ordersys.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String phone;
    private String address;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2: Create `UserMapper.java`**

```java
package com.ordersys.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordersys.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
```

- [ ] **Step 3: Create `UserService.java`**

```java
package com.ordersys.user.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ordersys.user.entity.User;
import com.ordersys.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    public User createUser(User user) {
        this.baseMapper.insert(user);
        return user;
    }
}
```

- [ ] **Step 4: Create `UserController.java`**

```java
package com.ordersys.user.controller;

import com.ordersys.common.Result;
import com.ordersys.user.entity.User;
import com.ordersys.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public Result<User> createUser(@RequestBody User user) {
        return Result.success(userService.createUser(user));
    }

    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }
}
```

- [ ] **Step 5: Compile and commit**

```bash
cd backend && mvn compile -q
# Expected: BUILD SUCCESS
git add backend/src/
git commit -m "feat: add user module"
```

---

### Task 4: Factory Pattern — Product Module

**Files:**
- Create: `backend/src/main/java/com/ordersys/product/factory/DishType.java`
- Create: `backend/src/main/java/com/ordersys/product/factory/AbstractDish.java`
- Create: `backend/src/main/java/com/ordersys/product/entity/Dish.java`
- Create: `backend/src/main/java/com/ordersys/product/factory/MainDish.java`
- Create: `backend/src/main/java/com/ordersys/product/factory/Beverage.java`
- Create: `backend/src/main/java/com/ordersys/product/factory/Dessert.java`
- Create: `backend/src/main/java/com/ordersys/product/factory/DishFactory.java`
- Create: `backend/src/main/java/com/ordersys/product/mapper/DishMapper.java`
- Create: `backend/src/main/java/com/ordersys/product/service/DishService.java`
- Create: `backend/src/main/java/com/ordersys/product/controller/DishController.java`
- Test: `backend/src/test/java/com/ordersys/product/DishFactoryTest.java`

- [ ] **Step 1: Write the failing test**

```java
// backend/src/test/java/com/ordersys/product/DishFactoryTest.java
package com.ordersys.product;

import com.ordersys.product.factory.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class DishFactoryTest {

    @Test
    void create_mainDish_returnsMainDishInstance() {
        AbstractDish dish = DishFactory.create(DishType.MAIN_DISH, "红烧肉盖饭", "经典红烧肉", 28.00);
        assertThat(dish).isInstanceOf(MainDish.class);
        assertThat(dish.getType()).isEqualTo(DishType.MAIN_DISH);
        assertThat(dish.getPrice().doubleValue()).isEqualTo(28.00);
    }

    @Test
    void create_beverage_returnsBeverageInstance() {
        AbstractDish dish = DishFactory.create(DishType.BEVERAGE, "珍珠奶茶", "手工奶茶", 12.00);
        assertThat(dish).isInstanceOf(Beverage.class);
        assertThat(dish.getType()).isEqualTo(DishType.BEVERAGE);
    }

    @Test
    void create_dessert_returnsDessertInstance() {
        AbstractDish dish = DishFactory.create(DishType.DESSERT, "芒果布丁", "香甜布丁", 15.00);
        assertThat(dish).isInstanceOf(Dessert.class);
        assertThat(dish.getType()).isEqualTo(DishType.DESSERT);
    }

    @Test
    void create_withZeroPrice_throwsIllegalArgument() {
        assertThatThrownBy(() -> DishFactory.create(DishType.MAIN_DISH, "测试", "描述", 0.00))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("价格");
    }

    @Test
    void create_withBlankName_throwsIllegalArgument() {
        assertThatThrownBy(() -> DishFactory.create(DishType.BEVERAGE, "", "描述", 10.00))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("名称");
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
cd backend && mvn test -Dtest=DishFactoryTest -q 2>&1 | tail -5
# Expected: FAIL — compilation error, classes not found
```

- [ ] **Step 3: Create `DishType.java`**

```java
package com.ordersys.product.factory;

public enum DishType {
    MAIN_DISH, BEVERAGE, DESSERT
}
```

- [ ] **Step 4: Create `Dish.java` (entity)**

```java
package com.ordersys.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("dish")
public class Dish {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String type;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 5: Create `AbstractDish.java`**

```java
package com.ordersys.product.factory;

import com.ordersys.product.entity.Dish;
import lombok.Getter;
import java.math.BigDecimal;

/**
 * 工厂模式 — 抽象产品。
 * 定义所有菜品类型的公共字段和行为契约；
 * 子类（MainDish / Beverage / Dessert）实现各自的校验与实体转换逻辑。
 */
@Getter
public abstract class AbstractDish {
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final DishType type;

    protected AbstractDish(String name, String description, double price, DishType type) {
        this.name = name;
        this.description = description;
        this.price = BigDecimal.valueOf(price);
        this.type = type;
    }

    public abstract void validate();

    public abstract Dish toDishEntity();
}
```

- [ ] **Step 6: Create `MainDish.java`**

```java
package com.ordersys.product.factory;

import com.ordersys.product.entity.Dish;

/** 工厂模式 — 主食类型的具体产品 */
public class MainDish extends AbstractDish {

    public MainDish(String name, String description, double price) {
        super(name, description, price, DishType.MAIN_DISH);
    }

    @Override
    public void validate() {
        if (getName() == null || getName().isBlank())
            throw new IllegalArgumentException("主食名称不能为空");
        if (getPrice().doubleValue() <= 0)
            throw new IllegalArgumentException("价格必须大于0");
    }

    @Override
    public Dish toDishEntity() {
        Dish dish = new Dish();
        dish.setName(getName());
        dish.setDescription(getDescription());
        dish.setPrice(getPrice());
        dish.setType(getType().name());
        dish.setStatus(1);
        return dish;
    }
}
```

- [ ] **Step 7: Create `Beverage.java`**

```java
package com.ordersys.product.factory;

import com.ordersys.product.entity.Dish;

/** 工厂模式 — 饮料类型的具体产品 */
public class Beverage extends AbstractDish {

    public Beverage(String name, String description, double price) {
        super(name, description, price, DishType.BEVERAGE);
    }

    @Override
    public void validate() {
        if (getName() == null || getName().isBlank())
            throw new IllegalArgumentException("饮料名称不能为空");
        if (getPrice().doubleValue() <= 0)
            throw new IllegalArgumentException("价格必须大于0");
    }

    @Override
    public Dish toDishEntity() {
        Dish dish = new Dish();
        dish.setName(getName());
        dish.setDescription(getDescription());
        dish.setPrice(getPrice());
        dish.setType(getType().name());
        dish.setStatus(1);
        return dish;
    }
}
```

- [ ] **Step 8: Create `Dessert.java`**

```java
package com.ordersys.product.factory;

import com.ordersys.product.entity.Dish;

/** 工厂模式 — 甜点类型的具体产品 */
public class Dessert extends AbstractDish {

    public Dessert(String name, String description, double price) {
        super(name, description, price, DishType.DESSERT);
    }

    @Override
    public void validate() {
        if (getName() == null || getName().isBlank())
            throw new IllegalArgumentException("甜点名称不能为空");
        if (getPrice().doubleValue() <= 0)
            throw new IllegalArgumentException("价格必须大于0");
    }

    @Override
    public Dish toDishEntity() {
        Dish dish = new Dish();
        dish.setName(getName());
        dish.setDescription(getDescription());
        dish.setPrice(getPrice());
        dish.setType(getType().name());
        dish.setStatus(1);
        return dish;
    }
}
```

- [ ] **Step 9: Create `DishFactory.java`**

```java
package com.ordersys.product.factory;

/**
 * 工厂模式 — 工厂类（静态工厂方法）。
 * 调用方只需传入 DishType，无需知道具体子类；
 * 工厂负责实例化并调用 validate() 确保对象合法。
 */
public class DishFactory {

    private DishFactory() {}

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

- [ ] **Step 10: Create `DishMapper.java`**

```java
package com.ordersys.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordersys.product.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
```

- [ ] **Step 11: Create `DishService.java`**

```java
package com.ordersys.product.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ordersys.product.entity.Dish;
import com.ordersys.product.factory.AbstractDish;
import com.ordersys.product.factory.DishFactory;
import com.ordersys.product.factory.DishType;
import com.ordersys.product.mapper.DishMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DishService extends ServiceImpl<DishMapper, Dish> {

    public Dish createDish(String type, String name, String description, double price) {
        AbstractDish abstractDish = DishFactory.create(DishType.valueOf(type), name, description, price);
        Dish dish = abstractDish.toDishEntity();
        this.baseMapper.insert(dish);
        return dish;
    }

    public List<Dish> listAvailable() {
        return this.lambdaQuery().eq(Dish::getStatus, 1).list();
    }
}
```

- [ ] **Step 12: Create `DishController.java`**

```java
package com.ordersys.product.controller;

import com.ordersys.common.Result;
import com.ordersys.product.entity.Dish;
import com.ordersys.product.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @PostMapping
    public Result<Dish> createDish(@RequestBody Map<String, Object> body) {
        String type        = (String) body.get("type");
        String name        = (String) body.get("name");
        String description = (String) body.getOrDefault("description", "");
        double price       = Double.parseDouble(body.get("price").toString());
        return Result.success(dishService.createDish(type, name, description, price));
    }

    @GetMapping
    public Result<List<Dish>> listDishes() {
        return Result.success(dishService.listAvailable());
    }

    @GetMapping("/{id}")
    public Result<Dish> getDish(@PathVariable Long id) {
        return Result.success(dishService.getById(id));
    }
}
```

- [ ] **Step 13: Run the test**

```bash
cd backend && mvn test -Dtest=DishFactoryTest -q 2>&1 | tail -5
# Expected: Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

- [ ] **Step 14: Commit**

```bash
git add backend/src/
git commit -m "feat: add product module with Factory pattern (MainDish/Beverage/Dessert)"
```

---

### Task 5: Builder Pattern — CustomDish

**Files:**
- Create: `backend/src/main/java/com/ordersys/order/builder/Size.java`
- Create: `backend/src/main/java/com/ordersys/order/builder/CustomDish.java`
- Create: `backend/src/main/java/com/ordersys/order/builder/CustomDishBuilder.java`
- Test: `backend/src/test/java/com/ordersys/order/CustomDishBuilderTest.java`

- [ ] **Step 1: Write the failing test**

```java
// backend/src/test/java/com/ordersys/order/CustomDishBuilderTest.java
package com.ordersys.order;

import com.ordersys.order.builder.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class CustomDishBuilderTest {

    @Test
    void build_withAllOptions_producesCorrectCustomDish() {
        CustomDish dish = new CustomDishBuilder(1L, "红烧肉盖饭", 28.00)
            .size(Size.LARGE)
            .addExtra("加辣")
            .addExtra("加蛋")
            .note("不要香菜")
            .build();

        assertThat(dish.getDishId()).isEqualTo(1L);
        assertThat(dish.getDishName()).isEqualTo("红烧肉盖饭");
        assertThat(dish.getSize()).isEqualTo(Size.LARGE);
        assertThat(dish.getExtras()).containsExactly("加辣", "加蛋");
        assertThat(dish.getNote()).isEqualTo("不要香菜");
        assertThat(dish.getUnitPrice().doubleValue()).isEqualTo(28.00);
    }

    @Test
    void build_defaults_areLargeWithNoExtras() {
        CustomDish dish = new CustomDishBuilder(2L, "珍珠奶茶", 12.00).build();

        assertThat(dish.getSize()).isEqualTo(Size.LARGE);
        assertThat(dish.getExtras()).isEmpty();
        assertThat(dish.getNote()).isNull();
        assertThat(dish.getQuantity()).isEqualTo(1);
    }

    @Test
    void extrasToJson_producesValidJsonArray() {
        CustomDish dish = new CustomDishBuilder(3L, "珍珠奶茶", 12.00)
            .addExtra("去冰")
            .addExtra("少糖")
            .build();

        assertThat(dish.extrasToJson()).isEqualTo("[\"去冰\",\"少糖\"]");
    }

    @Test
    void extrasToJson_whenEmpty_returnsEmptyArray() {
        CustomDish dish = new CustomDishBuilder(3L, "西瓜汁", 10.00).build();
        assertThat(dish.extrasToJson()).isEqualTo("[]");
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
cd backend && mvn test -Dtest=CustomDishBuilderTest -q 2>&1 | tail -5
# Expected: FAIL — compilation error, classes not found
```

- [ ] **Step 3: Create `Size.java`**

```java
package com.ordersys.order.builder;

public enum Size {
    SMALL, LARGE
}
```

- [ ] **Step 4: Create `CustomDish.java`**

```java
package com.ordersys.order.builder;

import lombok.Getter;
import java.math.BigDecimal;
import java.util.List;

/**
 * 建造者模式 — 产品类（值对象，不映射数据库）。
 * 由 CustomDishBuilder 构建，封装用户对单个菜品的所有个性化选择。
 * 包的私有构造函数确保只能通过 Builder 创建。
 */
@Getter
public class CustomDish {
    private final Long dishId;
    private final String dishName;
    private final BigDecimal unitPrice;
    private final int quantity;
    private final Size size;
    private final List<String> extras;
    private final String note;

    CustomDish(Long dishId, String dishName, double unitPrice, int quantity,
               Size size, List<String> extras, String note) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.unitPrice = BigDecimal.valueOf(unitPrice);
        this.quantity = quantity;
        this.size = size;
        this.extras = List.copyOf(extras);
        this.note = note;
    }

    /** 将加料列表序列化为 JSON 字符串，写入 order_item.extras 列 */
    public String extrasToJson() {
        if (extras.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < extras.size(); i++) {
            sb.append("\"").append(extras.get(i)).append("\"");
            if (i < extras.size() - 1) sb.append(",");
        }
        return sb.append("]").toString();
    }
}
```

- [ ] **Step 5: Create `CustomDishBuilder.java`**

```java
package com.ordersys.order.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * 建造者模式 — 建造者类。
 * 提供流式 API，让调用方灵活组合规格（大/小份）和加料；
 * build() 最终生成不可变的 CustomDish 对象。
 *
 * 使用示例：
 *   new CustomDishBuilder(dishId, dishName, price)
 *       .size(Size.LARGE)
 *       .addExtra("加辣")
 *       .note("不要香菜")
 *       .build();
 */
public class CustomDishBuilder {
    private final Long dishId;
    private final String dishName;
    private final double unitPrice;
    private int quantity = 1;
    private Size size = Size.LARGE;
    private final List<String> extras = new ArrayList<>();
    private String note;

    public CustomDishBuilder(Long dishId, String dishName, double unitPrice) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.unitPrice = unitPrice;
    }

    public CustomDishBuilder quantity(int quantity) {
        this.quantity = quantity;
        return this;
    }

    public CustomDishBuilder size(Size size) {
        this.size = size;
        return this;
    }

    public CustomDishBuilder addExtra(String extra) {
        this.extras.add(extra);
        return this;
    }

    public CustomDishBuilder note(String note) {
        this.note = note;
        return this;
    }

    public CustomDish build() {
        return new CustomDish(dishId, dishName, unitPrice, quantity, size, extras, note);
    }
}
```

- [ ] **Step 6: Run the test**

```bash
cd backend && mvn test -Dtest=CustomDishBuilderTest -q 2>&1 | tail -5
# Expected: Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

- [ ] **Step 7: Commit**

```bash
git add backend/src/
git commit -m "feat: add Builder pattern for customizable dish ordering"
```

---

### Task 6: Strategy Pattern — Payment

**Files:**
- Create: `backend/src/main/java/com/ordersys/payment/strategy/PaymentStrategy.java`
- Create: `backend/src/main/java/com/ordersys/payment/strategy/PaymentResult.java`
- Create: `backend/src/main/java/com/ordersys/payment/strategy/WechatPayStrategy.java`
- Create: `backend/src/main/java/com/ordersys/payment/strategy/AlipayStrategy.java`
- Create: `backend/src/main/java/com/ordersys/payment/strategy/PaymentContext.java`
- Create: `backend/src/main/java/com/ordersys/payment/entity/Payment.java`
- Create: `backend/src/main/java/com/ordersys/payment/mapper/PaymentMapper.java`
- Test: `backend/src/test/java/com/ordersys/payment/PaymentStrategyTest.java`

- [ ] **Step 1: Write the failing test**

```java
// backend/src/test/java/com/ordersys/payment/PaymentStrategyTest.java
package com.ordersys.payment;

import com.ordersys.payment.strategy.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class PaymentStrategyTest {

    @Test
    void wechatPay_returnsSuccessWithWechatPrefix() {
        PaymentContext ctx = new PaymentContext(new WechatPayStrategy());
        PaymentResult result = ctx.pay(28.00, 1001L);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTransactionId()).startsWith("WECHAT_");
        assertThat(result.getMethod()).isEqualTo("WECHAT");
    }

    @Test
    void alipay_returnsSuccessWithAlipayPrefix() {
        PaymentContext ctx = new PaymentContext(new AlipayStrategy());
        PaymentResult result = ctx.pay(28.00, 1001L);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTransactionId()).startsWith("ALIPAY_");
        assertThat(result.getMethod()).isEqualTo("ALIPAY");
    }

    @Test
    void paymentContext_setStrategy_switchesBehavior() {
        PaymentContext ctx = new PaymentContext(new WechatPayStrategy());
        ctx.setStrategy(new AlipayStrategy());
        PaymentResult result = ctx.pay(15.00, 1002L);

        assertThat(result.getMethod()).isEqualTo("ALIPAY");
        assertThat(result.getTransactionId()).startsWith("ALIPAY_");
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
cd backend && mvn test -Dtest=PaymentStrategyTest -q 2>&1 | tail -5
# Expected: FAIL — compilation error, classes not found
```

- [ ] **Step 3: Create `PaymentStrategy.java`**

```java
package com.ordersys.payment.strategy;

/**
 * 策略模式 — 策略接口。
 * 定义统一的支付行为契约；每种支付方式实现此接口，
 * 让 PaymentContext 可以在运行时动态切换支付方式。
 */
public interface PaymentStrategy {
    PaymentResult pay(double amount, Long orderId);
    String getMethodName();
}
```

- [ ] **Step 4: Create `PaymentResult.java`**

```java
package com.ordersys.payment.strategy;

import lombok.Data;

@Data
public class PaymentResult {
    private boolean success;
    private String transactionId;
    private String method;
    private String message;

    public static PaymentResult success(String transactionId, String method) {
        PaymentResult r = new PaymentResult();
        r.success = true;
        r.transactionId = transactionId;
        r.method = method;
        r.message = "支付成功";
        return r;
    }

    public static PaymentResult fail(String method, String message) {
        PaymentResult r = new PaymentResult();
        r.success = false;
        r.method = method;
        r.message = message;
        return r;
    }
}
```

- [ ] **Step 5: Create `WechatPayStrategy.java`**

```java
package com.ordersys.payment.strategy;

import java.util.UUID;

/** 策略模式 — 微信支付具体策略（Mock 实现，不调用真实 API） */
public class WechatPayStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(double amount, Long orderId) {
        String txId = "WECHAT_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return PaymentResult.success(txId, getMethodName());
    }

    @Override
    public String getMethodName() {
        return "WECHAT";
    }
}
```

- [ ] **Step 6: Create `AlipayStrategy.java`**

```java
package com.ordersys.payment.strategy;

import java.util.UUID;

/** 策略模式 — 支付宝具体策略（Mock 实现，不调用真实 API） */
public class AlipayStrategy implements PaymentStrategy {

    @Override
    public PaymentResult pay(double amount, Long orderId) {
        String txId = "ALIPAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        return PaymentResult.success(txId, getMethodName());
    }

    @Override
    public String getMethodName() {
        return "ALIPAY";
    }
}
```

- [ ] **Step 7: Create `PaymentContext.java`**

```java
package com.ordersys.payment.strategy;

/**
 * 策略模式 — 上下文类。
 * 持有 PaymentStrategy 引用，对调用方屏蔽具体支付实现；
 * 通过 setStrategy() 在运行时动态切换支付方式。
 */
public class PaymentContext {
    private PaymentStrategy strategy;

    public PaymentContext(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    public PaymentResult pay(double amount, Long orderId) {
        return strategy.pay(amount, orderId);
    }
}
```

- [ ] **Step 8: Create `Payment.java` (entity)**

```java
package com.ordersys.payment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("payment")
public class Payment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String method;
    private String status;
    private String transactionId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **Step 9: Create `PaymentMapper.java`**

```java
package com.ordersys.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordersys.payment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {
}
```

- [ ] **Step 10: Run the test**

```bash
cd backend && mvn test -Dtest=PaymentStrategyTest -q 2>&1 | tail -5
# Expected: Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

- [ ] **Step 11: Commit**

```bash
git add backend/src/
git commit -m "feat: add Strategy pattern for wechat/alipay payment"
```

---

### Task 7: State Pattern — Order States

**Files:**
- Create: `backend/src/main/java/com/ordersys/order/state/OrderState.java`
- Create: `backend/src/main/java/com/ordersys/order/entity/Order.java`
- Create: `backend/src/main/java/com/ordersys/order/state/PendingPaymentState.java`
- Create: `backend/src/main/java/com/ordersys/order/state/PaidState.java`
- Create: `backend/src/main/java/com/ordersys/order/state/PreparingState.java`
- Create: `backend/src/main/java/com/ordersys/order/state/DeliveringState.java`
- Create: `backend/src/main/java/com/ordersys/order/state/CompletedState.java`
- Create: `backend/src/main/java/com/ordersys/order/state/CancelledState.java`
- Test: `backend/src/test/java/com/ordersys/order/OrderStateTest.java`

- [ ] **Step 1: Write the failing test**

```java
// backend/src/test/java/com/ordersys/order/OrderStateTest.java
package com.ordersys.order;

import com.ordersys.order.entity.Order;
import com.ordersys.order.state.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class OrderStateTest {

    private Order newPendingOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setState(new PendingPaymentState());
        return order;
    }

    @Test
    void pay_fromPendingPayment_transitionsToPaid() {
        Order order = newPendingOrder();
        order.getState().pay(order);

        assertThat(order.getState()).isInstanceOf(PaidState.class);
        assertThat(order.getStatus()).isEqualTo("PAID");
    }

    @Test
    void accept_fromPaid_transitionsToPreparing() {
        Order order = newPendingOrder();
        order.getState().pay(order);
        order.getState().accept(order);

        assertThat(order.getState()).isInstanceOf(PreparingState.class);
        assertThat(order.getStatus()).isEqualTo("PREPARING");
    }

    @Test
    void fullFlow_pendingToCompleted() {
        Order order = newPendingOrder();
        order.getState().pay(order);
        order.getState().accept(order);
        order.getState().startDelivery(order);
        order.getState().complete(order);

        assertThat(order.getState()).isInstanceOf(CompletedState.class);
        assertThat(order.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void cancel_fromPendingPayment_transitionsToCancelled() {
        Order order = newPendingOrder();
        order.getState().cancel(order);

        assertThat(order.getState()).isInstanceOf(CancelledState.class);
        assertThat(order.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void cancel_fromCompleted_throwsIllegalState() {
        Order order = newPendingOrder();
        order.getState().pay(order);
        order.getState().accept(order);
        order.getState().startDelivery(order);
        order.getState().complete(order);

        assertThatThrownBy(() -> order.getState().cancel(order))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void pay_whenAlreadyPaid_throwsIllegalState() {
        Order order = newPendingOrder();
        order.getState().pay(order);

        assertThatThrownBy(() -> order.getState().pay(order))
            .isInstanceOf(IllegalStateException.class);
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
cd backend && mvn test -Dtest=OrderStateTest -q 2>&1 | tail -5
# Expected: FAIL — compilation error, classes not found
```

- [ ] **Step 3: Create `OrderState.java` (interface)**

```java
package com.ordersys.order.state;

import com.ordersys.order.entity.Order;

/**
 * 状态模式 — 状态接口。
 * 每个状态实现类决定当前状态下哪些操作合法，并负责迁移到下一状态。
 * Order 实体持有 OrderState 引用并委托给它，自身不含任何 if-else/switch。
 * default 实现统一抛出 IllegalStateException，子类只重写允许的操作。
 */
public interface OrderState {

    default void pay(Order order) {
        throw new IllegalStateException("当前状态[" + getStatusName() + "]不允许支付操作");
    }

    default void accept(Order order) {
        throw new IllegalStateException("当前状态[" + getStatusName() + "]不允许接单操作");
    }

    default void startDelivery(Order order) {
        throw new IllegalStateException("当前状态[" + getStatusName() + "]不允许开始配送操作");
    }

    default void complete(Order order) {
        throw new IllegalStateException("当前状态[" + getStatusName() + "]不允许完成操作");
    }

    default void cancel(Order order) {
        throw new IllegalStateException("当前状态[" + getStatusName() + "]不允许取消操作");
    }

    String getStatusName();
}
```

- [ ] **Step 4: Create `Order.java` (entity)**

```java
package com.ordersys.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.ordersys.order.state.OrderState;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 状态模式 — 当前状态对象（不持久化，@TableField(exist=false)）。
     * 调用 setState() 会同步更新 status 字符串供数据库存储。
     */
    @TableField(exist = false)
    private OrderState state;

    public void setState(OrderState state) {
        this.state = state;
        this.status = state.getStatusName();
    }
}
```

- [ ] **Step 5: Create all six state classes**

```java
// PendingPaymentState.java
package com.ordersys.order.state;

import com.ordersys.order.entity.Order;

/** 状态模式 — 待支付：允许 pay() 和 cancel() */
public class PendingPaymentState implements OrderState {
    @Override public void pay(Order order)    { order.setState(new PaidState()); }
    @Override public void cancel(Order order) { order.setState(new CancelledState()); }
    @Override public String getStatusName()   { return "PENDING_PAYMENT"; }
}
```

```java
// PaidState.java
package com.ordersys.order.state;

import com.ordersys.order.entity.Order;

/** 状态模式 — 已支付/待接单：允许 accept() 和 cancel() */
public class PaidState implements OrderState {
    @Override public void accept(Order order) { order.setState(new PreparingState()); }
    @Override public void cancel(Order order) { order.setState(new CancelledState()); }
    @Override public String getStatusName()   { return "PAID"; }
}
```

```java
// PreparingState.java
package com.ordersys.order.state;

import com.ordersys.order.entity.Order;

/** 状态模式 — 制作中：允许 startDelivery() 和 cancel() */
public class PreparingState implements OrderState {
    @Override public void startDelivery(Order order) { order.setState(new DeliveringState()); }
    @Override public void cancel(Order order)        { order.setState(new CancelledState()); }
    @Override public String getStatusName()          { return "PREPARING"; }
}
```

```java
// DeliveringState.java
package com.ordersys.order.state;

import com.ordersys.order.entity.Order;

/** 状态模式 — 配送中：允许 complete() 和 cancel() */
public class DeliveringState implements OrderState {
    @Override public void complete(Order order) { order.setState(new CompletedState()); }
    @Override public void cancel(Order order)   { order.setState(new CancelledState()); }
    @Override public String getStatusName()     { return "DELIVERING"; }
}
```

```java
// CompletedState.java
package com.ordersys.order.state;

/** 状态模式 — 已完成：终态，所有操作均继承 default 抛出 IllegalStateException */
public class CompletedState implements OrderState {
    @Override public String getStatusName() { return "COMPLETED"; }
}
```

```java
// CancelledState.java
package com.ordersys.order.state;

/** 状态模式 — 已取消：终态，所有操作均继承 default 抛出 IllegalStateException */
public class CancelledState implements OrderState {
    @Override public String getStatusName() { return "CANCELLED"; }
}
```

- [ ] **Step 6: Run the test**

```bash
cd backend && mvn test -Dtest=OrderStateTest -q 2>&1 | tail -5
# Expected: Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
```

- [ ] **Step 7: Commit**

```bash
git add backend/src/
git commit -m "feat: add State pattern for order lifecycle (6 states, no if-else in Order)"
```

---

### Task 8: Order Module — Entity, Mapper, Service, Controller

**Files:**
- Create: `backend/src/main/java/com/ordersys/order/entity/OrderItem.java`
- Create: `backend/src/main/java/com/ordersys/order/mapper/OrderMapper.java`
- Create: `backend/src/main/java/com/ordersys/order/mapper/OrderItemMapper.java`
- Create: `backend/src/main/java/com/ordersys/order/service/OrderService.java`
- Create: `backend/src/main/java/com/ordersys/order/controller/OrderController.java`
- Test: `backend/src/test/java/com/ordersys/order/OrderServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
// backend/src/test/java/com/ordersys/order/OrderServiceTest.java
package com.ordersys.order;

import com.ordersys.order.builder.*;
import com.ordersys.order.entity.Order;
import com.ordersys.order.entity.OrderItem;
import com.ordersys.order.mapper.OrderItemMapper;
import com.ordersys.order.mapper.OrderMapper;
import com.ordersys.order.service.OrderService;
import com.ordersys.order.state.PendingPaymentState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrder_setsStatusToPendingPaymentAndCalculatesTotal() {
        List<CustomDish> dishes = List.of(
            new CustomDishBuilder(1L, "红烧肉盖饭", 28.00).quantity(2).size(Size.LARGE).addExtra("加辣").build(),
            new CustomDishBuilder(3L, "珍珠奶茶",   12.00).size(Size.SMALL).build()
        );

        when(orderMapper.insert(any(Order.class))).thenReturn(1);
        when(orderItemMapper.insert(any(OrderItem.class))).thenReturn(1);

        Order order = orderService.createOrder(1L, dishes, "测试备注");

        assertThat(order.getStatus()).isEqualTo("PENDING_PAYMENT");
        assertThat(order.getState()).isInstanceOf(PendingPaymentState.class);
        // 2 × 28 + 1 × 12 = 68
        assertThat(order.getTotalAmount().doubleValue()).isEqualTo(68.00);
        verify(orderItemMapper, times(2)).insert(any(OrderItem.class));
    }
}
```

- [ ] **Step 2: Run test to confirm it fails**

```bash
cd backend && mvn test -Dtest=OrderServiceTest -q 2>&1 | tail -5
# Expected: FAIL — compilation error, classes not found
```

- [ ] **Step 3: Create `OrderItem.java`**

```java
package com.ordersys.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("order_item")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long dishId;
    private String dishName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String size;
    private String extras;
}
```

- [ ] **Step 4: Create `OrderMapper.java` and `OrderItemMapper.java`**

```java
// OrderMapper.java
package com.ordersys.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordersys.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
```

```java
// OrderItemMapper.java
package com.ordersys.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ordersys.order.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}
```

- [ ] **Step 5: Create `OrderService.java`**

```java
package com.ordersys.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ordersys.order.builder.CustomDish;
import com.ordersys.order.entity.Order;
import com.ordersys.order.entity.OrderItem;
import com.ordersys.order.mapper.OrderItemMapper;
import com.ordersys.order.mapper.OrderMapper;
import com.ordersys.order.state.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    /**
     * 创建订单，整合建造者模式（CustomDish 列表）和状态模式（初始状态 PendingPayment）。
     */
    @Transactional
    public Order createOrder(Long userId, List<CustomDish> dishes, String remark) {
        BigDecimal total = dishes.stream()
            .map(d -> d.getUnitPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setRemark(remark);
        order.setState(new PendingPaymentState());
        orderMapper.insert(order);

        for (CustomDish dish : dishes) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setDishId(dish.getDishId());
            item.setDishName(dish.getDishName());
            item.setQuantity(dish.getQuantity());
            item.setUnitPrice(dish.getUnitPrice());
            item.setSize(dish.getSize().name());
            item.setExtras(dish.extrasToJson());
            orderItemMapper.insert(item);
        }
        return order;
    }

    /** 从数据库加载订单并恢复状态对象（状态模式要求） */
    public Order getOrderWithState(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new IllegalArgumentException("订单不存在: " + orderId);
        order.setState(resolveState(order.getStatus()));
        return order;
    }

    private OrderState resolveState(String status) {
        return switch (status) {
            case "PENDING_PAYMENT" -> new PendingPaymentState();
            case "PAID"            -> new PaidState();
            case "PREPARING"       -> new PreparingState();
            case "DELIVERING"      -> new DeliveringState();
            case "COMPLETED"       -> new CompletedState();
            case "CANCELLED"       -> new CancelledState();
            default -> throw new IllegalArgumentException("未知状态: " + status);
        };
    }

    /** 商家操作：接单 / 开始配送 / 完成 / 取消，由状态对象处理迁移 */
    @Transactional
    public Order transition(Long orderId, String action) {
        Order order = getOrderWithState(orderId);
        switch (action) {
            case "accept"   -> order.getState().accept(order);
            case "deliver"  -> order.getState().startDelivery(order);
            case "complete" -> order.getState().complete(order);
            case "cancel"   -> order.getState().cancel(order);
            default -> throw new IllegalArgumentException("未知操作: " + action);
        }
        orderMapper.updateById(order);
        return order;
    }

    /** 支付成功后由 PaymentService 调用，触发 PendingPayment → Paid 的状态迁移 */
    @Transactional
    public Order markPaid(Long orderId) {
        Order order = getOrderWithState(orderId);
        order.getState().pay(order);
        orderMapper.updateById(order);
        return order;
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
    }
}
```

- [ ] **Step 6: Create `OrderController.java`**

```java
package com.ordersys.order.controller;

import com.ordersys.common.Result;
import com.ordersys.order.builder.*;
import com.ordersys.order.entity.Order;
import com.ordersys.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * 下单接口。
     * 请求体示例：
     * {
     *   "remark": "不要辣",
     *   "items": [
     *     { "dishId":1, "dishName":"红烧肉盖饭", "price":28.0,
     *       "quantity":1, "size":"LARGE", "extras":["加辣","加蛋"] }
     *   ]
     * }
     */
    @PostMapping
    public Result<Order> createOrder(@RequestParam Long userId,
                                     @RequestBody Map<String, Object> body) {
        String remark = (String) body.getOrDefault("remark", "");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");

        List<CustomDish> dishes = items.stream().map(item -> {
            Long dishId  = Long.parseLong(item.get("dishId").toString());
            String name  = (String) item.get("dishName");
            double price = Double.parseDouble(item.get("price").toString());
            int qty      = Integer.parseInt(item.getOrDefault("quantity", 1).toString());
            Size size    = Size.valueOf(((String) item.getOrDefault("size", "LARGE")).toUpperCase());

            CustomDishBuilder builder = new CustomDishBuilder(dishId, name, price)
                .quantity(qty).size(size);

            @SuppressWarnings("unchecked")
            List<String> extras = (List<String>) item.getOrDefault("extras", List.of());
            extras.forEach(builder::addExtra);

            if (item.containsKey("note")) builder.note((String) item.get("note"));
            return builder.build();
        }).toList();

        return Result.success(orderService.createOrder(userId, dishes, remark));
    }

    @GetMapping("/{id}")
    public Result<Order> getOrder(@PathVariable Long id) {
        return Result.success(orderService.getOrderWithState(id));
    }

    @PutMapping("/{id}/accept")
    public Result<Order> accept(@PathVariable Long id) {
        return Result.success(orderService.transition(id, "accept"));
    }

    @PutMapping("/{id}/deliver")
    public Result<Order> deliver(@PathVariable Long id) {
        return Result.success(orderService.transition(id, "deliver"));
    }

    @PutMapping("/{id}/complete")
    public Result<Order> complete(@PathVariable Long id) {
        return Result.success(orderService.transition(id, "complete"));
    }

    @PutMapping("/{id}/cancel")
    public Result<Order> cancel(@PathVariable Long id) {
        return Result.success(orderService.transition(id, "cancel"));
    }
}
```

- [ ] **Step 7: Run the test**

```bash
cd backend && mvn test -Dtest=OrderServiceTest -q 2>&1 | tail -5
# Expected: Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

- [ ] **Step 8: Commit**

```bash
git add backend/src/
git commit -m "feat: add order module integrating Builder pattern and State pattern"
```

---

### Task 9: Payment Service + Wire to Order

**Files:**
- Create: `backend/src/main/java/com/ordersys/payment/service/PaymentService.java`
- Create: `backend/src/main/java/com/ordersys/payment/controller/PaymentController.java`

- [ ] **Step 1: Create `PaymentService.java`**

```java
package com.ordersys.payment.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ordersys.order.service.OrderService;
import com.ordersys.payment.entity.Payment;
import com.ordersys.payment.mapper.PaymentMapper;
import com.ordersys.payment.strategy.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService extends ServiceImpl<PaymentMapper, Payment> {

    private final OrderService orderService;

    /**
     * 执行支付：策略模式选择支付方式 → Mock调用 → 持久化结果 → 触发订单状态流转。
     */
    @Transactional
    public Payment pay(Long orderId, double amount, String method) {
        PaymentStrategy strategy = switch (method.toUpperCase()) {
            case "WECHAT" -> new WechatPayStrategy();
            case "ALIPAY" -> new AlipayStrategy();
            default -> throw new IllegalArgumentException("不支持的支付方式: " + method);
        };

        PaymentContext ctx = new PaymentContext(strategy);
        PaymentResult result = ctx.pay(amount, orderId);

        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setAmount(BigDecimal.valueOf(amount));
        payment.setMethod(method.toUpperCase());
        payment.setStatus(result.isSuccess() ? "SUCCESS" : "FAILED");
        payment.setTransactionId(result.getTransactionId());
        this.baseMapper.insert(payment);

        // 支付成功后触发订单状态流转（状态模式：PendingPayment → Paid）
        if (result.isSuccess()) {
            orderService.markPaid(orderId);
        }
        return payment;
    }
}
```

- [ ] **Step 2: Create `PaymentController.java`**

```java
package com.ordersys.payment.controller;

import com.ordersys.common.Result;
import com.ordersys.payment.entity.Payment;
import com.ordersys.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 发起支付。
     * 请求体：{"amount": 28.0, "method": "WECHAT"}
     * 支付成功后订单状态自动由 PENDING_PAYMENT 流转为 PAID（状态模式）。
     */
    @PostMapping("/{orderId}")
    public Result<Payment> pay(@PathVariable Long orderId,
                               @RequestBody Map<String, Object> body) {
        double amount = Double.parseDouble(body.get("amount").toString());
        String method = (String) body.get("method");
        return Result.success(paymentService.pay(orderId, amount, method));
    }
}
```

- [ ] **Step 3: Run all tests**

```bash
cd backend && mvn test -q 2>&1 | tail -10
# Expected: BUILD SUCCESS
# Tests run: 14 (or similar), Failures: 0, Errors: 0
```

- [ ] **Step 4: Commit**

```bash
git add backend/src/
git commit -m "feat: add payment service wired to order state transition"
```

---

### Task 10: End-to-End Smoke Test

- [ ] **Step 1: Ensure MySQL is running**

```bash
docker compose -f docker/docker-compose.yml ps
# Expected: ordersys-mysql   Up (healthy)
# If not running: docker compose -f docker/docker-compose.yml up -d && sleep 15
```

- [ ] **Step 2: Start the backend**

```bash
cd backend && mvn spring-boot:run > /tmp/ordersys.log 2>&1 &
sleep 15
grep -q "Started OrdersysApplication" /tmp/ordersys.log && echo "OK" || cat /tmp/ordersys.log
# Expected: OK
```

- [ ] **Step 3: Create a dish (Factory pattern)**

```bash
curl -s -X POST http://localhost:8080/api/dish \
  -H "Content-Type: application/json" \
  -d '{"type":"MAIN_DISH","name":"测试盖饭","description":"香喷喷","price":25.0}' | python3 -m json.tool
# Expected: {"code":200,"message":"success","data":{"id":7,"type":"MAIN_DISH","name":"测试盖饭",...}}
```

- [ ] **Step 4: Place an order (Builder pattern)**

```bash
curl -s -X POST "http://localhost:8080/api/order?userId=1" \
  -H "Content-Type: application/json" \
  -d '{
    "remark": "不要辣",
    "items": [
      {"dishId":1,"dishName":"红烧肉盖饭","price":28.0,"quantity":1,"size":"LARGE","extras":["加辣","加蛋"]},
      {"dishId":3,"dishName":"珍珠奶茶","price":12.0,"quantity":1,"size":"SMALL","extras":["去冰"]}
    ]
  }' | python3 -m json.tool
# Expected: status="PENDING_PAYMENT", totalAmount=40.0
# Note the "id" field as ORDER_ID for next steps
```

- [ ] **Step 5: Pay the order (Strategy + State)**

```bash
# Replace 1 with the actual ORDER_ID from Step 4
curl -s -X POST http://localhost:8080/api/payment/1 \
  -H "Content-Type: application/json" \
  -d '{"amount":40.0,"method":"WECHAT"}' | python3 -m json.tool
# Expected: {"data":{"status":"SUCCESS","transactionId":"WECHAT_...","method":"WECHAT"}}
```

- [ ] **Step 6: Verify order status changed to PAID**

```bash
curl -s http://localhost:8080/api/order/1 | python3 -m json.tool
# Expected: "status":"PAID"
```

- [ ] **Step 7: Walk through remaining states**

```bash
curl -s -X PUT http://localhost:8080/api/order/1/accept   | python3 -m json.tool
# Expected: "status":"PREPARING"

curl -s -X PUT http://localhost:8080/api/order/1/deliver  | python3 -m json.tool
# Expected: "status":"DELIVERING"

curl -s -X PUT http://localhost:8080/api/order/1/complete | python3 -m json.tool
# Expected: "status":"COMPLETED"
```

- [ ] **Step 8: Verify completed order rejects cancel**

```bash
curl -s -X PUT http://localhost:8080/api/order/1/cancel | python3 -m json.tool
# Expected: {"code":500,"message":"当前状态[COMPLETED]不允许取消操作",...}
```

- [ ] **Step 9: Stop backend and commit**

```bash
kill $(lsof -ti:8080) 2>/dev/null || true
git add .
git commit -m "test: smoke test verified all four design patterns end-to-end"
```

---

## Summary

| Task | 内容 | 设计模式 |
|---|---|---|
| 1 | Docker MySQL + init schema + seed data | — |
| 2 | Spring Boot scaffold + common utilities | — |
| 3 | User module | — |
| 4 | Product module | **Factory** |
| 5 | CustomDish 建造者 | **Builder** |
| 6 | Payment strategy + entity + mapper | **Strategy** |
| 7 | Order state machine + Order entity | **State** |
| 8 | Order service + controller (整合 Builder+State) | Builder + State |
| 9 | PaymentService 串联 Strategy + State | Strategy + State |
| 10 | End-to-end smoke test | All |
