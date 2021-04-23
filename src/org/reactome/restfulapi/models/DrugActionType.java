package org.reactome.restfulapi.models;

import java.util.List;

public class DrugActionType extends ReactionType {
    
    private List<DrugActionType> instanceOf;
    
    public DrugActionType() {
    }

    public List<DrugActionType> getInstanceOf() {
        return instanceOf;
    }

    public void setInstanceOf(List<DrugActionType> instanceOf) {
        this.instanceOf = instanceOf;
    }

}
