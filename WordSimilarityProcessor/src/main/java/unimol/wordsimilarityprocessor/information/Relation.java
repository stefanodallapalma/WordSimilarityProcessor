package unimol.wordsimilarityprocessor.information;

import java.util.Objects;

/**
 *
 * @author Stefano Dalla Palma
 */
public class Relation {
    private String name;
    private int frequency;

    public Relation(String name) {
        this.name = name;
    }
    public Relation(String name, int frequency) {
        this.name = name;
        this.frequency = frequency;
    }

    public String getName() {
        return name;
    }

    public double getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    @Override
    public String toString() {
        return "Relationship{" + "name=" + name + ", frequency=" + frequency + '}';
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.name);
        hash = (int) (41 * hash + this.frequency);
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
        final Relation other = (Relation) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
    
    
    
}
