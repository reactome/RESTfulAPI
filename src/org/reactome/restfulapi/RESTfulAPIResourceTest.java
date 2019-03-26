package org.reactome.restfulapi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.gk.util.FileUtilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: home
 * Date: 6/15/11
 * Time: 8:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class RESTfulAPIResourceTest {
    // Two methods used in this class
    private final static String HTTP_GET = "Get";
    private final static String HTTP_POST = "Post";
    // Use the default port 80 since apache has been configured to relay to the 6080 port after the firewall.
//    private final static String RESTFUL_URL = "http://www.reactome.org/ReactomeRESTfulAPI/RESTfulWS/";
//    private final static String RESTFUL_URL = "http://reactomedev.oicr.on.ca:8080/ReactomeRESTfulAPI/RESTfulWS/";
//    private final static String RESTFUL_URL = "http://reactomews.oicr.on.ca:8080/ReactomeRESTfulAPI/RESTfulWS/";
//    private final static String RESTFUL_URL = "http://reactomerelease.oicr.on.ca:8080/ReactomeRESTfulAPI/RESTfulWS/";
//    private final static String RESTFUL_URL = "http://reactomecurator.oicr.on.ca/ReactomeRESTfulAPI/RESTfulWS/";
//    private final static String RESTFUL_URL = "http://cpws.reactome.org/ReactomeRESTfulAPI/RESTfulWS/";
    private final static String RESTFUL_URL = "http://localhost:8080/ReactomeRESTfulAPI/RESTfulWS/";
//    private final static String RESTFUL_URL = "http://reactomedev.oicr.on.ca:7080/ReactomeRESTfulAPI/RESTfulWS/";
    
    @Test
    public void testBioPaxExporter() throws Exception {
        String url = RESTFUL_URL + "biopaxExporter/Level3/201681";
        System.out.println(url);
        String text = callHttp(url,
                HTTP_GET,
                "");
        System.out.println("Output from biopaxexporter (level2):\n");
        System.out.println(text);
        
//        url = RESTFUL_URL + "biopaxExporter/Level3/109581";
//        System.out.println(url);
//        text = callHttp(url,
//                HTTP_GET,
//                "");
//        System.out.println("\nOutput from biopaxexporter (level3):\n");
//        System.out.println(text);
    }
    
    @Test
    public void testSBMLExport() throws Exception {
        String url = RESTFUL_URL + "sbmlExporter/109581";
        System.out.println(url);
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("Output from sbmlExporter:\n");
        System.out.println(text);
    }
    
    @Test
    public void testSBGNExport() throws Exception {
    	String url = RESTFUL_URL + "sbgnExporter/453274";
    	System.out.println(url);
    	String text = callHttp(url, HTTP_GET, "");
    	System.out.println("Output from sbgnExporter:\n");
    	System.out.println(text);
    }

    @Test
    public void testPathwayDiagram() throws Exception {
                String url = RESTFUL_URL + "pathwayDiagram/70326/png";
                String text = callHttp(url,
                        HTTP_GET,
                        "");
                System.out.println("Output from pathwaydiagram:\n");
                System.out.println(text);
                decodeInBase64(text, "2206290.png");
        //        
        //        url = RESTFUL_URL + "pathwayDiagram/109581/pdf";
        //        text = callHttp(url,
        //                HTTP_GET,
        //                "");
        //        System.out.println("Output from pathwaydiagram:\n");
        //        System.out.println(text);
        //        decodeInBase64(text, "69278.pdf");
        //        
        // A disease diagram: signaling by FGFR in cancer
//        String url = RESTFUL_URL + "pathwayDiagram/1226099/pdf";
//        String text = callHttp(url, HTTP_GET, "");
//        decodeInBase64(text, "1226099.pdf");
        //        
        //        // Pathway diagram containing multiple EntitySet
        //        url = RESTFUL_URL + "pathwayDiagram/71387/xml";
        //        System.out.println(url);
        //        text = callHttp(url, HTTP_GET, "");
        //        prettyPrintXML(text);
        
                url = RESTFUL_URL + "pathwayDiagram/70326/xml";
                text = callHttp(url, HTTP_GET, "");
                prettyPrintXML(text);
        
        
    }
    
    @Test
    public void testGetPhysicalToReferenceEntityMap() throws Exception {
        // Regulation of Apoptosis
    	Long dbId = 169911L;
    	// A very big Chicken pathway containing many small pathways
    	dbId = 5225808L;
    	dbId = 74160L;
        String url = RESTFUL_URL + "getPhysicalToReferenceEntityMaps/" + dbId;
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("\ngetPhysicalToReferenceEntityMaps for " + dbId + ":");
        System.out.println(text);
    }
    
    @Test
    public void testHighlightPathwayDiagram() throws Exception {
        // G2/M Transition: 453274
//        String url = RESTFUL_URL + "highlightPathwayDiagram/453274/pdf";
//        String fileName = "HighlightG2_MTransition.pdf";
        String url = RESTFUL_URL + "highlightPathwayDiagram/453274/png";
        String fileName = "HighlightG2_MTransition.png";
        // A list of genes
        String genes = "PPP2R1A,CEP192,AKAP9,CENPJ,CEP290,DYNC1H1";
        String text = callHttp(url, HTTP_POST, genes);
        decodeInBase64(text, fileName);
        
//        // Some users' list returns 204 error code
//        url = RESTFUL_URL + "highlightPathwayDiagram/3449037/xml";
//        genes = "LEFTY1,LEFTY2,TDGF1,NODAL";
//        text = callHttp(url, HTTP_POST, genes);
//        System.out.println(text);
//        decodeInBase64(text, "Test.pdf");
        
        //        // Test for a gene list in a file
        //        String dir = "/Users/gwu/Documents/wgm/work/ctbioscience/";
        //        String fileName = dir + "CellCycleCheckpointsGenes.txt";
        //        Set<String> genes = new FileUtility().loadInteractions(fileName);
        //        String query = InteractionUtilities.joinStringElements(",", genes);
        //        String url = RESTFUL_URL + "highlightPathwayDiagram/69620/pdf";
        //        String text = callHttp(url, HTTP_POST, query);
        //        decodeInBase64(text, dir + "CellCycleCheckpoints.pdf");
    }
    
    @Test
    public void testHitPathways() throws Exception {
        String url = RESTFUL_URL + "queryHitPathways";
        // A list of genes
        String genes = "PPP2R1A,CEP192,AKAP9,CENPJ,CEP290,DYNC1H1";
        String text = callHttp(url, HTTP_POST, genes);
        prettyPrintXML(text);
        Long dbId = 71387L;
        url = RESTFUL_URL + "highlightPathwayDiagram/" + dbId + "/pdf";
        text = callHttp(url, HTTP_POST, genes);
        decodeInBase64(text, "OneHitPathway.pdf");
    }
    
    public void decodeInBase64(String text, String fileName) throws IOException {
        byte[] content = Base64.decodeBase64(text);
        // Output the content into a local file
        FileOutputStream fos = new FileOutputStream(fileName);
        FileChannel fileChannel = fos.getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(content.length);
        byteBuffer.put(content);
        byteBuffer.flip();
        fileChannel.write(byteBuffer);
        fileChannel.close();
        fos.close();
    }
    
    /**
     * An example to query the FrontPage object and first FrontPmageItem.
     * @throws Exception
     */
//    @Test
//    public void testQueryFromPagePathways() throws Exception {
//        // The sole FrontPage item in realease 38
//        String url = RESTFUL_URL + "queryById/FrontPage/1591246";
//        String text = callHttp(url, HTTP_GET, "");
//        System.out.println("Front Page Instance:\n");
//        prettyPrintXML(text);
//        // Get the first FrontPageItem: Amyloids
//        // Process XML using JDOM
//        SAXBuilder builder = new SAXBuilder();
//        Document doc = builder.build(new StringReader(text));
//        Element first = doc.getRootElement().getChild("frontPageItem");
//        // Get the DB_ID
//        String firstDbId = first.getChildText("dbId");
//        url = RESTFUL_URL + "queryById/Event/" + firstDbId;
//        text = callHttp(url, HTTP_GET, "");
//        System.out.println("\nFirst Event in FrontPage:\n");
//        prettyPrintXML(text);
//    }
    
    @Test
    public void testPathwayHierarchy() throws Exception {
        String species[] = new String[] {
                "Homo sapiens",
                "Mus musculus",
                "Gallus gallus"
        };
        for (String name : species) {
            name = URLEncoder.encode(name, "utf-8");
            System.out.println("Encoded species: " + name);
            String specuesUrl = RESTFUL_URL + "pathwayHierarchy/" + name;
            String text = callHttp(specuesUrl, HTTP_GET, "");
            System.out.println("\nPathway hierarchy for " + name + ":");
            prettyPrintXML(text);
            FileUtilities fu = new FileUtilities();
            fu.setOutput(name + ".xml");
            fu.printLine(text);
            fu.close();
        }
    }
    
    @Test
    public void testQueryFrontPageItems() throws Exception  {
        String species[] = new String[] {
                "Homo sapiens",
                "Mus musculus",
                "Gallus gallus"
        };
        for (String name : species) {
            name = URLEncoder.encode(name, "utf-8");
            System.out.println("Encoded species: " + name);
            String specuesUrl = RESTFUL_URL + "frontPageItems/" + name;
            String text = callHttp(specuesUrl, HTTP_GET, "");
            System.out.println("\nFront page for " + name + ":");
            prettyPrintXML(text);
        }
    }
    
    @Test
    public void testGetDBName() throws Exception {
    	String url = RESTFUL_URL + "getDBName";
    	String text = callHttp(url, HTTP_GET, "");
    	System.out.println("DB Name: " + text);
    }
    
    @Test
    public void testSpeciesList() throws Exception {
        String url = RESTFUL_URL + "speciesList";
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("\nSpecies List:");
        prettyPrintXML(text);
    }
    
    @Test
    public void testQueryAncestors() throws Exception {
        String url = RESTFUL_URL + "queryEventAncestors/69019";
        System.out.println(url);
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("queryAncestors (4 branches for 69019): \n");
        prettyPrintXML(text);
    }

//    @Test
//    public void justATest() throws Exception {
//        String url = "http://reactomerelease.oicr.on.ca/ReactomeGWT/service/analysis/results/combined_analysis_set_http_7080_10_15_04_2013_13_27_25/expression_analysis";
//        String text = callHttp(url, HTTP_GET, "");
//        System.out.println(text);
//    }
    
    @Test
    public void testQueryById() throws Exception {
        // Check a pathway instance
        String url = RESTFUL_URL + "queryById/Pathway/6799604";
        String text = callHttp(url,
                HTTP_GET,
                "");
        System.out.println("Output from querybyid 6799604:");
        prettyPrintXML(text);
        
        // Test new ControlReferences
        url = RESTFUL_URL + "queryById/CatalystActivityReference/9642394";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput for a CatalystActivityReference:");
        prettyPrintXML(text);
        
        url = RESTFUL_URL + "queryById/RegulationReference/9641210";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput for a RegulationReference:");
        prettyPrintXML(text);
        
        // A reaction has both CatalystActivityReferene and RegulationReference
        url = RESTFUL_URL + "queryById/Reaction/893596";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput for a Reaction having both catalystActivityReference and regulationReference:");
        prettyPrintXML(text);
        
        // Check label in a PsiMod
        url = RESTFUL_URL + "queryById/PsiMod/448174";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput for a PsiMod:");
        prettyPrintXML(text);
        
        if (true)
            return;
        
        //Test for weird characters
        url = RESTFUL_URL + "queryById/DatabaseIdentifier/8870797";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput from queryById/DatabaseIdentifier/8870797:");
        prettyPrintXML(text);

        
        // For PE
        url = RESTFUL_URL + "queryById/DatabaseObject/418785";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput from queryById for 418785:");
        prettyPrintXML(text);
        
        // Check for ReferenceGeneProduct
        url = RESTFUL_URL + "queryById/ReferenceGeneProduct/155554";
        text = callHttp(url, HTTP_GET, "");
        prettyPrintXML(text);
        
        // Check a Requirement
        url = RESTFUL_URL + "queryById/Regulation/449175";
        text = callHttp(url, HTTP_GET, "");
        prettyPrintXML(text);
        
//        /// Another pathway
//        // Check a pathway instance
//        url = RESTFUL_URL + "querybyid/Pathway/198323";
//        text = callHttp(url,
//                HTTP_GET,
//                "");
//        System.out.println("Output from querybyid pathway 198323:\n");
//        prettyPrintXML(text);
//        // A reaction has hasMember value
//        url = RESTFUL_URL + "querybyid/Reaction/199299";
//        text = callHttp(url,
//                        HTTP_GET,
//                        "");
//        System.out.println("Output from querybyid reaction 199299:\n");
//        prettyPrintXML(text);
//        // Check a reaction instance
//        url = RESTFUL_URL + "querybyid/Person/69125";
//        text = callHttp(url, HTTP_GET, url);
//        System.out.println("Output from query reaction 69125:");
//        prettyPrintXML(text);
//        // Check another reaction
//        url = RESTFUL_URL + "querybyid/Reaction/451345";
//        text = callHttp(url, HTTP_GET, url);
//        System.out.println("Output from query reaction 451345:");
//        prettyPrintXML(text);
//        // Check a complex instance
//        url = RESTFUL_URL + "querybyid/Complex/446302";
//        text = callHttp(url, HTTP_GET, url);
//        System.out.println("Output from query complex 446302:");
//        prettyPrintXML(text);
//        // Check a LiteratureReference instance
//        url = RESTFUL_URL + "querybyid/LiteratureReference/69422";
//        text = callHttp(url, HTTP_GET, url);
//        System.out.println("Output from query reaction 69422:");
//        prettyPrintXML(text);
//        // Check a regulation instance
//        url = RESTFUL_URL + "querybyid/PositiveRegulation/111290";
//        text = callHttp(url, HTTP_GET, url);
//        System.out.println("Output from query positiveregulation 111290:");
//        prettyPrintXML(text);
//        //negative regulation instance
//        url = RESTFUL_URL + "querybyid/NegativeRegulation/500284";
//        text = callHttp(url, HTTP_GET, url);
//        System.out.println("Output from query negativeregulation 500284:");
//        prettyPrintXML(text);
//        
//        // Check based on stableId
        url = RESTFUL_URL + "queryById/Pathway/REACT_578";
        text = callHttp(url, HTTP_GET, url);
        System.out.println("Output from query pathway apoptosis REACT_578:");
        prettyPrintXML(text);
        
        url = RESTFUL_URL + "queryById/Reaction/REACT_21342";
        text = callHttp(url, HTTP_GET, url);
        System.out.println("Output from query reaction REACT_21342:");
        prettyPrintXML(text);
        
        // Check for a Summation
        url = RESTFUL_URL + "queryById/Summation/450983";
        text = callHttp(url, HTTP_GET, url);
        System.out.println("Output for query summation 450983:");
        prettyPrintXML(text);
        
        // Check a reaction with regulation
        url = RESTFUL_URL + "queryById/ReactionlikeEvent/2002440";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("Output for a reaction having regulators:");
        prettyPrintXML(text);
        
        // Check a reaction with entityFunctionalStatus and entityOnOtherCell
        url = RESTFUL_URL + "queryById/ReactionlikeEvent/2220944";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("Output for a reaction having entityFunctionalStauts and entityOnOtherCell:");
        prettyPrintXML(text);
        
        // Check EntityFunctionalStatus
        url = RESTFUL_URL + "queryById/EntityFunctionalStatus/2362350";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("Output for an EntityFunctionalStatus:");
        prettyPrintXML(text);
        
        // Check for InstanceEdit
//        String url = RESTFUL_URL + "querybyid/InstanceEdit/168269";
//        String text = callHttp(url, HTTP_GET, url);
//        System.out.println("Output from query InstanceEdit 168267:");
//        prettyPrintXML(text);
        
        // Check a PathwayDiagram instance
        url = RESTFUL_URL + "queryById/PathwayDiagram/2162173";
        text = callHttp(url, HTTP_GET, "");
        prettyPrintXML(text);
        
        // A reaction uses Domain
        url = RESTFUL_URL + "queryById/ReactionlikeEvent/74707";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("Reaction has two requiredInputComponent of Domain:");
        prettyPrintXML(text);
        
        // A new FragmentReplacedModification
        url = RESTFUL_URL + "queryById/FragmentReplacedModification/3769430";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("FragmentReplacedModification:");
        prettyPrintXML(text);
    }

    @Test
    public void testQueryByIds() throws Exception {
        String url = RESTFUL_URL + "queryByIds/Pathway";
        String text = callHttp(url,
                HTTP_POST,
                "ID=109607,109606,75153,169911");
        System.out.println("Output from querybyids:\n");
        prettyPrintXML(text);
        
        url = RESTFUL_URL + "queryByIds/EntityWithAccessionedSequence";
        text = callHttp(url, HTTP_POST, "ID=418785");
        System.out.println("\nOutput from another queryByIds:");
        prettyPrintXML(text);
    }

    @Test
    public void testListByQuery() throws Exception {
        String url = RESTFUL_URL + "listByQuery/DatabaseObject";
        String text = callHttp(url,
                HTTP_POST,
                "DB_ID=114298");
        System.out.println("Output from listByQuery for ID=114298:\n");
        prettyPrintXML(text);
        
        // Check a query based on name
        url = RESTFUL_URL + "listByQuery/Pathway";
        text = callHttp(url,
                HTTP_POST,
                "name=Apoptosis");
        System.out.println("Output from listByQuery for name=Apotosis:\n");
        prettyPrintXML(text);
        
        url = RESTFUL_URL + "listByQuery/Pathway";
        text = callHttp(url, HTTP_POST, "species=48887");
//        text = callHttp(url, HTTP_POST, "species=3042819");
        System.out.println("\nOutput from listByQuery for species=48887:\n");
        prettyPrintXML(text);
        
        url = RESTFUL_URL + "listByQuery/PathwayDiagram";
        text = callHttp(url, HTTP_POST, "representedPathway=169911");
        System.out.println("\nOutput from listByQuery for pathway=169911:\n");
        prettyPrintXML(text);
    }
    
    @Test
    public void testListByName() throws Exception {
        String url = RESTFUL_URL + "listByName/Event/Apoptosis/Homo+sapiens";
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("Query for Apoptosis for human");
        prettyPrintXML(text);
        
        url = RESTFUL_URL + "listByName/Event/Apoptosis/null";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("Query for Apoptosis for all species");
        prettyPrintXML(text);
    }
    
    @Test
    public void testqueryEventNameAndSummation() throws Exception {
        // Get a list of events first
        String url = RESTFUL_URL + "listByName/Event/Apoptosis/null";
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("Query for Apoptosis for all species");
        prettyPrintXML(text);
        // Query event's species and summation.
        SAXBuilder builder = new SAXBuilder();
        StringReader reader = new StringReader(text);
        Document document = builder.build(reader);
        Element root = document.getRootElement();
        StringBuilder ids = new StringBuilder();
        for (Object child : root.getChildren()) {
            Element childElm = (Element) child;
            String dbId = childElm.getChildText("dbId");
            ids.append(dbId).append(",");
        }
        ids.deleteCharAt(ids.length() - 1);
        url = RESTFUL_URL + "queryEventSpeciesAndSummation";
        text = callHttp(url, HTTP_POST, ids.toString());
        System.out.println("\nQuery for species and summation for Apoptosis:\n");
        prettyPrintXML(text);
    }

    @Test
    public void testListPathwayParticipants() throws Exception {
//        String url = RESTFUL_URL + "pathwayParticipants/75157";
        String url = RESTFUL_URL + "pathwayParticipants/109581";
        String text = callHttp(url,
                HTTP_GET,
                "");
        System.out.println("\nOutput from pathwayParticipants:");
        prettyPrintXML(text);
        
        url = RESTFUL_URL + "pathwayParticipants/168256";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput from pathwayParticipant for 168256:");
        prettyPrintXML(text);
    }
    
    @Test
    public void testTopLevelPathways() throws Exception {
        String url = RESTFUL_URL + "topLevelPathways";
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("Output from topLevelPathways:\n");
        prettyPrintXML(text);
    }
    
    // This test takes too long time to generate. Disable for the time being so that
    // we can do a suite of tests together.
//    @Test
//    public void testGeneSetInXML() throws Exception {
//        String url = RESTFUL_URL + "GeneSetInXML";
//        String text = callHttp(url, HTTP_GET, "");
//        System.out.println("\nOutput from GeneSetInXML:");
//        prettyPrintXML(text);
//    }
    
    @Test
    public void testDetailedView() throws Exception {
        String url = RESTFUL_URL + "detailedView/EntityWithAccessionedSequence/66212";
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput from detailedView:");
        prettyPrintXML(text);
        long time1 = System.currentTimeMillis();
        url = RESTFUL_URL + "detailedView/DatabaseObject/29370";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput from detailedView:");
        prettyPrintXML(text);
        long time2 = System.currentTimeMillis();
        System.out.println("Time: " + (time2 - time1));
        // Check entityFunctionalStatus
        url = RESTFUL_URL + "detailedView/DatabaseObject/2219536";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput from detailedView:");
        prettyPrintXML(text);
        // Check entityOnOtherCells
        url = RESTFUL_URL + "detailedView/DatabaseObject/2220944";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput from detailedView:");
        prettyPrintXML(text);
        // Check a pathway
        url = RESTFUL_URL + "detailedView/DatabaseObject/109581";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput from detailedView:");
        prettyPrintXML(text);
        // An event has regulation
        url = RESTFUL_URL + "detailedView/DatabaseObject/5262606";
        text = callHttp(url,
                HTTP_GET,
                "");
        System.out.println("Output from detailedView 5262606:\n");
        prettyPrintXML(text);
    }
    
//    
//    @Test
//    public void testExportParticipatingMolecules() throws Exception {
//        String url = RESTFUL_URL + "participatingMolecules/export/109581";
//        String text = callHttp(url, HTTP_GET, "");
//        System.out.println(text);
//    }

//    @Test
//    public void testListTopLevelPathways() throws Exception {
//        String url = RESTFUL_URL + "toplevelpathways";
//        String text = callHttp(url,
//                HTTP_GET,
//                "");
//        System.out.println("Output from toplevelpathways:\n");
//        prettyPrintXML(text);
//    }

    @Test
	public void testGetContainedEventIds() throws Exception {
	    String url = RESTFUL_URL + "getContainedEventIds/109581";
	    System.out.println(url);
	    String text = callHttp(url, HTTP_GET, "");
	    System.out.println("Contained Event DB_IDs for Apoptosis: " + text.length() + "\n");
	    prettyPrintXML(text);
	}

	@Test
    public void testQueryPathwaysforEntities() throws Exception {
        String url = RESTFUL_URL + "pathwaysForEntities";
//        String text = callHttp(url,
//                HTTP_POST,
//                "ID=114298"); 
//        System.out.println("Output from pathwaysforentities:\n");
//        prettyPrintXML(text);
        // CDC2 - 170075, HUS1 - 176374, MCM2 - 68557
        String text = callHttp(url,
                HTTP_POST,
                "ID=170075,176374,68557"); 
        System.out.println("Output from pathwaysforentities:\n");
        prettyPrintXML(text);
    }
    
    @Test
    public void testComplexSubunits() throws Exception {
        String url = RESTFUL_URL + "complexSubunits/75114";
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("Output from complex subunits (4 should be returned):");
        prettyPrintXML(text);
        // A complex has a DefinedSet as its subunit
        url = RESTFUL_URL + "complexSubunits/188362";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nOutput from complex having a DefinedSet as its subunit:");
        prettyPrintXML(text);     
        // Complex has SimpleEntity 
        url = RESTFUL_URL + "complexSubunits/2026019";
        text = callHttp(url, HTTP_GET, "");
        System.out.println("\nComplex containing SimpleEntity:");
        prettyPrintXML(text);
    }
    
    @Test
    public void testQueryReferences() throws Exception {
        String url = RESTFUL_URL + "queryReferences/140547";
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("Output from queryReferences:\n");
        prettyPrintXML(text);
    }
    
    @Test
    public void testPSICQUICServiceList() throws Exception {
        String url = RESTFUL_URL + "psicquicList";
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("List PSICQUIC services:");
        prettyPrintXML(text);
    }
    
    @Test
    public void testPSICQUICInteractionsQuery() throws Exception {
        Long[] dbIds = new Long[] {
                193937L,
                66212L, // EWAS FASL
                209799L, // SmallMolecule: Cu for MatrixDB.
                2658043L,
                375987L, //ABL1
                418812L, // CandidateSet DAPKs [Cytosol]
                418812L,
                418842L, // Complex: Unc5B with death domain:DAPK
                169911L // Pathway: Regulation of Apoptosis
        };
        String[] serviceNames = new String[] {
                "IntAct",
                "Reactome-FIs",
                //serviceName = "MINT";
                "MatrixDB",
                "MatrixDB",
                "ChEMBL",
                "IntAct",
                "MatrixDB",
                "IntAct",
                "IntAct"
        };
        for (int i = 0; i < dbIds.length; i++) {
//            if (i != (dbIds.length - 1))
//                continue;
            Long dbId = dbIds[i];
            String serviceName = serviceNames[i];
            String url = RESTFUL_URL + "psiquicInteractions/" + dbId + "/" + serviceName;
            String text = callHttp(url, HTTP_GET, "");
            System.out.println("\nQuery interactions for " + dbId + " in " + serviceName + ":");
            prettyPrintXML(text);
        }
    }
    
    @Test
    public void testExportPSICQUICInteractions() throws Exception {
        Long[] dbIds = new Long[] {
                193937L,
                66212L, // EWAS FASL
                209799L, // SmallMolecule: Cu for MatrixDB.
                2658043L,
                375987L, //ABL1
                169911L // Pathway: Regulation of Apoptosis
        };
        String[] serviceNames = new String[] {
                "IntAct",
                "Reactome-FIs",
                //serviceName = "MINT";
                "MatrixDB",
                "MatrixDB",
                "ChEMBL",
                "IntAct"
        };
        for (int i = 0; i < dbIds.length; i++) {
            if (i != dbIds.length - 1)
                continue;
            Long dbId = dbIds[i];
            String serviceName = serviceNames[i];
            String url = RESTFUL_URL + "exportPsiquicInteractions/" + dbId + "/" + serviceName;
            String text = callHttp(url, HTTP_GET, "");
            System.out.println("\nQuery interactions for " + dbId + " in " + serviceName + ":");
            prettyPrintXML(text);
        }
    }
    
    /**
     * This method is used to test a new user-submit PSICQUIC service.
     * @throws Exception
     */
    @Test
    public void testNewPSICQUIC() throws Exception {
        // Reactome FI service
        String psicquicUrl = "http://www.ebi.ac.uk/Tools/webservices/psicquic/reactome-fi/webservices/current/search/";
        String url = RESTFUL_URL + "submitNewPSICQUIC";
        String rtn = callHttp(url, 
                              HTTP_POST, 
                              psicquicUrl);
        System.out.println("registed name: " + rtn);
        
        // Test uploaded interaction
        Long dbId = 66212L; // EWAS FASL
        url = RESTFUL_URL + "psiquicInteractions/" + dbId + "/" + rtn;
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("\nQuery interactions for " + dbId + " in " + rtn + " based on new PSICQUIC:");
        prettyPrintXML(text);
    }
    
    /**
     * This test method is based on this web page: 
     * http://puspendu.wordpress.com/2012/08/23/restful-webservice-file-upload-with-jersey/
     * @throws Exception
     */
    @Test
    public void testUploadInteractionFile() throws Exception {
        // Test for gene-gene interactions in gene names
        String fileName = "FIsInGene_071012.txt";
        String fileType = "gene";
        
        String rtn = uploadFile(fileName, fileType);
        System.out.println("Return: " + rtn);
        testUploadInteractionFile(rtn);
        
        // Test for protein-protein interactions
        fileName = "FIs_Reactome.txt";
        fileType = "protein";
        rtn = uploadFile(fileName, fileType);
        testUploadInteractionFile(rtn);
        
        // Test for PSI-MI tab
        fileName = "FIsInMITTab.txt";
        fileType = "psimitab";
        rtn = uploadFile(fileName, fileType);
        testUploadInteractionFile(rtn);
    }

    private void testUploadInteractionFile(String rtn) throws IOException, JDOMException {
        // Test uploaded interaction
        Long dbId = 66212L; // EWAS FASL
        String url = RESTFUL_URL + "psiquicInteractions/" + dbId + "/" + rtn;
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("\nQuery interactions for " + dbId + " in " + rtn + " based on gene-gene interactions:");
        prettyPrintXML(text);
        System.out.println("\nExport interactions");
        url = RESTFUL_URL + "exportPsiquicInteractions/" + dbId + "/" + rtn;
        text = callHttp(url, HTTP_GET, "");
        prettyPrintXML(text);
    }

    private String uploadFile(String fileName, String fileType)
            throws UnsupportedEncodingException, IOException,
            ClientProtocolException {
        org.apache.http.client.HttpClient httpClient = new DefaultHttpClient();
        FileBody fileContent = new FileBody(new File(fileName));
        MultipartEntity reqEntity = new MultipartEntity();
        reqEntity.addPart("file", fileContent);
        StringBody fileTypeContent = new StringBody(fileType);
        reqEntity.addPart("fileType", fileTypeContent);
        String url = RESTFUL_URL + "uploadInteractionFile";
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(reqEntity);
        org.apache.http.HttpResponse respone = httpClient.execute(httpPost);
        HttpEntity resEntity = respone.getEntity();
        String rtn = readMethodReturn(resEntity.getContent());
        return rtn;
    }
        
    @Test
    public void testReferenceEntity() throws Exception {
        Long dbId = 66212L; // EWAS FASL
        dbId = 75975L;
        dbId = 109607L;
        String url = RESTFUL_URL + "referenceEntity/" + dbId;
        String text = callHttp(url, HTTP_GET, "");
        System.out.println("Query ReferenceEntity for " + dbId + ":");
        prettyPrintXML(text);
    }

    @Test
    public void testDoPathwayMapping() throws Exception {

    }

    @Test
    public void testDoPathwayEnrichmentAnalysis() throws Exception {
    }

    private void prettyPrintXML(String xml) throws JDOMException, IOException {
//        System.out.println(xml);
        // Check if it is an XML
        if (xml.startsWith("<?xml")) {
            SAXBuilder builder = new SAXBuilder();
            Reader reader = new StringReader(xml);
            Document doc = builder.build(reader);
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, System.out);
        }
        else {
//            FileUtilities fu = new FileUtilities();
//            fu.setOutput("tmp.txt");
//            fu.printLine(xml);
//            fu.close();
            System.out.println(xml);
        }
    }

    private String callHttp(String url,
                            String type,
                            String query) throws IOException {
        HttpMethod method = null;
        HttpClient client = null;
        if (type.equals(HTTP_POST)) {
            method = new PostMethod(url);
            client = initializeHTTPClient((PostMethod) method, query);
        } else {
            method = new GetMethod(url); // Default
            client = new HttpClient();
        }
        method.setRequestHeader("Accept", "text/plain, application/xml");
//        method.setRequestHeader("Accept", "application/json");
        int responseCode = client.executeMethod(method);
        if (responseCode == HttpStatus.SC_OK) {
            InputStream is = method.getResponseBodyAsStream();
            return readMethodReturn(is);
        } else {
            System.err.println("Error from server: " + method.getResponseBodyAsString());
            System.out.println("Response code: " + responseCode);
            throw new IllegalStateException(method.getResponseBodyAsString());
        }
    }

    private String readMethodReturn(InputStream is) throws IOException {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader reader = new BufferedReader(isr);
        StringBuilder builder = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null)
            builder.append(line).append("\n");
        reader.close();
        isr.close();
        is.close();
        // Remove the last new line
        String rtn = builder.toString();
        // Just in case an empty string is returned
        if (rtn.length() == 0)
            return rtn;
        return rtn.substring(0, rtn.length() - 1);
    }

    private HttpClient initializeHTTPClient(PostMethod post, String query) throws UnsupportedEncodingException {
        RequestEntity entity = new StringRequestEntity(query, "text/plain", "UTF-8");
        post.setRequestEntity(entity);
        post.setRequestHeader("Accept", "application/JSON, application/XML, text/plain");
//        post.setRequestHeader("Accept", "application/json");
        HttpClient client = new HttpClient();
        return client;
    }
}
