package com.cordalo.template.states;

import ch.cordalo.corda.common.test.MockCordaProxy;
import ch.cordalo.corda.ext.CordaProxy;
import com.cordalo.template.CordaloTemplateBaseTests;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.corda.client.jackson.JacksonSupport;
import org.junit.*;

@Ignore
public class PartyJsonSerializerTest extends CordaloTemplateBaseTests {

    public PartyJsonSerializerTest() { }

    @Before
    public void setup() {
        this.setup(false);
        CordaProxy.register(new MockCordaProxy(this.companyA));
    }
    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void json_e178() throws JsonProcessingException {
        ObjectMapper mapper = JacksonSupport.createDefaultMapper(CordaProxy.getInstance().getProxy());
        //ObjectMapper mapper = JacksonSupport.createNonRpcMapper();
        E178EventState e178 = E178EventState.request("123.456.789", this.companyA.party, this.companyB.party, "ZH");
        String json = mapper.writeValueAsString(e178);
        Assert.assertEquals("party is serialized", "", json);
    }
}
