package com.gwill.foreign_trade.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 产品情况数据模型
 * 封装从Excel文件中读取的工厂和产品信息
 */
public record ProductSituation (
        String factoryName,                     // 工厂名称
        String productName,                     // 产品名称
        BigDecimal taxRebateRate,               // 退税率（小数形式，如0.13表示13%）
        BigDecimal salesAmountInForeignCurrency, // PI外币销售金额
        BigDecimal actualPurchaseAmount,        // 实际采购金额（人民币）
        BigDecimal prepaidAmount,               // 已预付金额（人民币）
        BigDecimal taxPoint,                    // 税点（小数形式，如0.10表示10%）
        boolean agreeToInvoiceToAgent,          // 同意开票给代理公司
        boolean ableToInvoiceWithOverprice      // 可超额开票
) {
    
    // Excel输入数据表头常量
    public static final String HEADER_FACTORY_NAME = "工厂名称";
    public static final String HEADER_PRODUCT_NAME = "产品名称";
    public static final String HEADER_TAX_REBATE_RATE = "退税率";
    public static final String HEADER_SALES_AMOUNT_FOREIGN = "PI外币销售金额";
    public static final String HEADER_ACTUAL_PURCHASE_AMOUNT = "实际货值";
    public static final String HEADER_PREPAID_AMOUNT = "已预付金额";
    public static final String HEADER_TAX_POINT = "税点";
    public static final String HEADER_AGREE_TO_INVOICE_AGENT = "同意开票给代理公司";
    public static final String HEADER_ABLE_TO_INVOICE_OVERPRICE = "可超额开票";
    
    /**
     * 从CSV行数据构造ProductSituation
     * CSV格式：工厂名称,产品名称,退税率,PI外币销售金额,实际货值,已预付金额,税点,同意开票给代理公司,可超额开票
     */
    public static ProductSituation fromCsvRow(String[] csvRow) {
        if (csvRow.length < 9) {
            throw new IllegalArgumentException("CSV行数据不完整，需要至少9列数据");
        }
        
        String factoryName = csvRow[0].trim();
        String productName = csvRow[1].trim();
        
        // 处理退税率：如果包含%符号，去掉并转换为小数
        String taxRebateRateStr = csvRow[2].trim();
        BigDecimal taxRebateRate;
        if (taxRebateRateStr.endsWith("%")) {
            taxRebateRate = new BigDecimal(taxRebateRateStr.substring(0, taxRebateRateStr.length() - 1))
                    .divide(BigDecimal.valueOf(100));
        } else {
            taxRebateRate = new BigDecimal(taxRebateRateStr);
        }
        
        BigDecimal salesAmount = new BigDecimal(csvRow[3].trim());
        BigDecimal actualPurchaseAmount = new BigDecimal(csvRow[4].trim());
        BigDecimal prepaidAmount = new BigDecimal(csvRow[5].trim());
        
        // 处理税点：如果包含%符号，去掉并转换为小数
        String taxPointStr = csvRow[6].trim();
        BigDecimal taxPoint;
        if (taxPointStr.endsWith("%")) {
            taxPoint = new BigDecimal(taxPointStr.substring(0, taxPointStr.length() - 1))
                    .divide(BigDecimal.valueOf(100));
        } else {
            taxPoint = new BigDecimal(taxPointStr);
        }
        
        boolean agreeToInvoice = "是".equals(csvRow[7].trim()) || "true".equalsIgnoreCase(csvRow[7].trim());
        boolean ableToOverprice = "是".equals(csvRow[8].trim()) || "true".equalsIgnoreCase(csvRow[8].trim());
        
        return new ProductSituation(
            factoryName, productName, taxRebateRate,
            salesAmount, actualPurchaseAmount, prepaidAmount,
            taxPoint, agreeToInvoice, ableToOverprice
        );
    }
    
    /**
     * 从Excel行数据构造ProductSituation（使用类型安全的Map数据）
     * Excel字段映射：工厂名称,产品名称,退税率,PI外币销售金额,实际货值,已预付金额,税点,同意开票给代理公司,可超额开票
     */
    public static ProductSituation fromExcelRow(Map<String, Object> excelRow) {
        String factoryName = (String) excelRow.get(HEADER_FACTORY_NAME);
        String productName = (String) excelRow.get(HEADER_PRODUCT_NAME);
        
        // 从Excel读取的数据已经被excel-io按照元数据进行了类型转换
        BigDecimal taxRebateRate = (BigDecimal) excelRow.get(HEADER_TAX_REBATE_RATE);
        BigDecimal salesAmount = (BigDecimal) excelRow.get(HEADER_SALES_AMOUNT_FOREIGN);
        BigDecimal actualPurchaseAmount = (BigDecimal) excelRow.get(HEADER_ACTUAL_PURCHASE_AMOUNT);
        BigDecimal prepaidAmount = (BigDecimal) excelRow.get(HEADER_PREPAID_AMOUNT);
        BigDecimal taxPoint = (BigDecimal) excelRow.get(HEADER_TAX_POINT);
        
        // 处理布尔值字段
        Object agreeToInvoiceObj = excelRow.get(HEADER_AGREE_TO_INVOICE_AGENT);
        boolean agreeToInvoice = parseBoolean(agreeToInvoiceObj);
        
        Object ableToOverpriceObj = excelRow.get(HEADER_ABLE_TO_INVOICE_OVERPRICE);
        boolean ableToOverprice = parseBoolean(ableToOverpriceObj);
        
        return new ProductSituation(
            factoryName, productName, taxRebateRate,
            salesAmount, actualPurchaseAmount, prepaidAmount,
            taxPoint, agreeToInvoice, ableToOverprice
        );
    }
    
    /**
     * 辅助方法：解析布尔值（支持中文"是/否"和英文"true/false"）
     */
    private static boolean parseBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String str = ((String) value).trim();
            return "是".equals(str) || "true".equalsIgnoreCase(str);
        }
        return false;
    }
    
    /**
     * 是否为固定开票金额（不能超额开票的工厂只能按实际货值开票）
     */
    public boolean isFixedInvoiceAmount() {
        return !agreeToInvoiceToAgent || !ableToInvoiceWithOverprice;
    }
}