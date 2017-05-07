package unimol.wordsimilarityprocessor.graph;

import java.io.File;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 *
 * @author Stefano Dalla Palma
 */
public class GraphDbFactory {

    private static final File STORE_DIR = new File("C:/Users/User name/Documents/Neo4j/similarity.db");
    private static GraphDatabaseFactory dbFactory;
    private static GraphDatabaseService graphDb;

    private GraphDbFactory() {
    }

    public static synchronized GraphDatabaseService getInstance() {
        if (dbFactory == null) {
            dbFactory = new GraphDatabaseFactory();
            graphDb = dbFactory.newEmbeddedDatabase(STORE_DIR);
        } else {
            graphDb = dbFactory.newEmbeddedDatabase(STORE_DIR);
        }

        return graphDb;
    }

    public synchronized void shutdown() {
        if (graphDb != null) {
            graphDb.shutdown();
        }
    }
}
