package org.opengis.cite.ogcapiprocesses20.general;

import static io.restassured.http.Method.POST;
import static io.restassured.http.Method.PUT;
import static io.restassured.http.Method.DELETE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.opengis.cite.ogcapiprocesses20.EtsAssert.assertFalse;


import org.opengis.cite.ogcapiprocesses20.CommonFixture;
import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * A.2.1. General Tests
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class GeneralHttp extends CommonFixture {

    /**
     * <pre>
     * A.2.2. Deploy operation
     *
     * Abstract Test 1: /ats/dru/deploy-post-op
     * Test Purpose: Validate that the server support HTTP POST operation at the path /processes.
     * Requirement: /req/dru/deploy-post-op
     *
     * Test Method:
     *  1. Construct a path for each "rel=http://www.opengis.net/def/rel/ogc/1.0/processes" link on the landing page as well as for the {root}/processes path.
     *  2. Issue an HTTP POST request and validate that the response header does not contain `405 Method not allowed`
     * their HTTP 1.1 protocol. (untested)
     * </pre>
     */
    @Test(description = "Implements A.2.2. Deploy operation (Requirement /req/deploy-replace-undeploy/deploy/post-op)")
    public void testHttpProcessesPostDeployOp() {
        Response response = init().baseUri( rootUri.toString() ).when().request( POST, "/processes" );
        System.out.println("Status line");
        response.then().statusLine( containsString( "HTTP/1.1" ) );
        assertFalse( response.statusLine().contains("405"),
				    "The status code seem to be wrong: "+response.statusLine()+ " ");
        System.out.println(response.statusLine());
        System.out.println("/Status line");
    }

    /**
     * <pre>
     * A.2.3. Replace operation
     *
     * Abstract Test 1: /ats/dru/replace-put-op
     * Test Purpose: Validate that the server support HTTP PUT operation at the path /processes.
     * Requirement: /req/dru/replace-put-op
     *
     * Test Method:
     *  1. Construct a path for each "rel=http://www.opengis.net/def/rel/ogc/1.0/processes" link on the landing page as well as for the {root}/processes path, append them with `/{processId}` 
     *  2. Issue an HTTP PUT request and validate that the response header does not contain `405 Method not allowed`
     * </pre>
     */
    @Test(description = "Implements A.2.3. Replace operation (Requirement /req/deploy-replace-undeploy/replace/put-op)")
    public void testHttpProcessesPutReplaceOp() {
        Response response = init().baseUri( rootUri.toString() ).when().request( PUT, "/processes/not-existing-process" );
        System.out.println("Status line");
        response.then().statusLine( containsString( "HTTP/1.1" ) );
        assertFalse( response.statusLine().contains("405"),
				    "The status code seem to be wrong: "+response.statusLine()+ " ");
        System.out.println(response.statusLine());
        System.out.println("/Status line");
    }

    /**
     * <pre>
     * A.2.4. Undeploy operation
     *
     * Abstract Test 1: /ats/dru/undeploy-delete-op
     * Test Purpose: Validate that the server support HTTP DELETE operation at the path /processes/{processId}
     * Requirement: /req/dru/undeploy-delete-op
     *
     * Test Method:
     *  1. Construct a path for each "rel=http://www.opengis.net/def/rel/ogc/1.0/processes" link on the landing page as well as for the {root}/processes path, append them with `/{processId}`
     *  2. Issue an HTTP DELETE request and validate that the response header does not contain `405 Method not allowed`
     * their HTTP 1.1 protocol. (untested)
     * </pre>
     */
    @Test(description = "Implements A.2.4. Undeploy operation (Requirement /req/deploy-replace-undeploy/undeploy/delete-op)")
    public void testHttpProcessesDeleteUndeployOp() {
        Response response = init().baseUri( rootUri.toString() ).when().request( DELETE, "/processes/not-existing-process" );
        System.out.println("Status line");
        response.then().statusLine( containsString( "HTTP/1.1" ) );
        assertFalse( response.statusLine().contains("405"),
				    "The status code seem to be wrong: "+response.statusLine()+ " ");
        System.out.println(response.statusLine());
        System.out.println("/Status line");
    }

}
