/*
 * Created on Jun 7, 2013
 *
 */
package org.reactome.restfulapi.models;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author gwu
 *
 */
@XmlRootElement
public class EntityFunctionalStatus extends DatabaseObject {
    private List<FunctionalStatus> functionalStatus;
    private PhysicalEntity physicalEntity;
    private PhysicalEntity diseaseEntity;
    private PhysicalEntity normalEntity;
    
    public PhysicalEntity getDiseaseEntity() {
        return diseaseEntity;
    }

    public void setDiseaseEntity(PhysicalEntity diseaseEntity) {
        this.diseaseEntity = diseaseEntity;
    }

    public PhysicalEntity getNormalEntity() {
        return normalEntity;
    }

    public void setNormalEntity(PhysicalEntity normalEntity) {
        this.normalEntity = normalEntity;
    }

    public EntityFunctionalStatus() {
    }

    public List<FunctionalStatus> getFunctionalStatus() {
        return functionalStatus;
    }

    public void setFunctionalStatus(List<FunctionalStatus> functionalStatus) {
        this.functionalStatus = functionalStatus;
    }

    public PhysicalEntity getPhysicalEntity() {
        return physicalEntity;
    }

    public void setPhysicalEntity(PhysicalEntity physicalEntity) {
        this.physicalEntity = physicalEntity;
    }
    
    
    
}
