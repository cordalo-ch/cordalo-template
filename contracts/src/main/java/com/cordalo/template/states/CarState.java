package com.cordalo.template.states;

import com.cordalo.template.contracts.ChatMessageContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@BelongsToContract(ChatMessageContract.class)
public class CarState implements LinearState {

    @NotNull
    private UniqueIdentifier id;

    @NotNull
    private String make;

    @NotNull
    private String model;

    @NotNull
    private String type;

    @NotNull
    private String stammNr;

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return id;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return null;
    }

    @ConstructorForDeserialization
    public CarState(@NotNull UniqueIdentifier id, @NotNull String make, @NotNull String model, @NotNull String type, @NotNull String stammNr) {
        this.id = id;
        this.make = make;
        this.model = model;
        this.type = type;
        this.stammNr = stammNr;
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
}
