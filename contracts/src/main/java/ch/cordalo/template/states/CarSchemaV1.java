package ch.cordalo.template.states;

import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@CordaSerializable
public class CarSchemaV1 extends MappedSchema {

    public CarSchemaV1() {
        super(CarSchema.class, 1, ImmutableList.of(PersistentCar.class));
    }

    @Entity
    @Table(name = "car", indexes = @Index(name = "stammnr_idx", columnList = "stammNr", unique = false))
    public static class PersistentCar extends PersistentState implements Serializable {
        @Column(name = "linearId", nullable = false)
        UUID linearId;

        @Column(name = "make", nullable = false)
        String make;
        @Column(name = "model", nullable = false)
        String model;
        @Column(name = "type", nullable = false)
        String type;
        @Column(name = "stammNr", nullable = false)
        String stammNr;
        @Column(name = "creator", nullable = false)
        String creator;
        @Column(name = "owners")
        String owners;

        public PersistentCar(UUID linearId, String make, String model, String type, String stammNr, String creator, String owners) {
            this.linearId = linearId;
            this.make = make;
            this.model = model;
            this.type = type;
            this.stammNr = stammNr;
            this.creator = creator;
            this.owners = owners;
        }
        public PersistentCar() {
            this(UUID.randomUUID(), "", "", "", "", "", "");
        }
    }
}
