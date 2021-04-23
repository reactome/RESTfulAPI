package org.reactome.restfulapi.models;

import java.util.List;

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
