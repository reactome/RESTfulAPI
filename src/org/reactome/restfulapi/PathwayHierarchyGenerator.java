/*
 * Created on Oct 26, 2012
 *
 */
package org.reactome.restfulapi;

import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * This class is used to generate the pathway hierarchy as displayed in the web pathway browser. The implementation in
 * this class is modified from an original class in org.gk.scripts.
 * @author gwu
 *
 */
public class PathwayHierarchyGenerator {
    
    public PathwayHierarchyGenerator() {
    }
    
    /**
     * Generate an XML string for the passed list of pathways, which should be top-level pathways defined
     * by the FrontPage instance usually. However, pathways in non-human species can be used in this method
     * too.
     * @throws Exception
     */
    public String generatePathwayHierarchy(List<GKInstance> pathways,
                                           String speciesName) throws Exception {
        Element root = new Element("Pathways");
        if (speciesName != null) {
            root.setAttribute("species", speciesName);
            // Add a version
            if (pathways.get(0).getDbAdaptor() instanceof MySQLAdaptor) {
                MySQLAdaptor dba = (MySQLAdaptor) pathways.get(0).getDbAdaptor();
                Integer version = dba.getReleaseNumber();
                if (version != null)
                    root.setAttribute("release", version + "");
            }
        }
        for (GKInstance pathway : pathways)
            addEvent(pathway, 
                     root);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        outputter.output(root, bos);
        return bos.toString("utf-8"); // Default use UTF-8 charset.
    }
    
    private void addEvent(GKInstance inst, 
                          Element parentElm) throws Exception {
        Element elm = new Element(inst.getSchemClass().getName());
        elm.setAttribute("dbId", inst.getDBID().toString());
        elm.setAttribute("displayName", inst.getDisplayName());
        // Check if it has PathwayDiagram associated
        if (inst.getSchemClass().isa(ReactomeJavaConstants.Pathway)) {
            Collection<?> diagrams = inst.getReferers(ReactomeJavaConstants.representedPathway);
            if (diagrams != null && diagrams.size() > 0) {
                for (Iterator<?> it = diagrams.iterator(); it.hasNext();) {
                    GKInstance diagram = (GKInstance) it.next();
                    if (diagram.getSchemClass().isa(ReactomeJavaConstants.PathwayDiagram)) {
                        elm.setAttribute("hasDiagram", Boolean.TRUE + "");
                        break;
                    }
                }
            }
        }
        parentElm.addContent(elm);
        // Add children
        if (inst.getSchemClass().isValidAttribute(ReactomeJavaConstants.hasEvent)) {
            List<?> children = inst.getAttributeValuesList(ReactomeJavaConstants.hasEvent);
            for (Object obj : children) {
                GKInstance childInst = (GKInstance) obj;
                addEvent(childInst, elm);
            }
        }
    }
}