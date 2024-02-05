package My_Projects;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.awt.Color;


public class BankingSystem extends JFrame {

    private JTextField accountNumberField, amountField, transferAccountField;
    private JTextArea transactionHistoryArea;

    public BankingSystem() {
        initializeUI();
        initializeDatabase();
    }

    private void initializeUI() {
        setTitle("Online Banking System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));

        accountNumberField = new JTextField();
        amountField = new JTextField();
        transferAccountField = new JTextField();
        transactionHistoryArea = new JTextArea();
        transactionHistoryArea.setEditable(false);

        JButton depositButton = new JButton("Deposit");
        JButton withdrawButton = new JButton("Withdraw");
        JButton transferButton = new JButton("Transfer");
        JButton viewHistoryButton = new JButton("View Transaction History");
        depositButton.setBackground(Color.GREEN);
        withdrawButton.setBackground(Color.MAGENTA);
        transferButton.setBackground(Color.CYAN);
        viewHistoryButton.setBackground(Color.YELLOW);

        depositButton.addActionListener(e -> deposit());
        withdrawButton.addActionListener(e -> withdraw());
        transferButton.addActionListener(e -> transfer());
        viewHistoryButton.addActionListener(e -> viewTransactionHistory());

        panel.add(new JLabel("Account Number:"));
        panel.add(accountNumberField);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);
        panel.add(new JLabel("Transfer To Account:"));
        panel.add(transferAccountField);
        panel.add(depositButton);
        panel.add(withdrawButton);
        panel.add(transferButton);
        panel.add(viewHistoryButton);

        add(panel, BorderLayout.CENTER);
        add(new JScrollPane(transactionHistoryArea), BorderLayout.SOUTH);
    }

    private void initializeDatabase() {
        // Seting up database connection 
        String jdbcURL = "jdbc:mysql://localhost:3306/Bank";
        String username = "root";
        String password = "Bushra@2001";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connection = DriverManager.getConnection(jdbcURL, username, password);
            Statement statement = connection.createStatement();

            // Creating accounts table if not exists
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (account_number INT PRIMARY KEY, balance DOUBLE)");

            // Creating transaction table if not exists
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS transaction (transaction_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "account_number INT, transaction_type VARCHAR(10), amount DOUBLE, " +
                    "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY (account_number) REFERENCES accounts(account_number))");

            statement.close();
            connection.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private void deposit() {
        performTransaction("Deposit");
    }

    private void withdraw() {
        performTransaction("Withdraw");
    }

    private void transfer() {
        performTransaction("Transfer");
    }

    private void viewTransactionHistory() {
        transactionHistoryArea.setText("");
        int accountNumber = Integer.parseInt(accountNumberField.getText());

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bank", "root", "Bushra@2001");
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM transaction WHERE account_number = " + accountNumber);

            while (resultSet.next()) {
                String transactionType = resultSet.getString("transaction_type");
                double amount = resultSet.getDouble("amount");
                Timestamp transactionDate = resultSet.getTimestamp("transaction_date");

                transactionHistoryArea.append(transactionType + ": Rs" + amount + " on " + transactionDate + "\n");
            }

            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void performTransaction(String transactionType) {
        int accountNumber = Integer.parseInt(accountNumberField.getText());
        double amount = Double.parseDouble(amountField.getText());

        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/Bank", "root", "Bushra@2001");
            Statement statement = connection.createStatement();

            // Updation of balance in our accounts table
            if (transactionType.equals("Deposit")) {
                statement.executeUpdate("UPDATE accounts SET balance = balance + " + amount + " WHERE account_number = " + accountNumber);
            }
            else if (transactionType.equals("Withdraw")) {
                statement.executeUpdate("UPDATE accounts SET balance = balance - " + amount + " WHERE account_number = " + accountNumber);
            }
            else if (transactionType.equals("Transfer")) {
                int transferAccount = Integer.parseInt(transferAccountField.getText());
                statement.executeUpdate("UPDATE accounts SET balance = balance - " + amount + " WHERE account_number = " + accountNumber);
                statement.executeUpdate("UPDATE accounts SET balance = balance + " + amount + " WHERE account_number = " + transferAccount);
            }

            // Inserting  transaction record into our transactions table
            statement.executeUpdate("INSERT INTO transaction (account_number, transaction_type, amount) VALUES (" + accountNumber + ", '" + transactionType + "', " + amount + ")");

            JOptionPane.showMessageDialog(this, transactionType + " successful!");
            clearFields();
            statement.close();
            connection.close();
        } 
        catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error performing " + transactionType);
        }
    }

    private void clearFields() {
        accountNumberField.setText("");
        amountField.setText("");
        transferAccountField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BankingSystem bankingSystemUI = new BankingSystem();
            bankingSystemUI.setVisible(true);
        });
    }
}
