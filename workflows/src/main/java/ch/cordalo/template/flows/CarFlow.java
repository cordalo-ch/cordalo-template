package ch.cordalo.template.flows;

import ch.cordalo.corda.common.contracts.StateMachine;
import ch.cordalo.corda.common.flows.*;
import ch.cordalo.corda.common.states.Parties;
import ch.cordalo.template.contracts.CarContract;
import ch.cordalo.template.contracts.ServiceContract;
import ch.cordalo.template.contracts.ServiceStateMachine;
import ch.cordalo.template.states.CarState;
import ch.cordalo.template.states.ServiceState;
import co.paralleluniverse.fibers.Suspendable;
import kotlin.Unit;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.UntrustworthyData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Share extends SimpleBaseFlow implements SimpleFlow.Update<CarState> {

        @NotNull
        private final UniqueIdentifier id;
        @NotNull
        private final Party to;

        public Share(@NotNull UniqueIdentifier id, @NotNull Party to) {
            this.id = id;
            this.to = to;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Update(
                    CarState.class, this.id, this, new CarContract.Commands.Share());
        }

        @Override
        @Suspendable
        public CarState update(CarState state) throws FlowException {
            return state.share(this.to);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Search extends FlowLogic<CarState> {

        @NotNull
        private final UniqueIdentifier id;
        @NotNull
        private final Party cardossier;
        @NotNull
        private final String stammNr;

        public Search(@NotNull UniqueIdentifier id, Party cardossier, String stammNr) {
            this.id = id;
            this.cardossier = cardossier;
            this.stammNr = stammNr;
        }

        @Suspendable
        @Override
        public CarState call() throws FlowException {
            FlowSession flowSession = this.initiateFlow(this.cardossier);
            CarState car = flowSession.sendAndReceive(CarState.class, this.stammNr).unwrap(data -> {
                return data;
            });
            return car;
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

    @InitiatedBy(CarFlow.Share.class)
    public static class ShareResponder extends ResponderBaseFlow<CarState> {

        public ShareResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }


    /* running in counter party node */

    @InitiatedBy(CarFlow.Search.class)
    public static class SearchResponder extends ResponderBaseFlow<CarState> {

        public SearchResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            /* receive the requested StammNr from sender */
            String stammNr = this.otherFlow.receive(String.class).unwrap(data -> {
                return data;
            });
            /* search unconsumed car by StammNr */
            StateAndRef<CarState> lastStateRef = new FlowHelper<CarState>(this.getServiceHub()).getLastState(CarState.class);
            if (lastStateRef == null) {
                throw new FlowException("no car found in vault with stammNr "+stammNr);
            }
            CarState car = lastStateRef.getState().getData();

            /* try to share officially with corda the car and send back */
            // Share shareFlow = new Share(car.getLinearId(), this.otherFlow.getCounterparty());
            // subFlow(shareFlow);

            /* send back car to counter party */
            this.otherFlow.send(car);
            return null;
        }
    }
}
