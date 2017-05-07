package unimol.wordsimilarityprocessor.information;

import java.util.Objects;
import org.neo4j.graphdb.Node;
import unimol.wordsimilarityprocessor.processor.Word;

/**
 *
 * @author Stefano Dalla Palma
 */
public class RelationDependentPair {
    
    private Relation relation;
    private Word dependent;

    public RelationDependentPair(Relation relation, Word dependency) {
        this.relation = relation;
        this.dependent = dependency;
    }

    public Relation getRelation() {
        return relation;
    }

    public Word getDependent() {
        return dependent;
    }
    
    @Override
    public String toString() {
        return "RelationDependentPair{" + "relation=" + relation + ", dependent=" + dependent + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.relation);
        hash = 11 * hash + Objects.hashCode(this.dependent);
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
        final RelationDependentPair other = (RelationDependentPair) obj;
        if (!Objects.equals(this.relation.getName().toLowerCase(), other.relation.getName().toLowerCase())) {
            return false;
        }
        if (!Objects.equals(this.dependent.getWord().toLowerCase(), other.dependent.getWord().toLowerCase())) {
            return false;
        }
        return true;
    }

     
    
}
