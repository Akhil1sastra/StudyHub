package ui;

import db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Groups extends JFrame {

    int userId;

    DefaultListModel<String> joinedModel = new DefaultListModel<>();
    DefaultListModel<String> otherModel = new DefaultListModel<>();
    JList<String> joinedList = new JList<>(joinedModel);
    JList<String> otherList = new JList<>(otherModel);

    public Groups(int userId) {
        this.userId = userId;
        setTitle("StudyHub - Groups");
        setSize(700, 400);
        setLayout(new GridLayout(1, 2));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel joinedPanel = new JPanel(new BorderLayout());
        joinedPanel.add(new JLabel("Joined Groups", SwingConstants.CENTER), BorderLayout.NORTH);
        joinedPanel.add(new JScrollPane(joinedList), BorderLayout.CENTER);
        JPanel joinedBtnPanel = new JPanel();
        JButton openBtn = new JButton("Open Group");
        JButton createBtn = new JButton("Create Group");
        joinedBtnPanel.add(openBtn);
        joinedBtnPanel.add(createBtn);
        joinedPanel.add(joinedBtnPanel, BorderLayout.SOUTH);
        JPanel otherPanel = new JPanel(new BorderLayout());
        otherPanel.add(new JLabel("Other Groups", SwingConstants.CENTER), BorderLayout.NORTH);
        otherPanel.add(new JScrollPane(otherList), BorderLayout.CENTER);
        JButton joinBtn = new JButton("Join Group");
        otherPanel.add(joinBtn, BorderLayout.SOUTH);
        add(joinedPanel);
        add(otherPanel);
        loadGroups();
        openBtn.addActionListener(e -> openGroup());
        joinBtn.addActionListener(e -> joinGroup());
        createBtn.addActionListener(e -> createGroup());
        setVisible(true);
    }

    private void loadGroups() {
        joinedModel.clear();
        otherModel.clear();
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps1 = con.prepareStatement(
                    "SELECT g.id, g.name FROM groups g " +
                            "JOIN members m ON g.id = m.group_id " +
                            "WHERE m.user_id = ?"
            );
            ps1.setInt(1, userId);
            ResultSet rs1 = ps1.executeQuery();
            while (rs1.next()) {
                joinedModel.addElement( rs1.getInt("id") + " - " + rs1.getString("name"));
            }
            PreparedStatement ps2 = con.prepareStatement(
                    "SELECT id, name FROM groups WHERE id NOT IN " +
                            "(SELECT group_id FROM members WHERE user_id = ?)"
            );
            ps2.setInt(1, userId);
            ResultSet rs2 = ps2.executeQuery();
            while (rs2.next()) {
                otherModel.addElement( rs2.getInt("id") + " - " + rs2.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading groups");
        }
    }

    private void joinGroup() {
        String selected = otherList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a group to join");
            return;
        }
        int groupId = Integer.parseInt(selected.split(" - ")[0]);
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO members(user_id, group_id) VALUES (?, ?)"
            );
            ps.setInt(1, userId);
            ps.setInt(2, groupId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Joined group successfully");
            loadGroups();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Already joined or error occurred");
            e.printStackTrace();
        }
    }

    private void openGroup() {
        String selected = joinedList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Select a group to open");
            return;
        }
        int groupId = Integer.parseInt(selected.split(" - ")[0]);
        new GroupHome(userId, groupId);
    }

    private void createGroup() {
        String groupName = JOptionPane.showInputDialog(this, "Enter new group name");
        if (groupName == null || groupName.trim().isEmpty()) return;
        try {
            Connection con = DBConnection.getConnection();
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO groups(name, created_by) VALUES (?, ?)",
                    new String[]{"id"}
            );
            ps.setString(1, groupName);
            ps.setInt(2, userId);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            int groupId = 0;
            if (rs.next()) groupId = rs.getInt(1);
            PreparedStatement ps2 = con.prepareStatement(
                    "INSERT INTO members(user_id, group_id) VALUES (?, ?)"
            );
            ps2.setInt(1, userId);
            ps2.setInt(2, groupId);
            ps2.executeUpdate();
            JOptionPane.showMessageDialog(this, "Group created successfully!");
            loadGroups();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating group");
            e.printStackTrace();
        }
    }
}
