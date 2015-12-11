package org.reactome.restfulapi;


public class InstanceNotFoundException extends ReactomeRemoteException {

    private String clsName;
    private String identifier;
    private String propertyValue;

    public InstanceNotFoundException() {

    }

    public InstanceNotFoundException(Long identifier) {
        this.identifier = identifier + "";
    }

    public InstanceNotFoundException(String identifier) {
        this.identifier = identifier;
    }

    public InstanceNotFoundException(String clsName, Long identifier) {
        this.clsName = clsName;
        this.identifier = identifier + "";
    }

    public InstanceNotFoundException(String clsName, String value) {
        this.clsName = clsName;
        this.propertyValue = value;
    }

    public String getClsName() {
        return clsName;
    }

    public void setClsName(String clsName) {
        this.clsName = clsName;
    }

    public String getDbId() {
        return identifier;
    }

    public void setDbId(long dbId) {
        this.identifier = dbId + "";
    }

    public String toString() {
        if (clsName != null && identifier != null)
            return super.toString() + ": " + clsName + ": " + identifier;
        if (identifier != null)
            return super.toString() + ": " + identifier;
        return super.toString();
    }

}