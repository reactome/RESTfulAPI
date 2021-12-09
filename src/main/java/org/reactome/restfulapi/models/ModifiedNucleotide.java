package org.reactome.restfulapi.models;

public class ModifiedNucleotide extends TranscriptionalModification {
    
    private Integer coordinate;
    private DatabaseObject modification;
    
    public ModifiedNucleotide() {
    }

    public Integer getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Integer coordinate) {
        this.coordinate = coordinate;
    }

    public DatabaseObject getModification() {
        return modification;
    }

    public void setModification(DatabaseObject modification) {
        this.modification = modification;
    }

}
