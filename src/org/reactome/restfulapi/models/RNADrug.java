package org.reactome.restfulapi.models;

public class RNADrug extends Drug {
    private ReferenceRNASequence referenceEntity;
    
    public RNADrug() {
        
    }

    public ReferenceRNASequence getReferenceEntity() {
        return referenceEntity;
    }

    public void setReferenceEntity(ReferenceRNASequence referenceEntity) {
        this.referenceEntity = referenceEntity;
    }
    
}
