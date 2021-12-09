package org.reactome.restfulapi.models;

/**
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 */
public class Affiliation extends DatabaseObject {

    private String address;
    private String name;

    public Affiliation() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
