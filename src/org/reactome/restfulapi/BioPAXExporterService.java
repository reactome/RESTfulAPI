/*
 * Created on Jul 1, 2005
 *
 */
package org.reactome.restfulapi;

import org.gk.model.GKInstance;
import org.gk.persistence.MySQLAdaptor;
import org.jdom.Document;
import org.reactome.biopax.ReactomeToBioPAXXMLConverter;

/**
 * This class is used as a mediator to link ReactomeToBioPAXConverter and
 * BioPAXExporterController for the web application.
 *
 * @author guanming
 */
public class BioPAXExporterService {
    // The converting engine
    private ReactomeToBioPAXXMLConverter converter;
    private MySQLAdaptor dba;

    public BioPAXExporterService() {
    }

    /**
     * Get the BioPAX model in org.jdom.Document object format. This method is synchronized.
     *
     * @param dbName the Reactome database
     * @param dbID   the top level event DB_ID.
     * @return BioPAX model in org.jdom.Document format
     * @throws Exception
     */
    public synchronized Document getBioPAXModel(Long dbID) throws Exception {
        if (converter == null)
            converter = new ReactomeToBioPAXXMLConverter();
        if (dba == null) // dba has to be defined explicitly
            throw new IllegalStateException("BioPAXExporterService.getBioPAXModel(): " +
                    "No db adaptor defined!");
        GKInstance event = dba.fetchInstance(dbID);
        if (event == null)
            throw new IllegalStateException("BioPAXExporterService.getBioPAXModel(): " +
                    "No Instance found for the specified dbID");
        if (!event.getSchemClass().isa("Event")) {
            throw new IllegalStateException("BioPAXExporterService.getBioPAXModel(): " +
                    "The specified Instance is not an Event instance. Only Event instance can be exported now.");
        }
        converter.setReactomeEvent(event);
        converter.convert();
        return converter.getBioPAXModel();
    }

    public MySQLAdaptor getDba() {
        return dba;
    }

    public void setDba(MySQLAdaptor dba) {
        this.dba = dba;
    }

    /**
     * @return Returns the converter.
     */
    public ReactomeToBioPAXXMLConverter getConverter() {
        return converter;
    }

    /**
     * @param converter The converter to set.
     */
    public void setConverter(ReactomeToBioPAXXMLConverter converter) {
        this.converter = converter;
    }
}
