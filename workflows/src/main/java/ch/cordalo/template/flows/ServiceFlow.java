package ch.cordalo.template.flows;

import ch.cordalo.corda.common.contracts.JsonHelper;
import ch.cordalo.corda.common.contracts.StateMachine;
import ch.cordalo.corda.common.flows.*;
import ch.cordalo.corda.common.states.Parties;
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


public class ServiceFlow {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Create extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Create<ServiceState> {
        private final String serviceName;
        private final String data;
        private final Integer price;

        public Create(String serviceName, String data, Integer price) {
            this.serviceName = serviceName;
            this.data = data;
            this.price = price;
        }

        public Create(String serviceName) {
            this.serviceName = serviceName;
            this.data = "{}";
            this.price = 0;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Create(this, new ServiceContract.Commands.Create());
        }

        @Override
        @Suspendable
        public ServiceState create() throws FlowException {
            return new ServiceState(
                    new UniqueIdentifier(),
                    this.serviceName,
                    this.getOurIdentity(),
                    ServiceStateMachine.State("CREATED"),
                    JsonHelper.convertStringToJson(this.data),
                    null, this.price);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Update extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Update<ServiceState> {
        private final UniqueIdentifier id;
        private final String data;
        private final Integer price;

        public Update(UniqueIdentifier id, String data, Integer price) {
            this.id = id;
            this.data = data;
            this.price = price;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Update(
                    ServiceState.class,
                    this.id,
                    this,
                    new ServiceContract.Commands.Update()
            );
        }

        @Override
        @Suspendable
        public ServiceState update(ServiceState state) throws FlowException {
            return state.update(
                    JsonHelper.convertStringToJson(this.data),
                    this.price
            );
        }
    }

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Delete extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Delete<ServiceState> {
        private final UniqueIdentifier id;

        public Delete(UniqueIdentifier id) {
            this.id = id;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Delete(
                    ServiceState.class,
                    this.id,
                    this,
                    new ServiceContract.Commands.Delete()
            );
        }

        @Override
        @Suspendable
        public void validateToDelete(ServiceState state) throws FlowException {

        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Share extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.Update<ServiceState> {
        private final UniqueIdentifier id;
        private final Party serviceProvider;

        public Share(UniqueIdentifier id, Party serviceProvider) {
            this.id = id;
            this.serviceProvider = serviceProvider;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Update(
                    ServiceState.class, this.id, this, new ServiceContract.Commands.Share());
        }

        @Override
        @Suspendable
        public ServiceState update(ServiceState state) throws FlowException {
            return state.share(this.serviceProvider);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Action extends SimpleBaseFlow<SignedTransaction> implements SimpleFlow.UpdateBuilder<ServiceState> {
        private final UniqueIdentifier id;
        private final String action;

        public Action(UniqueIdentifier id, String action) {
            this.id = id;
            this.action = action;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_UpdateBuilder(ServiceState.class, this.id, this);
        }

        @Override
        @Suspendable
        public CommandData getCommand(StateAndRef<ServiceState> stateRef, ServiceState state, ServiceState newState) throws FlowException {
            if (newState.getStateObject().isLaterState(ServiceStateMachine.State("SHARED"))) {
                return new ServiceContract.Commands.ActionAfterShare(this.action);
            } else if (!newState.getState().equals(ServiceStateMachine.State("SHARED"))) {
                return new ServiceContract.Commands.ActionBeforeShare(this.action);
            } else {
                throw new FlowException("Sharing cannot be executed as action");
            }
        }

        @Override
        @Suspendable
        public void updateBuilder(TransactionBuilder transactionBuilder, StateAndRef<ServiceState> stateRef, ServiceState state, ServiceState newState) throws FlowException {
            transactionBuilder.addInputState(stateRef);
            transactionBuilder.addOutputState(newState);
        }

        @Override
        @Suspendable
        public ServiceState update(ServiceState state) throws FlowException {
            return state.withAction(
                    ServiceStateMachine.StateTransition(this.action));
        }
    }


    @InitiatedBy(ServiceFlow.Create.class)
    public static class CreateResponder extends ResponderBaseFlow<ServiceState> {

        public CreateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

    @InitiatedBy(ServiceFlow.Update.class)
    public static class UpdateResponder extends ResponderBaseFlow<ServiceState> {

        public UpdateResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }


    @InitiatedBy(ServiceFlow.Delete.class)
    public static class DeleteResponder extends ResponderBaseFlow<ServiceState> {

        public DeleteResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

    @InitiatedBy(ServiceFlow.Share.class)
    public static class ShareResponder extends ResponderBaseFlow<ServiceState> {

        public ShareResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

    @InitiatedBy(Action.class)
    public static class ActionResponder extends ResponderBaseFlow<ServiceState> {

        public ActionResponder(FlowSession otherFlow) {
            super(otherFlow);
        }

        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

}