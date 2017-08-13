package org.reactome.restfulapi.models;

public class ChemicalDrug extends Drug {
    private ReferenceMolecule referenceEntity;
    
    public ChemicalDrug() {
        
    }

    public ReferenceMolecule getReferenceEntity() {
        return referenceEntity;
    }

    public void setReferenceEntity(ReferenceMolecule referenceEntity) {
        this.referenceEntity = referenceEntity;
    }

}
