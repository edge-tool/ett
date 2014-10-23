package gov.nist.healthcare.ttt.webapp.integration.xdr

import gov.nist.healthcare.ttt.webapp.testFramework.TestApplication
import gov.nist.healthcare.ttt.webapp.xdr.controller.XdrTestCaseController
import gov.nist.healthcare.ttt.xdr.web.TkListener
import org.junit.Before
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.IntegrationTest
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import spock.lang.Specification
import sun.security.acl.PrincipalImpl

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebAppConfiguration
@IntegrationTest
@ContextConfiguration(loader = SpringApplicationContextLoader.class, classes = TestApplication.class)
class XdrTestCase1IntegrationTest extends Specification {


    @Autowired
    XdrTestCaseController controller

    @Autowired
    TkListener listener

    MockMvc mockMvcClient
    MockMvc mockMvcToolkit

    @Before
    public setup() {
        mockMvcClient = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build()

        mockMvcToolkit = MockMvcBuilders.standaloneSetup(listener)
                .setMessageConverters(new Jaxb2RootElementHttpMessageConverter())
                .build()
    }


    def "user succeeds in starting test case 1"() throws Exception {

        when: "receiving a request to run test case 1"
        MockHttpServletRequestBuilder getRequest = createEndpointRequest()

        then: "we receive back a success message"

        mockMvcClient.perform(getRequest)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("status").value("SUCCESS"))
                .andExpect(jsonPath("content.endpoint").value("http://"))
                .andExpect(jsonPath("content.endpointTLS").value("https://"))

        when: "receiving a validation report from toolkit"
        MockHttpServletRequestBuilder getRequest2 = reportRequest()

        then: "we store the validation in the database"

        mockMvcToolkit.perform(getRequest2)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()



        println "ok"
    }


    MockHttpServletRequestBuilder createEndpointRequest() {
        MockMvcRequestBuilders.post("/xdr/tc/1")
                .accept(MediaType.ALL)
                .content(testCaseConfig)
                .contentType(MediaType.APPLICATION_JSON)
                .principal(new PrincipalImpl("user1"))
    }


    MockHttpServletRequestBuilder reportRequest() {
        MockMvcRequestBuilders.post("/xdrNotification")
                .accept(MediaType.ALL)
                .content(XML)
                .contentType(MediaType.APPLICATION_XML)
    }

    private static String XML =
            """
<report>
    <simId>user1.1.2014</simId>
    <status>success</status>
    <details>blabla</details>
</report>
            """


    public String testCaseConfig =
            """{
    "tc_config": {
        "endpoint_url": "sut1.testlab1"
    }
}"""
}

