package org.reactome.restfulapi.models;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CellLineagePath extends Pathway {
//    private boolean hasDiagram;
//    private List<Event> hasEvent;
    private Anatomy tissue;
    
    public CellLineagePath() {
        
    }
    
//    public Boolean getHasDiagram() {
//        return this.hasDiagram;
//    }
//    
//    public void setHasDiagram(Boolean hasDiagram) {
//        this.hasDiagram = hasDiagram;
//    }
    
//    public List<Event> getHasEvent() {
//        return hasEvent;
//    }
//
//    public void setHasEvent(List<Event> hasEvent) {
//        this.hasEvent = hasEvent;
//    }
    
    public Anatomy getTissue() {
        return tissue;
    }

    public void setTissue(Anatomy tissue) {
        this.tissue = tissue;
    }
}
