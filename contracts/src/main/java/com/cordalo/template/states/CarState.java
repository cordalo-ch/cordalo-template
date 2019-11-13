package com.cordalo.template.states;

import ch.cordalo.corda.ext.Participants;
import com.cordalo.template.contracts.CarContract;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@BelongsToContract(CarContract.class)
public class CarState implements LinearState {

    @NotNull
    private final UniqueIdentifier linearId;

    @NotNull
    private final String make;

    @NotNull
    private final String model;

    @NotNull
    private final String type;

    @NotNull
    private final String stammNr;

    @NotNull
    @JsonIgnore
    private final Party creator;
    @NotNull
    @JsonIgnore
    private final List<Party> owners;

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return null;
    }

    @ConstructorForDeserialization
    public CarState(@NotNull UniqueIdentifier linearId, Party creator, @NotNull String make, @NotNull String model, @NotNull String type, @NotNull String stammNr, List<Party> owners) {
        this.linearId = linearId;
        this.creator = creator;
        this.make = make;
        this.model = model;
        this.type = type;
        this.stammNr = stammNr;
        this.owners = owners;
    }

    public Party getCreator() {
        return creator;
    }
    public String getCreatorX500() { return Participants.partyToX500(this.creator); }

    public List<Party> getOwners() {
        return owners;
    }
    public List<String> getOwnersX500() {
        return this.getOwners().stream().map(Participants::partyToX500).collect(Collectors.toList());
    }

    @NotNull
    public String getMake() {
        return make;
    }

    @NotNull
    public String getModel() {
        return model;
    }

    @NotNull
    public String getType() {
        return type;
    }

    @NotNull
    public String getStammNr() {
        return stammNr;
    }

    public CarState share(Party owner) {
        List<Party> list = new ArrayList<>(this.owners);
        list.add(owner);
        return new CarState(this.linearId, this.creator, this.make, this.model, this.type, this.stammNr, list);
    }
}
