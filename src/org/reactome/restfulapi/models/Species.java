package org.reactome.restfulapi.models;

// Generated Jul 8, 2011 1:48:55 PM by Hibernate Tools 3.4.0.CR1

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Species generated by hbm2java
 */
@XmlRootElement
public class Species extends DatabaseObject {

    private Integer speciesRank;
    private Integer species;
    private String speciesClass;

    public Integer getSpeciesRank() {
        return speciesRank;
    }

    public void setSpeciesRank(Integer speciesRank) {
        this.speciesRank = speciesRank;
    }

    public Integer getSpecies() {
        return species;
    }

    public void setSpecies(Integer species) {
        this.species = species;
    }

    public String getSpeciesClass() {
        return speciesClass;
    }

    public void setSpeciesClass(String speciesClass) {
        this.speciesClass = speciesClass;
    }


    public Species() {
    }

}
