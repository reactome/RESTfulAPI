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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    public String uploadInteractions(String fileType,
                                     InputStream uploadedIs) throws IOException {
        File file = getFile();
        FileOutputStream fos = new FileOutputStream(file);
        // Output file type first as an annotation line
        String fileTypeLine = "#FileType:" + fileType + "\n";
        fos.write(fileTypeLine.getBytes());
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
        // A 128 bit value: there is almost no chance to have a duplicated value using this UUID
        String uuid = UUID.randomUUID().toString();
        File file = new File(tempDir, FILE_PREFIX + uuid);
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
    
    @SuppressWarnings("unchecked")
    private Map<String, String> queryGeneNameToUniProtId(MySQLAdaptor dba,
                                                         Collection<String> genes) {
        try {
            Collection<GKInstance> c = dba.fetchInstanceByAttribute(ReactomeJavaConstants.ReferenceGeneProduct,
                                                                    ReactomeJavaConstants.geneName, 
                                                                    "=", 
                                                                    genes);
            Map<String, String> geneToId = new HashMap<String, String>();
            for (GKInstance inst : c) {
                List<String> geneNames = inst.getAttributeValuesList(ReactomeJavaConstants.geneName);
                String id = (String) inst.getAttributeValue(ReactomeJavaConstants.identifier);
                for (String geneName : geneNames) {
                    geneToId.put(geneName, id);
                }
            }
            return geneToId;
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new HashMap<String, String>();
    }
    
    private Map<String, String> queryUniProtIdToGeneName(MySQLAdaptor dba,
                                                         Collection<String> ids) {
        try {
            Collection<GKInstance> c = dba.fetchInstanceByAttribute(ReactomeJavaConstants.ReferenceGeneProduct,
                                                                    ReactomeJavaConstants.identifier,
                                                                    "=",
                                                                    ids);
            Map<String, String> idToGeneName = new HashMap<String, String>();
            for (GKInstance inst : c) {
                String id = (String) inst.getAttributeValue(ReactomeJavaConstants.identifier);
                String geneName = (String) inst.getAttributeValue(ReactomeJavaConstants.geneName);
                if (geneName == null)
                    continue;
                idToGeneName.put(id, geneName);
            }
            return idToGeneName;
        }
        catch(Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new HashMap<String, String>();
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
        File file = new File(tempDir, fileName);
        FileReader reader = new FileReader(file);
        BufferedReader br = new BufferedReader(reader);
        // The first line should be the file type
        String line = br.readLine();
        String fileType = getFileType(line);
        // Get the file type
        br.close();
        reader.close();
        LocalInteractionQuerier localQuerier = getLocalInteractionQuerier(fileType);
        if (localQuerier == null)
            return new QueryResults(); // Return an empty result instead of null to avoid a NullException.
        QueryResults results = localQuerier.queryInteractionsFromLocalFile(accessionToRefSeqId, 
                                                                           file);
        return results;
    }
    
    private String getFileType(String line) {
        int index = line.indexOf(":");
        return line.substring(index + 1);
    }
    
    private LocalInteractionQuerier getLocalInteractionQuerier(String type) {
        if (type.equals("gene"))
            return new GeneGeneInteractionQuerier();
        if (type.equals("protein"))
            return new ProteinProteinInteractionQuerier();
        if (type.equals("psimitab"))
            return new PSIMITabInteractionQuerier();
        return null;
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
    
    private interface LocalInteractionQuerier {
        public QueryResults queryInteractionsFromLocalFile(Map<String, String> accessionToRefSeqId,
                                                           File file) throws IOException;
    }
    
    private class PSIMITabInteractionQuerier implements LocalInteractionQuerier {
        
        public PSIMITabInteractionQuerier() {
        }

        @Override
        public QueryResults queryInteractionsFromLocalFile(Map<String, String> accessionToRefSeqId,
                                                           File file) throws IOException {
            Map<String, SimpleInteractorList> accToInteractorList = new HashMap<String, SimpleInteractorList>();
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String line = null;
            Set<String> uniprotIds = new HashSet<String>();
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#"))
                    continue; // Comments
                // Need to esacpe the first title line
                if (line.startsWith("unique id A"))
                    continue;
                String[] tokens = line.split("\t");
                for (String acc : accessionToRefSeqId.keySet()) {
                    SimpleInteractor interactor = checkInteraction(acc, tokens);
                    if (interactor == null)
                        continue;
                    SimpleInteractorList list = accToInteractorList.get(acc);
                    if (list == null) {
                        list = new SimpleInteractorList();
                        accToInteractorList.put(acc, list);
                    }
                    list.add(interactor);
                }
            }
            br.close();
            fileReader.close();
            // Convert the map to results
            QueryResults results = new QueryResults();
            for (String acc : accToInteractorList.keySet()) {
                SimpleQueryResult result = new SimpleQueryResult();
                results.addSimpleQueryResult(result);
                result.setQuery(acc);
                result.setRefSeqDBId(accessionToRefSeqId.get(acc));
                SimpleInteractorList interactorList = accToInteractorList.get(acc);
                result.setInteractionList(interactorList);
            }
            return results;
        }
        
        /**
         * Check if a parsed line from a PSI-MI tab contains interaction for a 
         * passed protein UniProt accession. If true, a SimpleInterctor will be
         * created. Otherwise, a null will be returned. The contents in the
         * tokens should be based on the PSI-MI tab spec described in this page:
         * http://code.google.com/p/psimi/wiki/PsimiTabFormat
         * @param acc
         * @param tokens
         * @return
         */
        private SimpleInteractor checkInteraction(String acc,
                                                  String[] tokens) {
            // First id
            String firstId = getFirstValue(tokens[0]);
            // Second id
            String secondId = getFirstValue(tokens[1]);
            if (!firstId.equals(acc) && !secondId.equals(acc))
                return null;
            SimpleInteractor interactor = new SimpleInteractor();
            if (firstId.equals(acc)) { // Second protein is the partner
                interactor.setAccession(secondId);
                // Get gene name
                interactor.setGenename(getFirstValue(tokens[3]));
            }
            else { // First protein is the partner
                interactor.setAccession(firstId);
                interactor.setGenename(getFirstValue(tokens[2]));
            }
            // Check if there is a score
            if (tokens.length >= 15 && tokens[14].length() > 0 && !tokens[14].equals("-")) {
                String scoreText = getFirstValue(tokens[14]);
                try {
                    interactor.setScore(new Double(scoreText)); 
                }
                catch(NumberFormatException e) {} // Just ignore it
            }
            return interactor;
        }
        
        private String getFirstValue(String token) {
            String[] tokens = token.split("\\|");
            int index = tokens[0].indexOf(":");
            return tokens[0].substring(index + 1);
        }
        
    }
    
    private class ProteinProteinInteractionQuerier implements LocalInteractionQuerier {
        
        public ProteinProteinInteractionQuerier() {
        }

        @Override
        public QueryResults queryInteractionsFromLocalFile(Map<String, String> accessionToRefSeqId,
                                                           File file) throws IOException {
            Map<String, Set<String>> accToPartner = new HashMap<String, Set<String>>();
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);
            String line = null;
            Set<String> uniprotIds = new HashSet<String>();
            while ((line = br.readLine()) != null) {
                if (line.startsWith("#"))
                    continue; // Comments
                String[] tokens = line.split("\t");
                for (String acc : accessionToRefSeqId.keySet()) {
                    String partner = getInteractionPartner(acc,
                                                           tokens);
                    if (partner == null)
                        continue;
                    // In this case, partner should be protein accession
                    Set<String> set = accToPartner.get(acc);
                    if (set == null) {
                        set = new HashSet<String>();
                        accToPartner.put(acc, set);
                    }
                    set.add(partner);
                    uniprotIds.add(partner);
                }
            }
            br.close();
            fileReader.close();
            Map<String, String> uniprotIdToGeneName = queryUniProtIdToGeneName(dba, uniprotIds);
            // Convert the map to results
            QueryResults results = new QueryResults();
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
                    interactor.setAccession(partner);
                    String geneName = uniprotIdToGeneName.get(partner);
                    interactor.setGenename(geneName);
                    interactors.add(interactor);
                }
            }
            return results;
        }
    }
    
    private class GeneGeneInteractionQuerier implements LocalInteractionQuerier {
        
        public GeneGeneInteractionQuerier() {
        }
        
        @Override
        public QueryResults queryInteractionsFromLocalFile(Map<String, String> accessionToRefSeqId,
                                                           File file) throws IOException {
            // Want to map from protein accession to gene name based on Reactome database
            Map<String, String> uniprotIdToGeneName = new HashMap<String, String>();
            try {
                for (String acc : accessionToRefSeqId.keySet()) {
                    String dbId = accessionToRefSeqId.get(acc);
                    GKInstance inst = dba.fetchInstance(new Long(dbId));
                    if (inst == null)
                        continue;
                    String geneName = (String) inst.getAttributeValue(ReactomeJavaConstants.geneName);
                    if (geneName == null)
                        continue;
                    uniprotIdToGeneName.put(acc, geneName);
                }
            }
            catch(Exception e) {
                logger.error(e.getMessage(), e);
            }
            Map<String, Set<String>> accToPartner = new HashMap<String, Set<String>>();
            Set<String> allGeneNames = new HashSet<String>();
            // Start reading
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            // The first line should be the file type
            String line = br.readLine(); // Escape this line
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\t");
                for (String acc : accessionToRefSeqId.keySet()) {
                    String gene = uniprotIdToGeneName.get(acc);
                    if (gene == null)
                        continue;
                    String partner = getInteractionPartner(gene,
                                                           tokens);
                    if (partner == null)
                        continue;
                    Set<String> set = accToPartner.get(acc);
                    if (set == null) {
                        set = new HashSet<String>();
                        accToPartner.put(acc, set);
                    }
                    set.add(partner);
                    allGeneNames.add(partner);
                }
            }
            br.close();
            reader.close();
            Map<String, String> geneNameToUniProtId = queryGeneNameToUniProtId(dba,
                                                                               allGeneNames);
            // Convert the map to results
            QueryResults results = new QueryResults();
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
                    String uniprotId = geneNameToUniProtId.get(partner);
                    interactor.setAccession(uniprotId);
                    interactors.add(interactor);
                }
            }
            return results;
        }
    }
    
}
