package unimol.wordsimilarityprocessor.graph;

import unimol.wordsimilarityprocessor.processor.Word;
import unimol.wordsimilarityprocessor.information.SimilarityProcessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Result;
import unimol.wordsimilarityprocessor.processor.DocumentProcessorThread;
import unimol.wordsimilarityprocessor.processor.SentenceProcessorThread;
import unimol.wordsimilarityprocessor.processor.Stemmer;

/**
 *
 * @author Stefano Dalla Palma
 */
public class Main {

    public static String WORD = "word";
    public static String SENTIMENT = "sentiment";
    public static String POS = "pos";
    public static String ADJ = "jj";
    public static String ADV = "rb";

    public enum NodeType implements Label {
        Word;
    }

    public enum RelationType implements RelationshipType {
        SimilarTo;
    }

    public static void main(String[] args) {

        DictionaryManager dic = new DictionaryManager();
        ArrayList<Word> seedList = dic.loadArrayAdjectives();
        ArrayList<Word> unratedAdjectives = dic.loadUnratedAdjectives();
        // HashMap<String, Integer> ratedAdverbs =  dic.loadAdverbs();
        //ArrayList<Word> unratedAdverbs =  dic.loadUnratedAdverbs();
        //ArrayList<Word> words = new ArrayList();

        GraphDatabaseService graphDb = GraphDbFactory.getInstance();

        //_____________________________________________________________________
        
        /// SALVA LE SOMIGLIANZE REGISTRATE NEL FILE similarities.txt 
       /* List<Word> infowords = new CopyOnWriteArrayList<Word>();
        HashMap<Word[], Double> similarities = new HashMap();
        File file = new File("similarities.txt");
        Scanner in = null;
        try {
            in = new Scanner(new FileReader(file));
            String line[];
            while (in.hasNextLine()) {

                line = in.nextLine().split("#");
                double similarity = Double.parseDouble(line[1].replace(" ", ""));

                String adjective[];

                adjective = line[0].split(",");

                Word adj1 = new Word(adjective[0].split("-")[0], adjective[0].split("-")[1]);
                Word adj2 = new Word(adjective[1].split("-")[0], adjective[1].split("-")[1].replace(" ", ""));

                Map<String, Object> params = new HashMap();

                if (!infowords.contains(adj1)) {
                    params.put("word1", adj1.getWord());
                    params.put("pos1", adj1.getPos());
                    try (Transaction tx = graphDb.beginTx()) {

                        Result result = graphDb.execute("MATCH (n) where n.word = {word1} and n.pos = {pos1} RETURN n.information", params);

                        if (result.hasNext()) {
                            Map<String, Object> map = result.next();
                            adj1.setInformation((double) map.get("n.information"));
                            infowords.add(adj1);
                        }
                        tx.success();
                    }
                    infowords.add(adj1);
                } else {
                    adj1.setInformation(infowords.get(infowords.indexOf(adj1)).getInformation());
                }
                if (!infowords.contains(adj2)) {
                    params.put("word1", adj2.getWord());
                    params.put("pos1", adj2.getPos());
                    try (Transaction tx = graphDb.beginTx()) {

                        Result result = graphDb.execute("MATCH (n) where n.word = {word1} and n.pos = {pos1} RETURN n.information", params);

                        if (result.hasNext()) {
                            Map<String, Object> map = result.next();
                            adj2.setInformation((double) map.get("n.information"));
                            infowords.add(adj2);
                        }
                        tx.success();
                    }
                    infowords.add(adj2);
                } else {
                    adj2.setInformation(infowords.get(infowords.indexOf(adj2)).getInformation());
                }

                Word[] adjs = new Word[2];
                adjs[0] = adj1;
                adjs[1] = adj2;
                similarities.put(adjs, similarity);
            }
        } catch (FileNotFoundException | NoSuchElementException ex) {
            Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        Map<String, Object> params = new HashMap();

        Iterator<Entry<Word[], Double>> itt = similarities.entrySet().iterator();

        while (itt.hasNext()) {
            Entry e = itt.next();
            Word[] words = (Word[]) e.getKey();
            System.out.println(words[0].toString() + " - " + words[1].toString() + " : " + e.getValue());
            params.put("seedWord", words[0].getWord());
            params.put("posSeedWord", words[0].getPos());
            params.put("information1", words[0].getInformation());
            params.put("adj", words[1].getWord());
            params.put("posAdj", words[1].getPos());
            params.put("information2", words[1].getInformation());
            params.put("similarity", e.getValue());

            try (Transaction tx = graphDb.beginTx()) {
                graphDb.execute("MATCH (n) where n.word = {seedWord} and n.pos = {posSeedWord} and n.information = {information1}"
                        + "WITH n MATCH(m) WHERE m.word = {adj} and m.pos = {posAdj} and m.information = {information2}"
                        + "CREATE (n)-[r:sim]->(m) SET r.similarity = {similarity}", params);
                tx.success();
            }
        }*/

        ///____________________________________________________________
        /* try (Transaction tx = graphDb.beginTx()) {

            Result results = graphDb.execute("MATCH (n) WHERE n.pos contains \"jj\" and n.information >= 300 AND n.information <500 RETURN n ORDER BY n.information DESC");

            while (results.hasNext()) {
                Node n = (Node) results.next().get("n");
                Word adjective = new Word((String) n.getProperty("word"), (String) n.getProperty("pos"));
                adjective.setInformation((double) n.getProperty("information"));
                words.add(adjective);
            }

            tx.success();
        }
        //graphDb.shutdown();
        System.out.println("Dictionaries loaded");

        SimilarityProcessor facade;

        HashMap<Word, Double> wordsInformation = new HashMap();
        HashMap<Word[], Double> similarities = new HashMap();
        Word[] pairWord = new Word[2];
        Stemmer stemmer = new Stemmer();
        Map<String, Object> param = new HashMap();

        for (Word seed : seedList) {
            seed.setWord(stemmer.stem(seed.getWord()));
            System.out.println("--| " + seed.getWord() + " |--");
            param.put("seedWord", seed.getWord());

            try (Transaction tx = graphDb.beginTx()) {
                Result result = graphDb.execute("match (n) where n.word = {seedWord} and n.pos contains \"jj\" return n", param);
                param.clear();
                if (!result.hasNext()) {
                    tx.success();
                    continue;
                } else {
                    Node node = (Node) result.next().get("n");
                    seed.setPos((String) node.getProperty("pos"));
                    //seed.setInformation((double) node.getProperty("information"));
                    seed.setInformation(Double.parseDouble(node.getProperty("information").toString()));
                }
            }
            pairWord[0] = seed;

            ExecutorService pool = Executors.newFixedThreadPool(4);

            /* for (Word comparedWord : words) {
                comparedWord.setWord(comparedWord.getWord());
                pairWord[1] = comparedWord;

                facade = new SimilarityProcessor(seed, comparedWord);
                facade.setDbService(graphDb);
                pool.execute(facade);

                /*System.out.printf("Sim(" + seed.getWord() + " [" + seed.getPos() + "], " + comparedWord.getWord() + " [" + comparedWord.getPos() + "]) = ");
                facade = new SimilarityProcessor(seed, comparedWord);
                facade.setDbService(graphDb);
                long start = System.nanoTime();
                facade.computeSimilarity();
                double similarity = facade.getSimilarity();
                System.out.println(similarity + " in " + ((System.nanoTime() - start) / 1000000000) + " s");

                if (similarity > 0) {
                    similarities.put(pairWord, similarity);
                }*/
 /* }

            pool.shutdown();

            try {
                pool.awaitTermination(10, TimeUnit.MINUTES);
            } catch (InterruptedException ex) {
                Logger.getLogger(DocumentProcessorThread.class.getName()).log(Level.SEVERE, null, ex);
            }

            while (!pool.isTerminated()) {
            }*/
        //}
        // graphDb = GraphDbFactory.getInstance();
        /* Iterator<Entry<Word[], Double>> it = similarities.entrySet().iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            Word[] w = (Word[]) e.getKey();

            System.out.println("Inserting: (" + w[0].getWord() + ")->[sim: " + e.getValue() + "]->(" + w[1].getWord() + ")");

            Map<String, Object> params = new HashMap();
            params.put("seedWord", w[0].getWord());
            params.put("posSeedWord", w[0].getPos());
            params.put("informationSeedWord", w[0].getInformation());
            params.put("adj", w[1].getWord());
            params.put("posAdj", w[1].getPos());
            params.put("informationAdj", w[1].getInformation());
            params.put("similarity", e.getValue());

            try (Transaction tx = graphDb.beginTx()) {
                graphDb.execute("MERGE (n:Word {word:{seedWord}, pos:{posSeedWord}, information:{informationSeedWord}})-[r:sim]->(m:Word {word:{adj}, pos:{posAdj}, information:{informationAdj}})"
                        + "ON MATCH SET r.similarity = {similarity} "
                        + "ON CREATE SET r.similarity = {similarity}", params);
                tx.success();
            }*/
        graphDb.shutdown();

    }

}
