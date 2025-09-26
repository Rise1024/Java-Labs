# Java 设计模式学习项目

基于 JDK 17 的 Java 设计模式实战项目，展示 23 种经典设计模式的实现和最佳实践。

## 🎯 项目简介

本项目提供了 Java 设计模式的全面学习案例，包括：

- **创建型模式**: 单例、工厂方法、抽象工厂、建造者、原型
- **结构型模式**: 适配器、装饰器、代理、外观、桥接、组合、享元
- **行为型模式**: 观察者、策略、命令、模板方法、责任链、状态、访问者、中介者、备忘录、迭代器、解释器

## 🛠️ 技术栈

- **JDK**: 17 (使用最新语言特性)
- **构建工具**: Maven 3.8+
- **测试框架**: JUnit 5.10.2 + Mockito + AssertJ
- **日志框架**: SLF4J + Logback
- **代码生成**: Lombok 1.18.32
- **工具库**: Apache Commons Lang3, Jackson

## 📁 项目结构

```
src/main/java/com/javalaabs/patterns/
├── creational/                 # 创建型模式
│   ├── singleton/              # 单例模式
│   ├── factory/                # 工厂模式
│   ├── builder/                # 建造者模式
│   ├── prototype/              # 原型模式
│   └── abstractfactory/        # 抽象工厂模式
├── structural/                 # 结构型模式
│   ├── adapter/                # 适配器模式
│   ├── decorator/              # 装饰器模式
│   ├── proxy/                  # 代理模式
│   ├── facade/                 # 外观模式
│   ├── bridge/                 # 桥接模式
│   ├── composite/              # 组合模式
│   └── flyweight/              # 享元模式
├── behavioral/                 # 行为型模式
│   ├── observer/               # 观察者模式
│   ├── strategy/               # 策略模式
│   ├── command/                # 命令模式
│   ├── template/               # 模板方法模式
│   ├── chain/                  # 责任链模式
│   ├── state/                  # 状态模式
│   ├── visitor/                # 访问者模式
│   ├── mediator/               # 中介者模式
│   ├── memento/                # 备忘录模式
│   ├── iterator/               # 迭代器模式
│   └── interpreter/            # 解释器模式
├── utils/                      # 工具类
│   └── PatternUtils.java       # 通用工具
└── DesignPatternDemo.java      # 综合演示程序

src/test/java/com/javalaabs/patterns/
├── creational/                 # 创建型模式测试
├── structural/                 # 结构型模式测试
└── behavioral/                 # 行为型模式测试
```

## 🚀 快速开始

### 编译和运行

```bash
# 编译项目
mvn clean compile

# 运行演示程序
mvn exec:java

# 运行单元测试
mvn test
```

## 📖 设计模式分类

### 创建型模式 (Creational Patterns)
处理对象创建机制，试图创建适合情况的对象

### 结构型模式 (Structural Patterns)
处理对象组合，描述如何将类或对象结合在一起形成更大的结构

### 行为型模式 (Behavioral Patterns)
关注对象之间的通信，描述对象之间怎样交互和怎样分配职责

## 🎓 学习建议

1. **循序渐进**: 建议按创建型 → 结构型 → 行为型的顺序学习
2. **理论结合实践**: 每个模式都有完整的代码示例和单元测试
3. **场景理解**: 重点理解每种模式的适用场景和解决的问题
4. **对比学习**: 理解相似模式之间的区别和联系

## 🔗 相关资源

- [设计模式技术博客系列](../blog/设计模式/)
- [Java设计模式最佳实践](https://dongsheng.online)

---

**注意**: 本项目采用 JDK 17 特性，请确保你的开发环境支持 Java 17 或更高版本。
