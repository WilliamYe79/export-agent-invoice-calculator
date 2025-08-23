# 外贸代理开票金额计算器

[![Java](https://img.shields.io/badge/Java-22-orange.svg)](https://openjdk.org/projects/jdk/22/)
[![Maven](https://img.shields.io/badge/Maven-3.9+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey.svg)]()

一个专业的 Java Swing 外贸代理开票金额计算系统，采用单例模式设计，提供双计算器架构，帮助外贸企业精确计算代理出口业务中的最优开票金额和复杂的多工厂分配方案。

## 📋 目录

- [功能特性](#-功能特性)
- [系统架构](#-系统架构)
- [业务场景](#-业务场景)
- [系统要求](#-系统要求)
- [快速开始](#-快速开始)
- [使用指南](#-使用指南)
- [计算原理](#-计算原理)
- [开发指南](#-开发指南)
- [常见问题](#-常见问题)
- [许可证](#-许可证)

## 🚀 功能特性

### 双计算器系统
- **单工厂计算器**：处理单一交易的精确计算和财务分析
- **多工厂计算器**：管理复杂的多供应商分配方案和资金流转
- **无缝切换**：单例模式确保计算器间状态保持和流畅导航

### 核心计算功能
- **高精度计算**：基于 BigDecimal 的 10 位精度金融计算
- **智能分配算法**：支持固定金额和灵活分配的混合策略
- **双分成模式**：相对分配率和绝对分配率自由切换
- **实时验证**：完整的资金流验证和平衡检查

### 数据管理功能
- **CSV导入导出**：支持多工厂数据的批量处理
- **专业表格显示**：8列详细数据展示，包含对公对私退款分析
- **格式化显示**：千位分隔符、百分比、汇率等专业格式
- **数据持久化**：计算过程和结果的完整保存

### 用户体验设计
- **双显示模式**：简洁模式（对外）和详细模式（内部）
- **响应式布局**：GridBagLayout 精确控制，适配不同屏幕
- **智能表单**：实时格式化、焦点管理、输入验证
- **专业界面**：清晰的功能分区和直观的操作流程

## 🏗️ 系统架构

### 设计模式
```
单例模式 + MVC架构 + 服务层分离
┌─────────────────────────────────────────────────┐
│                用户界面层                         │
│  SingleSupplierInvoiceCalculator (Singleton)      │
│  MultiFactoryInvoiceCalculator (Singleton)     │
├─────────────────────────────────────────────────┤
│                服务层                           │
│  MultiFactoryInvoiceCalculationService         │
├─────────────────────────────────────────────────┤
│                模型层                           │
│  CalculationParams | ProductSituation          │
│  CalculationResult | FactoryAllocation         │
│  MultiProductCalculationResult                 │
└─────────────────────────────────────────────────┘
```

### 技术栈
- **界面框架**：Java Swing + GridBagLayout/BorderLayout/CardLayout
- **业务逻辑**：Record 类 + BigDecimal 高精度计算
- **数据处理**：CSV 文件处理 + UTF-8 编码
- **构建部署**：Maven + Shade Plugin + Assembly Plugin

## 🌟 业务场景

### 单工厂业务场景
某外贸公司从国内采购100万人民币汽车配件，与海外客户签订17万美元销售合同，通过代理公司出口。

**计算目标**：
- ✅ 确定最优开票金额，确保收到全部美元款项
- ✅ 合理分享出口退税收益（如退税率13%，代理分成50%）
- ✅ 完整的财务影响分析和利润率计算

### 多工厂业务场景
一个大订单涉及3家不同工厂供货，每家工厂有不同的税点、预付款、开票政策：
- 华东机械厂：球笼万向节，货值10万，预付2万，税点10%，同意开票给代理公司但不可超额
- 江苏精密制造：转向器总成，货值15万，预付3万，税点9.5%，同意开票给代理公司且可超额
- 浙江汽配公司：刹车盘，货值8万，预付1万，税点11%，不同意开票给代理公司

**系统功能**：
- 📊 CSV 批量导入工厂数据
- 🧮 智能分配算法处理复杂约束
- 💰 详细的对公对私资金流分析
- 📈 完整的计算结果导出

## 💻 系统要求

### 运行环境
- **Java**：JDK 22 或更高版本
- **操作系统**：Windows 10+、macOS 10.14+、Linux（支持GUI）
- **内存**：最小 512MB RAM（推荐 1GB）
- **存储**：至少 50MB 可用空间

### 开发环境
- **Java**：JDK 22 with Preview Features
- **构建工具**：Maven 3.9.6+
- **IDE**：IntelliJ IDEA 2024+ / Eclipse 2024+

## 🚀 快速开始

### 1. 获取应用

**方式A：直接下载（推荐）**
```bash
# 下载预编译的可执行JAR
wget https://github.com/your-username/export-agent-invoice-calculator/releases/latest/export-agent-invoice-calculator-executable.jar
java -jar export-agent-invoice-calculator-executable.jar
```

**方式B：源码编译**
```bash
git clone https://github.com/your-username/export-agent-invoice-calculator.git
cd export-agent-invoice-calculator
mvn clean package
java -jar target/export-agent-invoice-calculator-1.0-executable.jar
```

### 2. 快速体验

**单工厂计算**
1. 启动应用（默认显示单工厂计算器）
2. 输入示例数据：采购100万，销售17万美元，汇率7.1，退税率13%
3. 选择代理分成方式：相对分配率50%
4. 点击"计算开票金额"查看结果

**多工厂计算**
1. 点击"多工厂分配计算"切换界面
2. 选择 `examples/multi_factory_situations.csv` 导入示例数据
3. 设置基础参数：销售17万美元，汇率7.1，退税率13%，相对分配率50%
4. 点击"计算分配"查看详细分配结果
5. 点击"导出结果"保存计算报告

## 📖 使用指南

### 单供应商计算器界面

```
┌─────────────────────────────────────────────────────────┐
│ 显示模式: ○ 简洁模式（仅开票金额） ● 详细模式（完整分析）    │
├─────────────────────────────────────────────────────────┤
│ 基础参数输入                                              │
│ 采购金额(人民币): [1,000,000.00] 元                     │
│ 销售金额:        [170,000.00] [美元 ▼]                  │
│ 汇率(CNY/外币):  [7.1000]                              │
│ 退税率:          [13.00] %                             │
├─────────────────────────────────────────────────────────┤
│ 代理分成方式                                              │
│ ● 相对分配率（占退税金额的百分比）      [50.00] %           │
│ ○ 绝对分配率（直接输入分给代理的退税率） [6.50] %            │
│ • 相对分配率: 如输入50，表示代理获得退税金额的50%           │
│ • 绝对分配率: 如输入6.5，表示代理获得开票金额×6.5%的固定金额 │
├─────────────────────────────────────────────────────────┤
│ [计算开票金额] [清空重置] [多工厂分配计算]                 │
├─────────────────────────────────────────────────────────┤
│ 详细计算结果                                              │
│ ========== 计算结果 ==========                         │
│ 【输入参数】                                              │
│ ★ 开票金额: 1,280,666.67 元                            │
│ 您的总收入: 1,280,666.66 元                            │
│ 您的利润额: 280,666.66 元                              │
│ 您的毛成本利润率: 28.07%                                │
│ ...                                                   │
└─────────────────────────────────────────────────────────┘
```

### 多工厂计算器界面
#### 也适用于处理：多工厂+委托方 混合开票的情况

```
┌─────────────────────────────────────────────────────────┐
│ 基础参数设置                                              │
│ 销售金额: [170,000.00] [美元(USD) ▼]                    │
│ 汇率(CNY/外币): [7.1000]                               │
├─────────────────────────────────────────────────────────┤
│ 代理分成方式                                              │
│ 相对分配率(占退税金额的百分比): [50.00] %                  │
│ • 如输入50，表示代理获得退税金额的50%                      │
├─────────────────────────────────────────────────────────┤
│ 工厂数据管理                                              │
│ CSV文件: [选择的文件路径.csv] [选择文件] [加载数据]         │
│ ┌───────────────────────────────────────────────────────┐ │
│ │工厂名称│产品名称│退税率│实际货值(元)│开票金额(元)│      │ │
│ │华东机械│球笼万向│13.00%│100,000     │128,000     │      │ │
│ │江苏精密│转向器  │13.00%│150,000     │195,200     │      │ │
│ │        │        │      │退税金额(元)│代理利润(元)│      │ │
│ │        │        │      │14,700      │8,000       │      │ │
│ │        │        │      │22,400      │12,300      │      │ │
│ │        │        │      │工厂对公应退│工厂对私应退│      │ │
│ │        │        │      │金额(元)    │金额(元)    │      │ │
│ │        │        │      │20,000      │5,200       │      │ │
│ │        │        │      │30,000      │15,500      │      │ │
│ └───────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────┤
│ 操作                                                     │
│ [计算分配] [清空数据] [导出结果] [返回单供应商计算]         │
├─────────────────────────────────────────────────────────┤
│ 计算结果                                  │ 资金流转详情      │
│ ========== 分配计算结果 ==========     │ ============   │
│ 【基础参数】                            │ 【总体资金概况】   │
│ 销售金额: 170,000.00 USD              │ 所有工厂总货值:   │
│ 汇率: 7.1000                          │ 330,000.00 元    │
│ 客户付款(折人民币): 1,207,000.00 元     │ 已预付定金总额:   │
│ 代理退税相对分配率: 50.00%             │ 50,000.00 元     │
│ 【总体计算】                            │ 剩余应付货款:     │
│ 开票给代理的工厂的实际采购总货值:        │ 280,000.00 元    │
│ 250,000.00 元                        │ 【资金流转步骤】   │
│ 总开票金额: 1,280,666.67 元           │ 1.您已向各工厂    │
│ 总退税金额: 147,333.33 元             │   支付定金总计    │
│ 代理分成金额: 73,666.67 元            │ 2.您需向代理公司  │
│ 【工厂分配明细】                        │   个人账户汇押金  │
│ 华东机械厂 - 球笼万向节:               │ 3.代理公司向各    │
│   实际货值: 100,000.00 元             │   工厂支付剩余    │
│   开票金额: 128,000.00 元             │   货款            │
│   开票溢价: 28,000.00 元              │ 4.工厂向代理公司  │
│   代理利润: 8,333.33 元               │   开具发票        │
│ ...                                   │ ...              │
└─────────────────────────────────────────────────────────┘
```

### CSV 文件格式

#### Excel模板使用说明

为了方便用户填写数据，我们提供了Excel模板文件 `examples/multi_factory_situations_template.xlsx`：

1. **填写数据**：使用Excel等电子表格软件打开模板文件，按照表头要求填写实际数据
2. **保存为CSV**：在Excel中选择"文件" → "另存为"
3. **⚠️ 重要**：保存时必须选择 **"CSV UTF-8 (逗号分隔) (*.csv)"** 格式，而不是普通的"CSV (逗号分隔) (*.csv)"格式
4. **编码说明**：UTF-8编码确保程序能正确识别中文内容，避免乱码问题

**输入文件格式 (`multi_factory_situations.csv`)**
```csv
工厂名称,产品名称,实际货值,已预付金额,税点,同意开票给代理公司,可超额开票,退税率
华东机械厂,球笼万向节,100000,20000,10.00,是,否,13.00
江苏精密制造,转向器总成,150000,30000,9.50,是,是,13.00
浙江汽配公司,刹车盘总成,80000,10000,11.00,否,否,13.00
```

**输出文件格式 (`multi_factory_calculation_results.csv`)**
```csv
工厂名称,产品名称,实际货值,已预付金额,税点,同意开票给代理公司,可超额开票,开票金额,退税金额,发货前代理公司须向工厂支付的金额,代理公司收到退税后应向工厂支付的余款金额,扣税金额,对公退款金额,对私退款金额
华东机械厂,球笼万向节,100000.00,20000.00,10.00,是,否,100000.00,11504.42,80000.00,100000.00,0.00,20000.00,0.00
江苏精密制造,转向器总成,150000.00,30000.00,9.50,是,是,195238.10,22389.58,120000.00,195238.10,4297.62,30000.00,40940.48
```

## 🧮 计算原理

### 核心数学模型

**主要计算公式**
```
开票金额 = (销售金额 × 汇率) × (1 + 退税率) ÷ (1 + 退税率 × 相对分配率)
```

**退税金额计算**
```
退税金额 = 开票金额 × 退税率 ÷ (1 + 退税率)
```

**代理利润计算**
```
代理利润 = 退税金额 × 相对分配率
```

### 分配模式转换

**绝对分配率 → 相对分配率**
```java
// 示例：退税率13%，绝对分配率6.5%
相对分配率 = 6.5% ÷ 13% = 50%
```

### 多工厂分配算法

#### 业务逻辑说明
系统处理复杂的混合开票场景，所有工厂都参与分配，但分配方式不同：

1. **固定金额分配**：
   - 不同意开票给代理：按实际货值分配（平进平出）
   - 同意开票但不可超额：按实际货值分配（避免税务风险）

2. **优化分配**：
   - 同意开票给代理 + 可超额开票：参与剩余金额的优化分配

#### 三层分配算法
```java
// 伪代码
// 第一步：计算所有产品的总开票金额
BigDecimal totalInvoiceAmount = 0;
for (Product p : allProducts) {
    BigDecimal productInvoice = calculateOptimalInvoiceAmount(p);
    totalInvoiceAmount += productInvoice;
}

// 第二步：识别不同类型的工厂
List<Factory> fixedFactories = new ArrayList<>();
List<Factory> flexibleFactories = new ArrayList<>();

for (Factory f : allFactories) {
    if (!f.agreesToInvoiceAgent() || !f.canOverprice()) {
        // 固定分配：不同意开票 或 不可超额开票
        fixedFactories.add(f);
        f.allocatedAmount = f.actualValue();  // 按实际货值分配
    } else {
        // 优化分配：同意开票 且 可超额开票
        flexibleFactories.add(f);
    }
}

// 第三步：计算剩余金额并分配给灵活工厂
BigDecimal fixedAmount = fixedFactories.sum(f -> f.actualValue());
BigDecimal remainingAmount = totalInvoiceAmount - fixedAmount;
BigDecimal flexibleTotalValue = flexibleFactories.sum(f -> f.actualValue());

for (Factory f : flexibleFactories) {
    BigDecimal ratio = f.actualValue() / flexibleTotalValue;
    f.allocatedAmount = remainingAmount * ratio;
    
    // 确保分配金额不小于实际货值
    if (f.allocatedAmount < f.actualValue()) {
        f.allocatedAmount = f.actualValue();
    }
}

// 第四步：计算各种退款
for (Factory f : allFactories) {
    // 对公应退金额 = 预付金额（如果同意开票给代理）
    f.publicRefund = f.agreesToInvoiceAgent() ? f.prepaidAmount() : 0;
    
    // 对私应退金额 = (开票金额 - 实际货值) × (1 - 税点)
    BigDecimal overprice = f.allocatedAmount - f.actualValue();
    f.privateRefund = overprice * (1 - f.taxPoint());
}
```

#### 关键业务规则
- **所有工厂都获得开票分配**，无论是否同意开票给代理
- **不同意开票的工厂**：外贸公司先收工厂发票，再按相同金额开票给代理
- **同意开票但不可超额的工厂**：按实际货值开票，避免税务风险
- **同意开票且可超额的工厂**：获得优化分配，最大化退税效益

## 🛠️ 开发指南

### 开发环境搭建

**1. 安装JDK 22**
```bash
# 使用SDKMAN安装（推荐）
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 22.0.2-tem
sdk use java 22.0.2-tem

# 验证安装
java --version
javac --version
```

**2. 克隆并构建项目**
```bash
git clone https://github.com/your-username/export-agent-invoice-calculator.git
cd export-agent-invoice-calculator

# 编译项目
mvn clean compile

# 运行测试（如果有）
mvn test

# 打包应用
mvn clean package

# 运行应用
java -jar target/export-agent-invoice-calculator-1.0-executable.jar
```

### 项目结构详解

```
export-agent-invoice-calculator/
├── src/main/java/com/gwill/foreign_trade/
│   ├── SingleSupplierInvoiceCalculator.java       # 单工厂计算器（单例）
│   ├── MultiFactoryInvoiceCalculator.java      # 多工厂计算器（单例）
│   ├── service/
│   │   └── MultiFactoryInvoiceCalculationService.java  # 核心业务逻辑
│   └── model/
│       ├── CalculationParams.java              # 计算参数记录
│       ├── CalculationResult.java              # 单次计算结果
│       ├── MultiProductCalculationResult.java  # 多产品计算结果
│       ├── ProductCalculationDetail.java       # 产品计算详情
│       ├── ProductSituation.java               # 工厂产品信息
│       └── FactoryAllocation.java              # 工厂分配结果
├── examples/                                   # 示例数据文件
│   ├── multi_factory_situations.csv           # 输入示例
│   └── multi_factory_calculation_results.csv  # 输出示例
├── pom.xml                                    # Maven配置
├── CLAUDE.md                                  # AI开发指南（英文）
├── README.md                                  # 项目文档（中文）
└── .gitignore                                 # Git忽略配置
```

### 核心代码模式

**单例模式实现**
```java
public class SingleSupplierInvoiceCalculator extends JFrame {
    private static SingleSupplierInvoiceCalculator instance;
    
    private SingleSupplierInvoiceCalculator() {
        initializeGUI();
    }
    
    public static synchronized SingleSupplierInvoiceCalculator getInstance() {
        if (instance == null) {
            instance = new SingleSupplierInvoiceCalculator();
        }
        return instance;
    }
}
```

**高精度计算**
```java
public class MultiFactoryInvoiceCalculationService {
    public static final int CALCULATION_PRECISION = 10;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    // 示例计算方法
    public static BigDecimal calculateInvoiceAmount(CalculationParams params) {
        BigDecimal numerator = params.clientPayment()
            .multiply(BigDecimal.ONE.add(params.taxRebateRate()));
        BigDecimal denominator = BigDecimal.ONE.add(
            params.taxRebateRate().multiply(params.agentRatio()));
        
        return numerator.divide(denominator, CALCULATION_PRECISION, ROUNDING_MODE);
    }
}
```

**GUI面板创建模式**
```java
private JPanel createInputPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBorder(new TitledBorder("基础参数设置"));
    GridBagConstraints gbc = new GridBagConstraints();
    
    // 精确布局控制
    gbc.insets = new Insets(3, 3, 3, 3);
    gbc.anchor = GridBagConstraints.WEST;
    
    // 添加组件...
    return panel;
}
```

### Maven配置特点

**多JAR打包策略**
```xml
<!-- Shade Plugin: 可执行JAR -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <configuration>
        <shadedArtifactAttached>true</shadedArtifactAttached>
        <shadedClassifierName>executable</shadedClassifierName>
        <filters>
            <!-- 排除Lombok和签名文件 -->
            <filter>
                <artifact>org.projectlombok:lombok</artifact>
                <excludes><exclude>**/*</exclude></excludes>
            </filter>
        </filters>
    </configuration>
</plugin>
```

## ❓ 常见问题

### Q1: 程序启动时为什么只显示部分按钮？

**现象**：多工厂计算器中只显示"选择文件"和"加载数据"按钮，其他按钮消失。

**原因**：布局管理器空间分配问题，通常由FlowLayout空间不足导致。

**解决方案**：
```java
// 已修复：使用GridBagLayout替代FlowLayout
JPanel panel = new JPanel(new GridBagLayout());
GridBagConstraints gbc = new GridBagConstraints();
gbc.weightx = 1.0;  // 关键：设置权重分配
```

### Q2: 计算结果显示"计算无解"如何处理？

**可能原因**：
- 退税率设置过高（超过100%）
- 绝对分配率超过退税率
- 参数组合导致分母为零

**解决步骤**：
1. 检查退税率是否在0-100%之间
2. 确保绝对分配率 ≤ 退税率
3. 尝试降低代理分成比例
4. 查看错误提示的具体信息

### Q3: CSV文件导入失败怎么办？

**文件格式要求**：
- UTF-8编码（避免中文乱码）
- 9列固定格式（参考examples目录示例）
- 数值字段不能包含中文字符
- 布尔值字段使用"是"/"否"

**Excel用户特别注意**：
- ⚠️ **必须使用UTF-8编码保存**：在Excel中另存为时选择"CSV UTF-8 (逗号分隔) (*.csv)"
- ❌ **避免使用普通CSV格式**：不要选择"CSV (逗号分隔) (*.csv)"格式，会导致中文乱码
- 📋 **使用提供的模板**：建议使用 `examples/multi_factory_situations_template.xlsx` 作为起点

**检查清单**：
```bash
# 检查文件编码
file -I examples/multi_factory_situations.csv

# 检查列数
head -1 examples/multi_factory_situations.csv | tr ',' '\n' | wc -l

# 检查数值格式
head -5 examples/multi_factory_situations.csv
```

### Q4: 如何理解对公和对私退款的区别？

**对公应退金额**：
- 含义：工厂通过公司账户退还给委托方的金额
- 计算：等于委托方预付给工厂的定金
- 适用：参与开票的工厂才有此金额

**对私应退金额**：  
- 含义：工厂通过个人账户退还的超额收款
- 计算：(开票金额 - 实际货值) × (1 - 税点)
- 适用：允许超额开票的工厂才有此金额

### Q5: 相对分配率和绝对分配率如何选择？

**相对分配率适用场景**：
- 退税政策相对稳定
- 希望分成比例固定
- 代理费用按退税金额比例计算

**绝对分配率适用场景**：
- 退税率可能变化
- 希望代理收益固定
- 合同约定固定的代理费率

**转换关系**：
```
相对分配率 = 绝对分配率 ÷ 退税率

示例：
退税率13%，绝对分配率6.5%
相对分配率 = 6.5% ÷ 13% = 50%
```

### Q6: 计算精度和显示精度的区别？

**系统精度设计**：
- **计算精度**：10位小数（避免累积误差）
- **显示精度**：2位小数（用户友好）
- **汇率精度**：4位小数（市场标准）

**代码示例**：
```java
// 计算：高精度
BigDecimal result = amount.divide(rate, 10, RoundingMode.HALF_UP);

// 显示：2位小数
String display = currencyFormat.format(result);  // "#,##0.00"
```

## 📄 许可证

本项目采用 MIT 许可证开源。

```
MIT License

Copyright (c) 2024 William YE of G-WILL Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

## 🤝 贡献指南

我们欢迎各种形式的贡献！

### 贡献类型
- 🐛 **Bug报告**：发现问题请提交详细的Issue
- 💡 **功能建议**：提出新功能想法和改进建议  
- 📖 **文档改进**：完善文档、翻译、示例
- 💻 **代码贡献**：修复Bug、实现新功能
- 🧪 **测试用例**：添加测试覆盖和边界用例

### 贡献流程
1. **Fork项目** 到您的GitHub账户
2. **创建分支** `git checkout -b feature/your-feature-name`
3. **开发测试** 在本地环境完成开发和测试
4. **提交代码** `git commit -m "Add: your feature description"`
5. **推送分支** `git push origin feature/your-feature-name`
6. **创建PR** 提交Pull Request并描述改动内容

### 代码规范
- 遵循项目现有的代码风格和命名约定
- 业务逻辑相关的变量和注释使用中文
- 技术方法名和类名使用英文
- 提交信息使用英文，格式：`Type: description`

## 📞 联系方式

- **项目维护者**：William YE of G-WILL Team
- **技术支持**：[GitHub Issues](https://github.com/WilliamYe79/export-agent-invoice-calculator/issues)
- **功能建议**：[GitHub Discussions](https://github.com/WilliamYe79/export-agent-invoice-calculator/discussions)

---

<div align="center">

**⭐ 如果这个项目对您有帮助，请给我们一个Star！**

**🔗 分享给更多需要的外贸朋友们**

Made with ❤️ by William YE of G-WILL Team

*专业的外贸计算工具，让复杂的代理业务变得简单*

</div>