package org.reactome.restfulapi.details.pmolecules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gk.model.GKInstance;
import org.gk.model.InstanceUtilities;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;
import org.gk.schema.SchemaClass;
import org.junit.Test;
import org.reactome.restfulapi.details.pmolecules.model.Molecule;
import org.reactome.restfulapi.details.pmolecules.model.MoleculeList;
import org.reactome.restfulapi.details.pmolecules.model.Reference;
import org.reactome.restfulapi.details.pmolecules.model.ResultContainer;
import org.reactome.restfulapi.details.pmolecules.types.MoleculeType;

import com.googlecode.gwt.crypto.gwtx.io.IOException;

public class ParticipatingMolecules {
	private MySQLAdaptor dba;
	
	public ParticipatingMolecules() {
	}
	
	public void setDBA(MySQLAdaptor dba) {
	    this.dba = dba;
	}
	
//	public ParticipatingMolecules(MySQLAdaptor dba) {
//		super();
//		this.dba = dba;
//	}

	/**
	 * Get the participating molecules in a specified instance.
	 * @param ins
	 * @return
	 * @throws Exception
	 */
	private Set<GKInstance> grepParticipatingMolecules(GKInstance ins) throws Exception{
		Set<GKInstance> res = new HashSet<GKInstance>();
		Set<GKInstance> set;
		
		String insName = ins.getSchemClass().getName();
		SchemaClass insSc = ins.getSchemClass();
		if(insName.equals(ReactomeJavaConstants.Pathway)){
			set = InstanceUtilities.grepPathwayParticipants(ins);
		}
		else if(insName.equals(ReactomeJavaConstants.Complex)){
			set = InstanceUtilities.getContainedComponents(ins);
		}
		else if(insSc.isa(ReactomeJavaConstants.ReactionlikeEvent)){
			set = InstanceUtilities.getReactionParticipants(ins);
		}
		else if (insName.equals(ReactomeJavaConstants.CandidateSet) ||
				 insName.equals(ReactomeJavaConstants.DefinedSet)){
			List<GKInstance> list = ins.getAttributeValuesList("hasMember");
			if(insName.equals(ReactomeJavaConstants.CandidateSet))
				list.addAll(ins.getAttributeValuesList("hasCandidate"));
			set = new HashSet<GKInstance>();
			set.addAll(list);
		}
		//Polymer->large molecule composed of repeating structural units
		else if (insName.equals(ReactomeJavaConstants.Polymer)){
			List<GKInstance> list = ins.getAttributeValuesList("repeatedUnit");
			set = new HashSet<GKInstance>();
			set.addAll(list);
		}
		else {
			set = new HashSet<GKInstance>();
			res.add(ins);
		}

		for(GKInstance tmp : set){
			res.addAll(grepParticipatingMolecules(tmp));
		}
		return res;
	}
	
	private ResultContainer getDetailsData(MySQLAdaptor dba, GKInstance topLevelPathway) throws IOException {
		ResultContainer res = new ResultContainer();
		
		if (topLevelPathway == null)
			res.setErrorMessage("topLevelPathway is null");
		else
			try {
				Set<GKInstance> molecules = grepParticipatingMolecules(topLevelPathway);
				List<GKInstance> list = new ArrayList<GKInstance>(molecules);
				InstanceUtilities.sortInstances(list);

				for (GKInstance inst: list){
					MoleculeType moleculeType = Molecule.inferMoleculeType(inst);
					Long id = inst.getDBID();
					String name = inst.getDisplayName();
					Molecule molecule = new Molecule(id, name, moleculeType);

					try{
						GKInstance refE = (GKInstance) inst.getAttributeValue("referenceEntity");
						molecule.addReference(getReference(refE));

						@SuppressWarnings("unchecked")
						List<GKInstance> cRs = refE.getAttributeValuesList("crossReference");
						for(GKInstance cR : cRs)
							molecule.addReference(getReference(cR));
					}catch(Exception e){
						//e.printStackTrace(System.err);
					}            	            
					res.addMolecule(moleculeType, molecule);
				}
			} catch (Exception e) {
				System.err.println("PathwayDetailsServlet.processRequest: WARNING Problems retreiving the participating molecules for topLevelPathway=" + topLevelPathway);
				e.printStackTrace(System.err);
				res.setErrorMessage("Problems retreiving the participating molecules for topLevelPathway=" + topLevelPathway);
			}
			
 		return res;
	}
	
	private Reference getReference(GKInstance inst){
		Reference reference = null;
		try{
			SchemaClass sc = inst.getSchemClass();
			if(sc.isValidAttribute("referenceDatabase") && sc.isValidAttribute("identifier")){
	    		GKInstance rd = (GKInstance) inst.getAttributeValue("referenceDatabase");
	    		String id = (String) inst.getAttributeValue("identifier");
	    		String url = (String) rd.getAttributeValue("accessUrl");
	    		if(url!=null)
	    			url = url.replace("###ID###", id);
	    		else
	    			url = "";
	    		String name = (String) rd.getAttributeValue("name");
				
	    		reference = new Reference(id, name, url);
	    		
	    		if(sc.isValidAttribute("geneName")){
	    			String geneName = (String) inst.getAttributeValue("geneName");
					reference.setGeneName(geneName);
	    		}
        	}
			
    	}catch(Exception e){/*Nothing here*/}
    	return reference;
	}
	
	public ResultContainer getParticipatingMolecules(Long paramID) throws IOException{
		if (dba == null) {
			System.err.println("ParticipatingMolecules.getParticipatingMolecules: WARNING - dba is null!");
		}
			
		GKInstance topLevelPathway = null;
		try {
			topLevelPathway = dba.fetchInstance(paramID);
		} catch (Exception e) {
			System.err.print("PathwayDetailsServlet.processRequest: WARNING problem fetching top level pathway");
			if (paramID == null)
				System.err.println(", paramID is null");
			else
				System.err.println(", paramID=" + paramID);
			e.printStackTrace(System.err);
			topLevelPathway = null;
		}
		
		ResultContainer result = this.getDetailsData(dba, topLevelPathway);
		return result;
	}
	
	@Test
	public void testGetParticipantingMolecules() throws Exception {
	    MySQLAdaptor dba = new MySQLAdaptor("localhost", "gk_current_ver41", "root", "macmysql01");
	    this.dba = dba;
	    ResultContainer container = getParticipatingMolecules(975040L);
	    List<MoleculeList> molecules = container.getParticipatingMolecules();
	    System.out.println("Total moleculeList: " + molecules.size());
	    MoleculeList list = molecules.get(0);
	    System.out.println("Total molecules in the first list: " + list.getMolecules().size());
	}
}
