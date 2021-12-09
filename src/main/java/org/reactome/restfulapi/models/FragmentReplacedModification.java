/*
 * Created on Jun 20, 2014
 *
 */
package org.reactome.restfulapi.models;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author gwu
 *
 */
@XmlRootElement
public class FragmentReplacedModification extends FragmentModification {
    private String alteredAminoAcidFragment;
    
    public FragmentReplacedModification() {
    }

    public String getAlteredAminoAcidFragment() {
        return alteredAminoAcidFragment;
    }

    public void setAlteredAminoAcidFragment(String alteredAminoAcidFragment) {
        this.alteredAminoAcidFragment = alteredAminoAcidFragment;
    }
    
    
    
}
