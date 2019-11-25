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

import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.template.flows.ChatMessageFlow;
import ch.cordalo.template.states.ChatMessageState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.transactions.SignedTransaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ChatMessageFlowTests extends CordaloTemplateBaseFlowTests {

    @Before
    public void setup() {
        this.setup(true, ChatMessageFlow.class);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }


    protected ChatMessageState newMessageFlow(CordaNodeEnvironment from, CordaNodeEnvironment to, String message) throws FlowException {
        FlowLogic<SignedTransaction> flow = new ChatMessageFlow.Send(to.party, message);
        return this.startFlowAndResult(from, flow, ChatMessageState.class);
    }

    protected ChatMessageState newJokeMessageFlow(CordaNodeEnvironment from, CordaNodeEnvironment to) throws FlowException {
        FlowLogic<SignedTransaction> flow = new ChatMessageFlow.Send(to.party);
        return this.startFlowAndResult(from, flow, ChatMessageState.class);
    }

    protected StateVerifier newReplyFlow(CordaNodeEnvironment from, UniqueIdentifier id, String message) throws FlowException {
        FlowLogic<SignedTransaction> flow = new ChatMessageFlow.Reply(id, message);
        return this.startFlow(from, flow);
    }

    @Test
    public void send_message() throws Exception {
        ChatMessageState message = this.newMessageFlow(companyA, companyB, "hello world");
        Assert.assertEquals("message text", "hello world", message.getMessage());
    }

    @Test
    public void send_joke_message() throws Exception {
        ChatMessageState message = this.newJokeMessageFlow(companyA, companyB);
        Assert.assertNotNull("message text", message.getMessage());
    }


    @Test
    public void reply_message() throws Exception {
        ChatMessageState message = this.newMessageFlow(companyA, companyB, "hello");
        Assert.assertEquals("chat message", "hello", message.getMessage());

        StateVerifier verifier = this.newReplyFlow(companyB, message.getLinearId(), "world");
        ChatMessageState replyMessage = verifier
                .output(ChatMessageState.class)
                .notThis(message)
                .one()
                .object();

        Assert.assertEquals("chat message reply", "world", replyMessage.getMessage());
    }

}
