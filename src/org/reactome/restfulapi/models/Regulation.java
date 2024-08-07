package org.reactome.restfulapi.models;

// Generated Jul 8, 2011 1:48:55 PM by Hibernate Tools 3.4.0.CR1

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Regulation generated by hbm2java
 */
@XmlRootElement
public class Regulation extends DatabaseObject {

    private InstanceEdit authored;
    private List<InstanceEdit> edited;
    private List<Figure> figure;
    private List<Publication> literatureReference;
    private List<String> name;
    private DatabaseObject regulatedEntity;
    private RegulationType regulationType;
    private DatabaseObject regulator;
    private String releaseDate;
    private List<InstanceEdit> reviewed;
    private List<InstanceEdit> revised;
    // New attribute in December, 2013
    private List<Pathway> containedInPathway;
    private GO_BiologicalProcess goBiologicalProcess;
    
    public Regulation() {
    }
    
    public GO_BiologicalProcess getGoBiologicalProcess() {
        return goBiologicalProcess;
    }

    public void setGoBiologicalProcess(GO_BiologicalProcess goBiologicalProcess) {
        this.goBiologicalProcess = goBiologicalProcess;
    }

    public List<Pathway> getContainedInPathway() {
        return containedInPathway;
    }

    public void setContainedInPathway(List<Pathway> containedInPathway) {
        this.containedInPathway = containedInPathway;
    }

    public InstanceEdit getAuthored() {
        return authored;
    }

    public void setAuthored(InstanceEdit authored) {
        this.authored = authored;
    }

    public List<InstanceEdit> getEdited() {
        return edited;
    }

    public void setEdited(List<InstanceEdit> edited) {
        this.edited = edited;
    }

    public List<Figure> getFigure() {
        return figure;
    }

    public void setFigure(List<Figure> figure) {
        this.figure = figure;
    }

    public List<Publication> getLiteratureReference() {
        return literatureReference;
    }

    public void setLiteratureReference(List<Publication> literatureReference) {
        this.literatureReference = literatureReference;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public DatabaseObject getRegulatedEntity() {
        return regulatedEntity;
    }

    public void setRegulatedEntity(DatabaseObject regulatedEntity) {
        this.regulatedEntity = regulatedEntity;
    }

    public RegulationType getRegulationType() {
        return regulationType;
    }

    public void setRegulationType(RegulationType regulationType) {
        this.regulationType = regulationType;
    }

    public DatabaseObject getRegulator() {
        return regulator;
    }

    public void setRegulator(DatabaseObject regulator) {
        this.regulator = regulator;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<InstanceEdit> getReviewed() {
        return reviewed;
    }

    public void setReviewed(List<InstanceEdit> reviewed) {
        this.reviewed = reviewed;
    }

    public List<InstanceEdit> getRevised() {
        return revised;
    }

    public void setRevised(List<InstanceEdit> revised) {
        this.revised = revised;
    }
}
