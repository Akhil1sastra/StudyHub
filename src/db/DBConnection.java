package db;

import javax.swing.*;
import java.sql.*;
import java.util.*;
import java.io.*;

public class DBConnection {

    public static Connection getConnection(){
        try{
        Properties p=new Properties();
        p.load(new FileInputStream(".env"));
        String URL=p.getProperty("url");
        String User=p.getProperty("user");
        String Password=p.getProperty("pass");
            return DriverManager.getConnection(URL,User,Password);
        }
        catch(Exception e){
            JOptionPane.showMessageDialog(null,
                    "Cannot connect! "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}

