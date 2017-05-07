package unimol.wordsimilarityprocessor.information;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import unimol.wordsimilarityprocessor.processor.Stemmer;
import unimol.wordsimilarityprocessor.processor.Word;


/**
 *
 * @author Stefano Dalla Palma
 */
public class SetWordsInformation {

    private final File storeDir = new File("C:/Users/stefa/Documents/Neo4j/sim.db");
    private final String SET_INFORMATION = "MATCH (w:Word) WHERE w.word = {word} AND w.pos = {pos} SET w.information = {information}";

    InformationProcessor infoProcessor;
    HashMap<Word, Double> wordsInformations;
    Stemmer stemmer;
    GraphDatabaseFactory dbFactory;
    GraphDatabaseService dbService;

    public SetWordsInformation() {
        this.stemmer = new Stemmer();
        this.wordsInformations = new HashMap();
        this.dbFactory = new GraphDatabaseFactory();
    }

    private void computeAdjectivesInformation(ArrayList<Word> adjectives) {
        int i = 1;
        for (Word adj : adjectives) {

            if (i % 100 == 0) {
                System.out.println("Db is updating...");
                updateDB();
                System.out.println("Update success.");
            }

            String stemmedAdj = this.stemmer.stem(adj.getWord());
            Word stemmedWord = new Word(stemmedAdj, adj.getPos());
            this.infoProcessor = new InformationProcessor(stemmedWord);
            this.infoProcessor.computeInformation();
            double information = this.infoProcessor.getInformation();

            if (information > 0) {
                System.out.println(i + ".Adj: " + adj.getWord() + " (" + stemmedAdj + "): " + information);
                this.wordsInformations.put(stemmedWord, information);
                i++;
            }
        }

        if (!this.wordsInformations.isEmpty()) {
            System.out.println("Db is updating...");
            updateDB();
            System.out.println("Update success.");
        }
    }

    private void computeAdjectivesInformation(ArrayList<Word> adjectives, boolean stemWord) {

        int i = 1;
        for (Word adj : adjectives) {

            if (i % 100 == 0) {
                System.out.println("Db is updating...");
                updateDB();
                System.out.println("Update success.");
            }

            if (stemWord) {
                String stemmedAdj = this.stemmer.stem(adj.getWord());
                String pos = adj.getPos();
                adj = new Word(stemmedAdj, pos);
            }

            this.infoProcessor = new InformationProcessor(adj);
            this.infoProcessor.computeInformation();
            double information = this.infoProcessor.getInformation();

            if (information > 0) {
                System.out.println(i + ".Adj: " + adj.getWord() + ": " + information);
                this.wordsInformations.put(adj, information);
                i++;
            }
        }

        if (!this.wordsInformations.isEmpty()) {
            System.out.println("Db is updating...");
            updateDB();
            System.out.println("Update success.");
        }
    }

    private void computeAdverbsInformation(ArrayList<Word> adverbs) {

        int i = 1;
        for (Word adv : adverbs) {
            if (i % 100 == 0) {
                System.out.println("Db is updating...");
                updateDB();
                System.out.println("Update success.");
            }
            String stemmedAdv = this.stemmer.stem(adv.getWord());
            Word stemmedWord = new Word(stemmedAdv, adv.getPos());
            this.infoProcessor = new InformationProcessor(stemmedWord);
            this.infoProcessor.computeInformation();
            double information = this.infoProcessor.getInformation();

            if (information > 0) {
                System.out.println(i + ".Adv: " + adv.getWord() + " (" + stemmedAdv + "): " + information);
                this.wordsInformations.put(stemmedWord, information);
                i++;
            }
        }

        if (!this.wordsInformations.isEmpty()) {
            System.out.println("Db is updating...");
            updateDB();
            System.out.println("Update success.");
        }
    }

    private void computeAdverbsInformation(ArrayList<Word> adverbs, boolean stemWord) {

        int i = 1;
        for (Word adv : adverbs) {
            if (i % 100 == 0) {
                System.out.println("Db is updating...");
                updateDB();
                System.out.println("Update success.");
            }

            if (stemWord) {
                String stemmedAdv = this.stemmer.stem(adv.getWord());
                String pos = adv.getPos();
                adv = new Word(stemmedAdv, pos);
            }

            this.infoProcessor = new InformationProcessor(adv);
            this.infoProcessor.computeInformation();
            double information = this.infoProcessor.getInformation();

            if (information > 0) {
                System.out.println(i + ".Adv: " + adv.getWord() + ": " + information);
                this.wordsInformations.put(adv, information);
                i++;
            }
        }

        if (!this.wordsInformations.isEmpty()) {
            System.out.println("Db is updating...");
            updateDB();
            System.out.println("Update success.");
        }
    }

    private void updateDB() {
        this.dbService = dbFactory.newEmbeddedDatabase(storeDir);

        // Update word.information for adjectives and adverbs into db
        Map<String, Object> params = new HashMap();

        Iterator<Entry<Word, Double>> it = this.wordsInformations.entrySet().iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            Word w = (Word) e.getKey();
            params.put("word", w.getWord());
            params.put("pos", w.getPos());
            params.put("information", e.getValue());
            try (Transaction tx = this.dbService.beginTx()) {
                this.dbService.execute(SET_INFORMATION, params);
                tx.success();
            }
            it.remove();
        }
        this.dbService.shutdown();
    }

    private ArrayList<Word> getAdjectivesWithoutInformation() {
        final String LOAD_ADJECTIVES_WITHOUT_INFORMATION = "MATCH (n) WHERE  n.information = 0 AND n.pos CONTAINS \"jj\"  RETURN n";
        ArrayList<Word> adjectives = new ArrayList();

        this.dbService = dbFactory.newEmbeddedDatabase(storeDir);
        try (Transaction tx = this.dbService.beginTx()) {
            Result results = this.dbService.execute(LOAD_ADJECTIVES_WITHOUT_INFORMATION);

            while (results.hasNext()) {
                Node n = (Node) results.next().get("n");
                adjectives.add(new Word(n.getProperty("word").toString(), n.getProperty("pos").toString()));
            }
            tx.success();
        }
        this.dbService.shutdown();

        return adjectives;
    }

    private ArrayList<Word> getAdverbsWithoutInformation() {
        final String LOAD_ADJECTIVES_WITHOUT_INFORMATION = "MATCH (n) WHERE  n.information = 0 AND n.pos CONTAINS \"rb\"  RETURN n";
        ArrayList<Word> adverbs = new ArrayList();

        this.dbService = dbFactory.newEmbeddedDatabase(storeDir);
        try (Transaction tx = this.dbService.beginTx()) {
            Result results = this.dbService.execute(LOAD_ADJECTIVES_WITHOUT_INFORMATION);

            while (results.hasNext()) {
                Node n = (Node) results.next().get("n");
                adverbs.add(new Word(n.getProperty("word").toString(), n.getProperty("pos").toString()));
            }
            tx.success();
        }
        this.dbService.shutdown();

        return adverbs;
    }
}
