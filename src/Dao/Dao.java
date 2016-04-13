/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Chaitali
 */
public class Dao {
    
    public static Connection getRelExtractConnection() {
        Connection connection = null;
        String url = "jdbc:postgresql://localhost:5432/RelExtract";
        String username = "postgres";
        String password = "skrp@9898533668";

        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException ex) {
            System.out.println("JDBC Driver Missing !!! ");
            ex.printStackTrace();
        } catch (SQLException ex) {
            System.out.println("Connection Failed! Check output console");
            ex.printStackTrace();
        }
        return connection;
    }
    
    
    
    public static Connection getMutkinConnection() {
        Connection connection = null;
        String url = "jdbc:postgresql://localhost:5432/mutkin";
        String username = "postgres";
        String password = "skrp@9898533668";

        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException ex) {
            System.out.println("JDBC Driver Missing !!! ");
            ex.printStackTrace();
        } catch (SQLException ex) {
            System.out.println("Connection Failed! Check output console");
            ex.printStackTrace();
        }
        return connection;
    }
    
    
    public static Connection getCaseStudy1Connection() {
        Connection connection = null;
        String url = "jdbc:postgresql://localhost:5432/casestudy1";
        String username = "postgres";
        String password = "skrp@9898533668";

        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url, username, password);

        } catch (ClassNotFoundException ex) {
            System.out.println("JDBC Driver Missing !!! ");
            ex.printStackTrace();
        } catch (SQLException ex) {
            System.out.println("Connection Failed! Check output console");
            ex.printStackTrace();
        }
        return connection;
    }
}
