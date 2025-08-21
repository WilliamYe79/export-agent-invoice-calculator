package com.gwill.foreign_trade.model;

import java.math.BigDecimal;

/**
 * 工厂分配结果
 * 记录每家工厂分配到的开票金额和应退款金额
 */
public record FactoryAllocation (
        String factoryName,                 // 工厂名称
        String productName,                 // 产品名称
        BigDecimal actualPurchaseAmount,    // 实际采购金额
        BigDecimal allocatedInvoiceAmount,  // 分配到的开票金额
        BigDecimal taxRebateAmount,         // 按照开票金额应获得的退税金额
        BigDecimal overpriceRefundAmount    // 工厂在扣除税点后，应通过个人账户退回的超出实际货值的收款
) {}