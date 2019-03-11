package org.reactome.restfulapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
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
//    private final static String RESTFUL_URL = "http://reactomecurator.oicr.on.ca:8080/ReactomeRESTfulAPI/RESTfulWS/";
    private final static String RESTFUL_URL = "http://localhost:8080/ReactomeRESTfulAPI/RESTfulWS/";
//    private final static String RESTFUL_URL = "http://reactomedev.oicr.on.ca:7080/ReactomeRESTfulAPI/RESTfulWS/";
    
    @Test
    public void testBioPaxExporter() throws Exception {
        String url = RESTFUL_URL + "biopaxExporter/Level3/201681";
        System.out.println(url);
        String text = callHttp(url,
                HTTP_GET,
                "");
        System.out.println("Output from biopaxexporter (level3):\n");
        System.out.println(text);
        
        url = RESTFUL_URL + "biopaxExporter/Level2/69615";
        System.out.println(url);
        text = callHttp(url,
                HTTP_GET,
                "");
        System.out.println("\nOutput from biopaxexporter (level2):\n");
        System.out.println(text);
    }
    
    @Test
    public void testGetDBName() throws Exception {
    	String url = RESTFUL_URL + "getDBName";
    	String text = callHttp(url, HTTP_GET, "");
    	System.out.println("DB Name: " + text);
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
