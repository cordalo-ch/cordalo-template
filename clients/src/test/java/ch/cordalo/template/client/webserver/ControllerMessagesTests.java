package ch.cordalo.template.client.webserver;

import ch.cordalo.corda.common.test.CordaTestNetwork;
import ch.cordalo.corda.common.test.MockCordaProxy;
import ch.cordalo.template.flows.ChatMessageFlow;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.Assert.assertEquals;

public class ControllerMessagesTests extends CordaloControllerBaseTests {

    @Before
    public void setup() {
        CordaTestNetwork network = this.setup(true,
                ChatMessageFlow.SendResponder.class,
                ChatMessageFlow.ReplyResponder.class
        );
        MockCordaProxy.updateInstance(companyA);
        this.setUpSpringTests();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }


    @Test
    public void getMe() throws Exception {
        String uri = "/api/v1/network/me";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.OK, HttpStatus.valueOf(status));
        String content = mvcResult.getResponse().getContentAsString();
    }

    @Test
    public void getPeers() throws Exception {
        String uri = "/api/v1/network/peers";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders.get(uri)
                .accept(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.OK, HttpStatus.valueOf(status));
        String content = mvcResult.getResponse().getContentAsString();
    }

    @Ignore
    public void post_message() throws Exception {
        String uri = "/api/v1/cordalo/template/messages";
        MvcResult mvcResult = mvc.perform(
                MockMvcRequestBuilders.post(uri)
                        .content(
                                "to=" + encode(this.companyB.party.toString()) + "&message=" + encode("hello world")
                        )
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
        ).andReturn();

        int status = mvcResult.getResponse().getStatus();
        assertEquals(HttpStatus.CREATED, HttpStatus.valueOf(status));
        String content = mvcResult.getResponse().getContentAsString();
    }


}
