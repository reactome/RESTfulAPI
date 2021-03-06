package org.reactome.restfulapi.models;

import javax.xml.bind.annotation.XmlRootElement;

// Generated Jul 8, 2011 1:48:55 PM by Hibernate Tools 3.4.0.CR1

/**
 * PathwayDiagram generated by hbm2java
 */
@XmlRootElement
public class PathwayDiagram extends DatabaseObject {

    private Integer height;
    private Pathway representedPathway;
    private String storedATXML;
    private Integer width;

    public PathwayDiagram() {
    }

    public Integer getHeight() {
        return this.height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Pathway getRepresentedPathway() {
        return this.representedPathway;
    }

    public void setRepresentedPathway(Pathway representedPathway) {
        this.representedPathway = representedPathway;
    }

    public String getStoredATXML() {
        return storedATXML;
    }

    public void setStoredATXML(String storedATXML) {
        this.storedATXML = storedATXML;
    }

    public Integer getWidth() {
        return this.width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

}
