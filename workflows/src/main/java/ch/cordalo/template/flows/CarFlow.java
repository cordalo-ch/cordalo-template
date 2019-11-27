/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.flows;

import ch.cordalo.corda.common.flows.*;
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
    public static class Search extends SimpleBaseFlow<CarState> implements SimpleFlow.Search<CarState, String> {

        @NotNull
        private final String stammNr;
        @NotNull
        private final Party cardossier;

        public Search(String stammNr, Party cardossier) {
            this.stammNr = stammNr;
            this.cardossier = cardossier;
        }

        @Suspendable
        @Override
        public CarState call() throws FlowException {
            return this.simpleFlow_Search(CarState.class, this, this.cardossier);
        }

        @Override
        @Suspendable
        public CarState search(FlowHelper<CarState> flowHelper, String valueToSearch) throws FlowException {
            StateAndRef<CarState> carByStammNr = getCarByStammNr(valueToSearch, this.getServiceHub());
            return carByStammNr == null ? null : carByStammNr.getState().getData();
        }

        @Override
        @Suspendable
        public String getValueToSearch() {
            return this.stammNr;
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
    public static class SearchResponder extends SearchResponderBaseFlow implements SimpleFlow.SearchResponder<CarState, String, SignedTransaction> {

        public SearchResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.responderFlow_receiveAndSend(String.class, this);
        }

        @Override
        @Suspendable
        public CarState search(FlowHelper<CarState> flowHelper, String valueToSearch) throws FlowException {
            StateAndRef<CarState> carByStammNr = getCarByStammNr(valueToSearch, this.getServiceHub());
            return carByStammNr == null ? null : carByStammNr.getState().getData();
        }

        @Override
        @Suspendable
        public FlowLogic<SignedTransaction> createShareStateFlow(CarState state, Party counterparty) {
            return new CarFlow.Share(state.getLinearId(), counterparty);
        }
    }

}
