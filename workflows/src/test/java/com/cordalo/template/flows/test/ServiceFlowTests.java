package com.cordalo.template.flows.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import com.cordalo.template.flows.ServiceFlow;
import com.cordalo.template.states.ServiceState;
import net.corda.core.transactions.SignedTransaction;
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
        SignedTransaction tx = this.newServiceCreateFlow("Exit", dataJSONString(), 7);
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.companyA.ledgerServices);
        ServiceState service = verifier
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("valid be true", "true", service.getData("flags.valid"));
        Assert.assertEquals("price must be 42", "7", String.valueOf(service.getPrice()));
    }


    @Test
    public void update_before_share_service() throws Exception {
        SignedTransaction tx = this.newServiceCreateFlow("Exit", dataJSONString(), 7);
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.companyA.ledgerServices);
        ServiceState service = verifier
                .output().one()
                .one(ServiceState.class)
                .object();
        Assert.assertEquals("addOn must be false", "false", service.getData("flags.addOn"));

        StateVerifier verifier2 = StateVerifier.fromTransaction(
                this.newServiceUpdateFlow(service.getLinearId(), dataUpdateJSONString(), 42),
                this.companyA.ledgerServices);
        ServiceState service2 = verifier2
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("addOn must be true", "true", service2.getData("flags.addOn"));
        Assert.assertEquals("price must be 42", "42", String.valueOf(service2.getPrice()));
    }


    @Test
    public void delete_before_share_service() throws Exception {
        SignedTransaction tx = this.newServiceCreateFlow("Exit", dataJSONString(), 7);
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.companyA.ledgerServices);
        ServiceState service = verifier
                .output().one()
                .one(ServiceState.class)
                .object();
        Assert.assertEquals("addOn must be false", "false", service.getData("flags.addOn"));

        StateVerifier verifier2 = StateVerifier.fromTransaction(
                this.newServiceDeleteFlow(service.getLinearId()),
                this.companyA.ledgerServices);
        verifier2.output().empty();
    }



    @Test
    public void share_service() throws Exception {
        StateVerifier verifier = StateVerifier.fromTransaction(
                this.newServiceCreateFlow("Exit", dataJSONString(), 7),
                this.companyA.ledgerServices);
        ServiceState service = verifier
                .output().one()
                .one(ServiceState.class)
                .object();

        StateVerifier verifier2 = StateVerifier.fromTransaction(
                this.newServiceShareFlow(service.getLinearId(), this.companyB.party),
                this.companyA.ledgerServices);
        ServiceState sharedService = verifier2
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("addOn be false", "false", sharedService.getData("flags.addOn"));
    }


    @Test
    public void delete_after_share_service() throws Exception {
        StateVerifier verifier = StateVerifier.fromTransaction(
                this.newServiceCreateFlow("Exit", dataJSONString(), 7),
                this.companyA.ledgerServices);
        ServiceState service = verifier
                .output().one()
                .one(ServiceState.class)
                .object();

        StateVerifier verifier2 = StateVerifier.fromTransaction(
                this.newServiceShareFlow(service.getLinearId(), this.companyB.party),
                this.companyA.ledgerServices);
        ServiceState sharedService = verifier2
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("addOn be false", "false", sharedService.getData("flags.addOn"));
        StateVerifier verifier3 = StateVerifier.fromTransaction(
                this.newServiceDeleteFlow(sharedService.getLinearId()),
                this.companyA.ledgerServices);
        verifier3.output().empty();
    }

    @Test
    public void action_ACCEPT_service() throws Exception {
        StateVerifier verifier = StateVerifier.fromTransaction(
                this.newServiceCreateFlow("Exit", dataJSONString(), 7),
                this.companyA.ledgerServices);
        ServiceState service = verifier
                .output().one()
                .one(ServiceState.class)
                .object();

        StateVerifier verifierS = StateVerifier.fromTransaction(
                this.newServiceShareFlow(service.getLinearId(), companyB.party),
                this.companyA.ledgerServices);
        ServiceState serviceS = verifierS
                .output().one()
                .one(ServiceState.class)
                .object();
        Assert.assertEquals("state is SHARED", "SHARED", serviceS.getState().toString());

        StateVerifier verifierA = StateVerifier.fromTransaction(
                this.newServiceActionFlow(serviceS.getLinearId(), "ACCEPT"),
                this.companyA.ledgerServices);
        ServiceState serviceA = verifierA
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("addOn be false", "false", serviceA.getData("flags.addOn"));
        Assert.assertEquals("Service2 must be service provider", companyB.party, serviceA.getServiceProvider());
        Assert.assertEquals("state is ACCEPTED", "ACCEPTED", serviceA.getState().toString());
    }



    @Test
    public void action_ACCEPT_by_counterparty_service() throws Exception {
        StateVerifier verifier = StateVerifier.fromTransaction(
                this.newServiceCreateFlow("Exit", dataJSONString(), 7),
                this.companyA.ledgerServices);
        ServiceState service = verifier
                .output().one()
                .one(ServiceState.class)
                .object();

        StateVerifier verifierS = StateVerifier.fromTransaction(
                this.newServiceShareFlow(service.getLinearId(), companyB.party),
                this.companyA.ledgerServices);
        ServiceState serviceS = verifierS
                .output().one()
                .one(ServiceState.class)
                .object();
        Assert.assertEquals("state is SHARED", "SHARED", serviceS.getState().toString());

        StateVerifier verifierA = StateVerifier.fromTransaction(
                this.newServiceActionFlowBy(serviceS.getLinearId(), "ACCEPT", companyB.node),
                this.companyA.ledgerServices);
        ServiceState serviceA = verifierA
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("addOn be false", "false", serviceA.getData("flags.addOn"));
        Assert.assertEquals("Service2 must be service provider", companyB.party, serviceA.getServiceProvider());
        Assert.assertEquals("state is ACCEPTED", "ACCEPTED", serviceA.getState().toString());
    }


    @Test
    public void action_CONFIRM_service() throws Exception {
        StateVerifier verifier = StateVerifier.fromTransaction(
                this.newServiceCreateFlow("Exit", dataJSONString(), 7),
                this.companyA.ledgerServices);
        ServiceState service = verifier
                .output().one()
                .one(ServiceState.class)
                .object();

        StateVerifier verifier1 = StateVerifier.fromTransaction(
                this.newServiceActionFlow(service.getLinearId(), "INFORM"),
                this.companyA.ledgerServices);
        ServiceState service1 = verifier1
                .output().one()
                .one(ServiceState.class)
                .object();

        StateVerifier verifier2 = StateVerifier.fromTransaction(
                this.newServiceActionFlow(service1.getLinearId(), "CONFIRM"),
                this.companyA.ledgerServices);
        ServiceState service2 = verifier2
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("addOn be false", "false", service2.getData("flags.addOn"));
        Assert.assertEquals("state is CONFIRMED", "CONFIRMED", service2.getState().toString());
    }



}