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

import ch.cordalo.corda.common.states.CordaloLinearState;
import ch.cordalo.corda.common.states.Parties;
import ch.cordalo.template.contracts.ChatMessageContract;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@BelongsToContract(ChatMessageContract.class)
public class ChatMessageState extends CordaloLinearState {

    @JsonIgnore
    @NotNull
    private Party sender;

    @JsonIgnore
    @NotNull
    private Party receiver;

    @NotNull
    private String message;

    @NotNull
    private UniqueIdentifier baseMessageId;


    @ConstructorForDeserialization
    public ChatMessageState(@NotNull UniqueIdentifier linearId, @NotNull Party sender, @NotNull Party receiver, @NotNull String message, UniqueIdentifier baseMessageId) {
        super(linearId);
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.baseMessageId = baseMessageId;
    }

    public ChatMessageState(@NotNull UniqueIdentifier linearId, @NotNull Party sender, @NotNull Party receiver, @NotNull String message) {
        this(linearId, sender, receiver, message, null);
    }

    @NotNull
    @JsonIgnore
    @Override
    protected Parties getParties() {
        return Parties.fromParties(this.sender, this.receiver);
    }

    @NotNull
    public Party getSender() {
        return sender;
    }

    public String getSenderX500() {
        return this.sender.getName().getX500Principal().getName();
    }

    @NotNull
    public Party getReceiver() {
        return receiver;
    }

    public String getReceiverX500() {
        return this.receiver.getName().getX500Principal().getName();
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    @NotNull
    public UniqueIdentifier getBaseMessageId() {
        return baseMessageId;
    }

    public ChatMessageState reply(String replyMessage) {
        return new ChatMessageState(new UniqueIdentifier(), this.receiver, this.sender, replyMessage, this.getLinearId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessageState that = (ChatMessageState) o;
        return this.getLinearId().equals(that.getLinearId()) &&
                getSender().equals(that.getSender()) &&
                getReceiver().equals(that.getReceiver()) &&
                getMessage().equals(that.getMessage()) &&
                Objects.equals(getBaseMessageId(), that.getBaseMessageId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getLinearId(), getSender(), getReceiver(), getMessage(), getBaseMessageId());
    }

}
