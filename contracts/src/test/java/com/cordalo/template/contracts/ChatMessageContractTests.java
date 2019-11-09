package com.cordalo.template.contracts;

import com.cordalo.template.CordaloTemplateBaseTests;
import com.cordalo.template.states.ChatMessageState;
import com.cordalo.template.states.ServiceState;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Assert;
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
