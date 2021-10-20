/*
 * Copyright © Oct 1, 2021 Factor-y S.r.l. (daniele.vistalli@factor-y.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.Policy;
import java.security.Security;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import lotus.domino.NotesThread;

@DisabledIfSystemProperty(named = "testSecurity",matches = "false")
public class TestNSFPolicyURLHandler {

    @BeforeAll
    @DisplayName("Initialize Notes API - policy edition")
    static void setup() throws InterruptedException {

	String jsecProps = System.getProperty("java.security.properties"); 
	
	assertNotNull(jsecProps, "Expected java.security.properties property to be set");
	System.out.println("Running with security properties override: " + jsecProps);

	
	System.out.println("Notes subsystem isloaded: " + NotesThread.isLoaded);

	NotesThread.sinitThread();

	assertTrue(NotesThread.isLoaded,"Expected notes to be loaded but it is not.");
	
    }
    
    @Test
    @Order(2)
    @DisplayName("Verify policy settings and try loading policy")
    void policyTest() {
	assertNotNull(Security.getProperty("policy.url.1"));
	assertNotNull(Security.getProperty("policy.url.2"));
	assertNotNull(Security.getProperty("policy.url.3"));
	String nsfPolicy = Security.getProperty("policy.url.4");
	assertEquals(nsfPolicy, "nsfpolicy:///global");

	Policy policy = Policy.getPolicy();
	assertNotNull(policy);
	
    }
    
    @Test
    @Order(1)
    @DisplayName("Load a Java Policy from nsfpolicy handler")
    void test() throws MalformedURLException, IOException {
	
	URLConnection conn = new URL("nsfpolicy:///global").openConnection();
	assertNotNull(conn, "An URL connection to the NSF policy was expected");

	Object content = conn.getContentType();
	assertNotNull(content);
	
	System.out.println(content);

    }
   

}
