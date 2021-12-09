/*
 * Created on Apr 30, 2013
 *
 */
package org.reactome.psicquic;

import java.io.File;
import java.io.FileOutputStream;
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
import org.gk.util.FileUtilities;
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
    // For user uploaded interaction files
    protected static final String FILE_PREFIX = "Interaction_File_";
    // For user submitted PSICQUIC URL
    protected static final String PSICQUIC_PREFIX = "USER_PSICQUIC_Service_";
    private String tempDir;
    private String fileName;
    // Database that is used to do gene name to protein accession mapping
    private MySQLAdaptor dba;
    
    public CustomizedInteractionService() {
    }
    
    /**
     * Use this method to register a user submitted PSICQUIC service.
     * @param url the url for the user submitted PSICQUIC service
     * @return a unique service name for the registered service.
     */
    public String registerUserPSICQUIC(String url) {
        String id = createUniqueId();
        String serviceName = PSICQUIC_PREFIX + id;
        CustomizedPSIQUICRegistry.getRegistry().registry(serviceName, url);
        return serviceName;
    }
    
    /**
     * Get a pre-registered PSICQUIC service URL.
     * @param serviceName
     * @return
     */
    public String getRegisteredPSICQUICUrl(String serviceName) {
        return CustomizedPSIQUICRegistry.getRegistry().getRegistedPSICQUICUrl(serviceName);
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
        // Register so that these saved file can be taken care in order to be
        // removed
        CustomizedPSIQUICRegistry.getRegistry().registry(file.getName(), 
                                                         file.getAbsolutePath());
        return file.getName();
    }
    
    /**
     * A helper method to get a file for a uploaded interaction file.
     * @return
     */
    private File getFile() {
        String uuid = createUniqueId();
        File file = new File(tempDir, FILE_PREFIX + uuid);
        return file;
    }

    private String createUniqueId() {
        // A 128 bit value: there is almost no chance to have a duplicated value using this UUID
        String uuid = UUID.randomUUID().toString();
        return uuid;
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
    
    @SuppressWarnings("unchecked")
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
        LocalInteractionQuerier localQuerier = getLocalInteractionQuerier();
        if (localQuerier == null)
            return new QueryResults(); // Return an empty result instead of null to avoid a NullException.
        File file = new File(tempDir, fileName);
        QueryResults results = localQuerier.queryInteractionsFromLocalFile(accessionToRefSeqId, 
                                                                           file);
        return results;
    }
    
    private String getFileType(String line) {
        int index = line.indexOf(":");
        return line.substring(index + 1);
    }
    
    private LocalInteractionQuerier getLocalInteractionQuerier() throws IOException {
        File file = new File(tempDir, fileName);
        FileUtilities fu = new FileUtilities();
        fu.setInput(file.getAbsolutePath());
        // The first line should be the file type
        String line = fu.readLine();
        String fileType = getFileType(line);
        // Get the file type
        fu.close();
        if (fileType.equals("gene"))
            return new GeneGeneInteractionQuerier();
        if (fileType.equals("protein"))
            return new ProteinProteinInteractionQuerier();
        if (fileType.equals("psimitab"))
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
    
    @Override
    protected List<String> getInteractionListFromRest(Map<String, String> accessionToRefEntityId) throws IOException {
        LocalInteractionQuerier querier = getLocalInteractionQuerier();
        if (querier == null)
            return new ArrayList<String>(); // A type is not supported
        File file = new File(tempDir, fileName);
        return querier.exportInteractions(accessionToRefEntityId, file);
    }

    /**
     * A private interface for search and exporting interactions.
     * @author gwu
     *
     */
    private interface LocalInteractionQuerier {
        /**
         * Query for a passed map from protein accession numbers to ReferenceEntity DB_IDs.
         * @param accessionToRefSeqId
         * @param file the temp file containing interactions
         * @return
         * @throws IOException
         */
        public QueryResults queryInteractionsFromLocalFile(Map<String, String> accessionToRefSeqId,
                                                           File file) throws IOException;
        
        /**
         * Export interactions into a list of lines.
         * @param accessionList
         * @param file
         * @return
         * @throws IOException
         */
        public List<String> exportInteractions(Map<String, String> accessionToRefEntityId,
                                               File file) throws IOException;
    }
    
    private class PSIMITabInteractionQuerier implements LocalInteractionQuerier {
        
        public PSIMITabInteractionQuerier() {
        }
        
        @Override
        public List<String> exportInteractions(Map<String, String> accessionToRefEntityId,
                                               File file) throws IOException {
            FileUtilities fu = new FileUtilities();
            fu.setInput(file.getAbsolutePath());
            String line = null;
            List<String> foundLines = new ArrayList<String>();
            while ((line = fu.readLine()) != null) {
                if (shouldEscape(line))
                    continue;
                String[] tokens = line.split("\t");
                for (String acc : accessionToRefEntityId.keySet()) {
                    if (containInteraction(acc, tokens))
                        foundLines.add(line);
                }
            }
            fu.close();
            return foundLines;
        }
        
        private boolean shouldEscape(String line) {
            if (line.startsWith("#"))
                return true; // Comments
            // Need to esacpe the first title line
            if (line.startsWith("unique id A"))
                return true;
            return false;
        }

        @Override
        public QueryResults queryInteractionsFromLocalFile(Map<String, String> accessionToRefSeqId,
                                                           File file) throws IOException {
            Map<String, SimpleInteractorList> accToInteractorList = new HashMap<String, SimpleInteractorList>();
            FileUtilities fu = new FileUtilities();
            fu.setInput(file.getAbsolutePath());
            String line = null;
            Set<String> uniprotIds = new HashSet<String>();
            while ((line = fu.readLine()) != null) {
                if (shouldEscape(line))
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
            fu.close();
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
         * Check if a parsed line has interaction for a passed UniProt accession.
         * @param acc
         * @param tokens
         * @return
         */
        private boolean containInteraction(String acc,
                                           String[] tokens) {
            // First id
            String firstId = getFirstValue(tokens[0]);
            // Second id
            String secondId = getFirstValue(tokens[1]);
            if (firstId.equals(acc) || secondId.equals(acc))
                return true;
            return false;
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
        public List<String> exportInteractions(Map<String, String> accessionToRefEntityId,
                                               File file) throws IOException {
            List<String> rtn = new ArrayList<String>();
            FileUtilities fu = new FileUtilities();
            fu.setInput(file.getAbsolutePath());
            String line = null;
            while ((line = fu.readLine()) != null) {
                if (line.startsWith("#"))
                    continue;
                String[] tokens = line.split("\t");
                for (String acc : accessionToRefEntityId.keySet()) {
                    String partner = getInteractionPartner(acc, tokens);
                    if (partner == null)
                        continue;
                    rtn.add(line);
                }
            }
            fu.close();
            return rtn;
        }

        @Override
        public QueryResults queryInteractionsFromLocalFile(Map<String, String> accessionToRefSeqId,
                                                           File file) throws IOException {
            Map<String, Set<String>> accToPartner = new HashMap<String, Set<String>>();
            FileUtilities fu = new FileUtilities();
            fu.setInput(file.getAbsolutePath());
            String line = null;
            Set<String> uniprotIds = new HashSet<String>();
            while ((line = fu.readLine()) != null) {
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
            fu.close();
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
        
        // TODO: This method and another queryInteractionsFromLocalFile() have some code duplicated. Probably a
        // refactor is needed.
        @Override
        public List<String> exportInteractions(Map<String, String> accessionToRefEntityId,
                                               File file) throws IOException {
            Map<String, String> uniprotAccToGeneName = createAccessionToGeneNameMap(accessionToRefEntityId);
            List<String> rtn = new ArrayList<String>();
            FileUtilities fu = new FileUtilities();
            fu.setInput(file.getAbsolutePath());
            String line = fu.readLine(); // File type line, which should be escaped
            while ((line = fu.readLine()) != null) {
                String[] tokens = line.split("\t");
                for (String acc : accessionToRefEntityId.keySet()) {
                    String gene = uniprotAccToGeneName.get(acc);
                    if (gene == null)
                        continue;
                    String partner = getInteractionPartner(gene,
                                                           tokens);
                    if (partner == null)
                        continue;
                    rtn.add(line);
                }
            }
            fu.close();
            return rtn;
        }

        @Override
        public QueryResults queryInteractionsFromLocalFile(Map<String, String> accessionToRefSeqId,
                                                           File file) throws IOException {
            Map<String, String> uniprotAccToGeneName = createAccessionToGeneNameMap(accessionToRefSeqId);
            Map<String, Set<String>> accToPartner = new HashMap<String, Set<String>>();
            Set<String> allGeneNames = new HashSet<String>();
            // Start reading
            FileUtilities fu = new FileUtilities();
            fu.setInput(file.getAbsolutePath());
            // The first line should be the file type
            String line = fu.readLine(); // Escape this line
            while ((line = fu.readLine()) != null) {
                String[] tokens = line.split("\t");
                for (String acc : accessionToRefSeqId.keySet()) {
                    String gene = uniprotAccToGeneName.get(acc);
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
            fu.close();
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

        private Map<String, String> createAccessionToGeneNameMap(Map<String, String> accessionToRefSeqId) {
            // Want to map from protein accession to gene name based on Reactome database
            Map<String, String> uniprotAccToGeneName = new HashMap<String, String>();
            try {
                for (String acc : accessionToRefSeqId.keySet()) {
                    String dbId = accessionToRefSeqId.get(acc);
                    GKInstance inst = dba.fetchInstance(new Long(dbId));
                    if (inst == null)
                        continue;
                    String geneName = (String) inst.getAttributeValue(ReactomeJavaConstants.geneName);
                    if (geneName == null)
                        continue;
                    uniprotAccToGeneName.put(acc, geneName);
                }
            }
            catch(Exception e) {
                logger.error(e.getMessage(), e);
            }
            return uniprotAccToGeneName;
        }
    }
    
}
