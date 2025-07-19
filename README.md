# 外贸代理开票金额计算器

[![Java](https://img.shields.io/badge/Java-22-orange.svg)](https://openjdk.org/projects/jdk/22/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey.svg)]()

一个基于 Java Swing 开发的专业外贸代理开票金额计算工具，帮助外贸企业精确计算在代理出口业务中的最优开票金额。

## 📋 目录

- [功能特性](#-功能特性)
- [业务场景](#-业务场景)
- [系统要求](#-系统要求)
- [快速开始](#-快速开始)
- [使用指南](#-使用指南)
- [计算原理](#-计算原理)
- [技术架构](#-技术架构)
- [开发指南](#-开发指南)
- [常见问题](#-常见问题)
- [许可证](#-许可证)

## 🚀 功能特性

### 核心功能
- **精确计算**：基于 BigDecimal 高精度数值计算，确保金额计算绝对准确
- **双显示模式**：简洁模式（仅显示开票金额）和详细模式（完整财务分析）
- **多货币支持**：支持美元、欧元、英镑、日元、人民币、俄罗斯卢布等6种主要货币
- **智能格式化**：数字字段自动格式化显示，支持千位分隔符

### 业务功能
- **双分成模式**：
  - 相对分配率：按退税金额百分比分成
  - 绝对分配率：按固定退税率分成
- **完整财务分析**：利润计算、毛利率分析、资金流验证
- **参数验证**：全面的输入验证和错误提示
- **结果导出**：详细的计算过程和公式展示

### 用户体验
- **直观界面**：清晰的参数分组和结果展示
- **智能隐藏**：简洁模式下自动隐藏敏感财务信息
- **实时计算**：即时显示计算结果和验证信息
- **数据保持**：模式切换时保持用户输入数据

## 🌟 业务场景

本工具专门为以下外贸业务场景设计：

### 典型应用场景
某外贸公司从国内采购100万人民币的汽车配件，与海外客户签订17万美元的销售合同。由于客户所在国实行外汇管制，需要通过有外汇额度的代理公司出口。

**计算目标**：确定给代理公司的最优开票金额，使得：
- 委托方收到海外客户的全部美元款项
- 合理分享出口退税收益
- 代理公司获得约定比例的退税分成

### 解决的问题
- ✅ 复杂的退税分成计算
- ✅ 多种货币和汇率处理
- ✅ 不同分成模式的快速切换
- ✅ 完整的财务影响分析
- ✅ 合规性参数验证

## 💻 系统要求

### 运行环境
- **Java**：JDK 22 或更高版本
- **操作系统**：Windows 10+、macOS 10.14+、Linux（支持GUI）
- **内存**：最小 512MB RAM
- **存储**：至少 50MB 可用空间

### 开发环境
- **Java**：JDK 22
- **构建工具**：Maven 3.9.6+
- **IDE**：IntelliJ IDEA

## 🚀 快速开始

### 1. 克隆项目
```bash
git clone https://github.com/your-username/export-agent-invoice-calculator.git
cd export-agent-invoice-calculator
```

### 2. 编译项目
```bash
mvn clean compile
```

### 3. 运行应用
```bash
mvn exec:java -Dexec.mainClass="com.gwill.foreign_trade.ExportAgentInvoiceCalculator"
```

### 4. 或者生成可执行JAR
```bash
mvn clean package
# 方式1：：运行可执行JAR（推荐）
java -jar target/export-agent-invoice-calculator-1.0-executable.jar

# 方式2：运行Assembly生成的JAR
java -jar target/export-agent-invoice-calculator-1.0-jar-with-dependencies.jar

# 方式3：运行标准JAR（需要classpath）
java -cp target/export-agent-invoice-calculator-1.0.jar com.gwill.foreign_trade.ExportAgentInvoiceCalculator
```

## 📖 使用指南

### 界面概览
```
┌─────────────────────────────────────────────────┐
│ 显示模式: ○ 简洁模式  ● 详细模式                    │
├─────────────────────────────────────────────────┤
│ 基础参数输入                                      │
│ ┌ 采购金额: [1,000,000.00] 元                    │
│ ┌ 销售金额: [170,000.00] [美元 ▼]                │
│ ┌ 汇率:     [7.1000]                            │
│ ┌ 退税率:   [13] %                               │
├─────────────────────────────────────────────────┤
│ 代理分成方式                                      │
│ ● 相对分配率 [50] %                              │
│ ○ 绝对分配率 [6.5] %                             │
├─────────────────────────────────────────────────┤
│ [计算开票金额] [清空重置]                          │
├─────────────────────────────────────────────────┤
│ 详细计算结果                                      │
│ ★ 开票金额: 1,290,909.09 元                     │
│ 海外客户付款: 1,207,000.00 元                    │
│ ...                                            │
└─────────────────────────────────────────────────┘
```

### 操作步骤

#### 基础设置
1. **选择显示模式**
   - 简洁模式：仅显示开票金额（适合与合作方讨论）
   - 详细模式：显示完整财务分析（内部决策使用）

2. **输入基础参数**
   - 采购金额：商品的采购成本（人民币）
   - 销售金额：与海外客户的合同金额
   - 货币选择：支持6种主要货币
   - 汇率：当前有效汇率（精确到4位小数）
   - 退税率：产品的出口退税率

#### 分成方式选择
**相对分配率模式**
- 适用场景：按退税金额的固定比例分成
- 示例：输入50表示代理获得退税金额的50%

**绝对分配率模式**  
- 适用场景：代理获得固定的退税率收益
- 示例：输入6.5表示代理获得开票金额×6.5%的固定收益

#### 结果解读
**简洁模式结果**
```
应开票金额: 1,290,909.09 元
```

**详细模式结果**
```
========== 计算结果 ==========

【输入参数】
采购金额: 1,000,000.00 元
销售金额: 170,000.00 USD
当前汇率: 7.1000
退税率: 13.00%
代理退税分成方式: 相对分配率 50.00%

【核心计算结果】
★ 开票金额: 1,280,666.67 元
海外客户付款（折合人民币）: 1,207,000.00 元
开票溢价: 73,666.67 元

【退税相关】
退税总金额: 147,333.33 元
代理公司利润: 73,666.67 元
您分享的退税: 73,666.66 元

【盈利分析】
您的总收入: 1,207,000.00 元
您的利润额: 290,909.09 元
您的毛成本利润率: 29.09%

【资金流验证】
代理公司收入: 1,207,000.00 + 147,333.33 = 1,354,333.33 元
代理公司支出: 1,280,666.66 元
代理公司净利润: 73,666.67 元
验证结果: 73,666.67 ≈ 73,666.67 ✓

【计算公式】
invoiceAmount = (salesAmount × exchangeRate) × (1 + taxRefundRate) ÷ (1 + taxRefundRate × agentRatio)
invoiceAmountInRMB = (170,000.00 × 7.1000) × (1 + 13%) ÷ [1 + 13.00% × 50.00%)
invoiceAmountInRMB = 1,207,000.00 × 1.13 ÷ 1.065 = 1,280,666.67 元
```

## 🧮 计算原理

### 核心公式
```
开票金额 = (销售金额 × 汇率) × (1 + 退税率) ÷ (1 + 退税率 × 代理分成比例)
```

### 数学推导
设：
- `S` = 销售金额（外币）
- `E` = 汇率（人民币/外币）
- `R` = 退税率
- `A` = 代理分成比例
- `X` = 开票金额（人民币）

**约束条件：**
1. 退税金额 = X × R
2. 代理利润 = 退税金额 × A
3. 资金平衡：S × E + 退税金额 = X + 代理利润

**求解过程：**
```
S × E + X × R/(1+R) = X + X × R/(1+R) × A

S × E = X + X × R × A/(1+R) - X × R/(1+R)

S × E = X × [1 + R × A/(1+R) - R/(1+R)]

S × E = X × [1 + R/(1+R) × (A - 1)]

S × E = X × [1 - R × (1-A)/(1+R)]
```

因此：
```
X = (S × E) ÷ [1 - R × (1-A)/(1+R)]
```

简化公式
分母可以进一步简化：
```
1 - R × (1-A)/(1+R) = [(1+R) - R × (1-A)]/(1+R)
                    = [1 + R - R + R × A]/(1+R)
                    = [1 + R × A]/(1+R)
```

所以最终的开票金额计算公式为：
```
X = (S × E) × (1+R) ÷ (1 + R × A)
```

### 分成模式转换
**绝对分配率转相对分配率：**
```
相对分成比例 = 绝对分配率 ÷ 退税率
```

## 🏗️ 技术架构

### 项目结构
```
export-agent-invoice-calculator/
├── src/main/java/com/gwill/foreign_trade/
│   └── ExportAgentInvoiceCalculator.java    # 主程序类
├── pom.xml                                   # Maven配置
├── README.md                                 # 项目文档
└── LICENSE                                   # 许可证
```

### 核心技术栈
- **界面框架**：Java Swing + GridBagLayout
- **数值计算**：BigDecimal（高精度计算）
- **构建工具**：Maven 3.9.6+
- **JDK版本**：OpenJDK 22

### 设计模式
- **MVC模式**：界面与业务逻辑分离
- **策略模式**：支持不同的分成计算策略
- **观察者模式**：界面状态同步更新

### 关键特性
```java
// 高精度计算设置
private static final int CALCULATION_PRECISION = 10;
private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

// 数字格式化
private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
private DecimalFormat exchangeRateFormat = new DecimalFormat("#,##0.0000");

// BigDecimal计算示例
BigDecimal invoiceAmount = clientPayment.divide(denominator, 
    CALCULATION_PRECISION, ROUNDING_MODE);
```

## 🛠️ 开发指南

### 开发环境搭建
1. **安装JDK 22**
   ```bash
   # 使用SDKMAN安装
   sdk install java 22.0.1-open
   sdk use java 22.0.1-open
   ```

2. **克隆并导入项目**
   ```bash
   git clone <repository-url>
   cd export-agent-invoice-calculator
   ```

3. **IDE配置**
   - IntelliJ IDEA：File → Open → 选择项目目录
   - Eclipse：File → Import → Existing Maven Projects

### 构建命令
```bash
# 清理编译
mvn clean compile

# 运行测试
mvn test

# 打包应用
mvn clean package

# 运行应用
mvn exec:java -Dexec.mainClass="com.gwill.foreign_trade.ExportAgentInvoiceCalculator"
```

### 代码结构说明
```java
public class ExportAgentInvoiceCalculator extends JFrame {
    // 常量定义
    private static final int CALCULATION_PRECISION = 10;
    
    // GUI组件
    private JTextField purchaseAmountField;
    private JTextField salesAmountField;
    // ...
    
    // 核心方法
    private void calculateInvoiceAmount()           // 主计算逻辑
    private void validateInputs()                   // 输入验证
    private void displayDetailedResults()           // 结果显示
    private BigDecimal convertAbsoluteRateToRelativeRatio()  // 分成转换
}
```

### 扩展开发
**添加新货币支持**
```java
private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "JPY", "CNY", "RUB", "NEW_CURRENCY"};
private static final String[] CURRENCY_NAMES = {
    "美元 (USD)", "欧元 (EUR)", "英镑 (GBP)",
    "日元 (JPY)", "人民币 (CNY)", "俄罗斯卢布 (RUB)", "新货币 (NEW)"
};
```

**自定义计算精度**
```java
private static final int CALCULATION_PRECISION = 12;  // 提高精度
```

## ❓ 常见问题

### Q1: 为什么计算结果显示"计算无解"？
**原因**：参数组合导致公式分母为零或接近零
**解决**：
- 检查退税率是否过高
- 调整代理分成比例
- 确保绝对分配率不超过退税率

### Q2: 数字格式显示异常怎么办？
**原因**：输入了非数字字符或格式不正确
**解决**：
- 确保只输入数字和小数点
- 千位分隔符会自动处理
- 失去焦点时自动格式化

### Q3: 简洁模式下为什么没有采购金额输入？
**设计**：简洁模式隐藏敏感财务信息，适合与合作方讨论
**切换**：选择详细模式可显示完整信息

### Q4: 相对分配率和绝对分配率有什么区别？
**相对分配率**：按退税金额的百分比分成，比例固定
**绝对分配率**：按开票金额的固定百分比分成，金额固定

### Q5: 如何验证计算结果的准确性？
**方法**：
1. 查看详细模式下的资金流验证
2. 手动验证：代理收入 - 代理支出 = 代理利润
3. 检查计算公式展示部分

### Q6: 支持的汇率精度是多少？
**汇率精度**：支持4位小数（如7.1234）
**计算精度**：内部使用10位小数精度计算
**显示精度**：金额显示2位小数

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

### MIT License 摘要
```
Copyright (c) 2024 Gwill

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software...
```

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📞 联系方式

- **项目维护者**：William YE of G-WILL Team
- **邮箱**：william@tensly.com
- **问题反馈**：[GitHub Issues](https://github.com/your-username/export-agent-invoice-calculator/issues)

---

<div align="center">

**⭐ 如果这个项目对您有帮助，请给我们一个Star！**

Made with ❤️ by William YE of G-WILL Team

</div>