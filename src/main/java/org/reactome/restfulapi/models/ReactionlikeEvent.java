package org.reactome.restfulapi.models;

// Generated Jul 8, 2011 1:48:55 PM by Hibernate Tools 3.4.0.CR1

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * ReactionlikeEvent generated by hbm2java
 */
@XmlRootElement
public class ReactionlikeEvent extends Event {

    private Boolean isChimeric;
    private List<PhysicalEntity> input;
    private List<PhysicalEntity> output;
    private List<PhysicalEntity> entityOnOtherCell;
    private List<DatabaseObject> requiredInputComponent;
    private ReactionlikeEvent hasMember;
    private List<CatalystActivity> catalystActivity;
    private List<ReactionlikeEvent> normalReaction;
    private List<EntityFunctionalStatus> entityFunctionalStatus;
    private String systematicName;
    private List<Regulation> regulatedBy;
    private List<RegulationReference> regulationReference;
    private List<InteractionEvent> hasInteraction;
    private List<ReactionType> reactionType;

    public ReactionlikeEvent() {
    }
    
    public List<InteractionEvent> getHasInteraction() {
        return hasInteraction;
    }

    public void setHasInteraction(List<InteractionEvent> hasInteraction) {
        this.hasInteraction = hasInteraction;
    }

    public List<ReactionType> getReactionType() {
        return reactionType;
    }

    public void setReactionType(List<ReactionType> reactionType) {
        this.reactionType = reactionType;
    }

    public List<RegulationReference> getRegulationReference() {
        return regulationReference;
    }

    public void setRegulationReference(List<RegulationReference> regulationReference) {
        this.regulationReference = regulationReference;
    }

    public CatalystActivityReference getCatalystActivityReference() {
        return catalystActivityReference;
    }

    public void setCatalystActivityReference(CatalystActivityReference catalystActivityReference) {
        this.catalystActivityReference = catalystActivityReference;
    }

    private CatalystActivityReference catalystActivityReference;
    
    public List<Regulation> getRegulatedBy() {
        return regulatedBy;
    }

    public void setRegulatedBy(List<Regulation> regulatedBy) {
        this.regulatedBy = regulatedBy;
    }

    public String getSystematicName() {
        return systematicName;
    }

    public void setSystematicName(String systematicName) {
        this.systematicName = systematicName;
    }

    public List<PhysicalEntity> getInput() {
        return input;
    }

    public void setInput(List<PhysicalEntity> input) {
        this.input = input;
    }

    public List<PhysicalEntity> getOutput() {
        return output;
    }

    public void setOutput(List<PhysicalEntity> output) {
        this.output = output;
    }


    public List<PhysicalEntity> getEntityOnOtherCell() {
        return entityOnOtherCell;
    }

    public void setEntityOnOtherCell(List<PhysicalEntity> entityOnOtherCell) {
        this.entityOnOtherCell = entityOnOtherCell;
    }

    public List<DatabaseObject> getRequiredInputComponent() {
        return requiredInputComponent;
    }

    public void setRequiredInputComponent(List<DatabaseObject> requiredInputComponent) {
        this.requiredInputComponent = requiredInputComponent;
    }

    public ReactionlikeEvent getHasMember() {
        return hasMember;
    }

    public void setHasMember(ReactionlikeEvent hasMember) {
        this.hasMember = hasMember;
    }

    public List<CatalystActivity> getCatalystActivity() {
        return catalystActivity;
    }

    public void setCatalystActivity(List<CatalystActivity> catalystActivity) {
        this.catalystActivity = catalystActivity;
    }

    public Boolean getIsChimeric() {
        return this.isChimeric;
    }

    public void setIsChimeric(Boolean isChimeric) {
        this.isChimeric = isChimeric;
    }

	public List<ReactionlikeEvent> getNormalReaction() {
		return this.normalReaction;
	}

	public void setNormalReaction(List<ReactionlikeEvent> normalReaction) {
		this.normalReaction = normalReaction;
	}

    public List<EntityFunctionalStatus> getEntityFunctionalStatus() {
        return entityFunctionalStatus;
    }

    public void setEntityFunctionalStatus(List<EntityFunctionalStatus> entityFunctionalStatus) {
        this.entityFunctionalStatus = entityFunctionalStatus;
    }
}