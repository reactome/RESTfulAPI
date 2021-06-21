package org.reactome.restfulapi.models;

// Generated Jul 8, 2011 1:48:55 PM by Hibernate Tools 3.4.0.CR1

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * PhysicalEntity generated by hbm2java
 */
@XmlRootElement
public class PhysicalEntity extends DatabaseObject implements Regulator {

    private InstanceEdit authored;
    private String definition;
    private GO_CellularComponent goCellularComponent;
    private String shortName;
    private List<PhysicalEntity> inferredFrom;
    private List<PhysicalEntity> inferredTo;
    private List<Figure> figure;
    private List<Summation> summation;
    private List<InstanceEdit> edited;
    private List<InstanceEdit> reviewed;
    private List<InstanceEdit> revised;
    private List<String> name;
    private List<EntityCompartment> compartment;
    private List<DatabaseIdentifier> crossReference;
    private List<Disease> disease;
    private List<Publication> literatureReference;
    // The following properties are used for detailed view
    private List<Event> catalyzedEvent; // List of Events catalysed by this PE
    private List<GO_MolecularFunction> goActivity; // List of GO MF related to this PE via CatalystActivity
    private List<Event> inhibitedEvent;
    private List<Event> activatedEvent;
    private List<Event> requiredEvent;
    private List<Event> producedByEvent;
    private List<Event> consumedByEvent;
    private String systematicName;
    private List<CellType> cellType;
//    private List<Domain> hasDomain;
    
    public PhysicalEntity() {
        
    }

    public List<CellType> getCellType() {
        return cellType;
    }

    public void setCellType(List<CellType> cellType) {
        this.cellType = cellType;
    }

    public String getSystematicName() {
        return systematicName;
    }

    public void setSystematicName(String systematicName) {
        this.systematicName = systematicName;
    }

    public InstanceEdit getAuthored() {
        return authored;
    }

    public void setAuthored(InstanceEdit authored) {
        this.authored = authored;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public GO_CellularComponent getGoCellularComponent() {
        return goCellularComponent;
    }

    public void setGoCellularComponent(GO_CellularComponent goCellularComponent) {
        this.goCellularComponent = goCellularComponent;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<PhysicalEntity> getInferredFrom() {
        return inferredFrom;
    }

    public void setInferredFrom(List<PhysicalEntity> inferredFrom) {
        this.inferredFrom = inferredFrom;
    }

    /**
     * Use this @XmlElements so that species can be marshaled for the detailed view.
     * Otherwise, if EntitySet and EWAS are both listed in the list for an EWAS, 
     * EntitySet's species cannot be marshaled.
     * @return
     */
    @XmlElements(
                 {
                     @XmlElement(type = Complex.class),
                     @XmlElement(type = EntitySet.class),
                     @XmlElement(type = GenomeEncodedEntity.class),
                     @XmlElement(type = Polymer.class),
                     @XmlElement(type = SimpleEntity.class)
                 }
            )
    public List<PhysicalEntity> getInferredTo() {
        return inferredTo;
    }

    public void setInferredTo(List<PhysicalEntity> inferredTo) {
        this.inferredTo = inferredTo;
    }

    public List<Figure> getFigure() {
        return figure;
    }

    public void setFigure(List<Figure> figure) {
        this.figure = figure;
    }

    public List<Summation> getSummation() {
        return summation;
    }

    public void setSummation(List<Summation> summation) {
        this.summation = summation;
    }

    public List<InstanceEdit> getEdited() {
        return edited;
    }

    public void setEdited(List<InstanceEdit> edited) {
        this.edited = edited;
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

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public List<EntityCompartment> getCompartment() {
        return compartment;
    }

    public void setCompartment(List<EntityCompartment> compartment) {
        this.compartment = compartment;
    }

    public List<DatabaseIdentifier> getCrossReference() {
        return crossReference;
    }

    public void setCrossReference(List<DatabaseIdentifier> crossReference) {
        this.crossReference = crossReference;
    }

    public List<Disease> getDisease() {
        return disease;
    }

    public void setDisease(List<Disease> disease) {
        this.disease = disease;
    }

    public List<Publication> getLiteratureReference() {
        return literatureReference;
    }

    public void setLiteratureReference(List<Publication> literatureReference) {
        this.literatureReference = literatureReference;
    }
    
    public void addCrossReference(DatabaseIdentifier dbi) {
        if (crossReference == null)
            crossReference = new ArrayList<DatabaseIdentifier>();
        // Avoid duplication
        for (DatabaseIdentifier tmp : crossReference) {
            if (tmp.getDbId().equals(dbi.getDbId()))
                return;
        }
        crossReference.add(dbi);
    }

    public List<Event> getCatalyzedEvent() {
        return catalyzedEvent;
    }

    public void setCatalyzedEvent(List<Event> catalyzedEvent) {
        this.catalyzedEvent = catalyzedEvent;
    }

    public List<GO_MolecularFunction> getGoActivity() {
        return goActivity;
    }

    public void setGoActivity(List<GO_MolecularFunction> goActivity) {
        this.goActivity = goActivity;
    }

    public List<Event> getInhibitedEvent() {
        return inhibitedEvent;
    }

    public void setInhibitedEvent(List<Event> inhibitedEvent) {
        this.inhibitedEvent = inhibitedEvent;
    }

    public List<Event> getActivatedEvent() {
        return activatedEvent;
    }

    public void setActivatedEvent(List<Event> activatedEvent) {
        this.activatedEvent = activatedEvent;
    }

    public List<Event> getRequiredEvent() {
        return requiredEvent;
    }

    public void setRequiredEvent(List<Event> requiredEvent) {
        this.requiredEvent = requiredEvent;
    }

    public List<Event> getProducedByEvent() {
        return producedByEvent;
    }

    public void setProducedByEvent(List<Event> producedByEvent) {
        this.producedByEvent = producedByEvent;
    }

    public List<Event> getConsumedByEvent() {
        return consumedByEvent;
    }

    public void setConsumedByEvent(List<Event> consumedByEvent) {
        this.consumedByEvent = consumedByEvent;
    }

//	public List<Domain> getHasDomain() {
//		return hasDomain;
//	}
//
//	public void setHasDomain(List<Domain> hasDomain) {
//		this.hasDomain = hasDomain;
//	}
    
}
