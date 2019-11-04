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
        return "{ \"service-industry\" : \"manufacturing\", \"flags\" : { \"valid\" : true, \"addOns\" : false } }";
    }
    public static String dataUpdateJSONString() {
        return "{ \"service-industry\" : \"manufacturing\", \"flags\" : { \"valid\" : true, \"addOns\" : true, \"ADD-ON1\" : true } }";
    }
    public static String dataUpdateAfterShareJSONString() {
        return "{ \"service-industry\" : \"manufacturing\", \"flags\" : { \"valid\" : true, \"addOns\" : true, \"ADD-ON1\" : true, \"UW\" : true } }";
    }


    @Test
    public void test_create() {
        ServiceState service = ServiceState.create(
                new UniqueIdentifier(),
                "ServiceB",
                this.companyA.party,
                JsonHelper.convertStringToJson(dataJSONString()));
        Assert.assertEquals("state must be CREATED",
                StateMachine.State.CREATED, service.getState());
    }
    @Test
    public void test_update_after_create() {
        ServiceState service = ServiceState.create(
                new UniqueIdentifier(),
                "ServiceC",
                this.companyA.party,
                JsonHelper.convertStringToJson(dataJSONString()));
        ServiceState serviceUpdated = service.update(JsonHelper.convertStringToJson(dataUpdateJSONString()));
        Assert.assertEquals("state must be still CREATED",
                StateMachine.State.CREATED, service.getState());
        Assert.assertEquals("old addOns value must be false",
                "false", JsonHelper.getDataValue(service.getServiceData(), "flags.addOns"));
        Assert.assertEquals("old addOns value must be true",
                "true", JsonHelper.getDataValue(serviceUpdated.getServiceData(), "flags.addOns"));
    }

}
