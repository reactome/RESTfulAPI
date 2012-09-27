/*
 * Created on Jun 26, 2012
 *
 */
package org.reactome.restfulapi.models;

/**
 * @author gwu
 *
 */
public class FragmentModification extends GeneticallyModifiedResidue {
    private Integer endPositionInReferenceSequence;
    private Integer startPositionInReferenceSequence;
    
    public FragmentModification() {
    }

    public Integer getEndPositionInReferenceSequence() {
        return endPositionInReferenceSequence;
    }

    public void setEndPositionInReferenceSequence(Integer endPositionInReferenceSequence) {
        this.endPositionInReferenceSequence = endPositionInReferenceSequence;
    }

    public Integer getStartPositionInReferenceSequence() {
        return startPositionInReferenceSequence;
    }

    public void setStartPositionInReferenceSequence(Integer startPositionInReferenceSequence) {
        this.startPositionInReferenceSequence = startPositionInReferenceSequence;
    }
    
    
    
}
