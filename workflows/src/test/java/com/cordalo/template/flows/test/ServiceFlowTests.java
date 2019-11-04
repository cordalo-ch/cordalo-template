package com.cordalo.template.flows.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import com.cordalo.template.flows.ServiceFlow;
import com.cordalo.template.states.ServiceState;
import net.corda.core.transactions.SignedTransaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServiceFlowTests extends CordaloTemplateBaseFlowTests {

    @Before
    public void setup() {
        this.setup(true,
            ServiceFlow.CreateResponder.class,
            ServiceFlow.UpdateResponder.class,
            ServiceFlow.DeleteResponder.class,
            ServiceFlow.ShareResponder.class,
            ServiceFlow.ActionResponder.class
        );
    }

    @After
    @Override
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
    public void create_service() throws Exception {
        SignedTransaction tx = this.newServiceCreateFlow("Exit", dataJSONString(), 7);
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.companyA.ledgerServices);
        ServiceState service = verifier
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("OKP be true", "true", service.getData("coverages.OKP"));
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
        Assert.assertEquals("ZVP must be false", "false", service.getData("coverages.ZVP"));

        StateVerifier verifier2 = StateVerifier.fromTransaction(
                this.newServiceUpdateFlow(service.getId(), dataUpdateJSONString(), 42),
                this.companyA.ledgerServices);
        ServiceState service2 = verifier2
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("ZVP must be true", "true", service2.getData("coverages.ZVP"));
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
        Assert.assertEquals("ZVP must be false", "false", service.getData("coverages.ZVP"));

        StateVerifier verifier2 = StateVerifier.fromTransaction(
                this.newServiceDeleteFlow(service.getId()),
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
                this.newServiceShareFlow(service.getId(), this.companyB.party),
                this.companyA.ledgerServices);
        ServiceState sharedService = verifier2
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("ZVP be false", "false", sharedService.getData("coverages.ZVP"));
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
                this.newServiceShareFlow(service.getId(), this.companyB.party),
                this.companyA.ledgerServices);
        ServiceState sharedService = verifier2
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("ZVP be false", "false", sharedService.getData("coverages.ZVP"));
        StateVerifier verifier3 = StateVerifier.fromTransaction(
                this.newServiceDeleteFlow(sharedService.getId()),
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
                this.newServiceShareFlow(service.getId(), companyB.party),
                this.companyA.ledgerServices);
        ServiceState serviceS = verifierS
                .output().one()
                .one(ServiceState.class)
                .object();
        Assert.assertEquals("state is SHARED", "SHARED", serviceS.getState().toString());

        StateVerifier verifierA = StateVerifier.fromTransaction(
                this.newServiceActionFlow(serviceS.getId(), "ACCEPT"),
                this.companyA.ledgerServices);
        ServiceState serviceA = verifierA
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("ZVP be false", "false", serviceA.getData("coverages.ZVP"));
        Assert.assertEquals("insurer2 must be service provider", companyB.party, serviceA.getServiceProvider());
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
                this.newServiceShareFlow(service.getId(), companyB.party),
                this.companyA.ledgerServices);
        ServiceState serviceS = verifierS
                .output().one()
                .one(ServiceState.class)
                .object();
        Assert.assertEquals("state is SHARED", "SHARED", serviceS.getState().toString());

        StateVerifier verifierA = StateVerifier.fromTransaction(
                this.newServiceActionFlowBy(serviceS.getId(), "ACCEPT", companyB.node),
                this.companyA.ledgerServices);
        ServiceState serviceA = verifierA
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("ZVP be false", "false", serviceA.getData("coverages.ZVP"));
        Assert.assertEquals("insurer2 must be service provider", companyB.party, serviceA.getServiceProvider());
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
                this.newServiceActionFlow(service.getId(), "INFORM"),
                this.companyA.ledgerServices);
        ServiceState service1 = verifier1
                .output().one()
                .one(ServiceState.class)
                .object();

        StateVerifier verifier2 = StateVerifier.fromTransaction(
                this.newServiceActionFlow(service1.getId(), "CONFIRM"),
                this.companyA.ledgerServices);
        ServiceState service2 = verifier2
                .output().one()
                .one(ServiceState.class)
                .object();

        Assert.assertEquals("ZVP be false", "false", service2.getData("coverages.ZVP"));
        Assert.assertEquals("state is CONFIRMED", "CONFIRMED", service2.getState().toString());
    }



}