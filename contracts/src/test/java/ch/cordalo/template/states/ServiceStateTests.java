/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.states;

import ch.cordalo.corda.common.contracts.JsonHelper;
import ch.cordalo.template.CordaloTemplateBaseTests;
import ch.cordalo.template.contracts.ServiceStateMachine;
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
                ServiceStateMachine.State("CREATED"), service.getState());
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
                ServiceStateMachine.State("CREATED"), service.getState());
        Assert.assertEquals("old addOns value must be false",
                "false", JsonHelper.getDataValue(service.getServiceData(), "flags.addOns"));
        Assert.assertEquals("old addOns value must be true",
                "true", JsonHelper.getDataValue(serviceUpdated.getServiceData(), "flags.addOns"));
    }

}
