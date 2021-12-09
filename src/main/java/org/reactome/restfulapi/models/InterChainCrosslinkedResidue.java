/*
 * Created on Jun 26, 2012
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
public class InterChainCrosslinkedResidue extends CrosslinkedResidue {
    private List<InterChainCrosslinkedResidue> equivalentTo;
    private List<ReferenceSequence> secondReferenceSequence;
    
    public InterChainCrosslinkedResidue() {
        
    }

    public List<InterChainCrosslinkedResidue> getEquivalentTo() {
        return equivalentTo;
    }

    public void setEquivalentTo(List<InterChainCrosslinkedResidue> equivalentTo) {
        this.equivalentTo = equivalentTo;
    }

    public List<ReferenceSequence> getSecondReferenceSequence() {
        return secondReferenceSequence;
    }

    public void setSecondReferenceSequence(List<ReferenceSequence> secondReferenceSequence) {
        this.secondReferenceSequence = secondReferenceSequence;
    }
    
}
