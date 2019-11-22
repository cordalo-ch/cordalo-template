package ch.cordalo.template.flows;

import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import ch.cordalo.corda.common.flows.SimpleBaseFlow;
import ch.cordalo.corda.common.flows.SimpleFlow;
import ch.cordalo.template.contracts.CarContract;
import ch.cordalo.template.states.CarState;
import co.paralleluniverse.fibers.Suspendable;
import kotlin.Unit;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CarFlow {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Create extends SimpleBaseFlow implements SimpleFlow.Create<CarState> {

        @NotNull
        private final UniqueIdentifier id;
        @NotNull
        private final String make;
        @NotNull
        private final String model;
        @NotNull
        private final String type;
        @NotNull
        private final String stammNr;
        @NotNull
        private final List<Party> owners;

        public Create(@NotNull UniqueIdentifier id, @NotNull String make, @NotNull String model, @NotNull String type, @NotNull String stammNr, @NotNull List<Party> owners) {
            this.id = id;
            this.make = make;
            this.model = model;
            this.type = type;
            this.stammNr = stammNr;
            this.owners = owners;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Create(this, new CarContract.Commands.Create());
        }

        @Override
        @Suspendable
        public CarState create() {
            return new CarState(this.id, this.getOurIdentity(), this.make, this.model, this.type, this.stammNr, this.owners);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Update extends SimpleBaseFlow implements SimpleFlow.Update<CarState> {

        @NotNull
        private final UniqueIdentifier id;
        @NotNull
        private final String make;
        @NotNull
        private final String model;
        @NotNull
        private final String type;

        public Update(@NotNull UniqueIdentifier id, @NotNull String make, @NotNull String model, @NotNull String type) {
            this.id = id;
            this.make = make;
            this.model = model;
            this.type = type;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Update(CarState.class, this.id, this, new CarContract.Commands.Update());
        }

        @Override
        @Suspendable
        public CarState update(CarState state) throws FlowException {
            return state.update(this.make, this.model, this.type);
        }
    }


    @InitiatedBy(CarFlow.Create.class)
    public static class CreateResponder extends ResponderBaseFlow<CarState> {

        public CreateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }


    @InitiatedBy(CarFlow.Update.class)
    public static class UpdateResponder extends ResponderBaseFlow<CarState> {

        public UpdateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }
}
