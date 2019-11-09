package com.cordalo.template.states;

import com.cordalo.template.contracts.ChatMessageContract;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@BelongsToContract(ChatMessageContract.class)
public class ChatMessageState implements LinearState {

    @NotNull
    private UniqueIdentifier id;

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
    public ChatMessageState(@NotNull UniqueIdentifier id, @NotNull Party sender, @NotNull Party receiver, @NotNull String message, UniqueIdentifier baseMessageId) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.baseMessageId = baseMessageId;
    }
    public ChatMessageState(@NotNull UniqueIdentifier id, @NotNull Party sender, @NotNull Party receiver, @NotNull String message) {
        this(id, sender, receiver, message, null);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.id;
    }

    @NotNull
    @JsonIgnore
    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> list = new ArrayList<>();
        list.add(this.sender);
        list.add(this.receiver);
        return list;
    }

    @NotNull
    @JsonIgnore
    public List<PublicKey> getParticipantKeys() {
        return getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }

    @NotNull
    public Party getSender() {
        return sender;
    }

    @NotNull
    public Party getReceiver() {
        return receiver;
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
        return id.equals(that.id) &&
                getSender().equals(that.getSender()) &&
                getReceiver().equals(that.getReceiver()) &&
                getMessage().equals(that.getMessage()) &&
                Objects.equals(getBaseMessageId(), that.getBaseMessageId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getSender(), getReceiver(), getMessage(), getBaseMessageId());
    }
}
