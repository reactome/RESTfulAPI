package org.reactome.restfulapi.models;

import java.util.List;

public class DrugType extends DatabaseObject {
    private List<String> name;
    private String definition;
    
    public DrugType() {
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }
    
}
