/*
 * Created on Jun 5, 2021
 *
 */
package org.reactome.restfulapi.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.pathwaylayout.DiagramGeneratorFromDB;
import org.gk.persistence.MySQLAdaptor;
import org.reactome.restfulapi.ReactomeToRESTfulAPIConverter;
import org.reactome.restfulapi.models.CellLineagePath;
import org.reactome.restfulapi.models.DatabaseObject;
import org.reactome.restfulapi.models.Pathway;
import org.reactome.restfulapi.models.Species;

/**
 * @author weiserj
 *
 */
public class CellLineagePathMapper extends PathwayMapper {
    
    public CellLineagePathMapper() {
        
    }

    @Override
    public void postProcess(GKInstance inst, 
                            DatabaseObject obj,
                            ReactomeToRESTfulAPIConverter converter) throws Exception {
        super.postProcess(inst, obj, converter);
        if (!validParameters(inst, obj))
            return;
        addCellLineagePathDiagramFlag(inst, obj);
    }

    @Override
    public void postShellProcess(GKInstance inst, DatabaseObject obj)
            throws Exception {
        super.postShellProcess(inst, obj);
        addCellLineagePathDiagramFlag(inst, obj);
        addCellLineagePathSpecies(inst, obj);
    }

    private void addCellLineagePathDiagramFlag(GKInstance inst, DatabaseObject obj) throws Exception {
        // Check if this Pathway has Diagram
        CellLineagePath cellLineagePath = (CellLineagePath) obj;
        cellLineagePath.setHasDiagram(false);
        if (inst.getDbAdaptor() instanceof MySQLAdaptor) {
            DiagramGeneratorFromDB diagramHelper = new DiagramGeneratorFromDB();
            diagramHelper.setMySQLAdaptor((MySQLAdaptor) inst.getDbAdaptor());
            GKInstance diagram = diagramHelper.getPathwayDiagram(inst);
            if (diagram != null)
                cellLineagePath.setHasDiagram(true);
        }
        else {
            Collection<?> diagrams = inst.getReferers(ReactomeJavaConstants.representedPathway);
            if (diagrams != null && diagrams.size() > 0) {
                for (Iterator<?> it = diagrams.iterator(); it.hasNext();) {
                    GKInstance diagram = (GKInstance) it.next();
                    if (diagram.getSchemClass().isa(ReactomeJavaConstants.PathwayDiagram)) {
                        cellLineagePath.setHasDiagram(true);
                        break;
                    }
                }
            }
        }
    }


    private void addCellLineagePathSpecies(GKInstance inst, DatabaseObject obj){
        CellLineagePath cellLineagePath = (CellLineagePath) obj;
        if(cellLineagePath.getSpecies()!=null) return; // Check if this has been handled
        if(inst.getSchemClass().isValidAttribute(ReactomeJavaConstants.species)){
            try {
                List<Species> speciesList = new ArrayList<Species>();
                List<GKInstance> values = inst.getAttributeValuesList(ReactomeJavaConstants.species);
                if (values != null && values.size() > 0) {
                    for (GKInstance s : values) {
                        Species species = new Species();
                        species.setDbId(s.getDBID());
                        species.setDisplayName(s.getDisplayName());
                        speciesList.add(species);
                    }
                }
                cellLineagePath.setSpecies(speciesList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
}
