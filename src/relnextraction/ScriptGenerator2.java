/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relnextraction;

import Dao.Dao;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

/**
 *
 * @author Chaitali
 */
public class ScriptGenerator2 {

    public static void main(String[] args) {
        try {
            Connection conn = Dao.getMutkinConnection();
            Statement st = conn.createStatement();
            String qry = "select distinct sentence.content, mutationimpact.predictionid, impactprediction.mutationid, "
                    + "impactprediction.impwordid, impactprediction.funcwordid, impactprediction.mutposstart, "
                    + "impactprediction.funcposstart, impactprediction.impposstart\n"
                    + "from sentence, mutationimpact, impactprediction \n"
                    + "where  mutationimpact.predictionid=impactprediction.id and \n"
                    + "sentence.id=impactprediction.sentenceid and\n"
                    + "impactprediction.impwordid != 0 and \n"
                    + "impactprediction.funcwordid != 0;";
            ResultSet rs = st.executeQuery(qry);
            int count = 0;
            while (rs.next()) {
                String sent = rs.getString("content");
                String content = sent.replaceAll("\\.(?!.*\\.)", " ");
                content = content.replace("-", " ");
//                content = content.replaceAll(",", "");
//                content = content.replaceAll(":", "");
//                content = content.replaceAll("'", "");
//                content = content.replaceAll("‘", "");
//                content = content.replaceAll("\\(", "");
//                content = content.replaceAll("\\)", "");
//                content = content.replaceAll(";", "");
                int mutidx = -1;
                int funcidx = -2;
                int impidx = -3;
                TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
                List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(content)).tokenize();

                int mid = rs.getInt("mutationid");
                int iid = rs.getInt("impwordid");
                int fid = rs.getInt("funcwordid");
                int midx = rs.getInt("mutposstart");
                int fidx = rs.getInt("funcposstart");
                int iidx = rs.getInt("impposstart");
//                System.out.println("\n" + content);
                String[] splitContent = content.split(" ");

                String mutqry = "select mutationaa from mutation where id = " + mid + ";";
                Statement st2 = conn.createStatement();
                ResultSet rs2 = st2.executeQuery(mutqry);
                if (rs2.next()) {
                    String mutword = rs2.getString("mutationaa");
//                    for (int i = 0; i < splitContent.length; i++) {
//                        String mut = splitContent[i];
//                        if (mut.contains(mutword.subSequence(0, mutword.length() - 2))) {
//                            mutidx = i;
////                            System.out.println("DbStartIdx : " + midx + " Idx : " + sent.indexOf(mut));
//                        }
//                    }
                    for (int i = 0; i < rawWords.size(); i++) {
                        String mut = rawWords.get(i).toString().trim();
                        if (mut.contains(mutword.subSequence(0, mutword.length() - 2))) {
                            mutidx = i+1;
//                            System.out.println("DbStartIdx : " + midx + " Idx : " + sent.indexOf(mut));
                        }
                    }
//                    System.out.println("Mutation Word : " + mutword + " @Position : " + mutidx);
                }

                String funcqry = "select word from functionword where id = " + fid + ";";
                Statement st3 = conn.createStatement();
                ResultSet rs3 = st3.executeQuery(funcqry);
                if (rs3.next()) {
                    String funcword = rs3.getString("word");
                    String functionwordStem = Stemmer.getRootWord(funcword.toLowerCase());
//                    for (int i = 0; i < splitContent.length; i++) {
//                        if (splitContent[i].startsWith("-")) {
//                            splitContent[i] = splitContent[i].replace("-", "");
//                        }
//                        String currentwordStem = Stemmer.getRootWord(splitContent[i].toLowerCase());
//                        if (functionwordStem.compareTo(currentwordStem) == 0) {
//                            funcidx = i;
////                            System.out.println("DbStartIdx : " + fidx + " Idx : " + sent.indexOf(splitContent[i]));
//                        }
//                    }
                    for (int i = 0; i < rawWords.size(); i++) {
                        String currentwordStem = Stemmer.getRootWord(rawWords.get(i).toString().toLowerCase());
                        if (functionwordStem.compareTo(currentwordStem) == 0) {
                            funcidx = i+1;
//                            System.out.println("DbStartIdx : " + fidx + " Idx : " + sent.indexOf(splitContent[i]));
                        }
                    }
//                    System.out.println("Function Word : " + funcword + " @Position : " + funcidx + " DbStartIdx : " + fidx );
                }

                String impqry = "select word from impactword where id = " + iid + ";";
                Statement st4 = conn.createStatement();
                ResultSet rs4 = st4.executeQuery(impqry);
                if (rs4.next()) {
                    String impword = rs4.getString("word");
                    String impwordStem = Stemmer.getRootWord(impword.toLowerCase());
//                    for (int i = 0; i < splitContent.length; i++) {
//                        String currentwordStem = Stemmer.getRootWord(splitContent[i].toLowerCase());
//                        if (impwordStem.compareTo(currentwordStem) == 0) {
//                            impidx = i;
////                            System.out.println("DbStartIdx : " + iidx + " Idx : " + sent.indexOf(splitContent[i]));
//                        }
//                    }
                    for (int i = 0; i < rawWords.size(); i++) {
                        String currentwordStem = Stemmer.getRootWord(rawWords.get(i).toString().toLowerCase());
                        if (impwordStem.compareTo(currentwordStem) == 0) {
                            impidx = i+1;
//                            System.out.println("DbStartIdx : " + iidx + " Idx : " + sent.indexOf(splitContent[i]));
                        }
                    }
//                    System.out.println("Impact Word : " + impword + " @Position : " + impidx);
                }
                sent = sent.replaceAll("'", "''");
                System.out.println("INSERT INTO \"mutkinTrainingData\"(\n"
                        + "            id, sentence, pos1, pos2, pos3)\n"
                        + "    VALUES (" + ++count + ", '" + sent + "', " + mutidx + ", " + funcidx + ", " + impidx + ");");
            }
        } catch (SQLException ex) {
            Logger.getLogger(ScriptGenerator2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
