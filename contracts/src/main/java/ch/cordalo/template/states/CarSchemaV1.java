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

import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
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
