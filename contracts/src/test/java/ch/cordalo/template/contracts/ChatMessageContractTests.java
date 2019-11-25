/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.contracts;

import ch.cordalo.template.CordaloTemplateBaseTests;
import ch.cordalo.template.states.ChatMessageState;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ChatMessageContractTests extends CordaloTemplateBaseTests {
    @Before
    public void setup() {
        this.setup(false);
    }

    @After
    public void tearDown() {
        super.tearDown();
    }


    private ChatMessageState newMessage(String msg) {
        return new ChatMessageState(
                new UniqueIdentifier(),
                this.companyA.party,
                this.companyB.party,
                msg);
    }

    private ChatMessageState newSelfieMessage(String msg) {
        return new ChatMessageState(
                new UniqueIdentifier(),
                this.companyA.party,
                this.companyA.party,
                msg);
    }

    @Test
    public void test_validate_self_messages_fail() {
        transaction(companyA.ledgerServices, tx -> {
            ChatMessageState message = newSelfieMessage("hello");
            tx.output(ChatMessageContract.ID, message);
            tx.command(message.getParticipantKeys(), new ChatMessageContract.Commands.Send());
            tx.fails();
            return null;
        });
    }


    @Test
    public void test_validate_empty_message_fail() {
        transaction(companyA.ledgerServices, tx -> {
            ChatMessageState message = newSelfieMessage("");
            tx.output(ChatMessageContract.ID, message);
            tx.command(message.getParticipantKeys(), new ChatMessageContract.Commands.Send());
            tx.failsWith("message cannot be empty");
            return null;
        });
    }

    @Test
    public void test_validate_new_message() {
        transaction(companyA.ledgerServices, tx -> {
            ChatMessageState message = newMessage("hello");
            tx.output(ChatMessageContract.ID, message);
            tx.command(message.getParticipantKeys(), new ChatMessageContract.Commands.Send());
            tx.verifies();
            return null;
        });
    }


    @Test
    public void test_validate_reply_message() {
        transaction(companyA.ledgerServices, tx -> {
            ChatMessageState message = newMessage("hello");
            ChatMessageState reply = message.reply("world");
            tx.input(ChatMessageContract.ID, message);
            tx.output(ChatMessageContract.ID, message);
            tx.output(ChatMessageContract.ID, reply);
            tx.command(message.getParticipantKeys(), new ChatMessageContract.Commands.Reply());
            //tx.command(message.getParticipantKeys(), new ChatMessageContract.Commands.Reference(message));
            tx.verifies();
            return null;
        });
    }

    @Test
    public void test_validate_reply_message_missing_input() {
        transaction(companyA.ledgerServices, tx -> {
            ChatMessageState message = newMessage("hello");
            ChatMessageState reply = message.reply("world");
            tx.input(ChatMessageContract.ID, message);
            tx.output(ChatMessageContract.ID, reply);
            tx.command(message.getParticipantKeys(), new ChatMessageContract.Commands.Reply());
            tx.fails();
            return null;
        });
    }


}
