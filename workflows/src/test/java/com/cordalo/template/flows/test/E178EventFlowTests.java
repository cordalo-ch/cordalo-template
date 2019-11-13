package com.cordalo.template.flows.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import com.cordalo.template.E178BaseTests;
import com.cordalo.template.flows.E178EventFlow;
import com.cordalo.template.states.E178EventState;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.flows.FlowLogic;
import net.corda.core.transactions.SignedTransaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class E178EventFlowTests extends E178BaseTests {

    @Before
    public void setup() {
        this.setup(true,
                E178EventFlow.RequestResponder.class,
                E178EventFlow.IssueResponder.class
        );
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }


    protected SignedTransaction newRequestE178(CordaNodeEnvironment retail, CordaNodeEnvironment leasing, String state) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new E178EventFlow.Request(leasing.party, state);
        CordaFuture<SignedTransaction> future = retail.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }

    @Test
    public void test_e178_request() throws Exception {
        SignedTransaction tx = this.newRequestE178(this.retailer,this.leasing, "ZH");
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.retailer.ledgerServices);
        E178EventState e178 = verifier
                .output().one()
                .one(E178EventState.class)
                .object();

        Assert.assertEquals("state must be ZH", "ZH", e178.getState());
    }

}
