package com.gwill.foreign_trade.model;

import java.math.BigDecimal;

/**
 * 计算结果封装类
 */
public record CalculationResult (
        BigDecimal clientPaymentInRMB,      // 客户付款（人民币）
        BigDecimal invoiceAmount,           // 开票金额
        BigDecimal taxRebateAmount,         // 退税金额
        BigDecimal agentProfit,             // 代理利润
        BigDecimal yourTaxRebateShareAmount      // 您应分得的退税金额
) {}