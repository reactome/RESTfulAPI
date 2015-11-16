/*
 * Created on Jun 5, 2012
 *
 */
package org.reactome.restfulapi.mapper;

import org.gk.model.GKInstance;
import org.gk.model.PersistenceAdaptor;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;
import org.reactome.restfulapi.ReactomeModelPostMapper;
import org.reactome.restfulapi.ReactomeToRESTfulAPIConverter;
import org.reactome.restfulapi.models.DatabaseObject;
import org.reactome.restfulapi.models.Event;
import org.reactome.restfulapi.models.Species;
import org.reactome.restfulapi.models.StableIdentifier;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This class is used to do some post-processing for Event.
 * @author gwu
 *i
 */
@SuppressWarnings("unchecked")

public class EventMapper extends ReactomeModelPostMapper {
	
	public EventMapper() {
	}
   
	private String releaseDate = null;
        
    @Override
    public void fillDetailedView(GKInstance inst,
                                 DatabaseObject obj,
                                 ReactomeToRESTfulAPIConverter converter) throws Exception {
        // A sanity check
        if (!validParameters(inst, obj))
            return;
        Event event = (Event) obj;
        // Check for followingEvent
        fetchFollowingEvents(inst, event, converter);
        // Fetch Regulations referred by this inst.
        fetchRegulations(inst, event, converter);
        // Get species for orthologousEvents
        processOrthologousEvents(inst, event, converter);
    }
    
    private void processOrthologousEvents(GKInstance inst,
                                          Event event,
                                          ReactomeToRESTfulAPIConverter converter) throws Exception {
        List<Event> orthologusEvents = event.getOrthologousEvent();
        if (orthologusEvents == null || orthologusEvents.size() == 0)
            return;
        PersistenceAdaptor dba = inst.getDbAdaptor();
        // Fetch orthologous events
        for (Event oEvent : orthologusEvents) {
            Long dbId = oEvent.getDbId();
            GKInstance oInst = dba.fetchInstance(dbId);
            List<GKInstance> oSpecies = oInst.getAttributeValuesList(ReactomeJavaConstants.species);
            if (oSpecies == null || oSpecies.size() == 0)
                continue;
            List<Species> species = new ArrayList<Species>();
            for (GKInstance oSpecies1 : oSpecies) {
                Species tmp = (Species) converter.createObject(oSpecies1);
                species.add(tmp);
            }
            oEvent.setSpecies(species);
        }
    }

    @Override
    public void postProcess(GKInstance inst, 
                            DatabaseObject obj,
                            ReactomeToRESTfulAPIConverter converter) throws Exception {
        addPathwayStableIdentifier(inst, obj);
    }

    private String getReleaseDate(GKInstance inst) throws Exception {
    	if (this.releaseDate != null) {
    		return this.releaseDate;
    	}
    	if (!(inst.getDbAdaptor() instanceof MySQLAdaptor)) {
            return null;
    	}
    	
        MySQLAdaptor dba = (MySQLAdaptor) inst.getDbAdaptor();
        
        Collection<?> instances = dba.fetchInstancesByClass(ReactomeJavaConstants._Release);
        if (instances == null || instances.size() == 0) {
            return null;
        }
        
        GKInstance release = (GKInstance) instances.iterator().next();
        String releaseDate = (String) release.getAttributeValue(ReactomeJavaConstants.releaseDate);
        this.releaseDate = releaseDate;
        return releaseDate;
    }
    
    /**
     * A helper method to fetch Regulation for the passed Event GKInstance.
     * @param inst
     * @param event
     * @param converter
     * @throws Exception
     */
    private void fetchRegulations(GKInstance inst,
                                  Event event,
                                  ReactomeToRESTfulAPIConverter converter) throws Exception {
        Collection<GKInstance> regulations = inst.getReferers(ReactomeJavaConstants.regulatedEntity);
        if (regulations == null || regulations.size() == 0)
            return;
        List<DatabaseObject> requirements = null;
        List<DatabaseObject> positiveRegulators = null;
        List<DatabaseObject> negativeRegulators = null;
        for (GKInstance regulation : regulations) {
            GKInstance regulator = (GKInstance) regulation.getAttributeValue(ReactomeJavaConstants.regulator);
            if (regulator == null)
                continue; // Just in case. This should not happen usually
            DatabaseObject converted = converter.createObject(regulator);
            // Have to check Requirement first since it is a subclass to PositiveRegulation
            if (regulation.getSchemClass().isa(ReactomeJavaConstants.Requirement)) {
                if (requirements == null)
                    requirements = new ArrayList<DatabaseObject>();
                requirements.add(converted);
            }
            else if (regulation.getSchemClass().isa(ReactomeJavaConstants.PositiveRegulation)) {    
                if (positiveRegulators == null)
                    positiveRegulators = new ArrayList<DatabaseObject>();
                positiveRegulators.add(converted);
            }
            else if (regulation.getSchemClass().isa(ReactomeJavaConstants.NegativeRegulation)) {
                if (negativeRegulators == null)
                    negativeRegulators = new ArrayList<DatabaseObject>();
                negativeRegulators.add(converted);
            }
        }
        event.setRequirements(requirements);
        event.setPositiveRegulators(positiveRegulators);
        event.setNegativeRegulators(negativeRegulators);
    }
    
    /**
     * A helper method to fetch following events from inst's referrers.
     * @param inst
     * @param currentEvent
     * @param converter
     * @throws Exception
     */
    private void fetchFollowingEvents(GKInstance inst, 
                                      Event currentEvent,
                                      ReactomeToRESTfulAPIConverter converter) throws Exception {
        Collection<GKInstance> followingEvent = inst.getReferers(ReactomeJavaConstants.precedingEvent);
        if (followingEvent != null && followingEvent.size() > 0) {
            List<Event> list = new ArrayList<Event>();
            for (GKInstance event : followingEvent) {
                Event converted = (Event) converter.createObject(event);
                list.add(converted);
            }
            currentEvent.setFollowingEvent(list);
        }
    }

    @Override
    protected boolean isValidObject(DatabaseObject obj) {
        return obj instanceof Event;
    }
    
    public Date releaseStartDate(String releaseDateString) throws ParseException {
    	
    	Date releaseStartDate = null;
    	DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    	Date releaseDate = format.parse(releaseDateString);
    	
		if (releaseDate != null) {
			// subtract three months                                                                                                                                                                                                          
			Calendar cal = Calendar.getInstance();
			cal.setTime(releaseDate);
			cal.add(Calendar.MONTH, -3);
			releaseStartDate = cal.getTime();
		}
		
        return releaseStartDate;
    }

    private Date releaseDateFromString(String dateString) throws ParseException {
    	Date date = null;	
    	DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    	if (dateString != null) {
    		date = format.parse(dateString);
    	}
    	return date;
    }
    
    @Override
    public void postShellProcess(GKInstance inst, DatabaseObject obj) throws Exception {
        if (!validParameters(inst, obj))
            return;
        Event event = (Event) obj;
        String releaseStatus = (String) inst.getAttributeValue(ReactomeJavaConstants.releaseStatus);
        if (releaseStatus != null) {
            event.setReleaseStatus(releaseStatus);
        }
        
        // Trigger updated release status if there is a 'revised' or 'modified' InstanceEdit
        // belonging to this release
        if (releaseStatus == null || ! (releaseStatus.equals("UPDATED") || releaseStatus.equals("NEW"))) {
        	List<GKInstance> revisions = inst.getAttributeValuesList("revised");
        	//revisions.addAll(inst.getAttributeValuesList("modified"));
        	for (Integer i=0;i < revisions.size();i++) {
        		GKInstance revised = revisions.get(i);
        			
        		String dateString = revised.getAttributeValue("dateTime").toString();
        		Date date = releaseDateFromString(dateString);
        		
        		String releaseDateString = getReleaseDate(inst);
        		Date releaseStart = null;
        		if (releaseDateString != null) {
        			releaseStart = releaseStartDate(releaseDateString);
        		}

        		// test to see if this InstanceEdit post-dates release minus 3 months
        		if (releaseStart != null && date != null && date.after(releaseStart)) {
        			event.setReleaseStatus("UPDATED");
        			break;
        		}
        	}
        }
        
        
        // Check if this Event is in disease
        GKInstance disease = (GKInstance) inst.getAttributeValue(ReactomeJavaConstants.disease);
        event.setIsInDisease(disease == null ? Boolean.FALSE : Boolean.TRUE);
        GKInstance inferredFrom = (GKInstance) inst.getAttributeValue(ReactomeJavaConstants.inferredFrom);
        event.setIsInferred(inferredFrom == null ? Boolean.FALSE : Boolean.TRUE);

        List<GKInstance> speciesList = inst.getAttributeValuesList(ReactomeJavaConstants.species);
        if (speciesList != null && speciesList.size() > 0) {
	    GKInstance firstSpecies = speciesList.get(0);
	    String name = firstSpecies.getDisplayName();
	    event.setSpeciesName(name);
        }

        addPathwayStableIdentifier(inst, obj);
    }

    private void addPathwayStableIdentifier(GKInstance inst, DatabaseObject obj) throws Exception {
        if (obj.getStableIdentifier() == null) {
            GKInstance stId = (GKInstance) inst.getAttributeValue(ReactomeJavaConstants.stableIdentifier);
            if (stId != null) {
                StableIdentifier stableIdentifier = new StableIdentifier();
                stableIdentifier.setDbId(stId.getDBID());
                stableIdentifier.setDisplayName(stId.getDisplayName());
                obj.setStableIdentifier(stableIdentifier);
            }
        }
    }
}
