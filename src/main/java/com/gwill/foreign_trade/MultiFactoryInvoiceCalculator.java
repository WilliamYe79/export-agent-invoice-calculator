package com.gwill.foreign_trade;

import com.gwill.foreign_trade.model.CalculationParams;
import com.gwill.foreign_trade.model.CalculationResult;
import com.gwill.foreign_trade.model.MultiProductCalculationResult;
import com.gwill.foreign_trade.model.ProductCalculationDetail;
import com.gwill.foreign_trade.model.ProductSituation;
import com.gwill.foreign_trade.service.MultiFactoryInvoiceCalculationService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

/**
 * 多工厂分配计算器
 * 支持从CSV文件读取工厂信息，计算各工厂开票金额和资金流转
 *
 * CSV文件格式：工厂名称,产品名称,实际货值,已预付金额,税点,同意开票给代理公司,可超额开票
 */

public class MultiFactoryInvoiceCalculator extends JFrame {

    // 单例实例
    private static MultiFactoryInvoiceCalculator instance;
    
    // 精度设置（使用服务类的常量）
    private static final int CALCULATION_PRECISION = MultiFactoryInvoiceCalculationService.CALCULATION_PRECISION;
    private static final RoundingMode ROUNDING_MODE = MultiFactoryInvoiceCalculationService.ROUNDING_MODE;

    // 货币选项
    private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "JPY", "CNY", "RUB"};
    private static final String[] CURRENCY_NAMES = {
            "美元 (USD)", "欧元 (EUR)", "英镑 (GBP)",
            "日元 (JPY)", "人民币 (CNY)", "俄罗斯卢布 (RUB)"
    };

    // 基础参数组件
    private JTextField salesAmountField;
    private JComboBox<String> currencyComboBox;
    private JTextField exchangeRateField;
    private JTextField agentRelativeRatioField;      // 相对分配率字段

    // 文件选择组件
    private JTextField csvFilePathField;
    private JButton browseFileButton;
    private JButton loadDataButton;

    // 数据显示组件
    private JTable detailedResultTable;
    private DefaultTableModel tableModel;
    private JTextArea calculationResultArea;
    private JTextArea cashFlowResultArea;

    // 计算按钮
    private JButton calculateButton;
    private JButton clearButton;
    private JButton exportButton;
    private JButton backToSingleButton;

    // 工厂数据
    private List<ProductSituation> productSituationList;
    
    // 计算结果数据（用于导出）
    private MultiProductCalculationResult lastCalculationResult;

    // 格式化器（使用服务类的方法）
    private DecimalFormat currencyFormat = MultiFactoryInvoiceCalculationService.CURRENCY_FORMAT;
    private DecimalFormat percentFormat = MultiFactoryInvoiceCalculationService.PERCENT_FORMAT;
    private DecimalFormat exchangeRateFormat = MultiFactoryInvoiceCalculationService.EXCHANGE_RATE_FORMAT;

    private MultiFactoryInvoiceCalculator() {
        productSituationList = new ArrayList<>();
        initializeGUI();
    }
    
    /**
     * 获取单例实例
     */
    public static synchronized MultiFactoryInvoiceCalculator getInstance() {
        if (instance == null) {
            instance = new MultiFactoryInvoiceCalculator();
        }
        return instance;
    }

    private void initializeGUI() {
        setTitle("多工厂代理开票分配计算器 v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 创建主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 基础参数面板
        JPanel inputPanel = createInputPanel();
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1.0; gbc.weighty = 0.0;
        mainPanel.add(inputPanel, gbc);

        // 代理分成方式面板
        JPanel ratePanel = createRatePanel();
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(ratePanel, gbc);

        // 工厂数据管理面板
        JPanel dataPanel = createDataPanel();
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.4;
        mainPanel.add(dataPanel, gbc);

        // 操作按钮面板
        JPanel buttonPanel = createButtonPanel();
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0.0;
        mainPanel.add(buttonPanel, gbc);

        // 计算结果面板
        JPanel resultPanel = createResultPanel();
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 0.6;
        mainPanel.add(resultPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 设置默认值
        setDefaultValues();

        // 设置窗口属性
        setSize(1200, 900);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("基础参数设置"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // 第一行：销售金额和货币
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("销售金额:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        salesAmountField = new JTextField(15);
        salesAmountField.setHorizontalAlignment(JTextField.RIGHT);
        panel.add(salesAmountField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        currencyComboBox = new JComboBox<>(CURRENCY_NAMES);
        currencyComboBox.setSelectedIndex(0); // 默认选择USD
        panel.add(currencyComboBox, gbc);

        // 第二行：汇率
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel exchangeRateLabel = new JLabel("汇率 (CNY/外币):");
        panel.add(exchangeRateLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        exchangeRateField = new JTextField(15);
        exchangeRateField.setHorizontalAlignment(JTextField.RIGHT);
        panel.add(exchangeRateField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel(""), gbc);



        return panel;
    }

    private JPanel createRatePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("代理分成方式"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // 相对分配率行
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("相对分配率 (占退税金额的百分比):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        agentRelativeRatioField = new JTextField(10);
        agentRelativeRatioField.setHorizontalAlignment(JTextField.RIGHT);
        panel.add(agentRelativeRatioField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("%"), gbc);

        // 添加说明标签
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel helpLabel = new JLabel("<html><small>" +
                "如输入50，表示代理获得退税金额的50%" +
                "</small></html>");
        helpLabel.setForeground(Color.GRAY);
        panel.add(helpLabel, gbc);

        return panel;
    }

    private JPanel createDataPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("工厂数据管理"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 文件选择区域
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("CSV文件:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        csvFilePathField = new JTextField(50);  // 增加长度
        csvFilePathField.setEditable(false);
        panel.add(csvFilePathField, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        browseFileButton = new JButton("选择文件");
        browseFileButton.addActionListener(e -> browseCSVFile());
        panel.add(browseFileButton, gbc);

        gbc.gridx = 3; gbc.gridy = 0;
        loadDataButton = new JButton("加载数据");
        loadDataButton.addActionListener(e -> loadFactoryData());
        panel.add(loadDataButton, gbc);

        // 数据表格
        String[] columns = {"工厂名称", "产品名称", "退税率", "实际货值(元)", "开票金额(元)", "退税金额(元)", "代理利润(元)", "工厂对公应退金额(元)", "工厂对私应退金额(元)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格只读
            }
        };

        detailedResultTable = new JTable(tableModel);
        detailedResultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailedResultTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane tableScrollPane = new JScrollPane(detailedResultTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 200));
        
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.BOTH; 
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        panel.add(tableScrollPane, gbc);

        return panel;
    }

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(new TitledBorder("计算结果"));

        // 计算结果面板
        JPanel calcPanel = new JPanel(new BorderLayout());
        calcPanel.setBorder(new TitledBorder("分配计算结果"));
        calculationResultArea = new JTextArea(10, 40);
        calculationResultArea.setEditable(false);
        calculationResultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane calcScrollPane = new JScrollPane(calculationResultArea);
        calcPanel.add(calcScrollPane, BorderLayout.CENTER);

        // 资金流结果面板
        JPanel cashPanel = new JPanel(new BorderLayout());
        cashPanel.setBorder(new TitledBorder("资金流转详情"));
        cashFlowResultArea = new JTextArea(10, 40);
        cashFlowResultArea.setEditable(false);
        cashFlowResultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane cashScrollPane = new JScrollPane(cashFlowResultArea);
        cashPanel.add(cashScrollPane, BorderLayout.CENTER);

        panel.add(calcPanel);
        panel.add(cashPanel);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(new TitledBorder("操作"));

        calculateButton = new JButton("计算分配");
        calculateButton.addActionListener(new CalculateButtonListener());
        panel.add(calculateButton);

        clearButton = new JButton("清空数据");
        clearButton.addActionListener(e -> clearAllData());
        panel.add(clearButton);

        exportButton = new JButton("导出结果");
        exportButton.addActionListener(e -> exportCalculationResults());
        exportButton.setEnabled(false); // 初始时禁用，计算完成后启用
        panel.add(exportButton);

        backToSingleButton = new JButton("返回单供应商计算");
        backToSingleButton.addActionListener(e -> backToSingleCalculator());
        panel.add(backToSingleButton);

        return panel;
    }

    private void browseCSVFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV文件 (*.csv)", "csv"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            csvFilePathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void loadFactoryData() {
        String filePath = csvFilePathField.getText().trim();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择CSV文件！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            productSituationList.clear();
            tableModel.setRowCount(0);

            try ( BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                int lineNumber = 0;

                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    if (lineNumber == 1) continue; // 跳过标题行

                    String[] parts = line.split(",");
                    if (parts.length >= 9) {
                        ProductSituation prodSituation = ProductSituation.fromCsvRow(parts);
                        productSituationList.add(prodSituation);
                    }
                }
            }

            JOptionPane.showMessageDialog(this,
                    String.format("成功加载 %d 家工厂数据！", productSituationList.size()),
                    "加载成功", JOptionPane.INFORMATION_MESSAGE);

        } catch ( IOException | NumberFormatException e ) {
            JOptionPane.showMessageDialog(this,
                    "CSV文件读取失败：" + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed( ActionEvent e ) {
            try {
                calculateDistribution();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(MultiFactoryInvoiceCalculator.this,
                        "计算出错: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void calculateDistribution() {
        // 验证基础参数
        if ( productSituationList.isEmpty()) {
            throw new IllegalArgumentException("请先加载工厂数据！");
        }

        // 获取基础参数
        BigDecimal salesAmount = MultiFactoryInvoiceCalculationService.parseBigDecimal(salesAmountField.getText(), "销售金额");
        BigDecimal exchangeRate = MultiFactoryInvoiceCalculationService.parseBigDecimal(exchangeRateField.getText(), "汇率");
        // 获取相对分配率
        BigDecimal agentRateInput = MultiFactoryInvoiceCalculationService.parseBigDecimal(agentRelativeRatioField.getText(), "相对分配率");
        
        // 计算代理分成比例
        BigDecimal agentRelativeRatio = agentRateInput.divide(BigDecimal.valueOf(100), MultiFactoryInvoiceCalculationService.CALCULATION_PRECISION, MultiFactoryInvoiceCalculationService.ROUNDING_MODE);
        if (agentRelativeRatio.compareTo(BigDecimal.ZERO) < 0 || agentRelativeRatio.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("相对分配率必须在0-100%之间");
        }
        // 按产品分别计算开票金额并汇总
        BigDecimal totalInvoiceAmount = BigDecimal.ZERO;
        BigDecimal totalClientPaymentInRMB = salesAmount.multiply(exchangeRate);
        
        for (ProductSituation prodSituation : productSituationList) {
//            if (prodSituation.agreeToInvoiceToAgent()) {
//            }
            var productParams = new CalculationParams(
                    prodSituation.salesAmountInForeignCurrency(),
                    exchangeRate,
                    prodSituation.taxRebateRate(),
                    agentRelativeRatio
            );
            CalculationResult productResult = MultiFactoryInvoiceCalculationService.calculateInvoiceAmount(productParams);
            totalInvoiceAmount = totalInvoiceAmount.add(productResult.invoiceAmount());
        }
        

        // 计算参与工厂的总货值
        BigDecimal factoryInvoicingToAgentTotalActualPurchaseAmount = productSituationList.stream()
                .filter( ProductSituation::agreeToInvoiceToAgent )
                .map( ProductSituation::actualPurchaseAmount )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (factoryInvoicingToAgentTotalActualPurchaseAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("没有参与的工厂！");
        }

        // 调用多产品计算方法获得详细结果
        MultiProductCalculationResult multiResult = MultiFactoryInvoiceCalculationService.calculateMultiProductsData(
                totalInvoiceAmount, productSituationList, exchangeRate, agentRelativeRatio );

        // 保存计算结果用于导出
        lastCalculationResult = multiResult;

        // 更新表格显示
        updateTableDisplay( multiResult.productCalculationDetails() );

        // 显示计算结果
        displayCalculationResults(salesAmount, exchangeRate, agentRelativeRatio, agentRateInput,
                totalInvoiceAmount, factoryInvoicingToAgentTotalActualPurchaseAmount, multiResult);

        // 显示资金流转
        displayCashFlowDetails( multiResult.productCalculationDetails() );
        
        // 启用导出按钮
        exportButton.setEnabled(true);
    }


    private void updateTableDisplay( List<ProductCalculationDetail> productCalculationDetails ) {
        // 清空现有数据并重新填充
        tableModel.setRowCount(0);
        
        for (int i = 0; i < productCalculationDetails.size(); i++) {
            ProductCalculationDetail detail = productCalculationDetails.get(i);
            ProductSituation prodSituation = productSituationList.get(i);
            
            // 列：工厂名称, 产品名称, 退税率, 实际货值, 开票金额, 退税金额, 代理利润, 工厂对公应退金额, 工厂对私应退金额
            Object[] row = {
                    detail.factoryName(),
                    detail.productName(),
                    MultiFactoryInvoiceCalculationService.formatPercentage(prodSituation.taxRebateRate()),
                    MultiFactoryInvoiceCalculationService.formatCurrency(detail.actualPurchaseAmount()),
                    MultiFactoryInvoiceCalculationService.formatCurrency(detail.invoiceAmount()),
                    MultiFactoryInvoiceCalculationService.formatCurrency(detail.taxRebateAmount()),
                    MultiFactoryInvoiceCalculationService.formatCurrency(detail.agentProfit()),
                    MultiFactoryInvoiceCalculationService.formatCurrency(detail.prepaymentRefundAmount()),
                    MultiFactoryInvoiceCalculationService.formatCurrency(detail.overpriceRefundFromFactory())
            };
            tableModel.addRow(row);
        }
    }

    private void displayCalculationResults(BigDecimal salesAmount, BigDecimal exchangeRate,
                                           BigDecimal agentRelativeRatio, BigDecimal agentRateInput,
                                           BigDecimal totalInvoiceAmount,
                                           BigDecimal factoryInvoicingToAgentTotalActualPurchaseAmount,
                                           MultiProductCalculationResult multiResult) {
        // 计算客户付款金额
        BigDecimal clientPaymentInRMB = salesAmount.multiply(exchangeRate);
        
        String basicInfo = String.format( """
                ========== 分配计算结果 ==========
                
                【基础参数】
                销售金额: %s %s
                汇率: %s
                客户付款(折人民币): %s 元
                代理分成方式: %s
                代理退税相对分配率: %s
                
                【总体计算】
                开票给代理的工厂的实际采购总货值: %s 元
                总开票金额: %s 元
                总退税金额: %s 元
                代理分成金额: %s 元
                """,
                MultiFactoryInvoiceCalculationService.formatCurrency(salesAmount),
                CURRENCIES[currencyComboBox.getSelectedIndex()],
                MultiFactoryInvoiceCalculationService.formatExchangeRate(exchangeRate),
                MultiFactoryInvoiceCalculationService.formatCurrency(clientPaymentInRMB),
                String.format("相对分配率 %.2f%%", agentRateInput.doubleValue()),
                MultiFactoryInvoiceCalculationService.formatPercentage(agentRelativeRatio),
                MultiFactoryInvoiceCalculationService.formatCurrency(factoryInvoicingToAgentTotalActualPurchaseAmount),
                MultiFactoryInvoiceCalculationService.formatCurrency(totalInvoiceAmount),
                MultiFactoryInvoiceCalculationService.formatCurrency(multiResult.totalTaxRebateAmount()),
                MultiFactoryInvoiceCalculationService.formatCurrency(multiResult.totalAgentProfit())
        );

        StringBuilder sb = new StringBuilder(basicInfo);

        sb.append("\n【工厂分配明细】\n");
        BigDecimal totalRefund = BigDecimal.ZERO;
        for (int i = 0; i < multiResult.productCalculationDetails().size(); i++) {
            ProductCalculationDetail detail = multiResult.productCalculationDetails().get(i);
            ProductSituation prodSituation = productSituationList.get(i);
            
            if (detail.invoiceAmount().compareTo(BigDecimal.ZERO) > 0) {
                sb.append(String.format("""
                        %s - %s:
                          退税率: %s
                          实际货值: %s 元
                          开票金额: %s 元
                          开票溢价: %s 元
                          退税金额: %s 元
                          代理利润: %s 元
                          应退金额: %s 元
                        
                        """,
                        detail.factoryName(),
                        detail.productName(),
                        MultiFactoryInvoiceCalculationService.formatPercentage(prodSituation.taxRebateRate()),
                        MultiFactoryInvoiceCalculationService.formatCurrency(detail.actualPurchaseAmount()),
                        MultiFactoryInvoiceCalculationService.formatCurrency(detail.invoiceAmount()),
                        MultiFactoryInvoiceCalculationService.formatCurrency(detail.invoiceAmount().subtract(detail.actualPurchaseAmount())),
                        MultiFactoryInvoiceCalculationService.formatCurrency(detail.taxRebateAmount()),
                        MultiFactoryInvoiceCalculationService.formatCurrency(detail.agentProfit()),
                        MultiFactoryInvoiceCalculationService.formatCurrency(detail.overpriceRefundFromFactory())
                ));
                totalRefund = totalRefund.add(detail.overpriceRefundFromFactory());
            }
        }

        sb.append(String.format("总应退金额: %s 元\n", MultiFactoryInvoiceCalculationService.formatCurrency(totalRefund)));

        calculationResultArea.setText(sb.toString());
        calculationResultArea.setCaretPosition(0);
    }

    private void displayCashFlowDetails( List<ProductCalculationDetail> productCalculationDetails ) {
        // 计算总体资金流
        BigDecimal totalPrepaid = productSituationList.stream()
                .map( ProductSituation::prepaidAmount )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActualValue = productSituationList.stream()
                .map( ProductSituation::actualPurchaseAmount )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRemaining = totalActualValue.subtract(totalPrepaid);

        String cashFlowHeader = String.format( """
                ========== 资金流转详情 ==========
                
                【总体资金概况】
                所有工厂总货值: %s 元
                已预付定金总额: %s 元
                剩余应付货款: %s 元
                
                【资金流转步骤】
                1. 您已向各工厂支付定金总计: %s 元
                2. 您需向代理公司个人账户汇押金: %s 元
                3. 代理公司向各工厂支付剩余货款
                4. 代理公司收到海关退单后，向该工厂发送开票通知
                5. 工厂向代理公司开具含13%%增值税发票
                6. 代理公司收到海外客户外币货款后，退回押金给您
                7. 代理公司收到退税后向工厂(或委托方——如果是委托方开票的话)支付第二笔货款
                8. 工厂退回定金和差额给您
                """,
                MultiFactoryInvoiceCalculationService.formatCurrency(totalActualValue),
                MultiFactoryInvoiceCalculationService.formatCurrency(totalPrepaid),
                MultiFactoryInvoiceCalculationService.formatCurrency(totalRemaining),
                MultiFactoryInvoiceCalculationService.formatCurrency(totalPrepaid),
                MultiFactoryInvoiceCalculationService.formatCurrency(totalRemaining)
        );

        StringBuilder sb = new StringBuilder(cashFlowHeader);

        sb.append("\n【各工厂资金流详情】\n");
        for (int i = 0; i < productCalculationDetails.size(); i++) {
            ProductCalculationDetail detail = productCalculationDetails.get(i);
            ProductSituation prodSituation = productSituationList.get(i);
            
            if (detail.invoiceAmount().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal remainingPayment = prodSituation.actualPurchaseAmount().subtract(prodSituation.prepaidAmount());
                BigDecimal secondPayment = detail.invoiceAmount().subtract(prodSituation.actualPurchaseAmount());

                sb.append(String.format("""
                        %s - %s:
                          已收定金: %s 元
                          代理付剩余货款: %s 元
                          开票金额: %s 元
                          代理付第二笔: %s 元
                          退回定金: %s 元
                          退回差额: %s 元
                        
                        """,
                        detail.factoryName(),
                        detail.productName(),
                        MultiFactoryInvoiceCalculationService.formatCurrency(prodSituation.prepaidAmount()),
                        MultiFactoryInvoiceCalculationService.formatCurrency(remainingPayment),
                        MultiFactoryInvoiceCalculationService.formatCurrency(detail.invoiceAmount()),
                        MultiFactoryInvoiceCalculationService.formatCurrency(secondPayment),
                        MultiFactoryInvoiceCalculationService.formatCurrency(prodSituation.prepaidAmount()),
                        MultiFactoryInvoiceCalculationService.formatCurrency(detail.overpriceRefundFromFactory())
                ));
            }
        }

        cashFlowResultArea.setText(sb.toString());
        cashFlowResultArea.setCaretPosition(0);
    }

    private BigDecimal parseBigDecimal(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        try {
            return new BigDecimal(text.trim().replaceAll(",", ""));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + "格式不正确！");
        }
    }

    private void setDefaultValues() {
        salesAmountField.setText("170000");
        exchangeRateField.setText("7.1000");
        agentRelativeRatioField.setText("50");
        currencyComboBox.setSelectedIndex(0);
    }

    private void clearAllData() {
        productSituationList.clear();
        tableModel.setRowCount(0);
        csvFilePathField.setText("");
        calculationResultArea.setText("");
        cashFlowResultArea.setText("");
        lastCalculationResult = null;
        exportButton.setEnabled(false);
    }
    
    /**
     * 导出计算结果到CSV文件
     */
    private void exportCalculationResults() {
        if (lastCalculationResult == null) {
            JOptionPane.showMessageDialog(this, "请先进行计算！", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("导出计算结果");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV文件 (*.csv)", "csv"));
        fileChooser.setSelectedFile(new java.io.File("multi_factory_calculation_results.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            try {
                exportToCSV(filePath);
                JOptionPane.showMessageDialog(this, 
                    "计算结果已成功导出到：\n" + filePath, 
                    "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "导出失败：" + e.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 将计算结果写入CSV文件
     */
    private void exportToCSV(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, java.nio.charset.StandardCharsets.UTF_8))) {
            // Adding the BOM character that Excel needs for UTF-8
            writer.write( '\uFEFF' );

            // 写入CSV头部
            writer.write("工厂名称,产品名称,退税率,实际货值,已预付金额,税点,同意开票给代理公司,可超额开票,");
            writer.write("开票金额,退税金额,发货前代理公司须向工厂支付的金额,代理公司收到退税后应向工厂支付的余款金额,");
            writer.write("扣税金额,对公退款金额,对私退款金额");
            writer.newLine();

            Map<String, Map<String, List<ProductSituation>>> prodSituationFactoryProductMap =
                    productSituationList.stream().collect(
                            groupingBy( ProductSituation::factoryName,
                                    groupingBy( ProductSituation::productName )
                            )
                    );
            
            // 写入数据行
            List<ProductCalculationDetail> details = lastCalculationResult.productCalculationDetails();
//            for (int i = 0; i < details.size(); i++) {
            for ( ProductCalculationDetail detail : details ) {
//                ProductCalculationDetail detail = details.get(i);
//                ProductSituation prodSituation = productSituationList.get(i);

                // 按照实际业务逻辑，如下Map中每一个List中应该有且仅有1个ProductSituation对象element
                String factoryName = detail.factoryName();
                String productName = detail.productName();
                ProductSituation prodSituation = prodSituationFactoryProductMap.get( factoryName ).get( productName ).getFirst();
                
                StringBuilder row = new StringBuilder();
                row.append(escapeCSVField(factoryName)).append(",");
                row.append(escapeCSVField(productName)).append(",");
                row.append(formatCSVPercentage(prodSituation.taxRebateRate())).append(",");
                row.append(formatCSVNumber(detail.actualPurchaseAmount())).append(",");
                row.append(formatCSVNumber(prodSituation.prepaidAmount())).append(",");
                row.append(formatCSVPercentage(prodSituation.taxPoint())).append(",");
                row.append(prodSituation.agreeToInvoiceToAgent() ? "是" : "否").append(",");
                row.append(prodSituation.ableToInvoiceWithOverprice() ? "是" : "否").append(",");
                row.append(formatCSVNumber(detail.invoiceAmount())).append(",");
                row.append(formatCSVNumber(detail.taxRebateAmount())).append(",");
                row.append(formatCSVNumber(detail.agentBalanceToFactoryBeforeShipment())).append(",");
                row.append(formatCSVNumber(detail.agentBalanceToFactoryAfterRebating())).append(",");
                row.append(formatCSVNumber(detail.overpriceTax())).append(",");
                row.append(formatCSVNumber(detail.prepaymentRefundAmount())).append(",");
                row.append(formatCSVNumber(detail.overpriceRefundFromFactory()));
                
                writer.write(row.toString());
                writer.newLine();
            }
        }
    }
    
    /**
     * 转义CSV字段中的特殊字符
     */
    private String escapeCSVField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
    
    /**
     * 格式化CSV中的数字（去掉千位分隔符）
     */
    private String formatCSVNumber(BigDecimal number) {
        if (number == null) return "0";
        return number.setScale(2, RoundingMode.HALF_UP).toString();
    }
    
    /**
     * 格式化CSV中的百分比数字
     */
    private String formatCSVPercentage(BigDecimal percentage) {
        if (percentage == null) return "0";
        return percentage.multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP).toString() + "%";
    }
    
    /**
     * 返回单工厂计算器
     */
    private void backToSingleCalculator() {
        // 隐藏当前窗口
        this.setVisible(false);
        
        // 显示单工厂计算器
        SwingUtilities.invokeLater(() -> {
            SingleSupplierInvoiceCalculator.getInstance().setVisible(true);
        });
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set Look and Feel");
        }

        SwingUtilities.invokeLater(() -> {
            getInstance().setVisible(true);
        });
    }
}
