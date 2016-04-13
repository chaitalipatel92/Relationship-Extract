/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relnextraction;

import Dao.Dao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chaitali
 */
public class Pattern {

    static int count = 1;

    public static void Insert(String pattern) {
        try {
            Connection conn = Dao.getRelExtractConnection();
            String qry = "Insert into pattern(patterncontent) values(?);";
            PreparedStatement preparedStatement = conn.prepareStatement(qry);
            preparedStatement.setString(1, pattern);
            preparedStatement.execute();
        } catch (SQLException ex) {
            Logger.getLogger(Pattern.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void Insert2(String pattern) {
        try {
            Connection conn = Dao.getRelExtractConnection();
            String qry = "Insert into pattern2(patterncontent) values(?);";
            PreparedStatement preparedStatement = conn.prepareStatement(qry);
            preparedStatement.setString(1, pattern);
            preparedStatement.execute();
        } catch (SQLException ex) {
            Logger.getLogger(Pattern.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void Insert3(String pattern) {
        try {
            Connection conn = Dao.getCaseStudy1Connection();
            String qry = "Insert into pattern values(?,?);";
            PreparedStatement preparedStatement = conn.prepareStatement(qry);
            preparedStatement.setInt(1, count++);
            preparedStatement.setString(2, pattern);
            preparedStatement.execute();
        } catch (SQLException ex) {
            Logger.getLogger(Pattern.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
