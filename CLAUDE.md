# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a professional Java Swing-based foreign trade agent invoice calculation system that helps foreign trade companies calculate optimal invoice amounts for export agency business. The application implements the singleton pattern and features a sophisticated dual-calculator architecture with seamless navigation between different calculation modes.

### Main Applications

1. **Single-Supplier Calculator** (`SingleSupplierInvoiceCalculator`) - Handles individual transactions where the trading company acts as supplier to the agent:
   - Detailed/concise display modes
   - Relative and absolute agent commission options
   - Real-time calculation with comprehensive financial analysis
   - Professional result formatting and validation

2. **Multi-Factory/Supplier Calculator** (`MultiFactoryInvoiceCalculator`) - Manages complex scenarios involving multiple factories and suppliers:
   - Product-specific tax rebate rate calculation
   - Mixed invoicing scenarios (factories + trading company as suppliers)
   - CSV data import/export functionality  
   - Intelligent factory/supplier allocation algorithms
   - Comprehensive financial flow analysis
   - Professional table-based result display with individual tax rates

## Build and Development Commands

### Core Development Commands
```bash
# Clean and compile
mvn clean compile

# Run single-supplier calculator (main entry point)
mvn exec:java -Dexec.mainClass="com.gwill.foreign_trade.SingleSupplierInvoiceCalculator"

# Run multi-factory/supplier calculator directly
mvn exec:java -Dexec.mainClass="com.gwill.foreign_trade.MultiFactoryInvoiceCalculator"

# Package application (creates multiple JAR variants)
mvn clean package

# Run executable JAR (recommended)
java -jar target/export-agent-invoice-calculator-1.0-executable.jar

# Run assembly JAR (alternative)
java -jar target/export-agent-invoice-calculator-1.0-jar-with-dependencies.jar
```

### Testing and Quality Assurance
- **Manual Testing Only** - This is a GUI-focused application without automated test infrastructure
- **Interactive Testing** - All functionality is validated through the Swing interface
- **Real-world Data Testing** - Use example CSV files in `examples/` directory for comprehensive testing

## System Architecture

### Application Architecture Patterns

#### Singleton Pattern Implementation
Both main calculators implement singleton pattern for:
- **Memory efficiency** - Single instance per calculator type
- **State persistence** - Maintains user data when switching between calculators
- **Seamless navigation** - Smooth transitions with preserved context

#### Model-View-Service Architecture
- **View Layer**: Swing GUI components with GridBagLayout for precision
- **Service Layer**: `MultiFactoryInvoiceCalculationService` for business logic
- **Model Layer**: Record classes in `com.gwill.foreign_trade.model` package

### Key Components Structure

#### 1. Main Calculator Classes
- **`SingleSupplierInvoiceCalculator.java`** (Singleton)
  - Single-supplier transaction processing (trading company → agent)
  - Dual display modes (detailed/concise)
  - Agent commission calculation (relative/absolute)
  - Navigation to multi-factory/supplier calculator

- **`MultiFactoryInvoiceCalculator.java`** (Singleton)  
  - Multi-factory/supplier allocation management with product-specific tax rates
  - Mixed scenario handling (factories + trading company as suppliers)
  - CSV import/export operations (9-column format)
  - Per-product invoice amount calculation and aggregation
  - Complex financial flow analysis
  - Simplified relative-ratio-only commission mode
  - Return navigation to single-supplier calculator

#### 2. Service Layer
- **`MultiFactoryInvoiceCalculationService.java`** - Core business logic:
  - High-precision mathematical calculations
  - Input validation and error handling
  - Currency formatting utilities
  - Multi-factory allocation algorithms

#### 3. Model Classes (Record-based)
- **`CalculationParams`** - Input parameters for calculations
- **`CalculationResult`** - Single calculation results with breakdown
- **`MultiProductCalculationResult`** - Comprehensive multi-product results
- **`ProductCalculationDetail`** - Individual product calculation details
- **`ProductSituation`** - Factory/product configuration from CSV
- **`FactoryAllocation`** - Factory-specific allocation results

## Core Business Logic

### Primary Calculation Formula
The system uses this sophisticated formula for invoice amount calculation:
```
invoiceAmount = (salesAmount × exchangeRate) × (1 + taxRebateRate) ÷ (1 + taxRebateRate × agentRelativeRatio)
```

**Formula Components:**
- `salesAmount` - Foreign currency sales amount from PI (Proforma Invoice) per product
- `exchangeRate` - CNY/Foreign currency conversion rate
- `taxRebateRate` - Chinese export tax rebate rate (小数形式, product-specific)
- `agentRelativeRatio` - Agent's relative share of tax rebate (小数形式)

### Agent Commission Calculation Modes

#### Single-Supplier Calculator
**Relative Allocation Rate Mode**
- User inputs percentage of tax rebate amount (e.g., 50% means agent gets 50% of total tax rebate)
- Direct calculation: `agentRelativeRatio = userInput / 100`

**Absolute Allocation Rate Mode**  
- User inputs absolute tax rate for agent (e.g., 6.5% means agent gets 6.5% of invoice amount)
- Conversion formula: `agentRelativeRatio = (userInput / 100) / taxRebateRate`

#### Multi-Factory/Supplier Calculator
**Simplified Relative-Only Mode**
- Only supports relative allocation rate for simplicity
- Single input field for percentage of tax rebate amount
- Applied consistently across all products regardless of their individual tax rebate rates

### Multi-Factory/Supplier Allocation Strategy

#### Business Process Overview
The multi-factory calculator handles complex invoicing scenarios where some factories directly invoice the agent, while others invoice the trading company:

1. **Direct to Agent Invoicing**: Factories that agree to invoice directly to the agent
   - Participate in the optimized allocation algorithm
   - Can support overprice invoicing if allowed
   - Subject to tax rebate optimization calculations

2. **Trading Company Pass-through**: Factories that don't agree to invoice the agent
   - Invoice the trading company at actual purchase value (平进平出)
   - Trading company re-issues identical invoices to the agent
   - These amounts are deducted from the total optimized invoice calculation
   - May or may not qualify for tax rebates (depends on specific circumstances)

#### Allocation Logic
1. **Total Invoice Calculation**: Based on all products using the optimization formula
2. **Pass-through Deduction**: Subtract actual purchase values of non-participating factories
3. **Participating Factory Allocation**:
   - **Fixed-amount suppliers** - Cannot exceed invoice amount, get exact actual purchase value
   - **Flexible suppliers** - Can handle overprice invoicing, share remaining optimized amount proportionally
4. **Proportional Distribution** - Based on actual purchase amount ratios among flexible suppliers
5. **Rounding Adjustment** - Last flexible supplier handles precision differences

## Data Formats and Integration

### CSV File Structure
Multi-factory/supplier calculator expects 9-column CSV format:
```csv
工厂名称,产品名称,退税率,PI外币销售金额,实际货值,已预付金额,税点,同意开票给代理公司,可超额开票
华东机械厂,球笼万向节,13.00%,15550,100000,20000,10.00%,是,否
江苏精密制造,转向器总成,13.00%,22857,150000,30000,9.50%,是,是
```

**Column Descriptions:**
- **工厂名称** - Factory/supplier name (can be trading company acting as supplier)
- **产品名称** - Product name
- **退税率** - Tax rebate rate specific to this product
- **PI外币销售金额** - Sales amount in foreign currency from PI
- **实际货值** - Actual purchase value in RMB
- **已预付金额** - Prepaid amount in RMB
- **税点** - Tax point percentage
- **同意开票给代理公司** - Agrees to invoice to agent (是/否)
- **可超额开票** - Can invoice with overprice (是/否)

### Example Files Location
- **Input**: `examples/multi_factory_situations.csv`
- **Output**: `examples/multi_factory_calculation_results.csv`

### Export Functionality
The multi-factory/supplier calculator can export comprehensive results including:
- Factory/supplier and product information with individual tax rebate rates
- Calculated invoice amounts and tax rebates per product
- Public/private refund amounts
- Overprice tax calculations
- Complete financial flow details

## Precision and Formatting Standards

### High-Precision Mathematics
```java
public static final int CALCULATION_PRECISION = 10;
public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
```
- All financial calculations use `BigDecimal` to avoid floating-point errors
- Consistent rounding strategy across the entire system

### Professional Formatting
```java
public static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00%");
public static final DecimalFormat EXCHANGE_RATE_FORMAT = new DecimalFormat("#,##0.0000");
```

## GUI Design Principles

### Layout Management
- **GridBagLayout** - Primary layout manager for precise component positioning  
- **BorderLayout** - Main panel organization
- **CardLayout** - Display mode switching in single calculator
- **FlowLayout** - Button groupings and simple alignments

### User Experience Features
#### Single-Supplier Calculator
- **Display Mode Toggle** - Concise (external use) vs. Detailed (internal analysis)
- **Real-time Formatting** - Automatic thousand separators on focus events
- **Input Validation** - Comprehensive error messages with field-specific guidance
- **Commission Mode Selection** - Relative vs. Absolute with helpful explanations

#### Multi-Factory/Supplier Calculator  
- **Structured Layout** - Clear separation of input, configuration, operations, and results
- **CSV Integration** - File browser with proper filtering and error handling
- **Table Display** - Professional data grid with tax rebate rate column and comprehensive structure
- **Export Capability** - Full result export with proper CSV formatting including individual tax rates

### Navigation System
- **Seamless Switching** - Singleton instances maintain state across navigation
- **Bidirectional Flow** - Single-supplier ↔ Multi-factory/supplier calculator transitions
- **Context Preservation** - User data and settings retained during switches

## Development Guidelines

### Code Organization Principles
- **Chinese Business Domain** - Variable names and comments in Chinese for domain clarity
- **English Technical Terms** - Method names and technical concepts in English
- **Consistent Patterns** - Standardized method naming (`createXxxPanel`, `calculateXxx`, `displayXxx`)
- **Service Separation** - Business logic isolated in service layer

### Key Design Patterns
- **Singleton Pattern** - Calculator main classes
- **Record Pattern** - Immutable data transfer objects
- **Service Layer Pattern** - Business logic separation
- **Factory Pattern** - Panel creation methods

### Important Constants and Configuration

```java
import com.gwill.foreign_trade.SingleSupplierInvoiceCalculator;// Supported currencies
private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "JPY", "CNY", "RUB"};

// Main class configuration
<main.class>com.gwill.foreign_trade.SingleSupplierInvoiceCalculator</main .class>

// Calculation precision
private static final int CALCULATION_PRECISION = 10;
```

### Adding New Features
1. **Extend Model Classes** - Add new record classes in model package for data structures
2. **Service Layer Extensions** - Add business logic to `MultiFactoryInvoiceCalculationService`
3. **GUI Consistency** - Follow existing `createXxxPanel()` patterns for UI components
4. **Validation Integration** - Use existing validation utilities and error handling patterns

## Maven Build Configuration

### Multi-JAR Strategy
The project creates multiple JAR variants for different deployment scenarios:

#### Maven Shade Plugin
- **Purpose**: Creates self-contained executable JAR
- **Output**: `export-agent-invoice-calculator-1.0-executable.jar`
- **Features**: All dependencies included, optimized for distribution

#### Maven Assembly Plugin  
- **Purpose**: Alternative packaging with dependencies
- **Output**: `export-agent-invoice-calculator-1.0-jar-with-dependencies.jar`
- **Features**: Traditional fat JAR approach

### Build Optimization
- **Lombok Integration** - Compile-time only, excluded from final JAR
- **JDK 22 Target** - Modern Java features including text blocks and records
- **UTF-8 Encoding** - Proper Chinese character support
- **Dependency Filtering** - Excluded unnecessary files (module-info, signatures)

### Quality Assurance Notes
- **No Unit Tests** - GUI-focused application relies on manual testing
- **Manual Validation** - Comprehensive testing through user interface
- **Real Data Testing** - Use provided example files for validation
- **Error Handling** - Comprehensive input validation with user-friendly messages

## Recent Bug Fixes and Improvements

### v1.0.1 - Multi-Factory Calculator Critical Fixes

#### Critical Bug Fix: Total Invoice Amount Calculation
**Problem**: Negative invoice amounts for flexible factories due to insufficient total allocation.

**Root Cause**: The `calculateDistribution()` method was only including factories that agree to invoice the agent (`agreeToInvoiceToAgent() == true`) in the total invoice amount calculation. This severely understated the required total, causing:
- Total invoice amount much smaller than actual total purchase values
- Insufficient allocation for participating factories
- Negative allocation amounts for flexible factories

**Business Logic Clarification**:
- **All factories** contribute to the total invoice calculation using the optimization formula
- **Non-participating factories** (those not agreeing to invoice agent) use 平进平出 (pass-through) pricing
- **Participating factories** share the optimized allocation after deducting pass-through amounts

**Solution**: Modified the calculation loop to include ALL products in `totalInvoiceAmount` calculation:
```java
// BEFORE (incorrect):
if (prodSituation.agreeToInvoiceToAgent()) {
    // Only some products included
}

// AFTER (correct):
for (ProductSituation prodSituation : productSituationList) {
    // ALL products included in total calculation
    var productParams = new CalculationParams(...);
    CalculationResult productResult = calculateInvoiceAmount(productParams);
    totalInvoiceAmount = totalInvoiceAmount.add(productResult.invoiceAmount());
}
```

**Impact**: 
- Eliminated negative invoice amounts
- Ensured all invoice amounts ≥ actual purchase values
- Maintained mathematical consistency between total and individual allocations

#### Secondary Fix: CSV Data Alignment
**Problem**: Data misalignment between input and output CSV files due to processing order changes.

**Solution**: Replaced index-based matching with factory name + product name matching in export logic.

### Testing and Verification
- **Test Scenarios**: Mixed factory participation (some agreeing to invoice agent, others not)
- **Mathematical Validation**: All calculations now produce positive, realistic results
- **Business Accuracy**: Correctly handles 平进平出 scenarios alongside optimized allocations
- **Data Integrity**: CSV export/import maintains perfect data alignment