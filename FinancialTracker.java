import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Financial Tracker - Main Application
 * 2nd Year, 4th Semester Project
 * Technology: Java Swing + MySQL JDBC
 */
public class FinancialTracker extends JFrame {

    // DB Connection
private static final String DB_URL = "jdbc:mysql://localhost:3306/financial_tracker";
private static final String DB_USER = "root";
private static final String DB_PASS = "Prince@807";  // ← fix this

    private Connection conn;
    private int currentUserId = 1; // Default user (after login)

    // UI Components
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JLabel totalIncomeLabel, totalExpenseLabel, balanceLabel;
    private JTextField amountField, descField;
    private JComboBox<String> categoryBox, typeBox;
    private JTextField dateField;

    public FinancialTracker() {
        connectDB();
        buildUI();
        loadTransactions();
        updateSummary();
    }

    // ─── DB Connection ───────────────────────────────────────────
    private void connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "DB Error: " + e.getMessage());
        }
    }

    // ─── Build UI ────────────────────────────────────────────────
    private void buildUI() {
        setTitle("💰 Financial Tracker - Prince");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top - Summary Panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        summaryPanel.setBackground(new Color(30, 30, 50));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        totalIncomeLabel  = makeSummaryCard("Total Income",  "₹0.00", new Color(39, 174, 96));
        totalExpenseLabel = makeSummaryCard("Total Expense", "₹0.00", new Color(192, 57, 43));
        balanceLabel      = makeSummaryCard("Balance",       "₹0.00", new Color(41, 128, 185));

        summaryPanel.add(totalIncomeLabel.getParent());
        summaryPanel.add(totalExpenseLabel.getParent());
        summaryPanel.add(balanceLabel.getParent());
        add(summaryPanel, BorderLayout.NORTH);

        // Center - Transaction Table
        String[] cols = {"ID", "Date", "Type", "Category", "Amount", "Description"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(28);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        transactionTable.getTableHeader().setBackground(new Color(52, 73, 94));
        transactionTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom - Add Transaction Panel
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        inputPanel.setBackground(new Color(44, 62, 80));
        inputPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), "Add Transaction",
            0, 0, new Font("Segoe UI", Font.BOLD, 12), Color.WHITE));

        typeBox = new JComboBox<>(new String[]{"expense", "income"});
        categoryBox = new JComboBox<>();
        loadCategories();

        amountField = new JTextField(8);
        descField = new JTextField(12);
        dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()), 10);

        JButton addBtn = new JButton("➕ Add");
        addBtn.setBackground(new Color(39, 174, 96));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.addActionListener(e -> addTransaction());

        JButton deleteBtn = new JButton("🗑 Delete");
        deleteBtn.setBackground(new Color(192, 57, 43));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        deleteBtn.addActionListener(e -> deleteTransaction());

        JButton analyzeBtn = new JButton("📊 Analyze (Python AI)");
        analyzeBtn.setBackground(new Color(142, 68, 173));
        analyzeBtn.setForeground(Color.WHITE);
        analyzeBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        analyzeBtn.addActionListener(e -> runPythonAnalysis());

        inputPanel.add(label("Type:")); inputPanel.add(typeBox);
        inputPanel.add(label("Category:")); inputPanel.add(categoryBox);
        inputPanel.add(label("Amount:")); inputPanel.add(amountField);
        inputPanel.add(label("Desc:")); inputPanel.add(descField);
        inputPanel.add(label("Date:")); inputPanel.add(dateField);
        inputPanel.add(addBtn);
        inputPanel.add(deleteBtn);
        inputPanel.add(analyzeBtn);

        add(inputPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return l;
    }

    private JLabel makeSummaryCard(String title, String value, Color color) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        card.setPreferredSize(new Dimension(200, 70));

        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titleLbl.setForeground(Color.WHITE);

        JLabel valueLbl = new JLabel(value, SwingConstants.CENTER);
        valueLbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLbl.setForeground(Color.WHITE);

        card.add(titleLbl);
        card.add(valueLbl);
        return valueLbl;
    }

    // ─── Load Data ───────────────────────────────────────────────
    private void loadCategories() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT category_name FROM categories ORDER BY category_id");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) categoryBox.addItem(rs.getString("category_name"));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadTransactions() {
        tableModel.setRowCount(0);
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT t.transaction_id, t.transaction_date, t.type, c.category_name, " +
                "t.amount, t.description FROM transactions t " +
                "JOIN categories c ON t.category_id = c.category_id " +
                "WHERE t.user_id = ? ORDER BY t.transaction_date DESC");
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("transaction_id"),
                    rs.getString("transaction_date"),
                    rs.getString("type"),
                    rs.getString("category_name"),
                    "₹" + rs.getDouble("amount"),
                    rs.getString("description")
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateSummary() {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT type, SUM(amount) as total FROM transactions " +
                "WHERE user_id = ? GROUP BY type");
            ps.setInt(1, currentUserId);
            ResultSet rs = ps.executeQuery();
            double income = 0, expense = 0;
            while (rs.next()) {
                if (rs.getString("type").equals("income")) income = rs.getDouble("total");
                else expense = rs.getDouble("total");
            }
            totalIncomeLabel.setText("₹" + String.format("%.2f", income));
            totalExpenseLabel.setText("₹" + String.format("%.2f", expense));
            balanceLabel.setText("₹" + String.format("%.2f", income - expense));
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── CRUD Operations ─────────────────────────────────────────
    private void addTransaction() {
        try {
            String category = (String) categoryBox.getSelectedItem();
            PreparedStatement ps1 = conn.prepareStatement(
                "SELECT category_id FROM categories WHERE category_name = ?");
            ps1.setString(1, category);
            ResultSet rs = ps1.executeQuery();
            int catId = rs.next() ? rs.getInt("category_id") : 10;

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO transactions (user_id, category_id, amount, type, description, transaction_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)");
            ps.setInt(1, currentUserId);
            ps.setInt(2, catId);
            ps.setDouble(3, Double.parseDouble(amountField.getText().trim()));
            ps.setString(4, (String) typeBox.getSelectedItem());
            ps.setString(5, descField.getText().trim());
            ps.setString(6, dateField.getText().trim());
            ps.executeUpdate();

            loadTransactions();
            updateSummary();
            amountField.setText("");
            descField.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void deleteTransaction() {
        int row = transactionTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a row to delete."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM transactions WHERE transaction_id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            loadTransactions();
            updateSummary();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ─── Python AI Analysis ──────────────────────────────────────
    private void runPythonAnalysis() {
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "python/analysis.py");
            pb.redirectErrorStream(true);
            Process proc = pb.start();
            byte[] output = proc.getInputStream().readAllBytes();
            JTextArea area = new JTextArea(new String(output), 20, 50);
            area.setFont(new Font("Monospaced", Font.PLAIN, 12));
            JOptionPane.showMessageDialog(this, new JScrollPane(area),
                "📊 AI Spending Pattern Analysis", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Run: python3 python/analysis.py manually.\n" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FinancialTracker::new);
    }
}
