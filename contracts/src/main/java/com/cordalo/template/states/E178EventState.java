package com.cordalo.template.states;

import com.cordalo.template.contracts.ChatMessageContract;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@BelongsToContract(ChatMessageContract.class)
public class E178EventState implements LinearState {

    @NotNull
    private UniqueIdentifier id;

    @JsonIgnore
    @NotNull
    private Party regulator;

    @JsonIgnore
    @NotNull
    private Party retailer ;

    @JsonIgnore
    @NotNull
    private Party leasing;

    @JsonIgnore
    @NotNull
    private Party insurer;

    @NotNull
    private String state;

    @CordaSerializable
    public enum E178StatusType {
        INITIAL,
        REQUESTED,
        ISSUED,
        INSURED,
        REGISTERED
    }

    @NotNull
    private E178StatusType status;

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return id;
    }

    @NotNull
    @JsonIgnore
    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> list = new ArrayList<>();
        list.add(this.regulator);
        list.add(this.retailer);
        list.add(this.insurer);
        list.add(this.leasing);
        return list;
    }

    @ConstructorForDeserialization
    public E178EventState(@NotNull UniqueIdentifier id,
                          @NotNull Party regulator,
                          @NotNull Party retailer,
                          @NotNull Party leasing,
                          @NotNull Party insurer,
                          @NotNull String state, @NotNull E178StatusType status) {
        this.id = id;
        this.regulator = regulator;
        this.retailer = retailer;
        this.leasing = leasing;
        this.insurer = insurer;
        this.state = state;
        this.status = status;
    }

    @NotNull
    @JsonIgnore
    public List<PublicKey> getParticipantKeys() {
        return getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }

    @NotNull
    public Party getRegulator() {
        return regulator;
    }

    @NotNull
    public Party getRetailer() {
        return retailer;
    }

    @NotNull
    public Party getLeasing() {
        return leasing;
    }

    @NotNull
    public Party getInsurer() {
        return insurer;
    }

    @NotNull
    public String getState() {
        return state;
    }

    @NotNull
    public E178StatusType getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        E178EventState e178EventState = (E178EventState) o;
        return id.equals(e178EventState.id) &&
                regulator.equals(e178EventState.regulator) &&
                retailer.equals(e178EventState.retailer) &&
                leasing.equals(e178EventState.leasing) &&
                insurer.equals(e178EventState.insurer) &&
                state.equals(e178EventState.state) &&
                status == e178EventState.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, regulator, retailer, leasing, insurer, state, status);
    }
}
