/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimol.wordsimilarityprocessor.processor;

import java.util.HashMap;
import java.util.Map;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

/**
 *
 * @author stefa
 */
public class UpdateThread implements Runnable {

    private final GraphDatabaseService dbService;
    private Predicate predicate;
    private Map<String, Object> params;

    public UpdateThread(GraphDatabaseService databaseService, Predicate toUpdate) {
        this.dbService = databaseService;
        this.predicate = toUpdate;
        this.params = new HashMap();
    }

    @Override
    public void run() {

        String UPDATE_QUERY = "MERGE (w1:Word {word:{word1}, pos:{pos1}}) "
                + "MERGE (w2:Word {word:{word2}, pos:{pos2}}) "
                + "MERGE (w1)-[r:#relation#]->(w2) "
                + "ON CREATE SET w1.information = 0, r.frequency = {frequency}, w2.information = 0 "
                + "ON MATCH SET r.frequency = r.frequency + {frequency}";

        this.params.put("word1", this.predicate.getGovernor().getWord());
        this.params.put("pos1", this.predicate.getGovernor().getPos());
        this.params.put("word2", this.predicate.getDependent().getWord());
        this.params.put("pos2", this.predicate.getDependent().getPos());
        this.params.put("frequency", this.predicate.getFrequency());

        //String update = UPDATE_QUERY;
        //update = update.replaceAll("#relation#", this.predicate.getRelation());
        UPDATE_QUERY = UPDATE_QUERY.replaceAll("#relation#", this.predicate.getRelation());

        try (Transaction tx = dbService.beginTx()) {
            this.dbService.execute(UPDATE_QUERY, params);
            tx.success();
        }

        this.predicate = null;
        this.params = null;
    }

}
