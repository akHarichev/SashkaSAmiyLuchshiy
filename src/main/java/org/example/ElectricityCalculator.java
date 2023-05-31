package org.example;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ElectricityCalculator extends JFrame {

    private JTextField textField1;
    private JTextField textField2;
    private JTextField textField3;
    private JButton button1;
    private JLabel label4;

    private Connection connection;

    public ElectricityCalculator() {
        setTitle("Электроэнергия");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        textField1 = new JTextField();
        textField1.setBounds(20, 20, 100, 20);
        add(textField1);

        textField2 = new JTextField();
        textField2.setBounds(20, 50, 100, 20);
        add(textField2);

        textField3 = new JTextField();
        textField3.setBounds(20, 80, 100, 20);
        add(textField3);

        button1 = new JButton("Вычислить");
        button1.setBounds(20, 110, 100, 20);
        button1.setEnabled(false);
        add(button1);

        label4 = new JLabel();
        label4.setBounds(20, 140, 200, 20);
        add(label4);

        textField1.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                    if (!((Character.toString(c).equals(",")) && (textField1.getText().indexOf(",") == -1)))
                        e.consume();
                }
            }

            public void keyReleased(KeyEvent e) {
                enableButton();
            }
        });

        textField2.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                    if (!((Character.toString(c).equals(",")) && (textField2.getText().indexOf(",") == -1)))
                        e.consume();
                }
            }

            public void keyReleased(KeyEvent e) {
                enableButton();
            }
        });

        textField3.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != KeyEvent.VK_BACK_SPACE) {
                    if (!((Character.toString(c).equals(",")) && (textField3.getText().indexOf(",") == -1)))
                        e.consume();
                }
            }

            public void keyReleased(KeyEvent e) {
                enableButton();
            }
        });

        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                calculatePrice();
            }
        });

        connectToDatabase();
        createTable();
        loadHistory();
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:electricity.db");
            System.out.println("Connected to database");
        } catch (SQLException e) {
            System.out.println("Failed to connect to database");
            e.printStackTrace();
        }
    }

    private void createTable() {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS history (id INTEGER PRIMARY KEY AUTOINCREMENT, prev FLOAT, curr FLOAT, traf FLOAT, price FLOAT)";
        try (PreparedStatement statement = connection.prepareStatement(createTableQuery)) {
            statement.execute();
        } catch (SQLException e) {
            System.out.println("Failed to create table");
            e.printStackTrace();
        }
    }

    private void loadHistory() {
        String selectQuery = "SELECT prev, curr, traf, price FROM history ORDER BY id DESC LIMIT 10";
        try (PreparedStatement statement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = statement.executeQuery()) {
            System.out.println("History:");
            while (resultSet.next()) {
                float prev = resultSet.getFloat("prev");
                float curr = resultSet.getFloat("curr");
                float traf = resultSet.getFloat("traf");
                float price = resultSet.getFloat("price");
                System.out.printf("Prev: %.2f, Curr: %.2f, Traf: %.2f, Price: %.2f\n", prev, curr, traf, price);
            }
        } catch (SQLException e) {
            System.out.println("Failed to load history");
            e.printStackTrace();
        }
    }

    private void saveEntry(float prev, float curr, float traf, float price) {
        String insertQuery = "INSERT INTO history (prev, curr, traf, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertQuery)) {
            statement.setFloat(1, prev);
            statement.setFloat(2, curr);
            statement.setFloat(3, traf);
            statement.setFloat(4, price);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to save entry");
            e.printStackTrace();
        }
    }

    private void enableButton() {
        if (textField1.getText().length() > 0 && textField2.getText().length() > 0 && textField3.getText().length() > 0) {
            button1.setEnabled(true);
        } else {
            button1.setEnabled(false);
        }
    }

    private void calculatePrice() {
        float curr; // текущее показание счетчика
        float prev; // предыдущее показание счетчика
        float traf; // цена за кВт
        float price; // сумма к оплате

        label4.setText("");
        try {
            prev = Float.parseFloat(textField1.getText());
            curr = Float.parseFloat(textField2.getText());
            traf = Float.parseFloat(textField3.getText());

            if (curr >= prev) {
                price = (curr - prev) * traf;
                label4.setText("Сумма к оплате: " + String.format("%.2f", price) + " руб.");

                saveEntry(prev, curr, traf, price);
            } else {
                JOptionPane.showMessageDialog(this, "Ошибка исходных данных.\n" +
                        "Текущее значение показания счетчика\n" +
                        "меньше предыдущего.", "Электроэнергия", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ошибка исходных данных.\n" +
                    "Исходные данные имеют неверный формат.\n" +
                    e.getMessage(), "Электроэнергия", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        ElectricityCalculator calculator = new ElectricityCalculator();
        calculator.setVisible(true);
    }
}