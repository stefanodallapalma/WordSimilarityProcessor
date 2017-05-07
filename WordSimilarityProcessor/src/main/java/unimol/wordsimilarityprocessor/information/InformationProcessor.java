package unimol.wordsimilarityprocessor.information;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import unimol.wordsimilarityprocessor.processor.Word;
import unimol.wordsimilarityprocessor.graph.GraphDbFactory;

/**
 *
 * @author Stefano Dalla Palma
 */
public class InformationProcessor {

    private final String LOAD_RELATION_DEPENDENT_PAIRS = "MATCH (h:Word)-[r]->(d:Word) WHERE h.word = {head} AND h.pos = {pos} RETURN r,d";
    //private final String LOAD_INFORMATION = "MATCH (w:Word) WHERE w.word = {word} AND w.pos= {pos}  RETURN w.information as information";
    private final String LOAD_INFORMATION = "MATCH (w:Word) WHERE w.word = {word} AND w.pos CONTAINS {pos}  RETURN w.information as information ORDER BY information DESC LIMIT 1";
    private final String GET_RELATION_FREQUENCY = "MATCH (h:Word)-[r:#relation#]->(d:Word) RETURN sum(r.frequency) as frequency";
    private final String GET_HEAD_RELATION_FREQUENCY = "MATCH (h:Word)-[r:#relation#]->(d:Word) WHERE h.word = {head} AND h.pos = {posHead} RETURN sum(r.frequency) as frequency";
    private final String GET_RELATION_DEPENDENT_FREQUENCY = "MATCH (h:Word)-[r:#relation#]->(d:Word) WHERE d.word = {dependent} AND d.pos = {posDependent} RETURN sum(r.frequency) as frequency";

    private final Word word;
    private double information;
    private ArrayList<RelationDependentPair> relationDependencyPairs;
    private HashMap<RelationDependentPair, Double> PairsSuchThatInformationGreaterThanZero;

    private GraphDatabaseService dbService;

    public InformationProcessor(Word word) {
        this.word = word;
        this.information = 0.0;
        this.relationDependencyPairs = new ArrayList();
        this.PairsSuchThatInformationGreaterThanZero = new HashMap();
    }

    public void computeInformation() {
        int R = 0;       // Relation frequency
        int HRD = 0;     // Head Relation Dependent frequency
        int HR = 0;      // Head Relation frequency
        int RD = 0;      // Relation Dependent frequency

        if (this.dbService == null) {
            dbService = GraphDbFactory.getInstance();
        }

        if (this.relationDependencyPairs.isEmpty()) {
            loadRelationDependecyPairs();
        } else {
            Map<String, Object> params = new HashMap();
            params.put("head", this.word.getWord());

            HashMap<Relation, Integer> rfMap = new HashMap();
            HashMap<RelationDependentPair, Integer> hrfMap = new HashMap();
            HashMap<RelationDependentPair, Integer> rdfMap = new HashMap();

            for (RelationDependentPair rd : this.relationDependencyPairs) {
                params.put("dependent", rd.getDependent().getWord());
                params.put("posHead", this.word.getPos());
                params.put("posDependent", rd.getDependent().getPos());

                HRD = (int) rd.getRelation().getFrequency();

                try (Transaction tx = dbService.beginTx()) {

                    if (rfMap.containsKey(rd.getRelation())) {
                        R = rfMap.get(rd.getRelation());
                    } else {
                        String select = GET_RELATION_FREQUENCY;
                        select = select.replaceAll("#relation#", rd.getRelation().getName());

                        // relationFrequencyResult                            
                        Result rfResult = dbService.execute(select, params);
                        if (rfResult.hasNext()) {
                            R = Integer.parseInt(rfResult.next().get("frequency").toString());
                        }
                        rfMap.put(rd.getRelation(), R);
                    }

                    if (hrfMap.containsKey(rd)) {
                        HR = hrfMap.get(rd);
                    } else {
                        String select = GET_HEAD_RELATION_FREQUENCY;
                        select = select.replaceAll("#relation#", rd.getRelation().getName());
                        // HeadRelationFrequencyResult
                        Result hrfResult = dbService.execute(select, params);
                        if (hrfResult.hasNext()) {
                            HR = Integer.parseInt(hrfResult.next().get("frequency").toString());
                        }
                        hrfMap.put(new RelationDependentPair(rd.getRelation(), this.word), HR);
                    }

                    if (rdfMap.containsKey(rd)) {
                        RD = rdfMap.get(rd);
                    } else {
                        String select = GET_RELATION_DEPENDENT_FREQUENCY;
                        select = select.replaceAll("#relation#", rd.getRelation().getName());

                        //relationDependentFrequencyResult
                        Result rdfResult = dbService.execute(select, params);
                        if (rdfResult.hasNext()) {
                            RD = Integer.parseInt(rdfResult.next().get("frequency").toString());
                        }
                        rdfMap.put(rd, RD);
                    }

                    tx.success();
                }
                double numerator = R * HRD;
                double denominator = HR * RD;

                if (denominator > 0 && (numerator / denominator) > 1) {
                    double info = Math.log(numerator / denominator);
                    this.information += info;
                    this.PairsSuchThatInformationGreaterThanZero.put(rd, info);
                }
            }

            //dbService.shutdown();
        }
    }

    public double loadInformation() {

        Map<String, Object> params = new HashMap();
        params.put("word", this.word.getWord());
        params.put("pos", this.word.getPos());

        GraphDatabaseService graphDb = null;

        if (this.dbService == null) {
            graphDb = GraphDbFactory.getInstance();
        } else {
            graphDb = this.dbService;
        }
        
        try (Transaction tx = graphDb.beginTx()) {
            Result result = graphDb.execute(LOAD_INFORMATION, params);
            if (result.hasNext()) {
                this.information = Double.parseDouble(result.next().get("information").toString());
            }
            tx.success();
        }

        if (this.dbService == null && graphDb != null) {
            graphDb.shutdown();
        }

        return this.information;
    }

    private void loadRelationDependecyPairs() {

        Map<String, Object> params = new HashMap();
        params.put("head", this.word.getWord());
        params.put("pos", this.word.getPos());

        if (dbService == null) {
            dbService = GraphDbFactory.getInstance();
        }
        try (Transaction tx = dbService.beginTx()) {
            Result result = dbService.execute(LOAD_RELATION_DEPENDENT_PAIRS, params);

            Map<String, Object> results;

            while (result.hasNext()) {
                results = result.next();
                Relationship relation = (Relationship) results.get("r");
                Node dependent = (Node) results.get("d");

                Object frequency = relation.getProperty("frequency");
                int f = Integer.parseInt(frequency.toString());

                this.relationDependencyPairs.add(new RelationDependentPair(
                        new Relation(relation.getType().name(), f),
                        new Word((String) dependent.getProperty("word"), (String) dependent.getProperty("pos"))));
            }

            tx.success();
        }
        //dbService.shutdown();
    }

    public void setRelationDependencyPairs(ArrayList<RelationDependentPair> relationDependencyPairs) {
        this.relationDependencyPairs = relationDependencyPairs;
    }

    public double getInformation() {
        return this.information;
    }

    public HashMap<RelationDependentPair, Double> getPairsSuchThatInformationGreaterThanZero() {
        return PairsSuchThatInformationGreaterThanZero;
    }

    public void setDbService(GraphDatabaseService dbService) {
        this.dbService = dbService;
    }

}
