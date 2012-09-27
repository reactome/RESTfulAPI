/*
 * Created on Jun 26, 2012
 *
 */
package org.reactome.restfulapi.models;

/**
 * @author gwu
 *
 */
public class GroupModifiedResidue extends TranslationalModification {
    private DatabaseObject modification;
    
    public DatabaseObject getModification() {
        return modification;
    }

    public void setModification(DatabaseObject modification) {
        this.modification = modification;
    }

    public GroupModifiedResidue() {
        
    }
    
}
