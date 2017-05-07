package unimol.wordsimilarityprocessor.information;

import unimol.wordsimilarityprocessor.graph.GraphDbFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import unimol.wordsimilarityprocessor.processor.Word;

/**
 *
 * @author Stefano Dalla Palma
 */
public class SimilarityProcessor implements Runnable {

    private final String GET_INTERSECTION_OF_RELATIONS = "MATCH (w1)-[r]->(n) WHERE w1.word={word1} and w1.pos={pos1} "
            + "WITH n MATCH (n)<-[r]-(w2) WHERE w2.word={word2} and w2.pos={pos2} "
            + "RETURN r,n";

    private Word word;
    private Word comparedWord;
    private double similarity;

    private ArrayList<RelationDependentPair> intersectionList;
    private GraphDatabaseService dbService;

    public SimilarityProcessor(Word word, Word comparedWord, GraphDatabaseService dbService) {
        this.word = word;
        this.comparedWord = comparedWord;
        this.intersectionList = new ArrayList();
        this.dbService = dbService;
    }

    public void computeSimilarity() {

        if (this.word.getWord().toLowerCase().equals(this.comparedWord.getWord().toLowerCase())) {
            this.similarity = 1.0;
        } else {

            if (this.dbService == null) {
                this.dbService = GraphDbFactory.getInstance();
            }
            
            Map<String, Object> params = new HashMap();
            params.put("adj1", this.word.getWord());
            params.put("adj2", this.comparedWord.getWord());

            try (Transaction tx = this.dbService.beginTx()) {
                Result results = this.dbService.execute("MATCH (n)-[r:sim]-(m) WHERE n.word = {adj1} AND m.word = {adj2} RETURN r.similarity as similarity ORDER BY similarity DESC LIMIT 1", params);

                if (results.hasNext()) {
                    this.similarity = Double.parseDouble(results.next().get("similarity").toString());
                } else {

                    // Check in the two words have relations in common
                    intersect();
                    if (!this.intersectionList.isEmpty()) {
                        double numerator = 0.0;
                        double denominator = 0.0;

                        /* Computes the information of 'word' and all pairs (relation, dependent) by setting up the RelationDependencyPairs to 'intersectionList' adding it to the 'numerator'
                         * and load the total information of the single word 'word' from the database adding it to 'denominator'
                         */
                        InformationProcessor wordProcessor = new InformationProcessor(this.word);
                        // Initialize the wordProcessor
                        wordProcessor.setRelationDependencyPairs(this.intersectionList);
                        wordProcessor.setDbService(dbService);

                        wordProcessor.computeInformation();
                        numerator += wordProcessor.getInformation();
                        if (this.word.getInformation() == 0.0) {
                            this.word.setInformation(wordProcessor.loadInformation());
                        }
                        denominator += this.word.getInformation();


                        /* Computes the information of 'comparedWord' and all pairs (relation, dependent) by setting up the RelationDependencyPairs to 'intersectionList' adding it to the 'numerator'
                         * and load the total information of the single word 'comparedWord' from the database adding it to 'denominator'
                         */
                        InformationProcessor comparedWordProcessor = new InformationProcessor(this.comparedWord);
                        // Initialize the omparedWordProcessor
                        comparedWordProcessor.setRelationDependencyPairs(this.intersectionList);
                        comparedWordProcessor.setDbService(dbService);
                        comparedWordProcessor.computeInformation();
                        numerator += comparedWordProcessor.getInformation();

                        if (this.comparedWord.getInformation() == 0.0) {
                            this.comparedWord.setInformation(comparedWordProcessor.loadInformation());
                        }
                        denominator += this.comparedWord.getInformation();

                        if (denominator > 0 && numerator > 0) {
                            this.similarity = numerator / denominator;
                            saveSimilarityInDb();
                        }
                    }
                }
                tx.success();
            }
        }
    }

    private void intersect() {

        // GraphDatabaseService dbService = GraphDbFactory.getInstance();
        Map<String, Object> params = new HashMap();
        params.put("word1", this.word.getWord());
        params.put("pos1", this.word.getPos());
        params.put("word2", this.comparedWord.getWord());
        params.put("pos2", this.comparedWord.getPos());

        try (Transaction tx = this.dbService.beginTx()) {
            Result results = this.dbService.execute(GET_INTERSECTION_OF_RELATIONS, params);

            while (results.hasNext()) {
                Map<String, Object> resultMap = results.next();
                Node node = (Node) resultMap.get("n");
                Relationship relationship = (Relationship) resultMap.get("r");

                try {
                    this.intersectionList.add(new RelationDependentPair(
                            new Relation(relationship.getType().name(), (int) Integer.parseInt(relationship.getProperty("frequency").toString())),
                            new Word(node.getProperty("word").toString(), node.getProperty("pos").toString())));
                } catch (NotFoundException ex) {

                }

            }
            tx.success();
        }
        //dbService.shutdown();
    }

    private double intersection(HashMap<RelationDependentPair, Double> map1, HashMap<RelationDependentPair, Double> map2) {
        double information = 0.0;

        Iterator<Entry<RelationDependentPair, Double>> it = map1.entrySet().iterator();
        Entry<RelationDependentPair, Double> entry;

        while (it.hasNext()) {
            entry = it.next();
            if (map2.containsKey(entry.getKey())) {
                information += entry.getValue() + map2.get(entry.getKey());
            }
        }

        return information;
    }

    public double getSimilarity() {
        return this.similarity;
    }

    public void setDbService(GraphDatabaseService dbService) {
        this.dbService = dbService;
    }

    private void saveSimilarityInDb() {
        Map<String, Object> params = new HashMap();
        params.put("seedWord", word.getWord());
        params.put("posSeedWord", word.getPos());
        params.put("information1", word.getInformation());
        params.put("adj", comparedWord.getWord());
        params.put("posAdj", comparedWord.getPos());
        params.put("information2", comparedWord.getInformation());
        params.put("similarity", this.similarity);

        try (Transaction tx = this.dbService.beginTx()) {
            this.dbService.execute("MATCH (n) where n.word = {seedWord} and n.pos = {posSeedWord} and n.information = {information1}"
                    + "WITH n MATCH(m) WHERE m.word = {adj} and m.pos = {posAdj} and m.information = {information2}"
                    + "CREATE (n)-[r:sim]->(m) SET r.similarity = {similarity}", params);
            tx.success();
        }
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public void setComparedWord(Word comparedWord) {
        this.comparedWord = comparedWord;
    }

    public void checkSimilarity() {

        Map<String, Object> params = new HashMap();
        params.put("adj1", this.word.getWord());
        params.put("adj2", this.comparedWord.getWord());
        try (Transaction tx = this.dbService.beginTx()) {
            Result results = this.dbService.execute("MATCH (n)-[r:sim]-(m) WHERE n.word = {adj1} AND m.word = {adj2} RETURN r.similarity as similarity ORDER BY similarity DESC LIMIT 1", params);

            if (results.hasNext()) {
                this.similarity = Double.parseDouble(results.next().get("similarity").toString());
            } else {
                computeSimilarity();
                if (this.similarity > 0) {
                    //saveSimilarityInDb();
                }
            }
            tx.success();
        }

    }

    @Override
    public void run() {

        //// Check if the similarity is stored in the db. If not, compute it
        Map<String, Object> params = new HashMap();
        params.put("adj1", this.word.getWord());
        params.put("adj2", this.comparedWord.getWord());
        try (Transaction tx = this.dbService.beginTx()) {
            Result results = this.dbService.execute("MATCH (n)-[r:sim]-(m) WHERE n.word = {adj1} AND m.word = {adj2} RETURN r.similarity as similarity ORDER BY similarity DESC LIMIT 1", params);

            if (results.hasNext()) {
                this.similarity = Double.parseDouble(results.next().get("similarity").toString());
            } else {
                computeSimilarity();
                if (this.similarity > 0) {
                    //saveSimilarityInDb();
                }
            }
            tx.success();
        }

        if (this.similarity > 0) {
            System.out.println("sim(" + this.word.getWord() + "[" + this.word.getPos() + "], " + this.comparedWord.getWord() + "[" + this.comparedWord.getPos() + "]) = " + this.similarity);
        }
        ////
        //if word and comparedWord != null
        /* computeSimilarity();
        if (this.similarity > 0) {
            saveSimilarityInDb();
            System.out.println("Inserted: (" + word.getWord() + ")->[sim: " + similarity + "]->(" + comparedWord.getWord() + ")");
        }*/
    }
}
