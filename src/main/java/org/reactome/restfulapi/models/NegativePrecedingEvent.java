package org.reactome.restfulapi.models;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NegativePrecedingEvent extends DatabaseObject {
    private String comment;
    private List<Event> precedingEvent;
    private NegativePrecedingEventReason reason;

    public NegativePrecedingEvent() {
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Event> getPrecedingEvent() {
        return precedingEvent;
    }

    public void setPrecedingEvent(List<Event> precedingEvent) {
        this.precedingEvent = precedingEvent;
    }

    public NegativePrecedingEventReason getReason() {
        return reason;
    }

    public void setReason(NegativePrecedingEventReason reason) {
        this.reason = reason;
    }
    
}
