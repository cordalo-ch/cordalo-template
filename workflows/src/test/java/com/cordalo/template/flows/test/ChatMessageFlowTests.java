package com.cordalo.template.flows.test;

import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.corda.common.test.MockCordaProxy;
import ch.cordalo.corda.ext.CordaProxy;
import com.cordalo.template.flows.ChatMessageFlow;
import com.cordalo.template.states.ChatMessageState;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowLogic;
import net.corda.core.transactions.SignedTransaction;
import org.junit.*;

import java.util.concurrent.ExecutionException;
@Ignore
public class ChatMessageFlowTests extends CordaloTemplateBaseFlowTests {

    @Before
    public void setup() {
        this.setup(true,
                ChatMessageFlow.SendResponder.class,
                ChatMessageFlow.ReplyResponder.class
        );
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }


    protected SignedTransaction newMessageFlow(CordaNodeEnvironment from, CordaNodeEnvironment to, String message) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new ChatMessageFlow.Send(to.party, message);
        CordaFuture<SignedTransaction> future = from.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }
    protected SignedTransaction newJokeMessageFlow(CordaNodeEnvironment from, CordaNodeEnvironment to) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new ChatMessageFlow.Send(to.party);
        CordaFuture<SignedTransaction> future = from.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }
    protected SignedTransaction newReplyFlow(CordaNodeEnvironment from, UniqueIdentifier id, String message) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new ChatMessageFlow.Reply(id, message);
        CordaFuture<SignedTransaction> future = from.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }

    @Test
    public void send_message() throws Exception {
        SignedTransaction tx = this.newMessageFlow(companyA, companyB,"hello world");
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.companyA.ledgerServices);
        ChatMessageState message = verifier
                .output().one()
                .one(ChatMessageState.class)
                .object();

        Assert.assertEquals("message text", "hello world", message.getMessage());
    }
    @Test
    public void send_joke_message() throws Exception {
        SignedTransaction tx = this.newJokeMessageFlow(companyA, companyB);
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.companyA.ledgerServices);
        ChatMessageState message = verifier
                .output().one()
                .one(ChatMessageState.class)
                .object();

        Assert.assertNotNull("message text", message.getMessage());
    }


    @Test
    public void reply_message() throws Exception {
        SignedTransaction tx = this.newMessageFlow(companyA, companyB,"hello");
        StateVerifier verifier = StateVerifier.fromTransaction(tx, this.companyA.ledgerServices);
        ChatMessageState message = verifier
                .output()
                .one()
                .one(ChatMessageState.class)
                .object();

        SignedTransaction tx2 = this.newReplyFlow(companyB, message.getLinearId(), "world");
        StateVerifier verifier2 = StateVerifier.fromTransaction(tx2, this.companyB.ledgerServices);
        ChatMessageState replyMessage = verifier2
                .output()
                .moreThanOne(2)
                .filterWhere(
                        x -> message.getLinearId().equals(((ChatMessageState)x).getBaseMessageId()))
                .one(ChatMessageState.class)
                .object();

        Assert.assertEquals("chat message", "hello", message.getMessage());
        Assert.assertEquals("chat message reply", "world", replyMessage.getMessage());
    }

}
