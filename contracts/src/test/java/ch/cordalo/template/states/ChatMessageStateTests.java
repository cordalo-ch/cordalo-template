/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.states;

import ch.cordalo.template.CordaloTemplateBaseTests;
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
