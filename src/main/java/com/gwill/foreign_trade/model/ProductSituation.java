package com.gwill.foreign_trade.model;

import java.math.BigDecimal;

/**
 * 产品情况数据模型
 * 封装从CSV文件中读取的工厂和产品信息
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
     * 是否为固定开票金额（不能超额开票的工厂只能按实际货值开票）
     */
    public boolean isFixedInvoiceAmount() {
        return !agreeToInvoiceToAgent || !ableToInvoiceWithOverprice;
    }
}