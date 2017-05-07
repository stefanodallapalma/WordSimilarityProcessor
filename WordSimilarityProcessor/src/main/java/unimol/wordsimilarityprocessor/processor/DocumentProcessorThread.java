package unimol.wordsimilarityprocessor.processor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 *
 * @author Stefano Dalla Palma
 */
public class DocumentProcessorThread extends Thread {

    private final File file;
    private String document;
    private ExecutorService executorService;
    private ArrayList<String> sentences;
    private TextProcessorUtils tpu;
    private static List<Predicate> synchronizedPredicatesToUpdate = Collections.synchronizedList(new ArrayList<Predicate>());

    public DocumentProcessorThread(File file, TextProcessorUtils tpu) {
        this.file = file;
        this.document = "";
        this.tpu = tpu;
    }

    @Override
    public void run() {

        // READ DOCUMENT
        try (Scanner in = new Scanner(new FileReader(this.file.getPath()))) {
            while (in.hasNextLine()) {
                this.document += in.nextLine() + " ";
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DocumentProcessorThread.class.getName()).log(Level.SEVERE, null, ex);
        }

        // SPLIT DOCUMENT
        this.sentences = this.tpu.documentToSentences(this.document);
        this.document = null;

        // PROCESS DOCUMENT 
        this.executorService = Executors.newFixedThreadPool(2);
        for (String s : sentences) {
            this.executorService.execute(new SentenceProcessorThread(s, this.tpu));
            s = null;
        }
        this.executorService.shutdown();

        try {
            this.executorService.awaitTermination(120, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(DocumentProcessorThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            System.out.println("#Sentences: " + this.sentences.size());
            this.sentences = null;
        }

        while (!executorService.isTerminated()) {
        }
        executorService = null;

        update();
    }

    public static void addPredicate(Predicate p) {
        synchronized (synchronizedPredicatesToUpdate) {
            if (synchronizedPredicatesToUpdate.contains(p)) {
                p = synchronizedPredicatesToUpdate.get(synchronizedPredicatesToUpdate.indexOf(p));
                p.incrementFrequency();
            } else {
                p.setFrequency(1);
                synchronizedPredicatesToUpdate.add(p);
            }
        }
    }

    public void update() {
        if (!synchronizedPredicatesToUpdate.isEmpty()) {
            System.out.println("I'm updating the db...");

            // SOSTITUIRE CON LA CLASSE CHE RITORNA L'ISTANZA DELLA CONNESSIONE AL DATABASE
            File storeDir = new File("C:/Users/stefa/Documents/Neo4j/sim.db");
            GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
            GraphDatabaseService db = dbFactory.newEmbeddedDatabase(storeDir);

            ExecutorService updateService = Executors.newFixedThreadPool(2);

            for (Predicate p : synchronizedPredicatesToUpdate) {
                updateService.execute(new UpdateThread(db, p));
            }
            updateService.shutdown();
            try {
                updateService.awaitTermination(360, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Logger.getLogger(DocumentProcessorThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            while (!updateService.isTerminated()) {
            }

            db.shutdown();
            synchronizedPredicatesToUpdate.clear();
            System.out.println("Update succesful...");
        }
    }

    public void end() {
        sentences = null;
        executorService = null;
        if (this.isAlive()) {
            this.interrupt();
        }
    }

  /*  public void update() {
        if (!synchronizedPredicatesToUpdate.isEmpty()) {
            System.out.println("I'm updating the db...");

            String UPDATE_QUERY = "MERGE (w1:Word {word:{word1}, pos:{pos1}}) "
            + "MERGE (w2:Word {word:{word2}, pos:{pos2}}) "
            + "MERGE (w1)-[r:#relation#]->(w2) "
            + "ON CREATE SET w1.information = 0, r.frequency = {frequency}, w2.information = 0 "
            + "ON MATCH SET r.frequency = r.frequency + {frequency}";
    
            // SOSTITUIRE CON LA CLASSE CHE RITORNA L'ISTANZA DELLA CONNESSIONE AL DATABASE
            File storeDir = new File("C:/Users/stefa/Documents/Neo4j/sim.db");
            GraphDatabaseFactory dbFactory = new GraphDatabaseFactory();
            GraphDatabaseService db = dbFactory.newEmbeddedDatabase(storeDir);

            Map<String, Object> params = new HashMap();

            try (Transaction tx = db.beginTx()) {
                for (Predicate p : synchronizedPredicatesToUpdate) {
                    params.put("word1", p.getGovernor().getWord());
                    params.put("pos1", p.getGovernor().getPos());
                    params.put("word2", p.getDependent().getWord());
                    params.put("pos2", p.getDependent().getPos());
                    params.put("frequency", p.getFrequency());

                    String update = UPDATE_QUERY;
                    update = update.replaceAll("#relation#", p.getRelation());
                    db.execute(update, params);

                    params.clear();
                    synchronizedPredicatesToUpdate.remove(p);
                }
                tx.success();
            }

            db.shutdown();
            synchronizedPredicatesToUpdate.clear();
            System.out.println("Update succesful...");
        }
    }*/
}
