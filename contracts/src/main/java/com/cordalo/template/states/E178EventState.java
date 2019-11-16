package com.cordalo.template.states;

import ch.cordalo.corda.ext.Participants;
import com.cordalo.template.contracts.E178EventContract;
import com.cordalo.template.contracts.E178StateMachine;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.util.List;
import java.util.Objects;

@BelongsToContract(E178EventContract.class)
public class E178EventState implements LinearState {

    @NotNull
    @JsonProperty
    private final UniqueIdentifier linearId;

    @Nullable
    @JsonProperty
    private final Party regulator;

    @Nullable
    @JsonProperty
    private final Party retailer ;

    @Nullable
    @JsonProperty
    private final Party leasing;

    @Nullable
    @JsonProperty
    private final Party insurer;

    @NotNull
    private final String state;

    @NotNull
    private final String stammNr;

    @NotNull
    private final E178StateMachine.State status;

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    @JsonGetter
    public List<AbstractParty> getParticipants() {
        return new Participants(
                this.retailer,
                this.leasing,
                this.regulator,
                this.insurer
        ).getParties();
    }

    @ConstructorForDeserialization
    public E178EventState(@NotNull UniqueIdentifier linearId, String stammNr,
                          @Nullable Party retailer,
                          @Nullable Party leasing,
                          @Nullable Party insurer,
                          @Nullable Party regulator,
                          @NotNull String state, @NotNull E178StateMachine.State status) {
        this.linearId = linearId;
        this.stammNr = stammNr;
        this.retailer = retailer;
        this.leasing = leasing;
        this.insurer = insurer;
        this.regulator = regulator;
        this.state = state;
        this.status = status;
    }

    @NotNull
    @JsonIgnore
    public List<PublicKey> getParticipantKeys() {
        return new Participants(this.getParticipants()).getPublicKeys();
    }

    @Nullable
    public Party getRegulator() {
        return regulator;
    }

    @Nullable
    public Party getRetailer() {
        return retailer;
    }

    @Nullable
    public Party getLeasing() {
        return leasing;
    }

    @Nullable
    public Party getInsurer() {
        return insurer;
    }

    @NotNull
    public String getState() {
        return state;
    }

    @NotNull
    public String getStammNr() {
        return stammNr;
    }

    @NotNull
    public E178StateMachine.State getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        E178EventState that = (E178EventState) o;
        return linearId.equals(that.linearId) &&
                getStammNr().equals(that.getStammNr()) &&
                Objects.equals(getRegulator(), that.getRegulator()) &&
                Objects.equals(getRetailer(), that.getRetailer()) &&
                Objects.equals(getLeasing(), that.getLeasing()) &&
                Objects.equals(getInsurer(), that.getInsurer()) &&
                getState().equals(that.getState()) &&
                getStatus() == that.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(linearId, getStammNr(), getRegulator(), getRetailer(), getLeasing(), getInsurer(), getState(), getStatus());
    }

    /* action */

    protected E178EventState changeState(E178StateMachine.State status){
        return new E178EventState(this.getLinearId(), this.getStammNr(), this.getRetailer(), this.getLeasing(), this.getInsurer(), this.getRegulator(), this.getState(), status);
    }
    public static E178EventState request(String stammNr, Party retail, Party leasing, String state) {
        return new E178EventState(new UniqueIdentifier(), stammNr, retail, leasing, null, null, state, E178StateMachine.State.REQUESTED);
    }
    public E178EventState issue(String state, Party regulator) {
        return new E178EventState(this.getLinearId(), this.getStammNr(), this.getRetailer(), this.getLeasing(), this.getInsurer(), regulator, state, E178StateMachine.State.ISSUED);
    }
    public E178EventState issue(Party regulator) {
        return new E178EventState(this.getLinearId(), this.getStammNr(), this.getRetailer(), this.getLeasing(), this.getInsurer(), regulator, this.getState(), E178StateMachine.State.ISSUED);
    }

    public E178EventState requestInsurance(Party insurer) {
        return new E178EventState(this.getLinearId(), this.getStammNr(), this.getRetailer(), this.getLeasing(), insurer, this.getRegulator(), this.getState(), E178StateMachine.State.INSURANCE_REQUESTED);
    }
    public E178EventState insure() {
        return this.changeState(E178StateMachine.State.INSURED);
    }
    public E178EventState registered() {
        return this.changeState(E178StateMachine.State.REGISTERED);
    }
    public E178EventState cancel() {
        return this.changeState(E178StateMachine.State.CANCELED);
    }

}
