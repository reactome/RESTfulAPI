package org.reactome.restfulapi.models;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public abstract class ControlReference extends DatabaseObject {
    
    private List<LiteratureReference> literatureReference;
    
    public ControlReference() {
        
    }

    public List<LiteratureReference> getLiteratureReference() {
        return literatureReference;
    }

    public void setLiteratureReference(List<LiteratureReference> literatureReference) {
        this.literatureReference = literatureReference;
    }

}
