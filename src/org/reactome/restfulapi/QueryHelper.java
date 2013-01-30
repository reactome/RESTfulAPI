/*
 * Created on Nov 10, 2005
 *
 */
package org.reactome.restfulapi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.gk.model.GKInstance;
import org.gk.model.InstanceUtilities;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;
import org.gk.schema.SchemaClass;
import org.junit.Test;
import org.reactome.restfulapi.models.Event;
import org.reactome.restfulapi.models.ListOfShellInstances;
import org.reactome.restfulapi.models.ShellInstance;

/**
 * This class is used to handle some complicated query for CaBioDomainService class.
 *
 * @author guanming
 */
@SuppressWarnings("unchecked")
public class QueryHelper {
    private MySQLAdaptor dba;
    private ReactomeToRESTfulAPIConverter converter;

    public QueryHelper() {
    }

    public void setMySQLAdaptor(MySQLAdaptor dba) {
        this.dba = dba;
    }
    
    public void setConverter(ReactomeToRESTfulAPIConverter converter) {
        this.converter = converter;
    }

    public List<GKInstance> query(String className,
                                  String caBioPropName,
                                  String caBioPropValue) throws Exception {
        Set<GKInstance> set = new HashSet<GKInstance>();
        // Handle special cases first
        
        SchemaClass cls = dba.getSchema().getClassByName(className);
        if (!cls.isValidAttribute(caBioPropName)) {
//            System.out.print(caBioPropName +"invalid");
            return new ArrayList<GKInstance>();
        }
        if (!cls.getAttribute(caBioPropName).isInstanceTypeAttribute()) {
//            System.out.println(caBioPropName +"notinstance");
            Collection instances = dba.fetchInstanceByAttribute(className, 
                                                                caBioPropName, 
                                                                "=", 
                                                                caBioPropValue);
            if (instances == null || instances.size() == 0)
                return new ArrayList<GKInstance>();
            set.addAll(instances);
        } 
        else {
//            System.out.println(caBioPropName +"isinstance");
            // Have to construct GKInstance from value for query
            //Set<GKInstance> criteria = buildQueryCriteria(caBioPropValue);
            GKInstance instance =  dba.fetchInstance(Long.parseLong(caBioPropValue));
            Collection instances = dba.fetchInstanceByAttribute(className,
                                                                caBioPropName,
                                                                "=",
                                                                instance);
            if (instances != null)
                set.addAll(instances);
        }
        List<GKInstance> gkInstances = new ArrayList<GKInstance>(set);
        InstanceUtilities.sortInstances(gkInstances);
        return gkInstances;
    }

    /**
     * Query ancestors for an Event.
     * @param dbId
     * @return
     * @throws Exception
     */
    public List<List<Event>> queryAncestors(GKInstance event) throws Exception {
        List<List<Event>> ancestors = new ArrayList<List<Event>>();
        List<Event> branch = new ArrayList<Event>();
        ancestors.add(branch);
        queryAncestors(ancestors, branch, event);
        return ancestors;
    }
    
    /**
     * A resurve way to get ancestors into a List<ShellInstance>. An event can have more than
     * one parent.
     * @param ancestors
     * @param branch
     * @param event
     * @throws Exception
     */
    private void queryAncestors(List<List<Event>> ancestors, 
                                List<Event> branch,
                                GKInstance event) throws Exception {
        Event convertedEvent = convertToEvent(event);
        if (convertedEvent != null)
            branch.add(0, convertedEvent);
        Collection<?> parents = event.getReferers(ReactomeJavaConstants.hasEvent);
        if (parents == null || parents.size() == 0)
            return;
        if (parents.size() == 1) {
            GKInstance parent = (GKInstance) parents.iterator().next();
            queryAncestors(ancestors, branch, parent);
        }
        else {
            // Need to make a copy first to avoid any overriding
            List<Event> copy = new ArrayList<Event>(branch);
            int index = 0;
            for (Iterator<?> it = parents.iterator(); it.hasNext();) {
                GKInstance parent = (GKInstance) it.next();
                if (index == 0) {
                    queryAncestors(ancestors, branch, parent);
                }
                else {
                    List<Event> newBranch = new ArrayList<Event>(copy);
                    ancestors.add(newBranch);
                    queryAncestors(ancestors, newBranch, parent);
                }
                index ++;
            }
        }
    }
    
    private Event convertToEvent(GKInstance instance) throws Exception {
        if (instance.getSchemClass().isa(ReactomeJavaConstants.Event)) {
            Event event = (Event) converter.createObject(instance);
            return event;
        }
        return null;
    }
    
    @Test
    public void testQueryAncestors() throws Exception {
        MySQLAdaptor dba = new MySQLAdaptor("localhost",
                                            "gk_current_ver42",
                                            "root",
                                            "macmysql01");
        setMySQLAdaptor(dba);
        ReactomeToRESTfulAPIConverter converter = new ReactomeToRESTfulAPIConverter();
        converter.setMapper(new ReactomeToRESTfulAPIMapper());
        converter.setPostMapperFactory(new ReactomeModelPostMapperFactory());
        setConverter(converter);
        // The following event should have four branches
        GKInstance event = dba.fetchInstance(69019L);
        List<List<Event>> ancestors = queryAncestors(event);
        System.out.println("Total branches: " + ancestors.size());
        for (List<Event> branch : ancestors) {
            for (Event inst : branch)
                System.out.print(inst.getDisplayName() + "||");
            System.out.println();
        }
    }

}
