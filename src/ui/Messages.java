package ui;

import db.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Messages extends JFrame {
    int userId;
    int groupId;
    JTextArea chatArea = new JTextArea();
    JTextField messageField = new JTextField();

    public Messages(int userId, int groupId) {
        this.userId = userId;
        this.groupId = groupId;
        setTitle("Group Messages - Group ID: " + groupId);
        setSize(500, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        chatArea.setEditable(false);
        JButton sendBtn = new JButton("Send");
        sendBtn.addActionListener(e -> sendMessage());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(messageField, BorderLayout.CENTER);
        bottom.add(sendBtn, BorderLayout.EAST);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        loadMessages();
        setVisible(true);
    }

    void loadMessages() {
        chatArea.setText("");
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "SELECT u.username, m.message FROM messages m " +
                            "JOIN users u ON m.user_id = u.id " +
                            "WHERE m.group_id = ?"
            );
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                chatArea.append(rs.getString("username") + ": " +
                        rs.getString("message") + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void sendMessage() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty()) return;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO messages(user_id, group_id, message) VALUES (?, ?, ?)"
            );
            ps.setInt(1, userId);
            ps.setInt(2, groupId);
            ps.setString(3, msg);
            ps.executeUpdate();
            messageField.setText("");
            loadMessages();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error sending message");
        }
    }
}
