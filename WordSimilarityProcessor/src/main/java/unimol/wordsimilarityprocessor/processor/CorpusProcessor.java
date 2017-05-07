package unimol.wordsimilarityprocessor.processor;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stefano Dalla Palma
 */
public class CorpusProcessor {

    private Queue<File> corpus;
    private FileManager fm;
    private TextProcessorUtils tpu;

    public CorpusProcessor(Queue<File> corpus) {
        this.corpus = corpus;
        this.tpu = new TextProcessorUtils();
    }

    public void process() {
        try {
            this.fm = new FileManager();
        } catch (IOException ex) {
            Logger.getLogger(CorpusProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (File f : this.corpus) {
            System.out.println("\n---------------------------------------");
            System.out.println(f.getName().toUpperCase());
            System.out.println("---------------------------------------\n");

            DocumentProcessorThread documentProcessorThread = new DocumentProcessorThread(f, this.tpu);
            documentProcessorThread.start();
            try {
                documentProcessorThread.join(21600000);
            } catch (InterruptedException ex) {
                Logger.getLogger(CorpusProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (documentProcessorThread.isAlive()) {
                documentProcessorThread.end();
                System.out.println("Thread aborted");
            }

            fm.moveInReadDirectory(f);
            f = null;
        }
    }

}
