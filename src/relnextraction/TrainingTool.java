/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package relnextraction;

import Dao.Dao;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import grph.Grph;
import grph.VertexPair;
import grph.in_memory.InMemoryGrph;
import grph.path.Path;
import grph.properties.Property;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import toools.set.IntHashSet;
import toools.set.IntSet;

/**
 *
 * @author Chaitali
 */
public class TrainingTool {

    public static LexicalizedParser lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
    public static ArrayList<String[]> queryTripless = new ArrayList<>();

    public static void main(String[] args) {

        TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        List<CoreLabel> rawWords = null;
        Connection c = Dao.getCaseStudy1Connection();
        Statement st1 = null, st2 = null, st3 = null, st4 = null;
        ResultSet rs1 = null, rs2 = null, rs3 = null, rs4 = null;
        int count = 1;

        try {
            String stExtract = "select * from sentence where \"isTraining\" = true;";

            st1 = c.createStatement();
            rs1 = st1.executeQuery(stExtract);

            while (rs1.next()) {

                int sentId = rs1.getInt("id");
                String sentence = rs1.getString("content");
                sentence = sentence.replaceAll("\\.(?!.*\\.)", " ").replace("-", " ");
                System.out.println("\n" + sentId + " " + sentence);
                rawWords = tokenizerFactory.getTokenizer(new StringReader(sentence)).tokenize();
//                System.out.println("\nRawWords : ");
//                for (int i = 0; i < rawWords.size(); i++) {
//                    System.out.print(i + "-"+rawWords.get(i) + " ");
//                }
                Tree parse = lp.apply(rawWords);
//                TreePrint tp2 = new TreePrint("typedDependenciesCollapsed");
//                System.out.println("Typed Dependencies : ");
//                tp2.printTree(parse);
//                System.out.println("RawWords Size : " + rawWords.size());
                TreebankLanguagePack tlp = new PennTreebankLanguagePack();
                GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
                GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
                Collection<TypedDependency> td = gs.typedDependenciesCCprocessed();
                Object[] list = td.toArray();
                com.hp.hpl.jena.rdf.model.Statement s;
                com.hp.hpl.jena.rdf.model.Statement[][] statements;
                ArrayList<com.hp.hpl.jena.rdf.model.Statement> tripleList;
                Set<String> tempList;
                Model model = ModelFactory.createDefaultModel();
                statements = new com.hp.hpl.jena.rdf.model.Statement[rawWords.size() + 1][rawWords.size() + 1];
                HashMap<Integer, String> PositionCategoryList = new HashMap<>();
                ArrayList<String> criticalCats = new ArrayList<>();
                boolean available, transitive;

                st2 = c.createStatement();
                String wordPosExtract = "select * from wordposition where \"sentId\" = " + sentId + ";";
//                System.out.println("Query : " + wordPosExtract);
                rs2 = st2.executeQuery(wordPosExtract);
                List<Integer> criticalNodes = new ArrayList<>();

                while (rs2.next()) {

                    String category = null;
                    int wordId = rs2.getInt("wordId");
                    int wordPos = rs2.getInt("wordPos");
//                    System.out.print("WordId : " + wordId + " Position : " + wordPos);
//                    System.out.print(wordPos + " ");
                    criticalNodes.add(wordPos);

                    st3 = c.createStatement();
                    String wordCatExtract = "select * from word where \"id\" = " + wordId + ";";
//                System.out.println("Query : " + wordPosExtract);
                    rs3 = st3.executeQuery(wordCatExtract);

                    while (rs3.next()) {

                        int catId = rs3.getInt("catId");
                        String word = rs3.getString("name");
                        String wordStem = rs3.getString("stem");
                        st4 = c.createStatement();
                        String catNameExtract = "select * from category where \"id\" = " + catId + ";";
//                System.out.println("Query : " + wordPosExtract);
                        rs4 = st4.executeQuery(catNameExtract);
                        if (rs4.next()) {
                            category = rs4.getString("name");
                        }
//                        System.out.print(" Category : " + category + " Word : " + word + "\n");

                    }
                    PositionCategoryList.put(wordPos, category);
                    criticalCats.add(category);
                }

                Grph g = new InMemoryGrph();
                Grph g1 = new InMemoryGrph();
                Property edgeProperty = g.getEdgeLabelProperty();
                Property nodeProperty = g.getVertexLabelProperty();
                tripleList = new ArrayList<>();
                tempList = new HashSet<>();
                int vertex1, vertex2;
                HashMap<String, String> replacementMap = new HashMap<>();

                for (int index = 0; index < rawWords.size(); index++) {
                    g.addVertex(index + 1);
                    String token = rawWords.get(index).toString().trim();
                    nodeProperty.setValue(index + 1, token);

                    if (PositionCategoryList.containsKey(index + 1)) {
                        replacementMap.put(PositionCategoryList.get(index + 1), token + "-" + (index + 1));
                    }
                }
                g.setVerticesLabel(nodeProperty);

                for (int index = 0; index < list.length; index++) {
//                    System.out.println(((TypedDependency) list[index]).gov().toString().substring(((TypedDependency) list[index]).gov().toString().lastIndexOf("-") + 1));
//                    System.out.println(((TypedDependency) list[index]).dep().toString().substring(((TypedDependency) list[index]).dep().toString().lastIndexOf("-") + 1));
                    vertex1 = Integer.parseInt(((TypedDependency) list[index]).gov().toString().substring(((TypedDependency) list[index]).gov().toString().lastIndexOf("-") + 1));
                    vertex2 = Integer.parseInt(((TypedDependency) list[index]).dep().toString().substring(((TypedDependency) list[index]).dep().toString().lastIndexOf("-") + 1));

//                System.out.println("Vertex1 " + vertex1 + " Vertex2 " + vertex2);
                    g.addSimpleEdge(vertex1, index + 1, vertex2, false);
                    edgeProperty.setValue(index + 1, ((TypedDependency) list[index]).reln().getShortName());

                    statements[vertex1][vertex2] = statements[vertex2][vertex1] = ResourceFactory.createStatement(
                            ResourceFactory.createResource("http://example.uga.edu/" + ((TypedDependency) list[index]).gov().toString()),
                            ResourceFactory.createProperty("http://example.uga.edu/" + ((TypedDependency) list[index]).reln().getShortName()),
                            ResourceFactory.createResource("http://example.uga.edu/" + ((TypedDependency) list[index]).dep().toString()));
//                System.out.println("Statements : " + statements[vertex1][vertex2]);
                }
                g.setEdgesLabel(edgeProperty);

//                System.out.println("HashMap : " + PositionCategoryList.entrySet());
//                System.out.println("CriticalCats : " + Arrays.toString(criticalCats.toArray()));                
                IntSet abc = new IntHashSet();
                Path p1 = null;
                for (int i = 0; i < criticalNodes.size(); i++) {
                    for (int j = 0; j < criticalNodes.size(); j++) {
                        if (i != j && g.containsAPath(criticalNodes.get(i), criticalNodes.get(j))) {
                            p1 = g.getShortestPath(criticalNodes.get(i), criticalNodes.get(j));
                            String pNodes = p1.toString();
                            String[] nds = pNodes.split(" ");
                            for (int k = 0; k < nds.length - 1; k++) {
                                abc.add(Integer.parseInt(nds[k].substring(1)));
//                        System.out.println("Vertex : " + Integer.parseInt(nds[k].substring(1)));
                            }
                        }
                    }
                }
//                System.out.println();
//                System.out.println("TripleList : " + tripleList);
//                System.out.println("ABC : " + abc);
                g1 = g.getSubgraphInducedByVertices(abc);
                Collection<VertexPair> edgePairs = g1.getEdgePairs();

                for (VertexPair edgePair : edgePairs) {
//                    System.out.println("EdgePair : (" + edgePair.first + ", " + edgePair.second + ")");
//                    System.out.println(statements[edgePair.first][edgePair.second]);
                    tripleList.add(statements[edgePair.first][edgePair.second]);
                }
//                g.highlight(g1, 1);
//                g.display();

//                System.out.println("TripleList : " + tripleList);
                model.add(tripleList);
                String query = "";
                StmtIterator sIter = model.listStatements(new SimpleSelector());
                while (sIter.hasNext()) {
                    s = sIter.nextStatement();
//                    System.out.println("stmt : " + s);
                    query = query + s.getSubject().toString() + " " + s.getPredicate().toString() + " " + s.getObject().toString() + ". ";

                    tempList.add(s.getSubject().toString());
                    tempList.add(s.getObject().toString());
                }
//                System.out.println("Query : " + query);
//                System.out.println("tempList : " + tempList);

                model.remove(tripleList);

                for (String key : replacementMap.keySet()) {
                    query = query.replace("http://example.uga.edu/" + replacementMap.get(key), "?" + key);
                }

                int i = 65;
                String toReplace = "";
                for (String replace : tempList) {
                    // String toReplace="?"+Character.toString ((char) i);
                    toReplace = "?" + String.valueOf(Character.toChars(i));
                    query = query.replace(replace, toReplace);
                    i++;
                }
                String preQuery = "", postQuery = "";
                for (String cat : criticalCats) {
                    preQuery = preQuery + " ?" + cat;
                    postQuery = postQuery + " ?" + cat + " textMine:type " + "textMine:" + cat + ".";
                }
                query = query.replace("http://example.uga.edu/", "textMine:");

//                available = checkAvailability(query);
//                transitive = transitivityCheck(query);
                if (!checkAvailability(query)  && !transitivityCheck(query)
                        ) {
                    addInTripleList(query);
                    String finalQuery = "PREFIX textMine: <http://example.uga.edu/> " + "Select" + preQuery + " where { " + query + postQuery + " }";
                    System.out.println(count++);
//                    System.out.println(count++ + " " + finalQuery + "\n");
//                    Pattern.Insert3(finalQuery);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(TrainingTool.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                c.close();
                if (st1 != null) {
                    st1.close();
                }
                if (st2 != null) {
                    st2.close();
                }
                if (st3 != null) {
                    st3.close();
                }
                if (st4 != null) {
                    st4.close();
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
                if (rs4 != null) {
                    rs4.close();
                }
            } catch (SQLException e) {
                e.getMessage();
            }
        }

    }

    private static boolean checkAvailability(String query) {
        if (queryTripless.isEmpty()) {
//            System.out.println("Null TripleList!!");
            return false;
        } else {
            ArrayList<String> newtest = new ArrayList<>();
            String[] testS1 = query.split("\\.\\s+");
            for (String test1 : testS1) {
                if (test1.endsWith(".")) {
                    test1 = test1.replaceAll("\\.", "");
                }
                test1 = test1.replaceAll("positive", "impact");
                test1 = test1.replaceAll("negative", "impact");
                test1 = test1.replaceAll("neutral", "impact");
                test1 = test1.replaceAll("unknown", "impact");
                newtest.add(test1);
            }
        System.out.println(newtest);
            String[] trips = new String[newtest.size()];
            trips = newtest.toArray(trips);
            Arrays.sort(trips);
            for (String[] trp : queryTripless) {
//                System.out.println("Triple : " + Arrays.toString(trp));
                if (Arrays.equals(trp, trips)) {
                    System.out.print("[");
                    for (String trp1 : trp) {
                        System.out.print(trp1 + ", ");
                    }
                    System.out.print("]");
//                    System.out.println();
//                    for (String trip : trips) {
//                        System.out.print(trip + " ");
//                    }
                    System.out.println("\nMatch \n");
                    return true;
                }
            }
        }
        return false;
    }

    private static void addInTripleList(String query) {
        ArrayList<String> newtest = new ArrayList<>();
        String[] testS1 = query.split("\\.\\s+");
        for (String test1 : testS1) {
            if (test1.endsWith(".")) {
                test1 = test1.replaceAll("\\.", "");
            }
            test1 = test1.replace("positive", "impact");
            test1 = test1.replace("negative", "impact");
            test1 = test1.replace("neutral", "impact");
            test1 = test1.replace("unknown", "impact");
            newtest.add(test1);
        }
//        System.out.println(newtest);
        String[] trips = new String[newtest.size()];
        trips = newtest.toArray(trips);
        Arrays.sort(trips);
        queryTripless.add(trips);
//        System.out.println(query);
        System.out.println("Added to queryTripless!! : " + query);
    }

    private static boolean transitivityCheck(String query) {
        int count = 0;
        boolean flag = false;

        //seperate each query triple and add to Arraylist
        ArrayList<String> querytriplist = new ArrayList<>();
        String[] querytriparray = query.split("\\.\\s+");
        for (String singletrip : querytriparray) {
            if (singletrip.endsWith(".")) {
                singletrip = singletrip.replaceAll("\\.", "");
            }
            singletrip = singletrip.replace("positive", "impact");
            singletrip = singletrip.replace("negative", "impact");
            singletrip = singletrip.replace("neutral", "impact");
            singletrip = singletrip.replace("unknown", "impact");
            querytriplist.add(singletrip);
        }

        //convert Arraylist to String[]
        String[] trips = new String[querytriplist.size()];
        trips = querytriplist.toArray(trips);

        //compare current query to all previously generated 
        //queries to check similarity
        for (String[] qTrips : queryTripless) {

            //collect queries having same number of triples as current query
            if (qTrips.length == trips.length) {
//                System.out.println("Same size");

                ArrayList<String> relations1 = new ArrayList<>();
                ArrayList<String> relations2 = new ArrayList<>();

                ArrayList<String> splitTriple1 = new ArrayList<>();
                ArrayList<String> splitTriple2 = new ArrayList<>();

                String[] r1;
                String[] r2;

                for (int i = 0; i < qTrips.length; i++) {
                    //split each triple by space
                    r1 = qTrips[i].split(" ");
                    r2 = trips[i].split(" ");

                    //add all those splited triple elements to some Arraylist called splitTriple
                    for (int j = 0; j < r1.length; j++) {
                        splitTriple1.add(r1[j]);
                        splitTriple2.add(r2[j]);
                    }

                    //add relation(predicate) of each triple to some Arraylist called relation
                    relations1.add(r1[1]);
                    relations2.add(r2[1]);
                }

                //collect queries having same function/mutatio/impact or dual/mutation as in current query
                //using splitTriple
                if ((splitTriple1.contains("?function") && splitTriple2.contains("?function"))
                        || (splitTriple1.contains("?mutation") && splitTriple2.contains("?mutation"))) {

                    //convert relation(predicate)list to String[]
                    String[] rel1 = new String[relations1.size()];
                    rel1 = relations1.toArray(rel1);
                    Arrays.sort(rel1);

                    String[] rel2 = new String[relations2.size()];
                    rel2 = relations2.toArray(rel2);
                    Arrays.sort(rel2);

                    //collect queries having same relations as current query
                    if (Arrays.equals(rel1, rel2)) {
//                        System.out.println("Same relations");
                        ArrayList<String> sorted1 = new ArrayList<>();
                        ArrayList<String> sorted2 = new ArrayList<>();

                        //sort triples based on relations' order
                        for (String rel11 : rel1) {
                            for (String qTrip : qTrips) {
                                if (qTrip.contains(rel11)) {
                                    sorted1.add(qTrip);
                                }
                            }
                        }
                        for (String rel21 : rel2) {
                            for (String trip : trips) {
                                if (trip.contains(rel21)) {
                                    sorted2.add(trip);
                                }
                            }
                        }

                        //convert sorted triplelist to String[]
                        String[] sortStr1 = new String[sorted1.size()];
                        sortStr1 = sorted1.toArray(sortStr1);

                        String[] sortStr2 = new String[sorted2.size()];
                        sortStr2 = sorted2.toArray(sortStr2);

                        //collect queries having same structure as current query
                        for (int j = 0; j < sortStr1.length; j++) {
//                            System.out.println("QueryTriple : " + sortStr1[j]);
                            sortStr1[j] = sortStr1[j].replace("?", "");
//                            System.out.println("CurrentQueryTriple : " + sortStr2[j]);
                            sortStr2[j] = sortStr2[j].replace("?", "");
                            String[] firstStrSplit = sortStr1[j].split(" ");
                            String[] secondStrSplit = sortStr2[j].split(" ");

                            if (sortStr1[j].matches("." + "\\s+" + firstStrSplit[1] + "\\s+" + ".")) {
                                if (sortStr2[j].matches(". " + secondStrSplit[1] + " .")) {
                                    count++;
                                } else {
                                    break;
                                }
                            } else if (sortStr1[j].matches("\\w+" + "\\s+" + firstStrSplit[1] + "\\s+" + ".")) {
                                if (sortStr2[j].matches(firstStrSplit[0] + "\\s+" + secondStrSplit[1] + "\\s+" + ".")) {
                                    count++;
                                } else {
                                    break;
                                }
                            } else if (sortStr1[j].matches("." + "\\s+" + firstStrSplit[1] + "\\s+" + "\\w+")) {
                                if (sortStr2[j].matches("." + "\\s+" + secondStrSplit[1] + "\\s+" + firstStrSplit[2])) {
                                    count++;
                                } else {
                                    break;
                                }
                            } else if (sortStr1[j].matches("\\w+" + "\\s+" + firstStrSplit[1] + "\\s+" + "\\w+")) {
                                if (sortStr2[j].matches(firstStrSplit[0] + "\\s+" + secondStrSplit[1] + "\\s+" + firstStrSplit[2])) {
                                    count++;
                                } else {
                                    break;
                                }
                            }
                        }
                        if (count == sortStr1.length) {
                            System.out.println("Query : " + querytriplist);
                            System.out.println("Existing trips : ");
                            for (String qTrip : qTrips) {
                                System.out.print(qTrip + ". ");
                            }
                            System.out.println("\nSimilar Query!!!!");                            
                            System.out.println();
                            return true;
                        } 
                    }
                }
            }
        }
        return false;
    }
}
