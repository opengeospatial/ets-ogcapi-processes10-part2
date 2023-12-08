package org.opengis.cite.ogcapiprocesses20.docker;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
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
import org.opengis.cite.ogcapiprocesses20.CommonFixture;
import org.opengis.cite.ogcapiprocesses20.SuiteAttribute;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	private String echoProcessId;

	private String ogcApplicationPackage;

	private String ogcApplicationPackageContent;

	private URL getProcessListURL;
    
    private static String dpValue="http://www.opengis.net/def/exceptions/ogcapi-processes-2/1.0/duplicated-process";

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
			ogcApplicationPackage= (String) testContext.getSuite().getAttribute( SuiteAttribute.URL_APP_PKG.getName() );
		} catch (MalformedURLException | ResolutionException | ValidationException e) {	
			
			Assert.fail("Could not set up endpoint: " + processListEndpointString + ". Exception: " + e.getLocalizedMessage());
		}
	}

	public void fetchOGCApplicationPackage() {
		try {
			// TODO: how to get the file path from the class?
			File file=new File("/root/te_base/scripts/ogcapi-processes-2.0/1.0/testdata/ogcapppkg.json");
			InputStream inputStream = new FileInputStream(file);
			StringBuilder resultStringBuilder = new StringBuilder();
			try (BufferedReader br
			= new BufferedReader(new InputStreamReader(inputStream))) {
				String line;
				while ((line = br.readLine()) != null) {
					resultStringBuilder.append(line).append("\n");
				}
			}
			ogcApplicationPackageContent=resultStringBuilder.toString();
			System.out.println(ogcApplicationPackageContent);
		} catch (Exception e) {
			Assert.fail("Failed parsing the default OGC Applicaiton Package "+ogcApplicationPackage+" Exception: "+e.getLocalizedMessage());
		}
	}

	/**
	 * <pre>
	 * Abstract Test 16: /conf/deploy-replace-undeploy/deploy-body-cwl
	 * Test Purpose: Validate that the server support Common Worflow Language encoding.
	 * Requirement: /req/deploy-replace-undeploy/deploy-body-cwl
	 * Test Method: 
	 * |===
	 * TODO: Check additional content
	 * </pre>
	 */
	@Test(description = "Implements Requirement /rec/deploy-replace-undeploy/deploy/body-cwl ", groups = "Deploy-Replace-Undeploy")
	public void testProcessDeploy() {
		final ValidationData<Void> data = new ValidationData<>();
		try {
			this.fetchOGCApplicationPackage();
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost request = new HttpPost(getProcessListURL.toString());
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-Type", "application/ogcapppkg+json");
			request.setEntity(new StringEntity(ogcApplicationPackageContent));
		    this.reqEntity = request;
			HttpResponse httpResponse = client.execute(request);
			Assert.assertTrue(httpResponse.getStatusLine().getStatusCode()==202 || httpResponse.getStatusLine().getStatusCode()==201,
					"Expected 201 or 202 Created when deploying a process "+httpResponse.getStatusLine());
			if(httpResponse.getStatusLine().getStatusCode()==201){
				Header[] headers = httpResponse.getHeaders("Location");
				Assert.assertTrue(headers.length==1, "Expected a single Location header");
				String[] tmpString = headers[0].getValue().split("/processes/");
				echoProcessId = tmpString[1];
			}
			httpResponse = client.execute(request);
			Assert.assertTrue(httpResponse.getStatusLine().getStatusCode()==409,
					"Expected 409 Conflict when deploying a process twice");
			StringWriter writer = new StringWriter();
			String encoding = StandardCharsets.UTF_8.name();
			IOUtils.copy(httpResponse.getEntity().getContent(), writer, encoding);
			String responsePayload = writer.toString();
			JsonNode responseNode = new ObjectMapper().readTree(responsePayload);
			String arrayNode =  responseNode.get("type").asText();
			Assert.assertTrue(arrayNode.equals(dpValue), "Expected "+dpValue+" when deploying a process twice");
		} catch (Exception e) {
			Assert.fail("Unable to deploy the process "+getProcessListURL.toString()+" Exception: "+e.getLocalizedMessage());
		}
	}

	/**
	 * <pre>
	 * Abstract Test 18: /conf/deploy-replace-undeploy/replace-body-cwl
	 * Test Purpose: Validate that the server support Common Worflow Language encoding.
	 * Requirement: /req/deploy-replace-undeploy/deploy-body-cwl
	 * Test Method: 
	 * |===
	 * TODO: Check additional content
	 * </pre>
	 */
	@Test(description = "Implements Requirement /rec/deploy-replace-undeploy/deploy/body-cwl ", groups = "Deploy-Replace-Undeploy", dependsOnMethods = { "testProcessDeploy" })
	public void testProcessReplace() {
		final ValidationData<Void> data = new ValidationData<>();
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPut request = new HttpPut(getProcessListURL.toString()+"/"+echoProcessId);
			request.setHeader("Accept", "application/json");
			request.setHeader("Content-Type", "application/ogcapppkg+json");
			request.setEntity(new StringEntity(ogcApplicationPackageContent));
		    this.reqEntity = request;
			HttpResponse httpResponse = client.execute(request);
			Assert.assertTrue(httpResponse.getStatusLine().getStatusCode()==204,
					"Expected 204 when replacing a process "+httpResponse.getStatusLine());
			HttpDelete request1 = new HttpDelete(getProcessListURL.toString()+"/"+echoProcessId);
			request.setHeader("Accept", "application/json");
			client.execute(request1);
		} catch (Exception e) {
			Assert.fail("Unable to replace the process "+getProcessListURL.toString()+" Exception: "+e.getLocalizedMessage());
		}
	}

}
