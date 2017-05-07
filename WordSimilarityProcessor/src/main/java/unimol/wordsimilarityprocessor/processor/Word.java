package unimol.wordsimilarityprocessor.processor;

import java.util.Objects;

/**
 *
 * @author Stefano Dalla Palma
 */
public class Word {

    private String word;
    private String pos;    // part-of-speech
    private double information;

    public Word(String word, String pos) {
        this.word = word;
        this.pos = pos;
        this.information = 0.0;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public double getInformation() {
        return information;
    }

    public void setInformation(double information) {
        this.information = information;
    }

    @Override
    public String toString() {
        return "Word{" + "word=" + word + ", pos=" + pos + ", information=" + information + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.word);
        hash = 17 * hash + Objects.hashCode(this.pos);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Word other = (Word) obj;
        if (!Objects.equals(this.word, other.word)) {
            return false;
        }
        if (!Objects.equals(this.pos, other.pos)) {
            return false;
        }
        return true;
    }

}
