package org.reactome.restfulapi.models;

// Generated Jul 8, 2011 1:48:55 PM by Hibernate Tools 3.4.0.CR1

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Event generated by hbm2java
 */
@XmlRootElement
public class Event extends DatabaseObject implements Regulator {

    private Boolean _doRelease;
    private List<InstanceEdit> authored;
    private List<InstanceEdit> edited;
    private List<InstanceEdit> revised;
    private List<InstanceEdit> reviewed;
    private List<Species> species;
    private String speciesName;
    private List<Species> relatedSpecies;
    private String definition;
    private EvidenceType evidenceType;
    private GO_BiologicalProcess goBiologicalProcess;
    private String releaseDate;
    private String keywords;
    private List<Summation> summation;
    private String releaseStatus;
    private List<Figure> figure;
    private List<Event> precedingEvent;
    private List<Event> followingEvent;
    private List<Publication> literatureReference;
    // Regulation related attributes
    private List<DatabaseObject> negativeRegulators;
    // Regulators in PositiveRegulations but not Requirements.
    // Note: Requirement is a subclass to PositiveRegulation.
    private List<DatabaseObject> positiveRegulators;
    private List<DatabaseObject> requirements;
    private List<DatabaseIdentifier> crossReference;
    private List<Disease> disease;
    // A simple label to indicate if this Event object is a disease
    private Boolean isInDisease;
    private List<Event> inferredFrom;
    // A simple flag to indicate if this Event is inferred from another
    private Boolean isInferred;
    private List<String> name;
    private List<Event> orthologousEvent;
    private List<Compartment> compartment;
    // Expose regulations directly so that more information can be displayed
    private List<PositiveRegulation> positiveRegulations;
    private List<NegativeRegulation> negativeRegulations;
    // Negative preceding/following relationships
    private List<NegativePrecedingEvent> negativePrecedingEvent;
    // For ReviewStatus-based star system
    private ReviewStatus reviewStatus;
    private ReviewStatus previousReviewStatus;
    private List<InstanceEdit> internalReviewed;
    private List<InstanceEdit> structureModified;
    
    public Event() {
    }

    public ReviewStatus getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(ReviewStatus reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public ReviewStatus getPreviousReviewStatus() {
        return previousReviewStatus;
    }

    public void setPreviousReviewStatus(ReviewStatus previousReviewStatus) {
        this.previousReviewStatus = previousReviewStatus;
    }

    public List<InstanceEdit> getInternalReviewed() {
        return internalReviewed;
    }

    public void setInternalReviewed(List<InstanceEdit> internalReviewed) {
        this.internalReviewed = internalReviewed;
    }

    public List<InstanceEdit> getStructureModified() {
        return structureModified;
    }

    public void setStructureModified(List<InstanceEdit> structureModified) {
        this.structureModified = structureModified;
    }

    public List<NegativePrecedingEvent> getNegativePrecedingEvent() {
        return negativePrecedingEvent;
    }

    public void setNegativePrecedingEvent(List<NegativePrecedingEvent> negativePrecedingEvent) {
        this.negativePrecedingEvent = negativePrecedingEvent;
    }

    public List<PositiveRegulation> getPositiveRegulations() {
        return positiveRegulations;
    }

    public void setPositiveRegulations(List<PositiveRegulation> positiveRegulations) {
        this.positiveRegulations = positiveRegulations;
    }

    public List<NegativeRegulation> getNegativeRegulations() {
        return negativeRegulations;
    }

    public void setNegativeRegulations(List<NegativeRegulation> negativeRegulations) {
        this.negativeRegulations = negativeRegulations;
    }

    public Boolean getIsInferred() {
        return isInferred;
    }

    public void setIsInferred(Boolean isInferred) {
        this.isInferred = isInferred;
    }

    public Boolean getIsInDisease() {
        return isInDisease;
    }

    public void setIsInDisease(Boolean isInDisease) {
        this.isInDisease = isInDisease;
    }

    public Boolean get_doRelease() {
        return _doRelease;
    }

    public void set_doRelease(Boolean _doRelease) {
        this._doRelease = _doRelease;
    }

    public List<InstanceEdit> getAuthored() {
        return authored;
    }

    public void setAuthored(List<InstanceEdit> authored) {
        this.authored = authored;
    }

    public List<InstanceEdit> getEdited() {
        return edited;
    }

    public void setEdited(List<InstanceEdit> edited) {
        this.edited = edited;
    }

    public List<InstanceEdit> getRevised() {
        return revised;
    }

    public void setRevised(List<InstanceEdit> revised) {
        this.revised = revised;
    }

    public List<InstanceEdit> getReviewed() {
        return reviewed;
    }

    public void setReviewed(List<InstanceEdit> reviewed) {
        this.reviewed = reviewed;
    }

    public List<Species> getSpecies() {
        return species;
    }

    public void setSpecies(List<Species> species) {
        this.species = species;
    }
    
    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String species) {
        this.speciesName = species;
    }
    
    public List<Species> getRelatedSpecies() {
		return relatedSpecies;
	}

	public void setRelatedSpecies(List<Species> relatedSpecies) {
		this.relatedSpecies = relatedSpecies;
	}

	public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public EvidenceType getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(EvidenceType evidenceType) {
        this.evidenceType = evidenceType;
    }

    public GO_BiologicalProcess getGoBiologicalProcess() {
        return goBiologicalProcess;
    }

    public void setGoBiologicalProcess(GO_BiologicalProcess goBiologicalProcess) {
        this.goBiologicalProcess = goBiologicalProcess;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public List<Summation> getSummation() {
        return summation;
    }

    public void setSummation(List<Summation> summation) {
        this.summation = summation;
    }

    public String getReleaseStatus() {
        return releaseStatus;
    }

    public void setReleaseStatus(String releaseStatus) {
        this.releaseStatus = releaseStatus;
    }

    public List<Figure> getFigure() {
        return figure;
    }

    public void setFigure(List<Figure> figure) {
        this.figure = figure;
    }

    public List<Event> getPrecedingEvent() {
        return precedingEvent;
    }

    public void setPrecedingEvent(List<Event> precedingEvent) {
        this.precedingEvent = precedingEvent;
    }

    public List<Event> getFollowingEvent() {
        return followingEvent;
    }

    public void setFollowingEvent(List<Event> followingEvent) {
        this.followingEvent = followingEvent;
    }

    public List<Publication> getLiteratureReference() {
        return literatureReference;
    }

    public void setLiteratureReference(List<Publication> literatureReference) {
        this.literatureReference = literatureReference;
    }

    public List<DatabaseObject> getNegativeRegulators() {
        return negativeRegulators;
    }

    public void setNegativeRegulators(List<DatabaseObject> negativeRegulators) {
        this.negativeRegulators = negativeRegulators;
    }

    public List<DatabaseObject> getPositiveRegulators() {
        return positiveRegulators;
    }

    public void setPositiveRegulators(List<DatabaseObject> positiveRegulators) {
        this.positiveRegulators = positiveRegulators;
    }

    public List<DatabaseObject> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<DatabaseObject> requirements) {
        this.requirements = requirements;
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

    public List<Event> getInferredFrom() {
        return inferredFrom;
    }

    public void setInferredFrom(List<Event> inferredFrom) {
        this.inferredFrom = inferredFrom;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public List<Event> getOrthologousEvent() {
        return orthologousEvent;
    }

    public void setOrthologousEvent(List<Event> orthologousEvent) {
        this.orthologousEvent = orthologousEvent;
    }

    public List<Compartment> getCompartment() {
        return compartment;
    }

    public void setCompartment(List<Compartment> compartment) {
        this.compartment = compartment;
    }

}
