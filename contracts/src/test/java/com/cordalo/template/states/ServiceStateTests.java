package com.cordalo.template.states;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.cordalo.template.CordaloTemplateBaseTests;
import com.cordalo.template.contracts.StateMachine;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServiceStateTests extends CordaloTemplateBaseTests {

    @Before
    public void setup() {
        this.setup(false);
    }

    @After
    public void tearDown() {
        super.tearDown();
    }


    public static String dataJSONString() {
        return "{ \"insurance-branch\" : \"health\", \"coverages\" : { \"OKP\" : true, \"ZVP\" : false } }";
    }
    public static String dataUpdateJSONString() {
        return "{ \"insurance-branch\" : \"health\", \"coverages\" : { \"OKP\" : true, \"ZVP\" : true, \"ADD-ON1\" : true } }";
    }
    public static String dataUpdateAfterShareJSONString() {
        return "{ \"insurance-branch\" : \"health\", \"coverages\" : { \"OKP\" : true, \"ZVP\" : true, \"ADD-ON1\" : true, \"UW\" : true } }";
    }


    @Test
    public void test_create() {
        ServiceState service = ServiceState.create(
                new UniqueIdentifier(),
                "insurance",
                this.insurance1.party,
                JsonHelper.convertStringToJson(dataJSONString()));
        Assert.assertEquals("state must be CREATED",
                StateMachine.State.CREATED, service.getState());
    }
    @Test
    public void test_update_after_create() {
        ServiceState service = ServiceState.create(
                new UniqueIdentifier(),
                "insurance",
                this.insurance1.party,
                JsonHelper.convertStringToJson(dataJSONString()));
        ServiceState serviceUpdated = service.update(JsonHelper.convertStringToJson(dataUpdateJSONString()));
        Assert.assertEquals("state must be still CREATED",
                StateMachine.State.CREATED, service.getState());
        Assert.assertEquals("old ZVP value must be false",
                "false", JsonHelper.getDataValue(service.getServiceData(), "coverages.ZVP"));
        Assert.assertEquals("old ZVP value must be true",
                "true", JsonHelper.getDataValue(serviceUpdated.getServiceData(), "coverages.ZVP"));
    }

}
