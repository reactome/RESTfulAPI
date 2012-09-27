/*
 * Created on Jun 26, 2012
 *
 */
package org.reactome.restfulapi.models;

/**
 * @author gwu
 *
 */
public class TranslationalModification extends AbstractModifiedResidue {
    private Integer coordinate;
    private PsiMod psiMod;
    
    public TranslationalModification() {
        
    }

    public Integer getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Integer coordinate) {
        this.coordinate = coordinate;
    }

    public PsiMod getPsiMod() {
        return psiMod;
    }

    public void setPsiMod(PsiMod psiMod) {
        this.psiMod = psiMod;
    }
    
}
