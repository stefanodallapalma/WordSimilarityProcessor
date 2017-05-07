package unimol.wordsimilarityprocessor.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import unimol.wordsimilarityprocessor.processor.Word;

public class DictionaryManager {

    private final String ADJ_DICTIONARY_DIR = "dictionary/adj_dictionary.txt";
    private final String ADV_DICTIONARY_DIR = "dictionary/adv_dictionary.txt";
    private final String UNRATED_ADJ_DIR = "dictionary/unrated_adj.txt";
    private final String UNRATED_ADV_DIR = "dictionary/unrated_adv.txt";
    private final String SENTI_WORDS = "dictionary/SentiWords_1.0.txt";
    private final String STOP_WORDS = "dictionary/stop-words.txt";

    public DictionaryManager() {

    }

    public HashMap<String, Integer> loadAdjectives() {

        HashMap<String, Integer> adjectives = new HashMap();
        File file = new File(ADJ_DICTIONARY_DIR);
        Scanner in = null;

        try {
            in = new Scanner(new FileReader(file));
            String line[];
            while (in.hasNextLine()) {
                line = in.nextLine().split("[\\s]+");
                adjectives.put(line[0], Integer.parseInt(line[1]));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return adjectives;
    }

    public ArrayList<Word> loadArrayAdjectives() {

        ArrayList<Word> adjectives = new ArrayList();
        File file = new File(ADJ_DICTIONARY_DIR);
        Scanner in = null;

        try {
            in = new Scanner(new FileReader(file));
            String line[];
            while (in.hasNextLine()) {
                line = in.nextLine().split("[\\s]+");
                adjectives.add(new Word(line[0], "jj"));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return adjectives;
    }

    public ArrayList<Word> loadUnratedAdjectives() {

        ArrayList<Word> adjectives = new ArrayList();

        File file = new File(UNRATED_ADJ_DIR);
        Scanner in = null;

        try {
            in = new Scanner(new FileReader(file));
            while (in.hasNextLine()) {
                String[] adj = in.nextLine().split(" ");
                adjectives.add(new Word(adj[0], adj[1]));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return adjectives;
    }

    public HashMap<String, Integer> loadAdverbs() {
        HashMap<String, Integer> adverbs = new HashMap();
        File file = new File(ADV_DICTIONARY_DIR);
        Scanner in = null;

        try {
            in = new Scanner(new FileReader(file));
            String line[];
            while (in.hasNextLine()) {
                line = in.nextLine().split("[\\s+]");
                adverbs.put(line[0], Integer.parseInt(line[1]));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return adverbs;
    }

    public ArrayList<Word> loadUnratedAdverbs() {

        ArrayList<Word> adverbs = new ArrayList();
        File file = new File(UNRATED_ADV_DIR);
        Scanner in = null;

        try {
            in = new Scanner(new FileReader(file));
            while (in.hasNextLine()) {
                String[] adv = in.nextLine().split(" ");
                adverbs.add(new Word(adv[0], adv[1]));
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DictionaryManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return adverbs;
    }

    public HashMap<String, Double> loadSentiWords(char pos) {
        HashMap<String, Double> sentiWords = new HashMap();
        File file = new File(SENTI_WORDS);
        Scanner in = null;

        if (pos == 'a' || pos == 'n' || pos == 'r' || pos == 'v') {
            try {
                in = new Scanner(new FileReader(file));
                String line[];
                while (in.hasNextLine()) {
                    line = in.nextLine().split("[\\s+]");       //line[0] = lemma#pos - line[1] = sentiment (double)
                    double sentiment = Double.parseDouble(line[1]);
                    String[] lemma_pos = line[0].split("#");

                    if (lemma_pos[1].charAt(0) == pos) {  //pos 
                        sentiWords.put(lemma_pos[0], sentiment);
                    }

                }
            } catch (FileNotFoundException ex) {
                // Ignore: return an empty map
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }

        return sentiWords;
    }

    public Set<String> loadStopWordsList() {
        Set<String> stopWordsList = new HashSet();
        File file = new File(STOP_WORDS);
        Scanner in = null;

        try {
            in = new Scanner(new FileReader(file));
           
            while (in.hasNextLine()) {
                String stopWord = in.nextLine();
                
                if(!stopWordsList.contains(stopWord)){
                    stopWordsList.add(stopWord);
                }

            }
        } catch (FileNotFoundException ex) {
            // Ignore: return an empty set
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return stopWordsList;
    }
}
