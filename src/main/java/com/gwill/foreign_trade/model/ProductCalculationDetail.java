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
) {
    
    // Excel输出结果表头常量 - 与ProductSituation的输入表头保持一致的部分
    public static final String HEADER_FACTORY_NAME = "工厂名称";
    public static final String HEADER_PRODUCT_NAME = "产品名称";
    public static final String HEADER_TAX_REBATE_RATE = "退税率";
    public static final String HEADER_ACTUAL_PURCHASE_AMOUNT = "实际货值";
    public static final String HEADER_PREPAID_AMOUNT = "已预付金额";
    public static final String HEADER_TAX_POINT = "税点";
    public static final String HEADER_AGREE_TO_INVOICE_AGENT = "同意开票给代理公司";
    public static final String HEADER_ABLE_TO_INVOICE_OVERPRICE = "可超额开票";
    
    // Excel输出结果表头常量 - 计算结果特有的字段
    public static final String HEADER_INVOICE_AMOUNT = "开票金额";
    public static final String HEADER_TAX_REBATE_AMOUNT = "退税金额";
    public static final String HEADER_AGENT_BALANCE_BEFORE_SHIPMENT = "发货前代理公司须向工厂支付的金额";
    public static final String HEADER_AGENT_BALANCE_AFTER_REBATING = "代理公司收到退税后应向工厂支付的余款金额";
    public static final String HEADER_OVERPRICE_TAX = "扣税金额";
    public static final String HEADER_PREPAYMENT_REFUND = "对公退款金额";
    public static final String HEADER_OVERPRICE_REFUND = "对私退款金额";
}