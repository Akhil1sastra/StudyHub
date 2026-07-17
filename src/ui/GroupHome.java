package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.sql.*;

public class GroupHome extends JFrame {

    int userId;
    int groupId;

    DefaultListModel<String> msgModel = new DefaultListModel<>();
    JList<String> msgList = new JList<>(msgModel);
    JTextField msgField = new JTextField();
    DefaultListModel<String> resModel = new DefaultListModel<>();
    JList<String> resList = new JList<>(resModel);

    public GroupHome(int userId, int groupId) {
        this.userId = userId;
        this.groupId = groupId;
        setTitle("Group Home - Group ID: " + groupId);
        setSize(800, 500);
        setLayout(new GridLayout(1, 2));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel msgPanel = new JPanel(new BorderLayout());
        msgPanel.add(new JLabel("Messages", SwingConstants.CENTER), BorderLayout.NORTH);
        msgPanel.add(new JScrollPane(msgList), BorderLayout.CENTER);
        JPanel sendPanel = new JPanel(new BorderLayout());
        sendPanel.add(msgField, BorderLayout.CENTER);
        JButton sendBtn = new JButton("Send");
        sendPanel.add(sendBtn, BorderLayout.EAST);
        msgPanel.add(sendPanel, BorderLayout.SOUTH);
        JPanel resPanel = new JPanel(new BorderLayout());
        resPanel.add(new JLabel("Resources", SwingConstants.CENTER), BorderLayout.NORTH);
        resPanel.add(new JScrollPane(resList), BorderLayout.CENTER);
        JPanel resBtnPanel = new JPanel();
        JButton addResBtn = new JButton("Add Resource");
        JButton downloadBtn = new JButton("Download Resource");
        resBtnPanel.add(addResBtn);
        resBtnPanel.add(downloadBtn);
        resPanel.add(resBtnPanel, BorderLayout.SOUTH);
        add(msgPanel);
        add(resPanel);
        loadMessages();
        loadResources();
        sendBtn.addActionListener(e -> sendMessage());
        addResBtn.addActionListener(e -> addResource());
        downloadBtn.addActionListener(e -> downloadResource());
        setVisible(true);
    }

    private void loadMessages() {
        msgModel.clear();
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT m.message, u.username FROM messages m " +
                    "JOIN users u ON m.user_id = u.id " +
                    "WHERE m.group_id = ? ORDER BY m.id";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                msgModel.addElement(rs.getString("username") + ": " + rs.getString("message"));
            }
            if (!hasRows) msgModel.addElement("No messages yet.");
        } catch (Exception e) {
            e.printStackTrace();
            msgModel.addElement("Error loading messages.");
        }
    }

    private void sendMessage() {
        String msg = msgField.getText().trim();
        if (msg.isEmpty()) return;
        try {
            Connection con = DBConnection.getConnection();
            String sql = "INSERT INTO messages(user_id, group_id, message) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, groupId);
            ps.setString(3, msg);
            ps.executeUpdate();
            msgField.setText("");
            loadMessages();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error sending message");
        }
    }

    private void loadResources() {
        resModel.clear();
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT r.id, r.title, r.descr, u.username " +
                    "FROM resources r " +
                    "JOIN users u ON r.upload_by = u.id " +
                    "WHERE r.group_id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, groupId);
            ResultSet rs = ps.executeQuery();

            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String descr = rs.getString("descr");
                String user = rs.getString("username");
                resModel.addElement(id + " - " + title + " : " + descr + " ( " + user + ")");
            }
            if (!hasRows) resModel.addElement("No resources available.");
        } catch (Exception e) {
            e.printStackTrace();
            resModel.addElement("Error loading resources.");
        }
    }

    private void addResource() {
        JTextField titleField = new JTextField();
        JTextField descField = new JTextField();
        Object[] fields = {
                "Title:", titleField,
                "Description:", descField
        };
        int option = JOptionPane.showConfirmDialog(this, fields, "Add Resource", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) return;
        JFileChooser chooser = new JFileChooser();
        int fileOption = chooser.showOpenDialog(this);
        if (fileOption != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try {
            Connection con = DBConnection.getConnection();
            String sql = "INSERT INTO resources(title, descr, upload_by, group_id, file_data) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, titleField.getText());
            ps.setString(2, descField.getText());
            ps.setInt(3, userId);
            ps.setInt(4, groupId);
            try (InputStream fis = new FileInputStream(file)) {
                ps.setBinaryStream(5, fis, (int) file.length());
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Resource uploaded successfully!");
            loadResources();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error uploading resource");
        }
    }

    private void downloadResource() {
        String selected = resList.getSelectedValue();
        if (selected == null || selected.startsWith("No") || selected.startsWith("Error")) {
            JOptionPane.showMessageDialog(this, "Select a valid resource to download");
            return;
        }
        int resourceId = Integer.parseInt(selected.split(" - ")[0]);
        try {
            Connection con = DBConnection.getConnection();
            String sql = "SELECT title, file_data FROM resources WHERE id = ?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, resourceId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String title = rs.getString("title");
                InputStream is = rs.getBinaryStream("file_data");
                JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(new File(title));
                int option = chooser.showSaveDialog(this);
                if (option != JFileChooser.APPROVE_OPTION) return;
                File outFile = chooser.getSelectedFile();
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                JOptionPane.showMessageDialog(this, "Resource downloaded successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error downloading resource");
        }
    }
}
