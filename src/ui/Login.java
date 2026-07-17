package ui;

import db.DBConnection;

import javax.swing.*;
import java.sql.*;
public class Login extends JFrame{

    JTextField userF;
    JPasswordField passwordF;

    public Login() {
        setTitle("StudyHub - Login");
        setSize(350, 220);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel userl= new JLabel("Username:");
        userl.setBounds(30, 30, 100, 25);
        add(userl);

        userF = new JTextField();
        userF.setBounds(130, 30, 150, 25);
        add(userF);

        JLabel passl = new JLabel("Password:");
        passl.setBounds(30, 70, 100, 25);
        add(passl);

        passwordF = new JPasswordField();
        passwordF.setBounds(130, 70, 150, 25);
        add(passwordF);

        JButton btn = new JButton("Login");
        btn.setBounds(120, 110, 100, 30);
        add(btn);

        JButton reg = new JButton("New User?");
        reg.setBounds(120, 150, 100, 25);
        add(reg);

        btn.addActionListener(e -> login());
        reg.addActionListener(e -> {
            new Register();
            dispose();
        });

        setVisible(true);
    }

    private void login() {
        String username = userF.getText();
        String password = new String(passwordF.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id=rs.getInt("id");
                JOptionPane.showMessageDialog(this, "Login Successful!");
                new Dashboard(id);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
