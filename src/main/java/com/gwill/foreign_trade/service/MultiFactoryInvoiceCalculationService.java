package com.gwill.foreign_trade.service;

import com.gwill.foreign_trade.model.CalculationParams;
import com.gwill.foreign_trade.model.CalculationResult;
import com.gwill.foreign_trade.model.FactoryAllocation;
import com.gwill.foreign_trade.model.MultiProductCalculationResult;
import com.gwill.foreign_trade.model.ProductCalculationDetail;
import com.gwill.foreign_trade.model.ProductSituation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * 开票计算服务类
 * 提供外贸代理开票金额相关的核心计算功能
 * 供单一代理和多工厂分配两种模式复用
 */
public class MultiFactoryInvoiceCalculationService {

    // 精度设置
    public static final int CALCULATION_PRECISION = 10;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    // 格式化器（静态共享）
    public static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,##0.00");
    public static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00%");
    public static final DecimalFormat EXCHANGE_RATE_FORMAT = new DecimalFormat("#,##0.0000");


    /**
     * 核心计算方法：计算开票金额和相关数值
     * @param params 用于计算的用户输入参数
     * @return 计算结果
     */
    public static CalculationResult calculateInvoiceAmount ( CalculationParams params ) {
        // 验证用于笼统计算的用户输入参数
        validateCalculationParams( params );

        // 计算客户付款（人民币等值）
        BigDecimal clientPaymentInRMB = params.salesAmount().multiply( params.exchangeRate() );

        // 计算分母
        BigDecimal denominator = BigDecimal.ONE.add( params.taxRebateRate().multiply( params.agentRelativeRatio() ) );
        // 验证分母不为零
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException( "参数组合导致计算公式分母为0，计算无解！请调整代理分成比例，重新计算。" );
        }

        // 计算开票金额: X = (S × E) × (1+R) ÷ (1 + R × A)
        // 即: invoiceAmountInRMB = (salesAmount × exchangeRate) × (1+taxRebateRate) ÷ (1 + taxRebateRate × agentRatio)
        BigDecimal numerator = clientPaymentInRMB.multiply( BigDecimal.ONE.add( params.taxRebateRate() ) );
        BigDecimal invoiceAmountInRMB = numerator.divide( denominator, CALCULATION_PRECISION, ROUNDING_MODE );

        // 计算退税金额: T = X × R/(1+R)
        BigDecimal taxRebateAmountInRMB = invoiceAmountInRMB.multiply( params.taxRebateRate() )
                .divide( BigDecimal.ONE.add( params.taxRebateRate() ), CALCULATION_PRECISION, ROUNDING_MODE );

        // 计算代理利润
        BigDecimal agentProfitInRMB = taxRebateAmountInRMB.multiply( params.agentRelativeRatio() );

        // 计算您应分得的退税金额
        BigDecimal yourTaxRebateAmountInRMB = taxRebateAmountInRMB.subtract( agentProfitInRMB );

        return new CalculationResult( clientPaymentInRMB, invoiceAmountInRMB, taxRebateAmountInRMB,
                agentProfitInRMB, yourTaxRebateAmountInRMB );
    }

    /**
     * 多产品计算方法：基于工厂分配结果计算各产品详情
     * @param totalInvoiceAmount 总开票金额
     * @param productSituationList 产品信息列表
     * @param exchangeRate 汇率
     * @param agentRelativeRatio 代理退税相对分配率（小数形式）
     * @return 多产品计算结果
     */
    public static MultiProductCalculationResult calculateMultiProductsData(
            BigDecimal totalInvoiceAmount,
            List<ProductSituation> productSituationList, BigDecimal exchangeRate, BigDecimal agentRelativeRatio ) {

        BigDecimal totalTaxRebate = BigDecimal.ZERO;
        BigDecimal totalAgentProfit = BigDecimal.ZERO;
        BigDecimal yourTotalTaxRebateShareAmount = BigDecimal.ZERO;

        List<ProductCalculationDetail> productCalcDetails = new ArrayList<>();

        List<FactoryAllocation> factoryAllocations = calculateFactoryAllocations(
                totalInvoiceAmount, productSituationList );

        Map<String, Map<String, List<ProductSituation>>> prodSituationFactoryProductMap =
                productSituationList.stream().collect(
                        groupingBy( ProductSituation::factoryName,
                                groupingBy( ProductSituation::productName )
                        )
                );

        for ( FactoryAllocation allocation : factoryAllocations ) {
            String factoryName = allocation.factoryName();
            String productName = allocation.productName();
            // 按照实际业务逻辑，如下Map中每一个List中应该有且仅有1个ProductSituation对象element
            ProductSituation prodSituation = prodSituationFactoryProductMap
                    .get( factoryName ).get( productName ).getFirst();

            // 计算该产品的退税金额
            BigDecimal taxRebateAmount = calculateTaxRebateAmount(
                    allocation.allocatedInvoiceAmount(), prodSituation.taxRebateRate() );

            // 计算代理公司从该产品获得的利润
            BigDecimal agentProfitFromProduct = taxRebateAmount.multiply( agentRelativeRatio );

            // 计算代理公司在收到退税后应向工厂支付的余款金额 = 开票金额 - 代理公司已向该工厂支付的金额
            // = 开票金额 - (实际货值 - 您已支付给工厂的金额)
            BigDecimal amountPaidByAgentToFactory = prodSituation.actualPurchaseAmount().subtract(
                    prodSituation.prepaidAmount() );
            BigDecimal balanceToFactoryAfterRebating = allocation.allocatedInvoiceAmount().subtract( amountPaidByAgentToFactory );

            // 计算工厂多开票金额按照税点产生的额外费用 = (开票金额 - 实际货值) × 税点
            BigDecimal overpriceAmount = allocation.allocatedInvoiceAmount().subtract(
                    prodSituation.actualPurchaseAmount() );
            BigDecimal overpriceTax = overpriceAmount.multiply( prodSituation.taxPoint() );

            // 累计总额
            totalTaxRebate = totalTaxRebate.add( taxRebateAmount );
            totalAgentProfit = totalAgentProfit.add( agentProfitFromProduct );
            yourTotalTaxRebateShareAmount = yourTotalTaxRebateShareAmount.add(
                    taxRebateAmount.subtract( agentProfitFromProduct ) );

            // 计算工厂对公应退金额：如果参与开票给代理公司，则为预付金额，否则为0
            BigDecimal prepaymentRefundAmount = prodSituation.agreeToInvoiceToAgent() ? 
                    prodSituation.prepaidAmount() : BigDecimal.ZERO;
                    
            // 记录产品详情
            var detail = new ProductCalculationDetail( factoryName, productName,
                    prodSituation.actualPurchaseAmount(), allocation.allocatedInvoiceAmount(), taxRebateAmount,
                    agentProfitFromProduct, amountPaidByAgentToFactory, balanceToFactoryAfterRebating, 
                    overpriceTax, prepaymentRefundAmount, allocation.overpriceRefundAmount() );
            productCalcDetails.add( detail );
        }

        return new MultiProductCalculationResult( totalInvoiceAmount, totalTaxRebate,
                totalAgentProfit, yourTotalTaxRebateShareAmount, productCalcDetails );
    }

    /**
     * 计算您的总收入
     * @param params 用户在界面上输入的总体信息（包括：PI上的外币销售金额、汇率、退税率、代理退税相对分配率）
     * @param result 基础计算结果
     * @return 总收入金额
     */
    public static BigDecimal calculateYourTotalIncome( CalculationParams params,
                                                       MultiProductCalculationResult result ) {
        BigDecimal totalSalesAmountInRMB = params.salesAmount().multiply( params.exchangeRate() );
        BigDecimal totalIncome = totalSalesAmountInRMB.add( result.yourTotalTaxRebateShareAmount() );

        return totalIncome;
    }

    /**
     * 计算以人民币为货币单位的你（委托方）的总净利润额。
     * @param params 用户在界面上输入的总体信息（包括：PI上的外币销售金额、汇率、退税率、代理退税相对分配率）
     * @param productsSituation 从输入CSV中读取的信息（包括：工厂名称,产品名称,实际货值,已预付金额,税点,同意开票给代理公司,可超额开票）
     * @param result 基础计算结果
     * @return 以人民币为货币单位的你（委托方）的总净利润额
     */
    public BigDecimal calculateYourNetProfit ( CalculationParams params, List<ProductSituation> productsSituation,
                                                MultiProductCalculationResult result ) {
        BigDecimal totalSalesAmountInRMB = params.salesAmount().multiply( params.exchangeRate() );;
        BigDecimal totalActualPurchaseAmountInRMB = result.productCalculationDetails().stream()
                .map( ProductCalculationDetail::actualPurchaseAmount )
                .reduce( BigDecimal.ZERO, BigDecimal::add );
        BigDecimal totalOverpriceTaxInRMB = result.productCalculationDetails().stream()
                .map( ProductCalculationDetail::overpriceTax )
                .reduce( BigDecimal.ZERO, BigDecimal::add );
        BigDecimal totalIncome = totalSalesAmountInRMB.add( result.yourTotalTaxRebateShareAmount() );
        BigDecimal totalDeduction = totalActualPurchaseAmountInRMB.add( totalOverpriceTaxInRMB );

        return totalIncome.subtract( totalDeduction );
    }

    /**
     * 计算你（委托方）的成本利润率。
     * @param yourNetProfitInRMB
     * @param purchaseAmountInRMB
     * @return 你（委托方）的成本利润率（小数形式）
     */
    public BigDecimal calculateYourGrossMarkup( BigDecimal yourNetProfitInRMB, BigDecimal purchaseAmountInRMB ) {
        return yourNetProfitInRMB.divide( purchaseAmountInRMB, CALCULATION_PRECISION, ROUNDING_MODE );
    }

    /**
     * 将用户手工输入的分配给代理公司的绝对退税率换算成退税金额中分配给代理公司的相对分配率（百分比）。
     * @param taxRefundRate 该款产品的海关退税率
     * @param agentRateInput 用户手工输入的分配给代理公司的绝对退税率
     * @return 退税金额中分配给代理公司的相对分配率（百分比）
     */
    public BigDecimal convertAbsoluteRateToRelativeRatio ( BigDecimal taxRefundRate, BigDecimal agentRateInput ) {
        BigDecimal agentRate = agentRateInput.divide( BigDecimal.valueOf( 100 ), CALCULATION_PRECISION, ROUNDING_MODE );
        if ( agentRate.compareTo( BigDecimal.ZERO ) < 0 || agentRate.compareTo( taxRefundRate ) > 0 ) {
            String errorMsg = String.format( "代理分成绝对退税率必须在0-%.2f%%之间",
                    taxRefundRate.multiply( BigDecimal.valueOf( 100 ) ).doubleValue() );
            throw new IllegalArgumentException( errorMsg );
        }
        BigDecimal relativeAgentRatio = agentRate.divide( taxRefundRate, CALCULATION_PRECISION, ROUNDING_MODE );
        return relativeAgentRatio;
    }

    /**
     * 按照发票金额和退税率来计算退税金额
     * 退税公式: T = X × R/(1+R)
     * 其中: T=退税金额, X=开票金额, R=退税率
     * @param invoiceAmountInRMB 发票金额（人民币）
     * @param taxRebateRate 退税率（小数形式）
     * @return 退税金额
     */
    public static BigDecimal calculateTaxRebateAmount( BigDecimal invoiceAmountInRMB, BigDecimal taxRebateRate ) {
        return invoiceAmountInRMB.multiply( taxRebateRate ).divide( BigDecimal.ONE.add( taxRebateRate ),
                CALCULATION_PRECISION, ROUNDING_MODE );
    }

    // =========================== 工具方法 ===========================

    /**
     * 解析BigDecimal，支持千位分隔符
     */
    public static BigDecimal parseBigDecimal(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        try {
            String cleanText = text.trim().replaceAll(",", "");
            BigDecimal value = new BigDecimal(cleanText);
            if (value.scale() > CALCULATION_PRECISION) {
                value = value.setScale(CALCULATION_PRECISION, ROUNDING_MODE);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + "格式不正确！请输入数字！");
        }
    }

    /**
     * 百分比转小数
     */
    public static BigDecimal percentageToDecimal(String percentageText, String fieldName) {
        BigDecimal percentage = parseBigDecimal(percentageText, fieldName);
        return percentage.divide(BigDecimal.valueOf(100), CALCULATION_PRECISION, ROUNDING_MODE);
    }

    // =========================== 格式化方法 ===========================

    /**
     * 格式化货币金额
     */
    public static String formatCurrency(BigDecimal amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    /**
     * 格式化百分比
     */
    public static String formatPercentage(BigDecimal decimal) {
        return PERCENT_FORMAT.format(decimal);
    }

    /**
     * 格式化汇率
     */
    public static String formatExchangeRate(BigDecimal rate) {
        return EXCHANGE_RATE_FORMAT.format(rate);
    }

    /**
     * 计算各家工厂分配的开票金额及退款金额
     * @param totalInvoiceAmountInRMB 总开票金额
     * @param productSituationList 从输入CSV中读取的信息（包括：工厂名称,产品名称,实际货值,已预付金额,税点,同意开票给代理公司,可超额开票）
     * @return 一个包含各家工厂分配的开票金额及退款金额的列表
     */
    public static List<FactoryAllocation> calculateFactoryAllocations(
            BigDecimal totalInvoiceAmountInRMB, List<ProductSituation> productSituationList ) {
        List<FactoryAllocation> allocationResults = new ArrayList<>();

        // 将可超额开票和无法超额开票的工厂及产品信息分成到两个List中
        List<ProductSituation> fixedInvoiceAmountProducts = new ArrayList<>();
        List<ProductSituation> flexibleInvoiceAmountProducts = new ArrayList<>();

        // 分配开票金额
        BigDecimal totalAllocatedAmount = BigDecimal.ZERO;

        for ( ProductSituation prodSituation : productSituationList ) {
            if ( !prodSituation.agreeToInvoiceToAgent() || !prodSituation.ableToInvoiceWithOverprice() ) {
                fixedInvoiceAmountProducts.add( prodSituation );
                totalAllocatedAmount = totalAllocatedAmount.add( prodSituation.actualPurchaseAmount() );

                BigDecimal taxRebateAmount = calculateTaxRebateAmount( 
                        prodSituation.actualPurchaseAmount(), prodSituation.taxRebateRate() );
                BigDecimal overpriceRefundAmount = BigDecimal.ZERO;

                allocationResults.add( new FactoryAllocation( prodSituation.factoryName(), prodSituation.productName(),
                        prodSituation.actualPurchaseAmount(), prodSituation.actualPurchaseAmount(),
                        taxRebateAmount, overpriceRefundAmount ) );
            } else {
                flexibleInvoiceAmountProducts.add( prodSituation );
            }
        }

        // 计算可灵活分配的总货值
        BigDecimal flexibleTotalActualPurchaseAmount = flexibleInvoiceAmountProducts.stream()
                .map( ProductSituation::actualPurchaseAmount )
                .reduce( BigDecimal.ZERO, BigDecimal::add );

        BigDecimal allocatableInvoiceAmount = totalInvoiceAmountInRMB.subtract( totalAllocatedAmount );

        Iterator<ProductSituation> prodSituationIter = flexibleInvoiceAmountProducts.iterator();
        while ( prodSituationIter.hasNext() ) {
            ProductSituation prodSituation = prodSituationIter.next();
            BigDecimal allocatedAmount;
            
            if ( !prodSituationIter.hasNext() ) {
                // 最后一家工厂调整尾差
                allocatedAmount = totalInvoiceAmountInRMB.subtract( totalAllocatedAmount );
            } else {
                // 计算分配比例：按该工厂货值占灵活工厂总货值的比例分配
                BigDecimal allocateRatio = prodSituation.actualPurchaseAmount().divide(
                        flexibleTotalActualPurchaseAmount, CALCULATION_PRECISION, ROUNDING_MODE );
                allocatedAmount = allocatableInvoiceAmount.multiply( allocateRatio );
            }
            
            totalAllocatedAmount = totalAllocatedAmount.add( allocatedAmount );

            // 计算该产品的退税金额
            BigDecimal taxRebateAmount = calculateTaxRebateAmount( allocatedAmount, prodSituation.taxRebateRate() );

            // 计算应退金额：(开票金额 - 实际货值) * (1 - 税点)
            BigDecimal invoiceAmountDiff = allocatedAmount.subtract( prodSituation.actualPurchaseAmount() );
            BigDecimal overpriceRefundAmount = invoiceAmountDiff.multiply(
                    BigDecimal.ONE.subtract( prodSituation.taxPoint() ) );

            allocationResults.add( new FactoryAllocation( prodSituation.factoryName(), prodSituation.productName(),
                    prodSituation.actualPurchaseAmount(), allocatedAmount,
                    taxRebateAmount, overpriceRefundAmount ) );
        }

        return allocationResults;
    }

    private static void validateCalculationParams ( CalculationParams params ) {
        if ( params.salesAmount().compareTo( BigDecimal.ZERO ) <= 0 )
            throw new IllegalArgumentException( "销售金额必须大于0" );
        if ( params.exchangeRate().compareTo( BigDecimal.ZERO ) <= 0 )
            throw new IllegalArgumentException( "汇率必须大于0" );
        if ( params.taxRebateRate().compareTo( BigDecimal.ZERO ) <= 0 ||
                params.taxRebateRate().compareTo( BigDecimal.ONE ) >= 0 )
            throw new IllegalArgumentException( "退税率必须在0-100%之间" );
        if ( params.agentRelativeRatio().compareTo( BigDecimal.ZERO ) < 0 ||
                params.agentRelativeRatio().compareTo( BigDecimal.valueOf( 100 ) ) > 0 )
            throw new IllegalArgumentException( "代理相对分成率必须在0-100%之间" );
    }
}
