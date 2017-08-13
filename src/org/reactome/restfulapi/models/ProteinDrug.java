package org.reactome.restfulapi.models;

public class ProteinDrug extends Drug {
    private ReferenceGeneProduct referenceEntity;
    
    public ProteinDrug() {
        
    }

    public ReferenceGeneProduct getReferenceEntity() {
        return referenceEntity;
    }

    public void setReferenceEntity(ReferenceGeneProduct referenceEntity) {
        this.referenceEntity = referenceEntity;
    }
    
}
