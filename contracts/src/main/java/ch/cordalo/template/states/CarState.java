package ch.cordalo.template.states;

import ch.cordalo.corda.common.states.CordaloLinearState;
import ch.cordalo.corda.common.states.Parties;
import ch.cordalo.template.contracts.CarContract;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@BelongsToContract(CarContract.class)
public class CarState extends CordaloLinearState implements QueryableState {

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
    @JsonIgnore
    protected Parties getParties() {
        return Parties.fromParties(this.creator).add(this.owners);
    }

    @ConstructorForDeserialization
    public CarState(@NotNull UniqueIdentifier linearId, Party creator, @NotNull String make, @NotNull String model, @NotNull String type, @NotNull String stammNr, List<Party> owners) {
        super(linearId);
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
    public String getCreatorX500() { return Parties.partyToX500(this.creator); }

    public List<Party> getOwners() {
        return owners;
    }
    public List<String> getOwnersX500() {
        return Parties.fromParties(this.owners).getPartiesX500();
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

    public CarState update(@NotNull String make, @NotNull String model, @NotNull String type) {
        return new CarState(this.linearId, this.creator, make, model, type, this.stammNr, this.owners);
    }

    public CarState share(Party owner) {
        List<Party> list = new ArrayList<>(this.owners);
        list.add(owner);
        return new CarState(this.linearId, this.creator, this.make, this.model, this.type, this.stammNr, list);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CarState)) return false;
        if (!super.equals(o)) return false;
        CarState carState = (CarState) o;
        return getMake().equals(carState.getMake()) &&
                getModel().equals(carState.getModel()) &&
                getType().equals(carState.getType()) &&
                getStammNr().equals(carState.getStammNr()) &&
                getCreator().equals(carState.getCreator()) &&
                getOwners().equals(carState.getOwners());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getMake(), getModel(), getType(), getStammNr(), getCreator(), getOwners());
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        if (schema != null) {
            return new CarSchemaV1.PersistentCar(
                    this.getLinearId().getId(),
                    this.getMake(),
                    this.getModel(),
                    this.getType(),
                    this.getStammNr(),
                    this.getCreatorX500(),
                    this.getOwnersX500().stream().collect(Collectors.joining("|"))
            );
        } else {
            throw new IllegalArgumentException("Unrecognised schema "+schema);
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return Lists.newArrayList(new CarSchemaV1());
    }
}
