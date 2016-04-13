/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relnextraction;

import Dao.Dao;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chaitali
 */
public class InitializeLists {

    public static void main(String[] args) {
        InitializeLists il = new InitializeLists();
        il.populateLists();
    }

    public void populateLists() {
        com.hp.hpl.jena.rdf.model.Statement s = null;
        List<com.hp.hpl.jena.rdf.model.Statement> statements = new ArrayList<>();
        Connection conn = Dao.getRelExtractConnection();
        Statement st1 = null, st2 = null, st3 = null;
        ResultSet rs1 = null, rs2 = null, rs3 = null;

        try {
            st1 = conn.createStatement();
            String query = "Select * from word;";
            rs1 = st1.executeQuery(query);
            int catId;
            String wordStem;
            String category;
            while (rs1.next()) {
                catId = rs1.getInt("catId");
                wordStem = rs1.getString("stem");

                st2 = conn.createStatement();
                String query2 = "Select * from category where \"id\" = " + catId + ";";
                rs2 = st2.executeQuery(query2);
                if (rs2.next()) {
                    category = rs2.getString("name");
                    TestingTool.wordCategorylist.put(wordStem, category);
//                    String str = TestingTool.ontoURI + wordStem + "\t" + TestingTool.ontoURI + "type" + "\t" + TestingTool.ontoURI + category;
//                    String[] sop = str.split("\t");
//                    s = ResourceFactory.createStatement(
//                            ResourceFactory.createResource(sop[0].trim()),
//                            ResourceFactory.createProperty(sop[1].trim()),
//                            ResourceFactory.createResource(sop[2].trim()));
//                    statements.add(s);
                }
            } 
//            TestingTool.model.add(statements);

            st3 = conn.createStatement();
//            String query3 = "select patterncontent from pattern;";
            String query3 = "select patterncontent from pattern2;";
            rs3 = st3.executeQuery(query3);
            while (rs3.next()) {
                String pattern = rs3.getString("patterncontent");
                TestingTool.patternQueries.add(pattern);
            }

//            System.out.println(wordCategorylist.entrySet());
        } catch (SQLException ex) {
            Logger.getLogger(InitializeLists.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conn.close();
                if (st1 != null) {
                    st1.close();
                }
                if (st2 != null) {
                    st2.close();
                }
                if (st3 != null) {
                    st3.close();
                }
                if (rs1 != null) {
                    rs1.close();
                }
                if (rs2 != null) {
                    rs2.close();
                }
                if (rs3 != null) {
                    rs3.close();
                }
            } catch (SQLException e) {
                e.getMessage();
            }
        }
    }

}
