package unimol.wordsimilarityprocessor.processor;

import edu.stanford.nlp.trees.Tree;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import unimol.wordsimilarityprocessor.exception.ParsingException;

/**
 *
 * @author Stefano Dalla Palma
 */
public class SentenceProcessorThread implements Runnable {

    private final String sentence;
    private final TextProcessorUtils tp;
    private ArrayList<Word> words;
    private Tree parse;
    private Stemmer stemmer;

    public SentenceProcessorThread(String sentence, TextProcessorUtils processor) {
        this.sentence = sentence;
        this.tp = processor;
        this.words = new ArrayList();
        this.stemmer = new Stemmer();
    }

    @Override
    public void run() {

        for (String s : this.tp.getTaggedWordList(this.sentence)) {
            String[] parts = s.split("/");
            String word = parts[0].toLowerCase();
            String pos = parts[1].toLowerCase();
            word = this.stemmer.stem(word);
            words.add(new Word(word, pos));
        }

        this.stemmer = null;

        try {
            // Tokenize and create the costituents tree
            parse = this.tp
                    .apply(this.tp
                            .tokenize(this.sentence)); 

            Object[] basicDependencies = this.tp.getBasicDependencies(parse);   // Get the basic dependencies from the tree       
            parse = null;

            for (Object o : basicDependencies) {
                String[] predicate = o.toString().split("\\(", 2);
                String relation = predicate[0].replace(":", "_") + "_of";

                if (!relation.equals("dep_of")) {
                    String[] governor_dependent = predicate[1].split(", ", 2);

                    //Dependent
                    governor_dependent[0] = governor_dependent[0].replaceAll("[\\W\\w0-9]+-", "");

                    //Governor
                    governor_dependent[1] = (governor_dependent[1].replaceAll("[\\W\\w0-9]+-", "").replaceAll("\\)", ""));

                    int dependent = Integer.parseInt(governor_dependent[0]);
                    int governor = Integer.parseInt(governor_dependent[1]);

                    if (dependent > 0) {
                        DocumentProcessorThread.addPredicate(new Predicate(words.get(governor - 1), relation, words.get(dependent - 1)));
                    }
                }
            }
        } catch (ParsingException ex) {
            Logger.getLogger(SentenceProcessorThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            this.words = null;
        }
    }
}
