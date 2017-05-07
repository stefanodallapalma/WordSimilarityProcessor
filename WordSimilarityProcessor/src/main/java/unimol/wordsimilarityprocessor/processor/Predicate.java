package unimol.wordsimilarityprocessor.processor;

import java.util.Objects;

/**
 *
 * @author Stefano Dalla Palma
 */
public class Predicate {

    private long id;
    private Word governor, dependent;
    private String relation;
    private int frequency;

    public Predicate() {

    }

    public Predicate(Word governor, String relation, Word dependent) {
        this.governor = governor;
        this.relation = relation;
        this.dependent = dependent;
    }

    public Predicate(Word governor, String relation, Word dependent, int frequency) {
        this.governor = governor;
        this.relation = relation;
        this.dependent = dependent;
        this.frequency = frequency;
    }

    public Predicate(long id, Word governor, String relation, Word dependent, int frequency) {
        this.id = id;
        this.governor = governor;
        this.relation = relation;
        this.dependent = dependent;
        this.frequency = frequency;
    }

    public long getId() {
        return id;
    }

    public Word getGovernor() {
        return governor;
    }

    public Word getDependent() {
        return dependent;
    }

    public String getRelation() {
        return relation;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void incrementFrequency() {
        this.frequency++;
    }

    @Override
    public String toString() {
        return "Predicate{" + "id=" + id + ", governor=" + governor + ", dependent=" + dependent + ", relation=" + relation + ", frequency=" + frequency + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.governor);
        hash = 47 * hash + Objects.hashCode(this.dependent);
        hash = 47 * hash + Objects.hashCode(this.relation);
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
        final Predicate other = (Predicate) obj;
        if (!Objects.equals(this.relation, other.relation)) {
            return false;
        }
        if (!Objects.equals(this.governor, other.governor)) {
            return false;
        }
        if (!Objects.equals(this.dependent, other.dependent)) {
            return false;
        }
        return true;
    }

   

}
