package org.reactome.restfulapi.models;

public class CellDevelopmentStep extends ReactionlikeEvent {
    
    private Anatomy tissue;
    
    public CellDevelopmentStep() {
        
    }

    public Anatomy getTissue() {
        return tissue;
    }

    public void setTissue(Anatomy tissue) {
        this.tissue = tissue;
    }

}
