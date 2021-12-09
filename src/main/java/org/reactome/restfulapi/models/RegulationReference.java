package org.reactome.restfulapi.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RegulationReference extends ControlReference {
    
    private Regulation regulation;
    
    public RegulationReference() {
    }

    public Regulation getRegulation() {
        return regulation;
    }

    public void setRegulation(Regulation regulation) {
        this.regulation = regulation;
    }
    
}
