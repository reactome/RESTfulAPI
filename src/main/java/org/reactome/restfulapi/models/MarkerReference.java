package org.reactome.restfulapi.models;

import java.util.List;

@SuppressWarnings("serial")
public class MarkerReference extends ControlReference {
    
    private List<EntityWithAccessionedSequence> marker;
    
    public MarkerReference() {
    }

    public List<EntityWithAccessionedSequence> getMarker() {
        return marker;
    }

    public void setMarker(List<EntityWithAccessionedSequence> marker) {
        this.marker = marker;
    }

}
