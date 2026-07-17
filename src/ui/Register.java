package ui;

import db.DBConnection;

import javax.swing.*;
import java.sql.*;

public class Register extends JFrame {

    JTextField usernameF;
    JPasswordField passwordF;

    public Register() {
        setTitle("StudyHub - Register");
        setSize(350, 230);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(30, 30, 100, 25);
        add(userLabel);

        usernameF = new JTextField();
        usernameF.setBounds(130, 30, 150, 25);
        add(usernameF);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(30, 70, 100, 25);
        add(passLabel);

        passwordF = new JPasswordField();
        passwordF.setBounds(130, 70, 150, 25);
        add(passwordF);

        JButton reg = new JButton("Register");
        reg.setBounds(120, 120, 100, 30);
        add(reg);

        JButton back = new JButton("Back");
        back.setBounds(120, 160, 100, 25);
        add(back);

        reg.addActionListener(e -> registerUser());
        back.addActionListener(e -> {
            new Login();
            dispose();
        });

        setVisible(true);
    }

    private void registerUser() {
        String username = usernameF.getText();
        String password = new String(passwordF.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            String checkSql = "SELECT * FROM users WHERE username=?";
            PreparedStatement checkPs = con.prepareStatement(checkSql);
            checkPs.setString(1, username);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Username already exists");
                return;
            }
            String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(insertSql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Registration Successful!");
            new Login();
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
