/*
 * Created on Apr 30, 2013
 *
 */
package org.reactome.psicquic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gk.model.GKInstance;
import org.gk.model.ReactomeJavaConstants;
import org.gk.persistence.MySQLAdaptor;
import org.reactome.psicquic.model.QueryResults;
import org.reactome.psicquic.model.SimpleInteractor;
import org.reactome.psicquic.model.SimpleInteractorList;
import org.reactome.psicquic.model.SimpleQueryResult;

/**
 * This class is used to handle uploaded interaction file.
 * @author gwu
 *
 */
public class CustomizedInteractionService extends PSICQUICRetriever {
    private final Logger logger = Logger.getLogger(CustomizedInteractionService.class);
    protected static final String FILE_PREFIX = "Interaction_File_";
    private String tempDir;
    private String fileName;
    // Database that is used to do gene name to protein accession mapping
    private MySQLAdaptor dba;
    
    public CustomizedInteractionService() {
    }
    
    /**
     * Upload an interaction file from the browser client side, and return a unique
     * id for the uploaded file.
     * @param uploadedIs
     * @return
     * @throws IOException
     */
    public String uploadInteractions(InputStream uploadedIs) throws IOException {
        File file = getFile();
        FileOutputStream fos = new FileOutputStream(file);
        int read = 0;
        byte[] bytes = new byte[10240]; // 10 k
        while ((read = uploadedIs.read(bytes)) > 0) {
            fos.write(bytes, 0, read);
        }
        fos.flush();
        fos.close();
        return file.getName();
    }
    
    /**
     * A helper method to get a file for a uploaded interaction file.
     * @return
     */
    private File getFile() {
        // Use a nano-second in order to construct a unqiue file name and id
        long ns = System.nanoTime();
        File file = new File(tempDir, FILE_PREFIX + ns);
        int count = 0;
        while (file.exists()) {
            count++;
            file = new File(tempDir, FILE_PREFIX + ns + "_" + count);
        }
        return file;
    }
    
    public void setTempDir(String dir) {
        this.tempDir = dir;
    }
    
    public String getTempDir() {
        return this.tempDir;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getFileName() {
        return this.fileName;
    }
    

    public MySQLAdaptor getDba() {
        return dba;
    }

    public void setDba(MySQLAdaptor dba) {
        this.dba = dba;
    }

    /**
     * A local version for querying interactions though it is still called the same
     * name.
     */
    @Override
    public QueryResults getDataFromRest(Map<String, String> accessionToRefSeqId) throws IOException {
        // Want to map from protein accession to gene name based on Reactome database
        Map<String, String> proteinToGene = new HashMap<String, String>();
        try {
            for (String acc : accessionToRefSeqId.keySet()) {
                String dbId = accessionToRefSeqId.get(acc);
                GKInstance inst = dba.fetchInstance(new Long(dbId));
                if (inst == null)
                    continue;
                String geneName = (String) inst.getAttributeValue(ReactomeJavaConstants.geneName);
                if (geneName == null)
                    continue;
                proteinToGene.put(acc, geneName);
            }
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        // Support gene-gene interaction
        QueryResults results = new QueryResults();
        try {
            File file = new File(tempDir, fileName);
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            Map<String, Set<String>> accToPartner = new HashMap<String, Set<String>>();
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\t");
                for (String acc : accessionToRefSeqId.keySet()) {
                    String gene = proteinToGene.get(acc);
                    if (gene == null)
                        continue;
                    String partner = getInteractionPartner(gene, tokens);
                    if (partner == null)
                        continue;
                    Set<String> set = accToPartner.get(acc);
                    if (set == null) {
                        set = new HashSet<String>();
                        accToPartner.put(acc, set);
                    }
                    set.add(partner);
                }
            }
            br.close();
            reader.close();
            // Convert the map to results
            for (String acc : accToPartner.keySet()) {
                SimpleQueryResult result = new SimpleQueryResult();
                results.addSimpleQueryResult(result);
                result.setQuery(acc);
                result.setRefSeqDBId(accessionToRefSeqId.get(acc));
                SimpleInteractorList interactorList = new SimpleInteractorList();
                result.setInteractionList(interactorList);
                List<SimpleInteractor> interactors = new ArrayList<SimpleInteractor>();
                interactorList.setInteractors(interactors); // Use this method to avoid a requirement in SimpleInteractor
                                                            // accession cannot be null for equal check
                for (String partner : accToPartner.get(acc)) {
                    SimpleInteractor interactor = new SimpleInteractor();
                    interactor.setGenename(partner);
                    interactors.add(interactor);
                }
            }
        }
        catch(IOException e) {
            logger.error(e.getMessage(), e);
            results.setErrorMessage(e.getMessage());
        }
        return results;
    }
    
    private String getInteractionPartner(String query, String[] tokens) {
        if (query.equals(tokens[0]))
            return tokens[1];
        if (query.equals(tokens[1]))
            return tokens[0];
        return null;
    }

    /**
     * A local version for exporting a list of interactions based on whatever format
     * is used.
     */
    @Override
    public String exportInteractions(Map<String, String> accessionToRefEntId)
            throws IOException {
        
        return super.exportInteractions(accessionToRefEntId);
    }
    
    
}
