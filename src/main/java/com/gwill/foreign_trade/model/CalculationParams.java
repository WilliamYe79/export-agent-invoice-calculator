package com.gwill.foreign_trade.model;

import java.math.BigDecimal;

/**
 * 计算参数封装类
 */
public record CalculationParams (
        BigDecimal salesAmount,     // PI上的销售金额（外币）
        BigDecimal exchangeRate,    // 汇率
        BigDecimal taxRebateRate,   // 退税率（小数形式）
        BigDecimal agentRelativeRatio       // 代理退税相对分配率（小数形式）
) {}