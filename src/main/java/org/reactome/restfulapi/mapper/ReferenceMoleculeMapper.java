package org.reactome.restfulapi.mapper;

import org.gk.model.GKInstance;
import org.gk.model.PersistenceAdaptor;
import org.reactome.restfulapi.ReactomeModelPostMapper;
import org.reactome.restfulapi.ReactomeToRESTfulAPIConverter;
import org.reactome.restfulapi.models.DatabaseObject;
import org.reactome.restfulapi.models.ReferenceMolecule;

public class ReferenceMoleculeMapper extends ReactomeModelPostMapper {

	@Override
	public void postProcess(GKInstance inst, DatabaseObject obj,
			ReactomeToRESTfulAPIConverter converter) throws Exception {
		// Want to provide ReferenceDatabase and converted URL.
        //setURL(inst, obj);        
	}

	@Override
	public void fillDetailedView(GKInstance inst, DatabaseObject obj,
			ReactomeToRESTfulAPIConverter converter) throws Exception {
        setURL(inst, obj);
	}
	
	@Override
	public void postShellProcess(GKInstance inst, DatabaseObject obj)
			throws Exception {
		// Want to provide ReferenceSequence and converted URL.
		//setURL(inst, obj);
	}
	
	private void setURL(GKInstance inst, DatabaseObject obj) throws Exception {	
		if (!isValidObject(obj))
			return;
		ReferenceMolecule rm = (ReferenceMolecule) obj;
        PersistenceAdaptor dba = inst.getDbAdaptor();
        assignValidURLToDatabaseIdentifier(dba, rm);
	}

	@Override
	protected boolean isValidObject(DatabaseObject obj) {
		return true; //(obj instanceof ReferenceDNASequence);
	}

}

