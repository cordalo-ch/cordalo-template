package ch.cordalo.template.flows.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.template.E178BaseTests;
import ch.cordalo.template.contracts.E178StateMachine;
import ch.cordalo.template.flows.E178EventFlow;
import ch.cordalo.template.states.E178EventState;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.LinearState;
import net.corda.core.flows.FlowLogic;
import net.corda.core.transactions.SignedTransaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

public class E178EventFlowTests extends E178BaseTests {
    private final static String STAMM_NR = "123.456.786";
    @Before
    public void setup() {
        this.setup(true,
                E178EventFlow.class
        );
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }


    protected SignedTransaction newRequestE178(String stammNr, CordaNodeEnvironment retail, CordaNodeEnvironment leasing, String state) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new E178EventFlow.Request(stammNr, leasing.party, state);
        CordaFuture<SignedTransaction> future = retail.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }
    protected SignedTransaction newIssueE178(E178EventState e178, CordaNodeEnvironment leasing, String state, CordaNodeEnvironment regulator) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new E178EventFlow.Issue(e178.getLinearId(), state, regulator.party);
        CordaFuture<SignedTransaction> future = leasing.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }
    protected SignedTransaction newRequestInsuranceE178(E178EventState e178, CordaNodeEnvironment retailer, CordaNodeEnvironment insurance) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new E178EventFlow.RequestInsurance(e178.getLinearId(), insurer1.party);
        CordaFuture<SignedTransaction> future = retailer.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }
    protected SignedTransaction newInsureE178(E178EventState e178, CordaNodeEnvironment insurer) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new E178EventFlow.Insure(e178.getLinearId());
        CordaFuture<SignedTransaction> future = insurer.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }
    protected SignedTransaction newRegisterE178(E178EventState e178, CordaNodeEnvironment regulator) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new E178EventFlow.Register(e178.getLinearId());
        CordaFuture<SignedTransaction> future = regulator.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }

    @Test
    public void test_e178_request() throws Exception {
        E178EventState e178 = verifyAndGet(E178EventState.class, this.newRequestE178(STAMM_NR, this.retailer, this.leasing, "ZH"));
        Assert.assertEquals("state must be ZH", "ZH", e178.getState());
    }

    @Test
    public void test_e178_issue() throws Exception {
        E178EventState e178 = verifyAndGet(E178EventState.class, this.newRequestE178(STAMM_NR, this.retailer, this.leasing, "ZH"));
        Assert.assertEquals("state must be ZH", "ZH", e178.getState());

        e178 = verifyAndGet(E178EventState.class, this.newIssueE178(e178, this.leasing, "AG", this.regulator));
        Assert.assertEquals("state must be AG", "AG", e178.getState());
    }

    @Test
    public void test_e178_request_insurance() throws Exception {
        E178EventState e178 = verifyAndGet(E178EventState.class, this.newRequestE178(STAMM_NR, this.retailer, this.leasing, "ZH"));
        Assert.assertEquals("state must be ZH", "ZH", e178.getState());

        e178 = verifyAndGet(E178EventState.class, this.newIssueE178(e178, this.leasing, "AG", this.regulator));
        Assert.assertEquals("state must be AG", "AG", e178.getState());

        e178 = verifyAndGet(E178EventState.class, this.newRequestInsuranceE178(e178, this.retailer, this.insurer1));
        Assert.assertTrue("insurer is set", e178.getInsurer().equals(this.insurer1.party));
    }

    private <T extends LinearState> T verifyAndGet(Class<T> stateClass, SignedTransaction tx) {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.retailer.ledgerServices);
        return verifier
                .output().one()
                .one(stateClass)
                .object();
    }


    @Test
    public void test_e178_insurance() throws Exception {
        E178EventState e178 = verifyAndGet(E178EventState.class, this.newRequestE178(STAMM_NR, this.retailer, this.leasing, "ZH"));
        Assert.assertEquals("state must be ZH", "ZH", e178.getState());

        e178 = verifyAndGet(E178EventState.class, this.newIssueE178(e178, this.leasing, "AG", this.regulator));
        Assert.assertEquals("state must be AG", "AG", e178.getState());

        e178 = verifyAndGet(E178EventState.class, this.newRequestInsuranceE178(e178, this.retailer, this.insurer1));
        Assert.assertTrue("insurer is set", e178.getInsurer().equals(this.insurer1.party));

        e178 = verifyAndGet(E178EventState.class, this.newInsureE178(e178, this.insurer1));
        Assert.assertTrue("e178 is insured", e178.getStatus().equals(E178StateMachine.State.INSURED));
    }

    @Test
    public void test_retailer_cancelE178_success() throws Exception {
        CordaNodeEnvironment node = this.retailer;
        createE178AndCancelOnNodeEnvironment(node);
    }

    @Test
    public void test_regulator_cancelE178_success() throws Exception {
        CordaNodeEnvironment node = this.regulator;
        createE178AndCancelOnNodeEnvironment(node);
    }

    @Test
    public void test_insurer1_cancelE178_success() throws Exception {
        CordaNodeEnvironment node = this.insurer1;
        createE178AndCancelOnNodeEnvironment(node);
    }

    @Test
    public void test_insurer2_cancelE178_success() throws Exception {
        CordaNodeEnvironment node = this.insurer2;
        createE178AndCancelOnNodeEnvironment(node);
    }

    private void createE178AndCancelOnNodeEnvironment(CordaNodeEnvironment retailer) throws ExecutionException, InterruptedException {
        E178EventState e178 = verifyAndGet(E178EventState.class, this.newRequestE178(STAMM_NR, retailer, this.leasing, "ZH"));

        FlowLogic<SignedTransaction> flow = new E178EventFlow.Cancel(e178.getLinearId());
        CordaFuture<SignedTransaction> future = retailer.node.startFlow(flow);
        network.runNetwork();
        SignedTransaction signedTransaction = future.get();
        E178EventState e178EventState = verifyAndGet(E178EventState.class, signedTransaction);
        Assert.assertTrue("e178 is cancelled", e178EventState.getStatus().equals(E178StateMachine.State.CANCELED));
    }

    @Test
    public void test_e178_register() throws Exception {
        E178EventState e178 = verifyAndGet(E178EventState.class, this.newRequestE178(STAMM_NR, this.retailer, this.leasing, "ZH"));
        Assert.assertEquals("state must be ZH", "ZH", e178.getState());

        e178 = verifyAndGet(E178EventState.class, this.newIssueE178(e178, this.leasing, "AG", this.regulator));
        Assert.assertEquals("state must be AG", "AG", e178.getState());

        e178 = verifyAndGet(E178EventState.class, this.newRequestInsuranceE178(e178, this.retailer, this.insurer1));
        Assert.assertTrue("insurer is set", e178.getInsurer().equals(this.insurer1.party));

        e178 = verifyAndGet(E178EventState.class, this.newInsureE178(e178, this.insurer1));
        Assert.assertTrue("e178 is insured", e178.getStatus().equals(E178StateMachine.State.INSURED));

        e178 = verifyAndGet(E178EventState.class, this.newRegisterE178(e178, this.regulator));
        Assert.assertTrue("e178 is registered", e178.getStatus().equals(E178StateMachine.State.REGISTERED));
    }


}
