package org.reactome.restfulapi.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Drug extends PhysicalEntity {
    private ReferenceTherapeutic referenceTherapeutic;
    // For backward compatibility. However, the actual type should be ReferenceTherapeutic.
    private ReferenceEntity referenceEntity;
    private DrugType drugType;
    
    public Drug() {
    }

    public DrugType getDrugType() {
        return drugType;
    }

    public void setDrugType(DrugType drugType) {
        this.drugType = drugType;
    }

    public ReferenceEntity getReferenceEntity() {
        return referenceEntity;
    }

    public void setReferenceEntity(ReferenceEntity referenceEntity) {
        this.referenceEntity = referenceEntity;
    }

    public ReferenceTherapeutic getReferenceTherapeutic() {
        return referenceTherapeutic;
    }

    public void setReferenceTherapeutic(ReferenceTherapeutic referenceTherapeutic) {
        this.referenceTherapeutic = referenceTherapeutic;
    }
    
}
