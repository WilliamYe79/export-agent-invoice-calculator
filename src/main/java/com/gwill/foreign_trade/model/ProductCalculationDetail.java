package com.gwill.foreign_trade.model;

import java.math.BigDecimal;

/**
 * 单个产品计算结果详情
 */
public record ProductCalculationDetail (
        String factoryName,         // 工厂名称
        String productName,         // 产品名称
        BigDecimal actualPurchaseAmount,     // 实际采购金额（人民币）
        BigDecimal invoiceAmount,   // 该产品的开票金额
        BigDecimal taxRebateAmount, // 该产品的退税金额
        BigDecimal agentProfit,     // 代理公司应从该产品中获得的分项利润
        BigDecimal agentBalanceToFactoryBeforeShipment, // 发货前代理须向工厂支付的金额
        BigDecimal agentBalanceToFactoryAfterRebating, // 代理公司在收到退税后应向工厂支付的余款金额
        BigDecimal overpriceTax,    // 超额开票须扣除的税金
        BigDecimal prepaymentRefundAmount,   // 工厂对公应退金额（委托方预付给工厂的金额，不参与则为0）
        BigDecimal overpriceRefundFromFactory   // 工厂在扣除税点后，应通过个人账户退回的超出实际货值的收款
) {}