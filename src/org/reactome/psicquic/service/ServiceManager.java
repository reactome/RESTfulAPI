/**
 * Copyright (c) 2011
 * European Bioinformatics Institute and Cold Spring Harbor Laboratory.
 */
package org.reactome.psicquic.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.reactome.psicquic.model.SimpleInteractor;

import psidev.psi.mi.tab.model.Confidence;
import uk.ac.ebi.enfin.mi.cluster.EncoreInteraction;

/**
 * 
 * Defines a basic behaviour of a ServiceManager and provides an entry point for
 * choosing the best ServiceManager in function of the service name
 * 
 * @author Antonio Fabregat <fabregat@ebi.ac.uk>
 * 
 */
public abstract class ServiceManager {
    /**
     * Contains a list of interactor accession values
     */
    protected List<String> interactorAccs = new ArrayList<String>();
    
    public ServiceManager() {
    }
    
	/**
	 * Returns a comma separated mappingID database names (used in micluster)
	 * @return a comma separated mappingID database names (used in micluster)
	 */
	public abstract String getMappingIdDbNames();

	/**
	 * Returns a SimpleInteractor object from an EnconreInteraction object and
	 * a query (depending on the implementation of each inherited manager class)
	 * 
	 * @param ei an EnconreInteraction object
	 * @param query the query specified by the user
	 * @return a SimpleInteractor object with the expected single Interactor
	 */
	public abstract SimpleInteractor getSimpleInteractor(EncoreInteraction ei, String query);

	/**
	 * Depending on the configuration defined in the setRetrieveBehaviour method
	 * this function will return the interactor name from the mapList
	 * 
	 * @param mapList
	 * @return the interactor name (in function the current class configuration)
	 */
	protected abstract String getInteractorName(Map<String, List<String>> mapList);

	/**
	 * MiClusters returns a list of confidences containing different scores
	 * values, so, in this method the 'intactPsiscore' is taken into account
	 * 
	 * @param ei
	 *            an EncoreInteraction object
	 * @return the score centred in 'intactPsiscore'
	 */
	protected double getScore(EncoreInteraction ei) {
		List<Confidence> confidences = ei.getConfidenceValues();
		double score = 0;
		for (Confidence co : confidences) {
			if (co.getType().equals("intactPsiscore"))
				score = Double.valueOf(co.getValue());
		}
		return score;
	}

	/**
	 * Based on the serviceName, return the most appropriated ServiceManager
	 * @param serviceName the service name
	 * @return the most appropriated ServiceManager
	 */
	public static final ServiceManager getServiceManager(String serviceName) {
		String serviceNameLC = serviceName.toLowerCase();
		ServiceManager sm;
		
		// CHEMBL
		if (serviceNameLC.matches(".*chembl.*")) {
			sm = new CHEMBLServiceManager();

		// DIP
		} else if (serviceNameLC.matches(".*dip.*")) {
			List<String> interactorAccs = new ArrayList<String>();
			interactorAccs.add("dip");
			sm = new DefaultServiceManager(interactorAccs,
					"ddbj/embl/genbank,uniprotkb,refseq,chebi,irefindex");

		// DRUGBANK
		} else if (serviceNameLC.matches(".*drugbank.*")) {
			List<String> interactorAccs = new ArrayList<String>();
			interactorAccs.add("uniprot");
			sm = new DefaultServiceManager(interactorAccs,
					"ddbj/embl/genbank,uniprotkb,refseq,chebi,irefindex");

		// IREFINDEX
		} else if (serviceNameLC.matches(".*irefindex.*")) {
			List<String> interactorAccs = new ArrayList<String>();
			interactorAccs.add("uniprotkb");
			sm = new IREFINDEXServiceManager(interactorAccs,
					"uniprotkb,ddbj/embl/genbank,refseq,chebi,irefindex");

		// STRING
		} else if (serviceNameLC.matches(".*string.*")) {
			sm = new STRINGServiceManager();
			
		// DEFAULT
		} else {
			sm = new DefaultServiceManager();
		}
		return sm;
	}
}
