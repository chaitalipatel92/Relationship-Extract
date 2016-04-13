package relnextraction;

import Dao.Dao;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Chaitali
 */
public class ScriptGenerator {

    public static void main(String[] args) {

//        //Get category List
//        try {
//            Dao d = new Dao();
//            Connection conn = d.getRelExtractConnection();
//            Statement st = null;
//            ResultSet rs = null;
//
//            String catlist = "Select * from category;";
//            st = conn.createStatement();
//            rs = st.executeQuery(catlist);
//            int count = 1;
//            while (rs.next()) {
//                Object CatId = rs.getObject(1);
//                Object CatName = rs.getObject(2);
//                System.out.println("ID : " + CatId + " Name : " + CatName);
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(ScriptGenerator.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        //Insert Script
//        try {
//            Dao d = new Dao();
//            Connection conn = Dao.getMutkinConnection();
//            Statement st = null;
//            ResultSet rs = null;
//            int count = 1;
//
//            String catlist = "Select distinct word, stem, impact from impactword;";
//            st = conn.createStatement();
//            rs = st.executeQuery(catlist);
//            while (rs.next()) {
//                Object impact = rs.getString("word");
//                Object impstem = rs.getString("stem");
//                Object imptype = rs.getString("impact");
//                if (imptype.equals("Positive")) {
//                    System.out.println("insert into word values (" +count++ + ", 1, '" + impact + "', '" + impstem + "');");
//                }
//                if (imptype.equals("Negative")) {
//                    System.out.println("insert into word values (" +count++ + ", 2, '" + impact + "', '" + impstem + "');");
//                }
//                if (imptype.equals("Neutral")) {
//                    System.out.println("insert into word values (" +count++ + ", 3, '" + impact + "', '" + impstem + "');");
//                }
////                if (imptype.equals("Positive")) {
////                    System.out.println("insert into word(\"catId\", name, stem) values (1, '" + impact + "', '" + impstem + "');");
////                }
////                if (imptype.equals("Negative")) {
////                    System.out.println("insert into word(\"catId\", name, stem) values (2, '" + impact + "', '" + impstem + "');");
////                }
////                if (imptype.equals("Neutral")) {
////                    System.out.println("insert into word(\"catId\", name, stem) values (3, '" + impact + "', '" + impstem + "');");
////                }
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(ScriptGenerator.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        //Insert Script
//        try {
//            Connection conn = Dao.getMutkinConnection();
//            Statement st = null;
//            ResultSet rs = null;
//            int count = 56;
//
//            String catlist = "Select distinct mutationaa from mutation;";
//            st = conn.createStatement();
//            rs = st.executeQuery(catlist);
//            while (rs.next()) {
//                Object mutation = rs.getString("mutationaa");
//                String mutstem = Stemmer.getRootWord(mutation.toString());
////                String mutstem = Stemmer.getRootWord(mutation.toString().toLowerCase());
//                    System.out.println("insert into word values (" +count++ + ", 4, '" + mutation + "', '" + mutstem + "');");                
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(ScriptGenerator.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        //Insert Script
//        try {
//            Connection conn = Dao.getMutkinConnection();
//            Statement st = null;
//            ResultSet rs = null;
//            int count = 30232;
//
//            String list = "Select distinct word from functionword;";
//            st = conn.createStatement();
//            rs = st.executeQuery(list);
//            while (rs.next()) {
//                Object function = rs.getString("word");
////                String funcstem = Stemmer.getRootWord(function.toString());
//                String funcstem = Stemmer.getRootWord(function.toString().toLowerCase());
//                    System.out.println("insert into word values (" +count++ + ", 5, '" + function + "', '" + funcstem + "');");                
//            }
//        } catch (SQLException ex) {
//            Logger.getLogger(ScriptGenerator.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        Insert Script
        HashMap<String, Integer> wordlist = new HashMap<>();
        try {
            Connection con = Dao.getCaseStudy1Connection();
            Statement st = null;
            ResultSet rs = null;
//            HashMap<Object, Object> wordlist = new HashMap<>();

            String list = "Select * from word;";
            st = con.createStatement();
            rs = st.executeQuery(list);
            while (rs.next()) {
                String word = rs.getString(4);
                int wordid = rs.getInt(1);
                wordlist.put(word, wordid);
            }
        } catch (SQLException ex) {
            Logger.getLogger(ScriptGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Insert Script
        try {
            Connection conn = Dao.getCaseStudy1Connection();
            String query = "select * from \"mutkinTrainingData\";";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(query);
            int id = 1;
            int wid = 1;
            while (rs.next()) {
                String sent = rs.getString("sentence");
                String content = sent.replaceAll("\\.(?!.*\\.)", "");
                content = content.replace("-", " ");
//                content = content.replaceAll(",", "");
//                content = content.replaceAll(":", "");
//                content = content.replaceAll("'", "");
//                content = content.replaceAll("‘", "");
//                content = content.replaceAll("\\(", "");
//                content = content.replaceAll("\\)", "");
//                content = content.replaceAll(";", "");
//                content = content.replace("\t", " ");

                int mid = rs.getInt("pos1");
                int iid = rs.getInt("pos3");
                int fid = rs.getInt("pos2");
                int sentid = rs.getInt("id");

//                String[] getWord = content.split(" ");
                TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
                List<CoreLabel> rawWords = tokenizerFactory.getTokenizer(new StringReader(content)).tokenize();

//                System.out.println("\n" + sentid + " : " + content);
//                System.out.println("Sentence total word size : " + rawWords.size());
//                System.out.println("Sentence total word size : " + getWord.length);

                if (mid <= rawWords.size() && iid <= rawWords.size() && fid <= rawWords.size() && mid > 0 && iid > 0 && fid > 0) {
//                if (mid < getWord.length && iid < getWord.length && fid < getWord.length && mid >= 0 && iid >= 0 && fid >= 0) {
//                    System.out.println("mid : " + mid + " : " + getWord[mid] + " iid : " + iid + " : " + getWord[iid] + " fid : " + fid + " : " + getWord[fid]);
//                    if(getWord[mid].startsWith("-")){
//                        getWord[mid] = getWord[mid].replace("-", "");
//                    }
//                    if(getWord[fid].startsWith("-")){
//                        getWord[fid] = getWord[fid].replace("-", "");
//                    }
//                    if(getWord[iid].startsWith("-")){
//                        getWord[iid] = getWord[iid].replace("-", "");
//                    }
//                    String mut = Stemmer.getRootWord(getWord[mid].trim());
                    String mut = Stemmer.getRootWord(rawWords.get(mid-1).toString().trim());
//                    if (mut.contains("-")) {
//                        String parts[] = mut.split("\\-");
//                        for (int j = 0; j < parts.length; j++) {
//                            mut = parts[j].trim();
//                        }
//                    } else 
                        if (mut.contains("/")) {

                        String parts[] = mut.split("\\/");
                        parts[parts.length - 1] = parts[parts.length - 1] + " ";
                        String baseMut = parts[0].trim(); //.substring(0, parts[0].trim().length()-1 );
                        //System.out.println(baseMut);
                        if (baseMut.length() > 2) {
                            if (baseMut.charAt(baseMut.length() - 1) == '\\') {
                                baseMut = baseMut.substring(0, baseMut.length() - 2);
                            } else {
                                baseMut = baseMut.substring(0, baseMut.length() - 1);
                            }
                        }
//                System.out.println("BaseMutation : " + baseMut);
                        for (int j = 0; j < parts.length; j++) {

                            if (parts[j].length() > 1) {
                                if (parts[j].trim().charAt(parts[j].trim().length() - 1) == '\\') {
                                    parts[j] = parts[j].trim().substring(0, parts[j].trim().length() - 1);
                                }

                                mut = baseMut + parts[j].trim();
                            }
                        }
                    }

                    String imp = Stemmer.getRootWord(rawWords.get(iid-1).toString().trim());
                    String func = Stemmer.getRootWord(rawWords.get(fid-1).toString().trim());
//                        System.out.println("mut : " + mut + " imp : " + imp + " func : " + func);

                    if (null != wordlist.get(mut) && null != wordlist.get(imp) && null != wordlist.get(func)) {
                        int newmutid = wordlist.get(mut);
                        int newimpid = wordlist.get(imp);
                        int newfuncid = wordlist.get(func);
                        System.out.println("insert into sentence values(" + id + ", '" + content + "', true);");
                        System.out.println("insert into wordposition values(" + wid++ + ", " + id + ", " + newmutid + ", " + mid + ");");
                        System.out.println("insert into wordposition values(" + wid++ + ", " + id + ", " + newimpid + ", " + iid + ");");
                        System.out.println("insert into wordposition values(" + wid++ + ", " + id + ", " + newfuncid + ", " + fid + ");");
                        id++;
                    }else{
//                        System.out.println("WORD DOES NOT EXIST");
                    }
                } else {
//                    System.out.println("mid : " + mid + " iid : " + iid  + " fid : " + fid );
//                    System.out.println("------INDEX OUT OF BOUND--------");
                }
            }
//            System.out.println("Dual : " + count);
        } catch (SQLException ex) {
            Logger.getLogger(ScriptGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
