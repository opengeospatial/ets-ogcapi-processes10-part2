package org.opengis.cite.ogcapiprocesses20part2.dru;

import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openapi4j.core.exception.ResolutionException;
import org.openapi4j.core.validation.ValidationException;
import org.openapi4j.operation.validator.validation.OperationValidator;
import org.openapi4j.parser.OpenApi3Parser;
import org.openapi4j.parser.model.v3.OpenApi3;
import org.openapi4j.parser.model.v3.Operation;
import org.openapi4j.parser.model.v3.Path;
import org.openapi4j.schema.validator.ValidationData;
import org.opengis.cite.ogcapiprocesses20part2.CommonFixture;
import org.opengis.cite.ogcapiprocesses20part2.SuiteAttribute;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 *
 * A.2.5. List of processes {root}/processes
 *
 * @author <a href="mailto:b.pross@52north.org">Benjamin Pross </a>
 */
public class Default extends CommonFixture {

	private static final String OPERATION_ID = "getProcesses";

	private OpenApi3 openApi3;
	
	private String getProcessListPath = "/processes";

	private String getAlternateProcessListPath = null;

	private String echoProcessId;
	
	private URL getProcessListURL;
    
    private static String uctValue="http://www.opengis.net/def/exceptions/ogcapi-processes-2/1.0/unsupported-media-type";
	private static String ipValue="http://www.opengis.net/def/exceptions/ogcapi-processes-2/1.0/immutable-process";

	@BeforeClass
	public void setup(ITestContext testContext) {		
		String processListEndpointString = rootUri.toString() + getProcessListPath;		
		try {		
			openApi3 = new OpenApi3Parser().parse(specURI.toURL(), false);
			addServerUnderTest(openApi3);
		    final Path path = openApi3.getPathItemByOperationId(OPERATION_ID);
		    final Operation operation = openApi3.getOperationById(OPERATION_ID);
		    new OperationValidator(openApi3, path, operation);
		    getProcessListURL = new URL(processListEndpointString);
			echoProcessId = (String) testContext.getSuite().getAttribute( SuiteAttribute.ECHO_PROCESS_ID.getName() );
		} catch (MalformedURLException | ResolutionException | ValidationException e) {	
			
			Assert.fail("Could not set up endpoint: " + processListEndpointString + ". Exception: " + e.getLocalizedMessage());
		}
	}

	public void fetchLandingPageList() {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpUriRequest request = new HttpGet(rootUri.toString());
			request.setHeader("Accept", "application/json");
		    this.reqEntity = request;
			HttpResponse httpResponse = client.execute(request);
			StringWriter writer = new StringWriter();
			String encoding = StandardCharsets.UTF_8.name();
			IOUtils.copy(httpResponse.getEntity().getContent(), writer, encoding);
			String responsePayload = writer.toString();
			JsonNode responseNode = new ObjectMapper().readTree(responsePayload);
			ArrayNode arrayNode = (ArrayNode) responseNode.get("links");
			for(int iCnt=0;iCnt<arrayNode.size();iCnt++){
				if(arrayNode.get(iCnt).get("rel").asText()=="http://www.opengis.net/def/rel/ogc/1.0/processes")
					this.getAlternateProcessListPath=arrayNode.get(iCnt).get("href").asText();
			}
		} catch (Exception e) {
			Assert.fail("Default.testProcessList(): An exception occured when trying to retrieve the processes list from "+getProcessListURL.toString());
		}
	}

	public void validateStaticIndicator(String urlString){
		try{
			HttpClient client = HttpClientBuilder.create().build();
			HttpUriRequest request = new HttpGet(urlString);
			request.setHeader("Accept", "application/json");
			this.reqEntity = request;
			HttpResponse httpResponse = client.execute(request);
			StringWriter writer = new StringWriter();
			String encoding = StandardCharsets.UTF_8.name();
			IOUtils.copy(httpResponse.getEntity().getContent(), writer, encoding);
			String responsePayload = writer.toString();
			JsonNode responseNode = new ObjectMapper().readTree(responsePayload);
			ArrayNode arrayNode = (ArrayNode) responseNode.get("processes");
			Assert.assertTrue(arrayNode.size()>0,"No processes listed at "+getProcessListURL.toString());
			for(int iCnt=0;iCnt<arrayNode.size();iCnt++){
				Assert.assertTrue(arrayNode.get(iCnt).get("mutable").isBoolean(),
						"Process "+arrayNode.get(iCnt).get("id")+" has no boolean mutable property");
			}
		} catch (Exception e) {
			Assert.fail("Error occured parsing static-indicator from "+getProcessListURL.toString()+". Reported exception : "+e.getLocalizedMessage());
		}
	}
	/**
	 * <pre>
	 * Abstract Test 8: /conf/deploy-replace-undeploy/static-indicator
	 * Test Purpose: Validate that information about the processes contain the boolean mutable property.
	 * Requirement: /req/deploy-replace-undeploy/static-indicator
	 * Test Method: 
	 * |===
	 * TODO: Check additional content
	 * </pre>
	 */
	@Test(description = "Implements Requirement /req/deploy-replace-undeploy/static-indicator ", groups = "Deploy-Replace-Undeploy")
	public void testProcessesImmutable() {
		final ValidationData<Void> data = new ValidationData<>();
		try {
			validateStaticIndicator(getProcessListURL.toString());
			this.fetchLandingPageList();
			if(this.getAlternateProcessListPath!=null){
				validateStaticIndicator(this.getAlternateProcessListPath);
			}
		} catch (Exception e) {
			Assert.fail("Default.testProcessList(): An exception occured when trying to retrieve the processes list from "+getProcessListURL.toString());
		}
	}

	public void validateUnsupportedPostUrl(String urlString,String methodString){
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpUriRequest request = methodString.equals("POST") ? new HttpPost(urlString): new HttpPut(urlString);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-Type", "text/plain");
			this.reqEntity = request;
			HttpResponse httpResponse = client.execute(request);

			// 1. Validate that a document was returned with an HTTP status code of 415.
			Assert.assertTrue(httpResponse.getStatusLine().getStatusCode()==415,
				"Status code is different from 415: "+httpResponse.getStatusLine().getStatusCode());

			StringWriter writer = new StringWriter();
			String encoding = StandardCharsets.UTF_8.name();
			IOUtils.copy(httpResponse.getEntity().getContent(), writer, encoding);
			String responsePayload = writer.toString();
			JsonNode responseNode = new ObjectMapper().readTree(responsePayload);

			// 3. Validate that the document contains the exception type “http://www.opengis.net/def/exceptions/ogcapi-processes-2/1.0/unsupported-content-type”.
			Assert.assertTrue(responseNode.get("type").asText().equals(Default.uctValue),
				"The exception type is not "+Default.uctValue+" but "+responseNode.get("type").asText()+" was found");

		} catch (Exception e) {
			Assert.fail("An exception occured when trying to validate Unsupported Content-Type "+getProcessListURL.toString()+":"+e.getLocalizedMessage());
		}
	}
	/**
	 * <pre>
	 * Abstract Test 4: /conf/deploy-replace-undeploy/deploy-unsupported-content-type
	 * Test Purpose: Validate that the server return a 415 status code with a relevant exception
	 * Requirement: /req/deploy-replace-undeploy/deploy-unsupported-content-type
	 * Test Method: 
	 * |===
	 * TODO: Check additional content
	 * </pre>
	 */
	@Test(description = "Implements Requirement /req/deploy-replace-undeploy/deploy/unsupported-content-type ", groups = "Deploy-Replace-Undeploy")
	public void testPostUnsupportedContentType() {
		final ValidationData<Void> data = new ValidationData<>();
		try {
			this.validateUnsupportedPostUrl(getProcessListURL.toString(),"POST");
			this.fetchLandingPageList();
			if(this.getAlternateProcessListPath!=null){
				this.validateUnsupportedPostUrl(this.getAlternateProcessListPath,"POST");
			}
		} catch (Exception e) {
			Assert.fail("An exception occured using the following URL: "+getProcessListURL.toString()+". Repported exception :"+e.getLocalizedMessage());
		}
	}

	/**
	 * <pre>
	 * Abstract Test 7: /conf/deploy-replace-undeploy/deploy-unsupported-content-type
	 * Test Purpose: Validate that the server return a 415 status code with a relevant exception
	 * Requirement: /req/deploy-replace-undeploy/deploy-unsupported-content-type
	 * Test Method: 
	 * |===
	 * TODO: Check additional content
	 * </pre>
	 */
	@Test(description = "Implements Requirement /req/deploy-replace-undeploy/replace/unsupported-content-type ", groups = "Deploy-Replace-Undeploy")
	public void testPutUnsupportedContentType() {
		final ValidationData<Void> data = new ValidationData<>();
		try {
			this.validateUnsupportedPostUrl(getProcessListURL.toString()+"/"+echoProcessId,"PUT");
			this.fetchLandingPageList();
			if(this.getAlternateProcessListPath!=null){
				this.validateUnsupportedPostUrl(this.getAlternateProcessListPath+"/"+echoProcessId,"PUT");
			}
		} catch (Exception e) {
			Assert.fail("An exception occured using the following URL: "+getProcessListURL.toString()+"/"+echoProcessId+". Repported exception :"+e.getLocalizedMessage());
		}
	}

	public String fetchDeployableProcess(boolean isMutable) {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpUriRequest request = new HttpGet(rootUri.toString()+"/processes");
			request.setHeader("Accept", "application/json");
		    this.reqEntity = request;
			HttpResponse httpResponse = client.execute(request);
			StringWriter writer = new StringWriter();
			String encoding = StandardCharsets.UTF_8.name();
			IOUtils.copy(httpResponse.getEntity().getContent(), writer, encoding);
			String responsePayload = writer.toString();
			JsonNode responseNode = new ObjectMapper().readTree(responsePayload);
			ArrayNode arrayNode = (ArrayNode) responseNode.get("processes");
			for(int iCnt=0;iCnt<arrayNode.size();iCnt++){
				System.out.println(isMutable);
				System.out.println(arrayNode.get(iCnt).get("mutable").asBoolean());
				System.out.println(arrayNode.get(iCnt).get("id").asText());
				if((isMutable && arrayNode.get(iCnt).get("mutable").asBoolean())
					||
					(!isMutable && !arrayNode.get(iCnt).get("mutable").asBoolean()))
					return arrayNode.get(iCnt).get("id").asText();
			}
			System.out.println("No process found with mutable="+isMutable);
			return null;
		} catch (Exception e) {
			Assert.fail("An exception occured when searching for a process with the mutable property "+isMutable+" from "+getProcessListURL.toString()+". Reported exception: "+e.getLocalizedMessage());
			return null;
		}
	}	
	/**
	 * <pre>
	 * Abstract Test 9: /conf/deploy-replace-undeploy/undeploy-response
	 * Test Purpose: Validate that the server return a 204 status code when removing a mutable process
	 * Requirement: /req/deploy-replace-undeploy/undeploy-response
	 * Test Method: 
	 * |===
	 * TODO: Check additional content
	 * </pre>
	 */
	@Test(description = "Implements Requirement /req/deploy-replace-undeploy/undeploy/response ", groups = "Deploy-Replace-Undeploy")
	public void testUndeployResponse() {
		System.out.println("Start testUndeployResponse() ...");
		final ValidationData<Void> data = new ValidationData<>();
		String processId=null;
		try {
			processId=this.fetchDeployableProcess(true);
			System.out.println("Process id: "+processId);
			HttpClient client = HttpClientBuilder.create().build();
			HttpUriRequest request = new HttpDelete(getProcessListURL.toString()+"/"+processId);
			request.setHeader("Accept", "application/json");
			this.reqEntity = request;
			HttpResponse httpResponse = client.execute(request);
			// 1. Validate that a document was returned with an HTTP status code of 204.
			Assert.assertTrue(httpResponse.getStatusLine().getStatusCode()==204,
				"Status code is different from 204: "+httpResponse.getStatusLine());

		} catch (Exception e) {
			Assert.fail("An exception occured using the following URL: "+getProcessListURL.toString()+"/"+processId+". Repported exception :"+e.getLocalizedMessage());
		}
	}
	/**
	 * <pre>
	 * Abstract Test 10: /conf/deploy-replace-undeploy/undeploy-response-immutable
	 * Test Purpose: Validate that the server return a 403 status code when removing an immutable process
	 * Requirement: /req/deploy-replace-undeploy/undeploy-response-immutable
	 * Test Method: 
	 * |===
	 * TODO: Check additional content
	 * </pre>
	 */
	@Test(description = "Implements Requirement /req/deploy-replace-undeploy/undeploy/response ", groups = "Deploy-Replace-Undeploy")
	public void testUndeployResponseImmutable() {
		System.out.println("Start testUndeployResponse() ...");
		final ValidationData<Void> data = new ValidationData<>();
		String processId=null;
		try {
			// 1. From the processes list, pick one process with the mutable attribute set to false
			processId=this.fetchDeployableProcess(false);
			System.out.println("Process id: "+processId);
			HttpClient client = HttpClientBuilder.create().build();
			HttpUriRequest request = new HttpDelete(getProcessListURL.toString()+"/"+processId);
			request.setHeader("Accept", "application/json");
			this.reqEntity = request;
			// 2. Send a DELETE request on the /processes/{processId} where {processId} is set to the process selected in previous step
			HttpResponse httpResponse = client.execute(request);
			// 3. Validate that a document was returned with an HTTP status code of 204.
			Assert.assertTrue(httpResponse.getStatusLine().getStatusCode()==403,
				"Status code is different from 403: "+httpResponse.getStatusLine().getStatusCode());

			StringWriter writer = new StringWriter();
			String encoding = StandardCharsets.UTF_8.name();
			IOUtils.copy(httpResponse.getEntity().getContent(), writer, encoding);
			String responsePayload = writer.toString();
			JsonNode responseNode = new ObjectMapper().readTree(responsePayload);

			// 5. Validate that the document contains the exception type “http://www.opengis.net/def/exceptions/ogcapi-processes-2/1.0/unsupported-content-type”.
			Assert.assertTrue(responseNode.get("type").asText().equals(Default.ipValue),
				"The exception type is not "+Default.ipValue+" but "+responseNode.get("type").asText()+" was found");

		} catch (Exception e) {
			Assert.fail("An exception occured using the following URL: "+getProcessListURL.toString()+"/"+processId+". Repported exception :"+e.getLocalizedMessage());
		}
	}


}
