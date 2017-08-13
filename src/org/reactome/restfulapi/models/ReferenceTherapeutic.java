package org.reactome.restfulapi.models;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ReferenceTherapeutic extends ReferenceEntity {
    private String abbreviation;
    private List<String> approvalSource;
    private Boolean approved;
    private String inn;
    private String type;
    
    public ReferenceTherapeutic() {
        
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public List<String> getApprovalSource() {
        return approvalSource;
    }

    public void setApprovalSource(List<String> approvalSource) {
        this.approvalSource = approvalSource;
    }

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
}
