package ui;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {

    int userId;

    public Dashboard(int userId) {
        this.userId = userId;
        setTitle("StudyHub - Dashboard");
        setSize(300, 200);
        setLayout(new GridLayout(2, 1));
        JButton grp = new JButton("Groups");
        grp.addActionListener(e -> new Groups(userId));
        add(grp);
        JButton back = new JButton("Back");
        back.addActionListener(e -> {
            new Login();
            dispose();
        });
        add(back);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}
