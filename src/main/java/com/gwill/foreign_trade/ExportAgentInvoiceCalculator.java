package com.gwill.foreign_trade;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * 外贸代理开票金额计算器
 * 基于JDK 22开发的Swing GUI应用程序
 *
 * 计算公式: invoiceAmount = (salesAmount × exchangeRate) ÷ [1 - taxRefundRate × (1 - agentRatio)]
 * 简写为：X = (S × E) ÷ [1 - R × (1 - A)]
 * 其中: S=销售金额(美元), E=汇率, R=退税率, A=代理分成比例, X=开票金额
 */
public class ExportAgentInvoiceCalculator extends JFrame {

    // 精度设置
    private static final int CALCULATION_PRECISION = 10;
    private static final int DISPLAY_PRECISION = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    // 货币选项
    private static final String[] CURRENCIES = {"USD", "EUR", "GBP", "JPY", "CNY", "RUB"};
    private static final String[] CURRENCY_NAMES = {
            "美元 (USD)", "欧元 (EUR)", "英镑 (GBP)",
            "日元 (JPY)", "人民币 (CNY)", "俄罗斯卢布 (RUB)"
    };

    private static final String DETAILED_RESULT_PANEL_KEY = "detailed";
    private static final String SIMPLE_RESULT_PANEL_KEY = "simple";

    // GUI组件
    private JTextField purchaseAmountField;  // 采购金额
    private JTextField salesAmountField;     // 销售金额
    private JComboBox<String> currencyComboBox;     // 货币选项
    private JTextField exchangeRateField;    // 汇率
    private JTextField taxRefundRateField;   // 退税率
    private JTextField agentRelativeRatioField;      // 相对分成率字段
    private JTextField agentAbsoluteRateField;       // 绝对分成率字段

    // 显示模式单选按钮组
    private JRadioButton simpleModeRadioBtn;         // 简洁模式
    private JRadioButton detailedModeRadioBtn;       // 详细模式
    private ButtonGroup modeGroup;

    // 单选按钮组
    private JRadioButton relativeRatioRadioBtn;  // 相对分配率
    private JRadioButton absoluteRateRadioBtn;  // 绝对分配率
    private ButtonGroup rateTypeGroup;

    // 显示模式控制
    private JPanel purchaseAmountPanel;              // 采购金额面板
    private JTextArea detailedResultArea;            // 详细结果显示区域
    private JTextField simpleResultInvoiceAmountField;            // 简洁结果开票金额显示字段
    private JPanel detailedResultPanel;              // 详细结果面板
    private JPanel simpleResultPanel;                // 简洁结果面板

    // 计算按钮
    private JButton calculateButton;
    private JButton clearButton;

    // 数字格式化器
    private DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
    private DecimalFormat percentFormat = new DecimalFormat("0.00%");
    private DecimalFormat exchangeRateFormat = new DecimalFormat("#,##0.0000");

    public ExportAgentInvoiceCalculator () throws HeadlessException {
        initializeGUI();
    }

    private void initializeGUI () {
        setTitle("外贸代理开票金额计算器 v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 创建主面板
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 显示模式选择面板
        JPanel modePanel = createModePanel();
        gbc.gridx = 0; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; gbc.weighty = 0.0;
        mainPanel.add(modePanel, gbc);

        // 输入参数面板
        JPanel inputPanel = createInputPanel();
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(inputPanel, gbc);

        // 分成方式选择面板
        JPanel rateTypePanel = createRateTypePanel();
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(rateTypePanel, gbc);

        // 按钮面板
        JPanel buttonPanel = createButtonPanel();
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(buttonPanel, gbc);

        // 结果显示面板
        JPanel resultPanel = createResultPanel();
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        mainPanel.add(resultPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // 设置默认值
        setDefaultValues();

        // 初始化显示模式
        initializeDisplayMode();

        // 设置窗口属性
        setSize(650, 750);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JPanel createModePanel () {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(new TitledBorder("显示模式"));

        // 创建模式选择单选按钮组
        simpleModeRadioBtn = new JRadioButton("简洁模式（仅计算开票金额）", true);
        detailedModeRadioBtn = new JRadioButton("详细模式（显示完整计算过程和盈利分析）", false);
        modeGroup = new ButtonGroup();
        modeGroup.add(simpleModeRadioBtn);
        modeGroup.add(detailedModeRadioBtn);

        // 添加事件监听器
        simpleModeRadioBtn.addActionListener(_ -> toggleDisplayMode());
        detailedModeRadioBtn.addActionListener(_ -> toggleDisplayMode());

        panel.add(simpleModeRadioBtn);
        panel.add(detailedModeRadioBtn);

        return panel;
    }

    private JPanel createInputPanel () {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("基础参数输入"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // 采购金额（可隐藏）
        purchaseAmountPanel = new JPanel(new GridBagLayout());
        GridBagConstraints purchaseGbc = new GridBagConstraints();
        purchaseGbc.insets = new Insets(0, 0, 0, 0);
        purchaseGbc.anchor = GridBagConstraints.WEST;

        purchaseGbc.gridx = 0;
        purchaseAmountPanel.add(new JLabel("采购金额 (人民币):"), purchaseGbc);
        purchaseGbc.gridx = 1; purchaseGbc.fill = GridBagConstraints.HORIZONTAL; purchaseGbc.weightx = 1.0;
        purchaseAmountField = createAmountTextField();
        purchaseAmountPanel.add(purchaseAmountField, purchaseGbc);
        purchaseGbc.gridx = 2; purchaseGbc.fill = GridBagConstraints.NONE; purchaseGbc.weightx = 0.0;
        purchaseAmountPanel.add(new JLabel("元"), purchaseGbc);

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(purchaseAmountPanel, gbc);

        // 销售金额
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("销售金额:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        salesAmountField = createAmountTextField();
        panel.add(salesAmountField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        currencyComboBox = new JComboBox<>(CURRENCY_NAMES);
        currencyComboBox.setSelectedIndex(0); // 默认选择USD
        panel.add(currencyComboBox, gbc);

        // 汇率
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel exchangeRateLabel = new JLabel("汇率 (CNY/外币):");
        panel.add(exchangeRateLabel, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        exchangeRateField = new JTextField(15);
        exchangeRateField.setHorizontalAlignment( JTextField.RIGHT );
        panel.add(exchangeRateField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel(""), gbc);

        // 退税率
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("退税率:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        taxRefundRateField = new JTextField(15);
        taxRefundRateField.setHorizontalAlignment( JTextField.RIGHT );
        panel.add(taxRefundRateField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("%"), gbc);

        return panel;
    }

    private JPanel createRateTypePanel () {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("代理分成方式"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 3, 3, 3);
        gbc.anchor = GridBagConstraints.WEST;

        // 创建单选按钮组
        relativeRatioRadioBtn = new JRadioButton("相对分配率 (占退税金额的百分比)", true);
        absoluteRateRadioBtn = new JRadioButton("绝对分配率 (直接输入分给代理的退税率)");
        rateTypeGroup = new ButtonGroup();
        rateTypeGroup.add( relativeRatioRadioBtn );
        rateTypeGroup.add( absoluteRateRadioBtn );

        // 相对分配率行
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(relativeRatioRadioBtn, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        agentRelativeRatioField = new JTextField(10);
        agentRelativeRatioField.setHorizontalAlignment(JTextField.RIGHT);
        panel.add(agentRelativeRatioField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("%"), gbc);

        // 绝对分配率行
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(absoluteRateRadioBtn, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        agentAbsoluteRateField = new JTextField(10);
        agentAbsoluteRateField.setHorizontalAlignment(JTextField.RIGHT);
        panel.add(agentAbsoluteRateField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        panel.add(new JLabel("%"), gbc);

        // 添加说明标签
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel helpLabel = new JLabel("<html><small>" +
                "• 相对分配率: 如输入50，表示代理获得退税金额的50%<br>" +
                "• 绝对分配率: 如输入6.5，表示代理获得开票金额×6.5%的固定金额" +
                "</small></html>");
        helpLabel.setForeground(Color.GRAY);
        panel.add(helpLabel, gbc);

        return panel;
    }

    private JTextField createAmountTextField () {
        JTextField amountField = new JTextField(15);
        amountField.setHorizontalAlignment(JTextField.RIGHT);

        // 添加失焦事件监听器，格式化数字显示
        amountField.addFocusListener( new FocusAdapter() {
            @Override
            public void focusLost ( FocusEvent e ) {
                formatAmountField( amountField );
            }

            @Override
            public void focusGained ( FocusEvent e ) {
                formatAmountFieldAsNormalNumber( amountField );
            }
        } );
        return amountField;
    }

    private void formatAmountField ( JTextField amountField ) {
        String strAmount = amountField.getText().trim();
        if ( !strAmount.isEmpty() ) {
            try {
                // 尝试解析并格式化
                BigDecimal amount = new BigDecimal( strAmount.replaceAll( ",", "" ) );
                amountField.setText( currencyFormat.format( amount ) );
            } catch ( NumberFormatException e ) {
                // 如果解析失败，保持原文本
            }
        }
    }

    private void formatAmountFieldAsNormalNumber ( JTextField amountField ) {
        String strAmount = amountField.getText().trim();
        if ( !strAmount.isEmpty() ) {
            try {
                // 尝试解析并格式化
                BigDecimal amount = new BigDecimal( strAmount.replaceAll( ",", "" ) );
                amountField.setText( amount.setScale( DISPLAY_PRECISION, ROUNDING_MODE ).toString() );
            } catch ( NumberFormatException e ) {
                // 如果解析失败，保持原文本
            }
        }
    }

    private JPanel createButtonPanel () {
        JPanel panel = new JPanel(new FlowLayout());

        calculateButton = new JButton("计算开票金额");
        calculateButton.setPreferredSize(new Dimension(120, 30));
        calculateButton.addActionListener(new CalculateButtonListener());

        clearButton = new JButton("清空重置");
        clearButton.setPreferredSize(new Dimension(120, 30));
        clearButton.addActionListener(_ -> clearAllFields());

        panel.add(calculateButton);
        panel.add(clearButton);

        return panel;
    }

    private JPanel createResultPanel () {
        JPanel panel = new JPanel(new CardLayout());

        // 详细结果面板
        detailedResultPanel = new JPanel(new BorderLayout());
        detailedResultPanel.setBorder(new TitledBorder("详细计算结果"));

        detailedResultArea = new JTextArea(15, 50);
        detailedResultArea.setEditable(false);
        detailedResultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailedResultArea.setBackground(new Color(248, 248, 248));

        JScrollPane scrollPane = new JScrollPane(detailedResultArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        detailedResultPanel.add(scrollPane, BorderLayout.CENTER);

        // 简洁结果面板
        simpleResultPanel = new JPanel(new GridBagLayout());
        simpleResultPanel.setBorder(new TitledBorder("开票金额"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        gbc.gridx = 0; gbc.gridy = 0;
        simpleResultPanel.add(new JLabel("应开票金额 (人民币):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        simpleResultInvoiceAmountField = new JTextField(15);
        simpleResultInvoiceAmountField.setEditable(false);
        simpleResultInvoiceAmountField.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        simpleResultInvoiceAmountField.setHorizontalAlignment(JTextField.CENTER);
        simpleResultInvoiceAmountField.setBackground(new Color(240, 255, 240));
        simpleResultPanel.add(simpleResultInvoiceAmountField, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0.0;
        JLabel unitLabel = new JLabel("元");
        unitLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        simpleResultPanel.add(unitLabel, gbc);

        panel.add(detailedResultPanel, DETAILED_RESULT_PANEL_KEY);
        panel.add(simpleResultPanel, SIMPLE_RESULT_PANEL_KEY);

        return panel;
    }

    private void initializeDisplayMode () {
        // 确保初始状态正确
        toggleDisplayMode();
    }

    private void toggleDisplayMode () {
        boolean isDetailedMode = detailedModeRadioBtn.isSelected();

        // 控制采购金额面板的显示
        purchaseAmountPanel.setVisible(isDetailedMode);

        // 切换结果显示面板
        CardLayout cardLayout = (CardLayout) ((JPanel) detailedResultPanel.getParent()).getLayout();
        if (isDetailedMode) {
            cardLayout.show(detailedResultPanel.getParent(), DETAILED_RESULT_PANEL_KEY);
            formatAmountField( purchaseAmountField );
        } else {
            cardLayout.show(detailedResultPanel.getParent(), SIMPLE_RESULT_PANEL_KEY);
        }
        formatAmountField( salesAmountField );

        // 清空结果
        detailedResultArea.setText("");
        simpleResultInvoiceAmountField.setText("");

        // 重新布局
        revalidate();
        repaint();
    }

    private void setDefaultValues () {
        purchaseAmountField.setText("1000000");
        salesAmountField.setText("170000");
        exchangeRateField.setText("7.1000");
        taxRefundRateField.setText("13");
        agentRelativeRatioField.setText("50");
        agentAbsoluteRateField.setText("6.5");
        currencyComboBox.setSelectedIndex(0); // USD
    }

    private void clearAllFields () {
        purchaseAmountField.setText("");
        salesAmountField.setText("");
        exchangeRateField.setText("");
        taxRefundRateField.setText("");
        agentRelativeRatioField.setText("");
        agentAbsoluteRateField.setText("");
        detailedResultArea.setText("");
        simpleResultInvoiceAmountField.setText("");
        relativeRatioRadioBtn.setSelected(true);
        currencyComboBox.setSelectedIndex(0);

        // 重新初始化显示模式
        toggleDisplayMode();
    }

    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed ( ActionEvent e ) {
            try {
                calculateInvoiceAmount();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(ExportAgentInvoiceCalculator.this,
                        "计算出错: " + ex.getMessage(),
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void calculateInvoiceAmount () {
        // 获取输入值
        BigDecimal purchaseAmountInRMB = detailedModeRadioBtn.isSelected() ?
                parseBigDecimal(purchaseAmountField.getText(), "采购金额") :
                null;
        BigDecimal salesAmount = parseBigDecimal(salesAmountField.getText(), "销售金额");
        BigDecimal exchangeRate = parseBigDecimal(exchangeRateField.getText(), "汇率");
        BigDecimal taxRefundRate = parseBigDecimal(taxRefundRateField.getText(), "退税率")
                .divide(BigDecimal.valueOf(100), CALCULATION_PRECISION, ROUNDING_MODE);

        // 根据选择的分成方式获取对应的输入值
        BigDecimal agentRateInput;
        if (relativeRatioRadioBtn.isSelected()) {
            agentRateInput = parseBigDecimal(agentRelativeRatioField.getText(), "相对分配率");
        } else {
            agentRateInput = parseBigDecimal(agentAbsoluteRateField.getText(), "绝对分配率");
        }

        // 验证输入
        validateInputs(purchaseAmountInRMB, salesAmount, exchangeRate, taxRefundRate, agentRateInput);

        // 计算代理分成比例
        BigDecimal agentRatio;
        if ( relativeRatioRadioBtn.isSelected()) {
            // 相对分配率
            agentRatio = agentRateInput.divide(BigDecimal.valueOf(100), CALCULATION_PRECISION, ROUNDING_MODE);
            if (agentRatio.compareTo(BigDecimal.ZERO) < 0 || agentRatio.compareTo(BigDecimal.ONE) > 0) {
                throw new IllegalArgumentException("相对分配率必须在0-100%之间");
            }
        } else {
            // 绝对分配率
            agentRatio = convertAbsoluteRateToRelativeRatio(salesAmount, exchangeRate, taxRefundRate, agentRateInput);
        }

        // 验证分母不为零
        BigDecimal denominator = BigDecimal.ONE.subtract(
                taxRefundRate.multiply(BigDecimal.ONE.subtract(agentRatio))
        );
        if (denominator.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("参数组合导致计算公式分母为0，计算无解！请调整代理分成比例，重新计算。");
        }

        // 计算开票金额: invoiceAmountInRMB = (salesAmount × exchangeRate) ÷ [1 - taxRefundRate × (1 - agentRatio)]
        BigDecimal clientPaymentInRMB = salesAmount.multiply(exchangeRate);
        BigDecimal invoiceAmountInRMB = clientPaymentInRMB.divide(denominator, CALCULATION_PRECISION, ROUNDING_MODE);

        // 计算相关数值
        BigDecimal taxRefundAmountInRMB = invoiceAmountInRMB.multiply(taxRefundRate);
        BigDecimal agentProfitInRMB = taxRefundAmountInRMB.multiply(agentRatio);
        // 显示结果
        if (detailedModeRadioBtn.isSelected()) {
            BigDecimal yourTotalIncomeInRMB = calculateYourTotalIncome( salesAmount, exchangeRate, taxRefundAmountInRMB, agentProfitInRMB );
            BigDecimal yourNetProfitInRMB = calculateYourNetProfit( purchaseAmountInRMB, salesAmount, exchangeRate,
                    taxRefundAmountInRMB, agentProfitInRMB );
            BigDecimal yourGrossMarkup = yourNetProfitInRMB.divide( purchaseAmountInRMB, CALCULATION_PRECISION, ROUNDING_MODE )
                    .multiply( BigDecimal.valueOf(100) );
            displayDetailedResults(purchaseAmountInRMB, salesAmount, exchangeRate, taxRefundRate, agentRatio,
                    agentRateInput, invoiceAmountInRMB, taxRefundAmountInRMB, agentProfitInRMB,
                    clientPaymentInRMB, yourTotalIncomeInRMB, yourNetProfitInRMB, yourGrossMarkup);
        } else {
            displaySimpleResult(invoiceAmountInRMB);
        }
    }

    /**
     * 计算以人民币为货币单位的你（委托方）的总收款额。
     * @param salesAmount 以外币为货币单位的PI上的销售额
     * @param exchangeRate 汇率
     * @param taxRefundAmountInRMB 以人民币为货币单位的退税额
     * @param agentProfitInRMB 以人民币为货币单位的须分给代理公司的退税额
     * @return 以人民币为货币单位的你（委托方）的总收款额
     */
    private BigDecimal calculateYourTotalIncome (
            BigDecimal salesAmount, BigDecimal exchangeRate, BigDecimal taxRefundAmountInRMB,
            BigDecimal agentProfitInRMB ) {
        BigDecimal salesAmountInRMB = salesAmount.multiply( exchangeRate );
        BigDecimal yourTotalIncomeInRMB = salesAmountInRMB.add( taxRefundAmountInRMB );
        yourTotalIncomeInRMB = yourTotalIncomeInRMB.subtract( agentProfitInRMB );

        return yourTotalIncomeInRMB;
    }

    /**
     * 计算以人民币为货币单位的你（委托方）的总利润额。
     * @param purchaseAmountInRMB 以人民币为货币单位的采购成本
     * @param salesAmount 以外币为货币单位的PI上的销售额
     * @param exchangeRate 汇率
     * @param taxRefundAmountInRMB 以人民币为货币单位的退税额
     * @param agentProfitInRMB 以人民币为货币单位的须分给代理公司的退税额
     * @return 以人民币为货币单位的你（委托方）的总利润额
     */
    private BigDecimal calculateYourNetProfit (
            BigDecimal purchaseAmountInRMB, BigDecimal salesAmount, BigDecimal exchangeRate,
            BigDecimal taxRefundAmountInRMB, BigDecimal agentProfitInRMB ) {
        BigDecimal salesAmountInRMB = salesAmount.multiply( exchangeRate );
        BigDecimal yourNetProfitInRMB = salesAmountInRMB.subtract( purchaseAmountInRMB );
        yourNetProfitInRMB = yourNetProfitInRMB.add( taxRefundAmountInRMB );
        yourNetProfitInRMB = yourNetProfitInRMB.subtract( agentProfitInRMB );

        return yourNetProfitInRMB;
    }

    private void validateInputs (
            BigDecimal purchaseAmount, BigDecimal salesAmount, BigDecimal exchangeRate,
            BigDecimal taxRefundRate, BigDecimal agentRateInput ) {
        if ( detailedModeRadioBtn.isSelected() ) {
            if ( purchaseAmount == null || purchaseAmount.compareTo( BigDecimal.ZERO ) <= 0 ) {
                throw new IllegalArgumentException("采购金额必须大于0");
            }
        }

        if (salesAmount.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("销售金额必须大于0");
        if (exchangeRate.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("汇率必须大于0");
        if (taxRefundRate.compareTo(BigDecimal.ZERO) <= 0 || taxRefundRate.compareTo(BigDecimal.ONE) >= 0)
            throw new IllegalArgumentException("退税率必须在0-100%之间");
        if (agentRateInput.compareTo(BigDecimal.ZERO) < 0 || agentRateInput.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("代理分成率必须在0-100%之间");
    }

    /**
     * 将用户手工输入的分配给代理公司的绝对退税率换算成退税金额中分配给代理公司的相对分配率（百分比）。
     * @param salesAmount 与海外客户签订的PI中的外汇金额
     * @param exchangeRate 汇率
     * @param taxRefundRate 该款产品的海关退税率
     * @param agentRateInput 用户手工输入的分配给代理公司的绝对退税率
     * @return 退税金额中分配给代理公司的相对分配率（百分比）
     */
    private BigDecimal convertAbsoluteRateToRelativeRatio (
            BigDecimal salesAmount, BigDecimal exchangeRate, BigDecimal taxRefundRate, BigDecimal agentRateInput ) {
        BigDecimal agentRate = agentRateInput.divide( BigDecimal.valueOf( 100 ), CALCULATION_PRECISION, ROUNDING_MODE );
        if ( agentRate.compareTo( BigDecimal.ZERO ) < 0 || agentRate.compareTo( taxRefundRate ) > 0 ) {
            String errorMsg = String.format( "代理分成绝对退税率必须在0-%.2f%%之间",
                    taxRefundRate.multiply( BigDecimal.valueOf( 100 ) ).doubleValue() );
            throw new IllegalArgumentException( errorMsg );
        }
        BigDecimal relativeAgentRatio = agentRate.divide( taxRefundRate, CALCULATION_PRECISION, ROUNDING_MODE );
        return relativeAgentRatio;
    }

    private BigDecimal parseBigDecimal ( String text, String fieldName ) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "不能为空");
        }
        try {
            BigDecimal value = new BigDecimal( text.trim().replaceAll( ",", "" ) );
            if ( value.scale() > CALCULATION_PRECISION ) {
                value = value.setScale( CALCULATION_PRECISION, ROUNDING_MODE );
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(fieldName + "格式不正确！请输入数字！");
        }
    }

    private void displaySimpleResult ( BigDecimal invoiceAmountInRMB ) {
        simpleResultInvoiceAmountField.setText( currencyFormat.format( invoiceAmountInRMB ) );
    }

    private void displayDetailedResults (
            BigDecimal purchaseAmountInRMB, BigDecimal salesAmount, BigDecimal exchangeRate,
            BigDecimal taxRefundRate, BigDecimal agentRatio, BigDecimal agentRateInput,
            BigDecimal invoiceAmountInRMB, BigDecimal taxRefundAmountInRMB, BigDecimal agentProfitInRMB,
            BigDecimal clientPaymentInRMB, BigDecimal yourTotalIncomeInRMB, BigDecimal yourNetProfitInRMB,
            BigDecimal yourGrossMarkup ) {
        StringBuilder sb = new StringBuilder();
        sb.append("========== 计算结果 ==========\n\n");

        // 输入参数回显
        sb.append("【输入参数】\n");
        sb.append(String.format("采购金额: %s 元\n", currencyFormat.format(purchaseAmountInRMB)));
        sb.append(String.format("销售金额: %s %s\n", currencyFormat.format(salesAmount),
                CURRENCIES[currencyComboBox.getSelectedIndex()]));
        sb.append(String.format("当前汇率: %s\n", exchangeRateFormat.format(exchangeRate)));
        sb.append(String.format("退税率: %s\n", percentFormat.format(taxRefundRate)));

        if ( relativeRatioRadioBtn.isSelected()) {
            sb.append(String.format("代理退税分成方式: 相对分配率 %.2f%%\n", agentRateInput.doubleValue()));
        } else {
            sb.append(String.format("代理退税分成方式: 绝对分配率 %.2f%%\n", agentRateInput.doubleValue()));
            sb.append(String.format("等效退税相对分成比例: %s\n", percentFormat.format(agentRatio)));
        }

        sb.append("\n【核心计算结果】\n");
        sb.append(String.format("★ 开票金额: %s 元\n", currencyFormat.format(invoiceAmountInRMB)));
        sb.append(String.format("海外客户付款（折合人民币）: %s 元\n", currencyFormat.format(clientPaymentInRMB)));
        sb.append(String.format("开票溢价: %s 元\n", currencyFormat.format(invoiceAmountInRMB.subtract(purchaseAmountInRMB))));

        sb.append("\n【退税相关】\n");
        sb.append(String.format("退税总金额: %s 元\n", currencyFormat.format(taxRefundAmountInRMB)));
        sb.append(String.format("代理公司利润: %s 元\n", currencyFormat.format(agentProfitInRMB)));
        sb.append(String.format("您分享的退税: %s 元\n", currencyFormat.format(taxRefundAmountInRMB.subtract(agentProfitInRMB))));

        sb.append("\n【盈利分析】\n");
        sb.append(String.format("您的总收入: %s 元\n", currencyFormat.format(yourTotalIncomeInRMB)));
        sb.append(String.format("您的利润额: %s 元\n", currencyFormat.format(yourNetProfitInRMB)));
        sb.append(String.format("您的毛成本利润率: %.2f%%\n", yourGrossMarkup.doubleValue()));

        sb.append("\n【资金流验证】\n");
        BigDecimal agentIncome = clientPaymentInRMB.add(taxRefundAmountInRMB);
        BigDecimal agentExpense = invoiceAmountInRMB;
        BigDecimal agentNetProfit = agentIncome.subtract(agentExpense);

        sb.append(String.format("代理公司收入: %s + %s = %s 元\n",
                currencyFormat.format(clientPaymentInRMB), currencyFormat.format(taxRefundAmountInRMB),
                currencyFormat.format(agentIncome)));
        sb.append(String.format("代理公司支出: %s 元\n", currencyFormat.format(agentExpense)));
        sb.append(String.format("代理公司净利润: %s 元\n", currencyFormat.format(agentNetProfit)));
        sb.append(String.format("验证结果: %s ≈ %s ",
                currencyFormat.format(agentNetProfit), currencyFormat.format(agentProfitInRMB)));
        sb.append(agentNetProfit.subtract(agentProfitInRMB).abs().compareTo(BigDecimal.valueOf( 0.01 )) < 0 ? "✓" : "✗");

        sb.append("\n\n【计算公式】\n");
        sb.append("invoiceAmountInRMB = (salesAmount × exchangeRate) ÷ [1 - taxRefundRate × (1 - agentRatio)]\n");
        sb.append(String.format("invoiceAmountInRMB = (%s × %s) ÷ [1 - %s × (1 - %s)]\n",
                currencyFormat.format(salesAmount), exchangeRateFormat.format(exchangeRate),
                percentFormat.format(taxRefundRate), percentFormat.format(agentRatio)));
        sb.append(String.format("invoiceAmountInRMB = %s ÷ %s = %s 元\n",
                currencyFormat.format(clientPaymentInRMB),
                currencyFormat.format(BigDecimal.ONE.subtract(taxRefundRate.multiply(BigDecimal.ONE.subtract(agentRatio)))),
                currencyFormat.format(invoiceAmountInRMB)));

        detailedResultArea.setText(sb.toString());
        detailedResultArea.setCaretPosition(0);
    }

    public static void main ( String[] args ) {
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 使用默认外观
            System.err.println("Failed in customizing Look and Feel, so, just use default Look and Feel.");
        }

        // 启动GUI
        SwingUtilities.invokeLater( () -> {
            new ExportAgentInvoiceCalculator().setVisible( true );
        } );
    }
}
