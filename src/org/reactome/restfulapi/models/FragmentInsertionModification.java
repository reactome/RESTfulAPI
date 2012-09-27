/*
 * Created on Jun 26, 2012
 *
 */
package org.reactome.restfulapi.models;

/**
 * @author gwu
 *
 */
public class FragmentInsertionModification extends FragmentModification {
    private Integer coordinate;
    
    public FragmentInsertionModification() {
        
    }

    public Integer getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Integer coordinate) {
        this.coordinate = coordinate;
    }
    
}
