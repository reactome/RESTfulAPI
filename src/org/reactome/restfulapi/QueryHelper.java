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
    private ReactomeToRESTfulAPIMapper mapper;


    public QueryHelper() {
    }

    public void setMySQLAdaptor(MySQLAdaptor dba) {
        this.dba = dba;
    }

    public void setMapper(ReactomeToRESTfulAPIMapper mapper) {
        this.mapper = mapper;
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
    public List<ListOfShellInstances> queryAncestors(GKInstance event) throws Exception {
        List<ListOfShellInstances> ancestors = new ArrayList<ListOfShellInstances>();
        ListOfShellInstances branch = new ListOfShellInstances();
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
    private void queryAncestors(List<ListOfShellInstances> ancestors, 
                                ListOfShellInstances branch,
                                GKInstance event) throws Exception {
        branch.addInstance(0, convertToShellInstance(event));
        Collection<?> parents = event.getReferers(ReactomeJavaConstants.hasEvent);
        if (parents == null || parents.size() == 0)
            return;
        if (parents.size() == 1) {
            GKInstance parent = (GKInstance) parents.iterator().next();
            queryAncestors(ancestors, branch, parent);
        }
        else {
            // Need to make a copy first to avoid any overriding
            ListOfShellInstances copy = branch.copy();
            int index = 0;
            for (Iterator<?> it = parents.iterator(); it.hasNext();) {
                GKInstance parent = (GKInstance) it.next();
                if (index == 0) {
                    queryAncestors(ancestors, branch, parent);
                }
                else {
                    ListOfShellInstances newBranch = copy.copy();
                    ancestors.add(newBranch);
                    queryAncestors(ancestors, newBranch, parent);
                }
                index ++;
            }
        }
    }
    
    private ShellInstance convertToShellInstance(GKInstance instance) {
        ShellInstance shell = new ShellInstance();
        shell.setDbId(instance.getDBID());
        shell.setDisplayName(instance.getDisplayName());
        shell.setClassName(instance.getSchemClass().getName());
        return shell;
    }
    
    @Test
    public void testQueryAncestors() throws Exception {
        MySQLAdaptor dba = new MySQLAdaptor("localhost",
                                            "test_gk_central_slice_one_pathway",
                                            "root",
                                            "macmysql01");
        setMySQLAdaptor(dba);
        // The following event should have four branches
        GKInstance event = dba.fetchInstance(69019L);
        List<ListOfShellInstances> ancestors = queryAncestors(event);
        System.out.println("Total branches: " + ancestors.size());
        for (ListOfShellInstances branch : ancestors) {
            for (ShellInstance inst : branch.getInstance())
                System.out.print(inst.getDisplayName() + "||");
            System.out.println();
        }
    }

}
