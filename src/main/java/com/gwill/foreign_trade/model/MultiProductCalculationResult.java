package com.gwill.foreign_trade.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * 多产品计算的详细结果
 */
public record MultiProductCalculationResult (
        BigDecimal totalInvoiceAmount,      // 总开票金额
        BigDecimal totalTaxRebateAmount,    // 总退税金额
        BigDecimal totalAgentProfit,        // 代理公司应获得的总利润
        BigDecimal yourTotalTaxRebateShareAmount, // 您应分得的总退税金额
        List<ProductCalculationDetail> productCalculationDetails   // 各产品计算详情
) {}