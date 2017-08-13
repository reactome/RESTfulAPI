package org.reactome.restfulapi.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Drug extends PhysicalEntity {
    private ReferenceTherapeutic referenceTherapeutic;
    
    public Drug() {
        
    }

    public ReferenceTherapeutic getReferenceTherapeutic() {
        return referenceTherapeutic;
    }

    public void setReferenceTherapeutic(ReferenceTherapeutic referenceTherapeutic) {
        this.referenceTherapeutic = referenceTherapeutic;
    }
    
}
