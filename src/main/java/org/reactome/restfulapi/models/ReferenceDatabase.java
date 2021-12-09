/*
 * Created on Jun 25, 2012
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
public class ReferenceDatabase extends DatabaseObject {
    private String accessUrl;
    private List<String> name;
    private String url;
    private String resourceIdentifier;
    
    public ReferenceDatabase() {
        
    }

    public String getResourceIdentifier() {
        return resourceIdentifier;
    }

    public void setResourceIdentifier(String resourceIdentifier) {
        this.resourceIdentifier = resourceIdentifier;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    public void setAccessUrl(String accessurl) {
        this.accessUrl = accessurl;
    }

    public List<String> getName() {
        return name;
    }

    public void setName(List<String> name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
}
