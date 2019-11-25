package ch.cordalo.template.flows;

import ch.cordalo.corda.common.flows.FlowHelper;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import ch.cordalo.corda.common.flows.SimpleBaseFlow;
import ch.cordalo.corda.common.flows.SimpleFlow;
import ch.cordalo.template.contracts.CarContract;
import ch.cordalo.template.states.CarSchemaV1;
import ch.cordalo.template.states.CarState;
import co.paralleluniverse.fibers.Suspendable;
import kotlin.Unit;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.Builder;
import net.corda.core.node.services.vault.CriteriaExpression;
import net.corda.core.node.services.vault.FieldInfo;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.corda.core.node.services.vault.QueryCriteriaUtils.getField;

public class CarFlow {

    @NotNull
    @Suspendable
    protected static QueryCriteria getStammNrQueryCriteria(String stammNr) throws FlowException {
        QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        FieldInfo stammNrField = null;
        try {
            stammNrField = getField("stammNr", CarSchemaV1.PersistentCar.class);
        } catch (NoSuchFieldException e) {
            throw new FlowException("error while getting stammNr fields", e);
        }

        CriteriaExpression stammNrCriteria = Builder.equal(stammNrField, stammNr);
        QueryCriteria stammNrQuery = new QueryCriteria.VaultCustomQueryCriteria(stammNrCriteria);

        return generalCriteria.and(stammNrQuery);
    }

    @NotNull
    @Suspendable
    protected static StateAndRef<CarState> getCarByStammNr(String stammNr, ServiceHub serviceHub) throws FlowException {
        QueryCriteria query = getStammNrQueryCriteria(stammNr);
        return new FlowHelper<CarState>(serviceHub)
                .getLastStateByCriteria(CarState.class, query);
    }

    @NotNull
    @Suspendable
    protected static StateAndRef<CarState> getCarByLinearId(UniqueIdentifier id, ServiceHub serviceHub) throws FlowException {
        return new FlowHelper<CarState>(serviceHub)
                .getLastStateByLinearId(CarState.class, id);
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Create extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Create<CarState> {

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
    public static class Update extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Update<CarState> {

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
    public static class Share extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Update<CarState> {

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
    public static class Search extends SimpleBaseFlow<CarState> {

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
            /* search on local vault if already shared */
            StateAndRef<CarState> carByStammNr = getCarByStammNr(this.stammNr, this.getServiceHub());
            if (carByStammNr != null) {
                return carByStammNr.getState().getData();
            }

            /* initiate flow at counterparty to get LinearId from car after successful sharing within responder */
            FlowSession flowSession = this.initiateFlow(this.cardossier);
            UniqueIdentifier carLinearId = flowSession.sendAndReceive(UniqueIdentifier.class, this.stammNr).unwrap(id -> {
                return id;
            });
            /* car not found and not synched */
            if (carLinearId == null) {
                return null;
            }
            /* car found and synched with linear Id */
            StateAndRef<CarState> lastStateByLinearId = getCarByLinearId(carLinearId, this.getServiceHub());
            if (lastStateByLinearId == null) {
                throw new FlowException("Car not found in vault after search & share id=" + carLinearId);
            }
            return lastStateByLinearId.getState().getData();
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
            StateAndRef<CarState> carByStammNr = getCarByStammNr(stammNr, this.getServiceHub());
            if (carByStammNr == null) {
                this.otherFlow.send(null);
                return null;
            }

            CarState car = carByStammNr.getState().getData();
            /* try to share officially with corda the car and send back */
            Share shareFlow = new Share(car.getLinearId(), this.otherFlow.getCounterparty());
            subFlow(shareFlow);

            /* send back car linear id to counter party */
            this.otherFlow.send(car.getLinearId());
            return null;
        }
    }

}
