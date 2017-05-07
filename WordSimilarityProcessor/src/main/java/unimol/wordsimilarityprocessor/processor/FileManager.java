package unimol.wordsimilarityprocessor.processor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stefano Dalla Palma
 */
public class FileManager {
    private final String DB_DIR = "database";
    private final String CORPUS_DIR = "database/corpus";
    private final String READ_DIR = "database/corpus/read";
    private final String BNC_DIR = "database/corpus/BNC";      // the British National Corpus directory
    private final String XMLs_DIR = "database/corpus/BNC/XMLs";
    private final String TXTs_DIR = "database/corpus/BNC/TXTs";

    private File dbDir;
    private File corpusDir;
    private File readDir;
    private File bncDir;
    private File xmlsDir;
    private File txtsDir;

    public FileManager() throws IOException {
        initialize();
    }

    private void initialize() throws IOException {
        // Create the directories if don't exist
        this.dbDir = new File(DB_DIR);
        this.corpusDir = new File(CORPUS_DIR);
        this.readDir = new File(READ_DIR);
        this.bncDir = new File(BNC_DIR);
        this.xmlsDir = new File(XMLs_DIR);
        this.txtsDir = new File(TXTs_DIR);

        if (!dbDir.exists() && !dbDir.isFile()) {
            dbDir.mkdirs();
        }
        if (!corpusDir.exists() && !corpusDir.isFile()) {
            corpusDir.mkdirs();
        }
        if (!readDir.exists() && !readDir.isFile()) {
            readDir.mkdirs();
        }
        if (!bncDir.exists() && !bncDir.isFile()) {
            bncDir.mkdirs();
        }
        if (!xmlsDir.exists() && !xmlsDir.isFile()) {
            xmlsDir.mkdirs();
        }
        if (!txtsDir.exists() && !txtsDir.isFile()) {
            txtsDir.mkdirs();
        }
    }

    public File[] loadCorpus() {
        File folder = new File(CORPUS_DIR);
        return (folder.exists() && folder.isDirectory()) ? folder.listFiles() : null;
    }

    public File[] loadBritshNationalCorpus() {
        File folder = new File(TXTs_DIR);
        return (folder.exists() && folder.isDirectory()) ? folder.listFiles() : null;
    }

    public File[] loadBritshNationalXMLCorpus() {
        File folder = new File(XMLs_DIR);
        return (folder.exists() && folder.isDirectory()) ? folder.listFiles() : null;
    }

    public Queue<File> getFilesFromDirectory(File[] parentDirectory) {
        if (parentDirectory == null) {
            return null; //throw exception
        }

        Queue<File> queue = new LinkedList<File>();

        for (File node : parentDirectory) {
            if (node.isDirectory()) {
                Queue<File> tmpQueue = getFilesFromDirectory(node.listFiles());

                for (File f : tmpQueue) {
                    queue.add(f);
                }
            } else {
                queue.add(node);
            }
        }
        return queue;
    }

    public void moveInReadDirectory(File file) {
        File newlocation = new File(READ_DIR);
        if (!newlocation.exists()) {
            newlocation.mkdirs();
        }

        if (file.renameTo(new File(READ_DIR + "/" + file.getName()))) {
            System.out.println("File is moved successful!");
        } else {
            //Throw an exception
            System.out.println("File is failed to move!");
        }
    }

    public File getDbDirectory() {
        return (this.dbDir.exists() && this.dbDir.isDirectory()) ? this.dbDir : null;
    }

    public File getCorpusDirectory() {
        return (this.corpusDir.exists() && this.corpusDir.isDirectory()) ? this.corpusDir : null;
    }

    public File getReadDirectory() {
        return (this.readDir.exists() && this.readDir.isDirectory()) ? this.readDir : null;
    }

    public File getBNCDirectory() {
        return (this.bncDir.exists() && this.bncDir.isDirectory()) ? this.bncDir : null;
    }

    public File getXmlsDirectory() {
        return (this.xmlsDir.exists() && this.xmlsDir.isDirectory()) ? this.xmlsDir : null;
    }

    public File getTxtsDirectory() {
        return (this.txtsDir.exists() && this.txtsDir.isDirectory()) ? this.txtsDir : null;
    }

    public void createAdjectivesList(ArrayList<String> wordlist) {
        File file = new File("database/adjectives.txt");

        try {
            FileWriter writer = new FileWriter(file, true);
            PrintWriter printLine = new PrintWriter(writer);

            for (String adj : wordlist) {

                printLine.println(adj);

            }
            printLine.close();
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void createAdverbsList(ArrayList<String> wordlist) {
        File file = new File("database/adverbs.txt");

        try {
            FileWriter writer = new FileWriter(file);
            PrintWriter printLine = new PrintWriter(writer);

            for (String adv : wordlist) {
                printLine.println(adv);
            }
            printLine.close();
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
