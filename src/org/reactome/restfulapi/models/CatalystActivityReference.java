package org.reactome.restfulapi.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CatalystActivityReference extends ControlReference {
    
    private CatalystActivity catalystActivity;
    
    public CatalystActivityReference() {
        
    }

    public CatalystActivity getCatalystActivity() {
        return catalystActivity;
    }

    public void setCatalystActivity(CatalystActivity catalystActivity) {
        this.catalystActivity = catalystActivity;
    }

}
