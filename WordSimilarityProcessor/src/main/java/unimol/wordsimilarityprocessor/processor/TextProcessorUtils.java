/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package unimol.wordsimilarityprocessor.processor;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.RuntimeInterruptedException;
import java.io.BufferedReader;
import java.io.StringReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import unimol.wordsimilarityprocessor.exception.ParsingException;

/**
 *
 * @author Stefano Dalla Palma
 */
public class TextProcessorUtils {

    private final LexicalizedParser lp;
    private final MaxentTagger tagger;
    private final TreebankLanguagePack tlp;
    private final GrammaticalStructureFactory gsf;

    public TextProcessorUtils() {
        this.lp = LexicalizedParser.loadModel("edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
        this.tagger = new MaxentTagger("edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
        this.tlp = new PennTreebankLanguagePack();
        this.gsf = tlp.grammaticalStructureFactory();
    }

    public Tree apply(List<CoreLabel> list) throws ParsingException {
        try {
            return this.lp.apply(list);
        } catch (RuntimeInterruptedException ex) {
            throw new ParsingException("Error when parsing the document.");
        }
    }


    public ArrayList<String> documentToSentences(String source) {
        ArrayList<String> splittedSentences = new ArrayList();

        //Sentence splitter
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.ENGLISH);
        iterator.setText(source);

        int start = iterator.first();

        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            splittedSentences.add(source.substring(start, end));
        }

        return splittedSentences;
    }

    public List<CoreLabel> tokenize(String source) {
        TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        return tokenizerFactory.getTokenizer(new StringReader(source)).tokenize();
    }

    public ArrayList<String> getTaggedWordList(String sentence) {
        
        // POS Tagger
        List<List<HasWord>> tokenizedSentence = MaxentTagger.tokenizeText(new BufferedReader(new StringReader(sentence)));

        ArrayList<String> strings = new ArrayList();

        for (List<HasWord> s : tokenizedSentence) {
            List<TaggedWord> taggedSentence = tagger.tagSentence(s);

            for (Object o : taggedSentence.toArray()) {
                strings.add(o.toString());
            }
        }

        return strings;
    }

    public Object[] getBasicDependencies(Tree parse) {
        Collection<TypedDependency> tdl = null;

        if (this.tlp != null && this.gsf != null) {
            GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
            tdl = gs.typedDependencies();
        } else {
            // throw new LoadPennTreebankLanguagePackException("Load a PennTreebankLanguagePack before finding dependencies!");
            // throw new LoadGrammaticalStructureFactoryException("Load a GrammaticalStructureFactory before calling a new GrammaticalStructre!");
        }
        return tdl.toArray();
    }
}
