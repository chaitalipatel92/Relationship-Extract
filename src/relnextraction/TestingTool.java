/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relnextraction;

import Dao.Dao;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import freemarker.core.ParseException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
//import freemarker.template.Configuration;
//import freemarker.template.MalformedTemplateNameException;
//import freemarker.template.SimpleHash;
//import freemarker.template.SimpleList;
//import freemarker.template.Template;
//import freemarker.template.TemplateException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 *
 * @author Chaitali
 */
public class TestingTool {

    public static String ontoURI = "http://example.uga.edu/";
    public static ArrayList<String> patternQueries = new ArrayList<>();
    public static HashMap<String, String> wordCategorylist = new HashMap<>();
    public static LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
    public static Model model = ModelFactory.createDefaultModel();

    public static void main(String[] args) {

//        Configuration cfg = new Configuration();
        try {
//            Template template = cfg.getTemplate("src/helloworld.ftl");
            InitializeLists il = new InitializeLists();
            il.populateLists();
//            Writer file = new FileWriter(new File("D:/ftl/blog-template-output.txt"));

            Connection conn = Dao.getRelExtractConnection();
            Statement st1 = conn.createStatement();
            ResultSet rs1;
            String query = "Select * from sentence;";
            rs1 = st1.executeQuery(query);
            String sentence;
            HashMap<String, ArrayList<String>> wordsPerCategory = new HashMap<>();
//            SimpleHash modelRoot = new SimpleHash();
            long startTime = System.currentTimeMillis();
            while (rs1.next()) {
                sentence = rs1.getString("content");
                sentence = sentence.replaceAll("\\.(?!.*\\.)", " ");
                System.out.println("\n\n" + sentence);
                TokenizerFactory<CoreLabel> tokenizerFactory;
                List<CoreLabel> rawWords;
                tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
                rawWords = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
                ArrayList<String> tripleList = new ArrayList<>();

                for (int i = 0; i < rawWords.size(); i++) {

                    String token = rawWords.get(i).toString().trim();
                    String stem = Stemmer.getRootWord(token);

                    if (wordCategorylist.containsKey(stem)) {
                        String str;
                        str = ontoURI + stem + "\t" + ontoURI + "type" + "\t" + ontoURI + wordCategorylist.get(stem);
                        tripleList.add(str);
//                        System.out.println("word matched : " + token + "-" + wordCategorylist.get(stem));
                    }
                }
                
                com.hp.hpl.jena.query.Query pattquery = null;
                QueryExecution qe = null;
                com.hp.hpl.jena.rdf.model.Statement s = null;
                List<com.hp.hpl.jena.rdf.model.Statement> statements = new ArrayList<>();
                Tree parse = lp.apply(rawWords);
                TreebankLanguagePack tlp = new PennTreebankLanguagePack();
                GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
                GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
                Collection<TypedDependency> td = gs.typedDependenciesCCprocessed();
                Object[] list = td.toArray();

                for (Object object : list) {

                    String sub = ((TypedDependency) object).gov().toString();
                    String subStem = Stemmer.getRootWord(sub.substring(0, sub.lastIndexOf("-")));
//                    System.out.println(sub + " : " + subStem);
                    String pred = ((TypedDependency) object).reln().getShortName();
//                    System.out.println(pred + " : " + predStem);
                    String obj = ((TypedDependency) object).dep().toString();
                    String objStem = Stemmer.getRootWord(obj.substring(0, obj.lastIndexOf("-")));
//                    System.out.println(obj + " : " + objStem);
//                    s = ResourceFactory.createStatement(
//                            ResourceFactory.createResource(ontoURI + ((TypedDependency) object).gov().toString()),
//                            ResourceFactory.createProperty(ontoURI + ((TypedDependency) object).reln().getShortName()),
//                            ResourceFactory.createResource(ontoURI + ((TypedDependency) object).dep().toString()));
                    s = ResourceFactory.createStatement(
                            ResourceFactory.createResource(ontoURI + subStem),
                            ResourceFactory.createProperty(ontoURI + pred),
                            ResourceFactory.createResource(ontoURI + objStem));
//                    System.out.println("Statement : " + s);
                    statements.add(s);
                }
                for (String triple : tripleList) {
                    String[] sop = triple.split("\t");
                    s = ResourceFactory.createStatement(ResourceFactory.createResource(sop[0].trim()), ResourceFactory.createProperty(sop[1].trim()), ResourceFactory.createResource(sop[2].trim()));
                    statements.add(s);
                }
                model.add(statements);

                for (String queryPattern : patternQueries) {
                    pattquery = QueryFactory.create(queryPattern);
                    qe = QueryExecutionFactory.create(pattquery, model);
                    com.hp.hpl.jena.query.ResultSet results = qe.execSelect();

                    if (results.hasNext()) {
                        System.out.println("----------------------Mathced pattern/query-----------------------");
//                        System.out.println(queryPattern);
                        
//                        QuerySolution result = results.next();
//                        Iterator<String> variables = result.varNames();

//                        SimpleList catList = new SimpleList();

//                        while (variables.hasNext()) {
//                            SimpleHash category = new SimpleHash();
//                            String varName = variables.next();
//                            String varValue = result.get(varName).toString().substring(23);
//                            System.out.println("Original : " + varName + " " + result.get(varName).toString().substring(23));
//                            category.put("name", varName);
//                            category.put("value", varValue);
//                            System.out.println("Added : " + category.get("name") + " " + category.get("value"));
//                            catList.add(category);
//                        }
//                        modelRoot.put("Categories", catList);
//
//                        Writer out1 = new OutputStreamWriter(System.out);
//                        template.process(modelRoot, out1);
//                        out1.flush();
//
//                        template.process(modelRoot, file);
                    }
                }
                qe.close();
                model.remove(statements);
            }
//            file.flush();
//            file.close();
            
            System.out.println("Total Time : " + (System.currentTimeMillis() - startTime));
        } catch (SQLException ex) {
            Logger.getLogger(TestingTool.class.getName()).log(Level.SEVERE, null, ex);
        } 
//        catch (MalformedTemplateNameException ex) {
//            Logger.getLogger(TestingTool.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ParseException ex) {
//            Logger.getLogger(TestingTool.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(TestingTool.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (TemplateException ex) {
//            Logger.getLogger(TestingTool.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
}
