package org.reactome.restfulapi.models;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@SuppressWarnings("serial")
public class InteractionEvent extends Event {
    
    private List<PhysicalEntity> partners;
    
    public InteractionEvent() {
        
    }

    public List<PhysicalEntity> getPartners() {
        return partners;
    }

    public void setPartners(List<PhysicalEntity> partners) {
        this.partners = partners;
    }

}
