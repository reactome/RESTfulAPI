/*
 * Created on Jun 26, 2012
 *
 */
package org.reactome.restfulapi.models;

/**
 * @author gwu
 *
 */
public class AbstractModifiedResidue extends DatabaseObject {
    
    private ReferenceSequence referenceSequence;
    
    public AbstractModifiedResidue() {
        
    }

    public ReferenceSequence getReferenceSequence() {
        return referenceSequence;
    }

    public void setReferenceSequence(ReferenceSequence referenceSequence) {
        this.referenceSequence = referenceSequence;
    }
    
}
