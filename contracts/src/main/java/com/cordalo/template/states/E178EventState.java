package com.cordalo.template.states;

import ch.cordalo.corda.ext.Participants;
import com.cordalo.template.contracts.E178EventContract;
import com.cordalo.template.contracts.E178StateMachine;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.util.List;
import java.util.Objects;

@BelongsToContract(E178EventContract.class)
public class E178EventState implements LinearState {

    @NotNull
    private final UniqueIdentifier id;

    @JsonIgnore
    @Nullable
    private final Party regulator;

    @JsonIgnore
    @Nullable
    private final Party retailer ;

    @JsonIgnore
    @Nullable
    private final Party leasing;

    @JsonIgnore
    @Nullable
    private final Party insurer;

    @NotNull
    private final String state;

    @NotNull
    private final E178StateMachine.State status;

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return id;
    }

    @NotNull
    @JsonIgnore
    @Override
    public List<AbstractParty> getParticipants() {
        return new Participants(
                this.retailer,
                this.leasing,
                this.regulator,
                this.insurer
        ).getParties();
    }

    @ConstructorForDeserialization
    public E178EventState(@NotNull UniqueIdentifier id,
                          @Nullable Party retailer,
                          @Nullable Party leasing,
                          @Nullable Party insurer,
                          @Nullable Party regulator,
                          @NotNull String state, @NotNull E178StateMachine.State status) {
        this.id = id;
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
    @NotNull
    public List<String> getParticipantsX500() {
        return new Participants(this.getParticipants()).getPartiesX500();
    }

    @Nullable
    public Party getRegulator() {
        return regulator;
    }
    public String getRegulatorX500() { return Participants.partyToX500(this.getRegulator());}

    @Nullable
    public Party getRetailer() {
        return retailer;
    }
    public String getRetailerX500() { return Participants.partyToX500(this.getRetailer());}

    @Nullable
    public Party getLeasing() {
        return leasing;
    }
    public String getLeasingX500() { return Participants.partyToX500(this.getLeasing());}

    @Nullable
    public Party getInsurer() {
        return insurer;
    }
    public String getInsurerX500() { return Participants.partyToX500(this.getInsurer());}

    @NotNull
    public String getState() {
        return state;
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
        return id.equals(that.id) &&
                Objects.equals(getRegulator(), that.getRegulator()) &&
                Objects.equals(getRetailer(), that.getRetailer()) &&
                Objects.equals(getLeasing(), that.getLeasing()) &&
                Objects.equals(getInsurer(), that.getInsurer()) &&
                getState().equals(that.getState()) &&
                getStatus() == that.getStatus();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, getRegulator(), getRetailer(), getLeasing(), getInsurer(), getState(), getStatus());
    }

    /* action */

    protected E178EventState changeState(E178StateMachine.State status){
        return new E178EventState(this.getLinearId(), this.getRetailer(), this.getLeasing(), this.getInsurer(), this.getRegulator(), this.getState(), status);
    }
    public static E178EventState request(Party retail, Party leasing, String state) {
        return new E178EventState(new UniqueIdentifier(), retail, leasing, null, null, state, E178StateMachine.State.REQUESTED);
    }
    public E178EventState issue(String state, Party regulator) {
        return new E178EventState(this.getLinearId(), this.getRetailer(), this.getLeasing(), this.getInsurer(), regulator, state, E178StateMachine.State.ISSUED);
    }
    public E178EventState issue(Party regulator) {
        return new E178EventState(this.getLinearId(), this.getRetailer(), this.getLeasing(), this.getInsurer(), regulator, this.getState(), E178StateMachine.State.ISSUED);
    }

    public E178EventState requestInsurance(Party insurer) {
        return new E178EventState(this.getLinearId(), this.getRetailer(), this.getLeasing(), insurer, this.getRegulator(), this.getState(), E178StateMachine.State.INSURANCE_REQUESTED);
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
