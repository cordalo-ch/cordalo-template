/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.flows.test;

import ch.cordalo.template.flows.ServiceFlow;
import ch.cordalo.template.states.ServiceState;
import org.junit.*;

public class ServiceFlowTests extends CordaloTemplateBaseFlowTests {

    @Before
    public void setup() {
        this.setup(true, ServiceFlow.class);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }


    public static String dataJSONString() {
        return "{ \"service-industry\" : \"manufacturing\", \"flags\" : { \"valid\" : true, \"addOn\" : false } }";
    }

    public static String dataUpdateJSONString() {
        return "{ \"service-industry\" : \"manufacturing\", \"flags\" : { \"valid\" : true, \"addOn\" : true, \"ADD-ON1\" : true } }";
    }

    public static String dataUpdateAfterShareJSONString() {
        return "{ \"service-industry\" : \"manufacturing\", \"flags\" : { \"valid\" : true, \"addOn\" : true, \"ADD-ON1\" : true, \"UW\" : true } }";
    }


    @Test
    public void create_service() throws Exception {
        ServiceState service = this.newServiceCreateFlow(this.companyA, "Exit", dataJSONString(), 7);
        Assert.assertEquals("valid be true", "true", service.getData("flags.valid"));
        Assert.assertEquals("price must be 42", "7", String.valueOf(service.getPrice()));
    }


    @Test
    public void update_before_share_service() throws Exception {
        ServiceState service = this.newServiceCreateFlow(this.companyA, "Exit", dataJSONString(), 7);
        Assert.assertEquals("addOn must be false", "false", service.getData("flags.addOn"));

        ServiceState service2 = this.newServiceUpdateFlow(companyA, service.getLinearId(), dataUpdateJSONString(), 42);
        Assert.assertEquals("addOn must be true", "true", service2.getData("flags.addOn"));
        Assert.assertEquals("price must be 42", "42", String.valueOf(service2.getPrice()));
    }


    @Test
    public void delete_before_share_service() throws Exception {
        ServiceState service = this.newServiceCreateFlow(this.companyA, "Exit", dataJSONString(), 7);
        Assert.assertEquals("addOn must be false", "false", service.getData("flags.addOn"));
        this.newServiceDeleteFlow(service.getLinearId());
    }


    @Test
    public void share_service() throws Exception {
        ServiceState service = this.newServiceCreateFlow(this.companyA, "Exit", dataJSONString(), 7);
        ServiceState sharedService = this.newServiceShareFlow(this.companyA, service.getLinearId(), this.companyB.party);
        Assert.assertEquals("addOn be false", "false", sharedService.getData("flags.addOn"));
    }


    @Test
    public void delete_after_share_service() throws Exception {
        ServiceState service = this.newServiceCreateFlow(this.companyA, "Exit", dataJSONString(), 7);
        ServiceState sharedService = this.newServiceShareFlow(this.companyA, service.getLinearId(), this.companyB.party);

        Assert.assertEquals("addOn be false", "false", sharedService.getData("flags.addOn"));
        this.newServiceDeleteFlow(sharedService.getLinearId());
    }

    @Test
    @Ignore
    public void action_ACCEPT_service() throws Exception {
        ServiceState service = this.newServiceCreateFlow(this.companyA, "Exit", dataJSONString(), 7);

        ServiceState serviceS = this.newServiceShareFlow(this.companyA, service.getLinearId(), this.companyB.party);
        Assert.assertEquals("state is SHARED", "SHARED", serviceS.getState().toString());
        ServiceState serviceA = this.newServiceActionFlow(this.companyA, serviceS.getLinearId(), "ACCEPT");

        Assert.assertEquals("addOn be false", "false", serviceA.getData("flags.addOn"));
        Assert.assertEquals("Service2 must be service provider", companyB.party, serviceA.getServiceProvider());
        Assert.assertEquals("state is ACCEPTED", "ACCEPTED", serviceA.getState().toString());
    }


    @Test
    @Ignore
    public void action_ACCEPT_by_counterparty_service() throws Exception {
        ServiceState service = this.newServiceCreateFlow(this.companyA, "Exit", dataJSONString(), 7);
        ServiceState serviceS = this.newServiceShareFlow(this.companyA, service.getLinearId(), this.companyB.party);
        Assert.assertEquals("state is SHARED", "SHARED", serviceS.getState().toString());
        ServiceState serviceA = this.newServiceActionFlow(this.companyB, serviceS.getLinearId(), "ACCEPT");

        Assert.assertEquals("addOn be false", "false", serviceA.getData("flags.addOn"));
        Assert.assertEquals("Service2 must be service provider", companyB.party, serviceA.getServiceProvider());
        Assert.assertEquals("state is ACCEPTED", "ACCEPTED", serviceA.getState().toString());
    }


    @Test
    @Ignore
    public void action_CONFIRM_service() throws Exception {
        ServiceState service = this.newServiceCreateFlow(this.companyA, "Exit", dataJSONString(), 7);
        ServiceState serviceB = this.newServiceActionFlow(this.companyA, service.getLinearId(), "INFORM");
        ServiceState serviceC = this.newServiceActionFlow(this.companyA, service.getLinearId(), "CONFIRM");
        Assert.assertEquals("addOn be false", "false", serviceC.getData("flags.addOn"));
        Assert.assertEquals("state is CONFIRMED", "CONFIRMED", serviceC.getState().toString());
    }


}