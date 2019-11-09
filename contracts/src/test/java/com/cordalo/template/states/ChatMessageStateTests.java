package com.cordalo.template.states;

import com.cordalo.template.CordaloTemplateBaseTests;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ChatMessageStateTests extends CordaloTemplateBaseTests {
    @Before
    public void setup() {
        this.setup(false);
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void test_send_ok() {
        ChatMessageState message = new ChatMessageState(
                new UniqueIdentifier(),
                this.companyA.party,
                this.companyB.party,
                "a cool message");

        Assert.assertNotNull("object must be created", message);
        Assert.assertEquals("message is set",
                "a cool message", message.getMessage()
        );
        Assert.assertEquals("participants are 2",
                2, message.getParticipants().size()
        );
        Assert.assertNull("new message has not base message",
                message.getBaseMessageId()
        );
    }


    @Test
    public void test_reply_ok() {
        ChatMessageState message = new ChatMessageState(
                new UniqueIdentifier(),
                this.companyA.party,
                this.companyB.party,
                "a cool message");
        ChatMessageState reply = message.reply("yeah cool");

        Assert.assertNotNull("reply must be created", reply);
        Assert.assertEquals("reply message is set",
                "yeah cool", reply.getMessage()
        );
        Assert.assertEquals("reply receiver and sender are flipped",
                message.getSender(), reply.getReceiver()
        );
        Assert.assertEquals("reply sender and receiver are flipped",
                message.getReceiver(), reply.getSender()
        );
        Assert.assertEquals("reply base message must be message that was replied to",
                message.getLinearId(), reply.getBaseMessageId()
        );
    }



}
