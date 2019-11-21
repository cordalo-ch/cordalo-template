package ch.cordalo.template.flows;

import ch.cordalo.corda.common.contracts.JsonHelper;
import ch.cordalo.corda.common.contracts.StateMachine;
import ch.cordalo.corda.common.flows.*;
import ch.cordalo.corda.common.states.Parties;
import ch.cordalo.template.contracts.ServiceStateMachine;
import co.paralleluniverse.fibers.Suspendable;
import ch.cordalo.template.contracts.ServiceContract;
import ch.cordalo.template.states.ServiceState;
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
    public static class Create extends SimpleBaseFlow implements SimpleFlow.Create<ServiceState> {
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
    public static class Update extends SimpleBaseFlow implements SimpleFlow.Update<ServiceState> {
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
    public static class Delete extends SimpleBaseFlow implements SimpleFlow.Delete<ServiceState> {
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
    public static class Share extends BaseFlow {
        private final UniqueIdentifier id;
        private final Party serviceProvider;

        public Share(UniqueIdentifier id, Party serviceProvider) {
            this.id = id;
            this.serviceProvider = serviceProvider;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return this.progressTracker_sync;
        }


        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            getProgressTracker().setCurrentStep(PREPARATION);
            // We get a reference to our own identity.
            Party me = getOurIdentity();

            /* ============================================================================
             *         TODO 1 - Create our object !
             * ===========================================================================*/

            StateAndRef<ServiceState> serviceRef = new FlowHelper<ServiceState>(this.getServiceHub()).getLastStateByLinearId(ServiceState.class, this.id);
            if (serviceRef == null) {
                throw new FlowException("service with id "+this.id+" not found");
            }
            ServiceState service = this.getStateByRef(serviceRef);

            // We create our new TokenState.
            ServiceState sharedService = service.share(this.serviceProvider);

            /* ============================================================================
             *      TODO 3 - Build our issuance transaction to update the ledger!
             * ===========================================================================*/
            // We build our transaction.
            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    sharedService,
                    new ServiceContract.Commands.Share());
            transactionBuilder.addInputState(serviceRef);
            transactionBuilder.addOutputState(sharedService);

            /* ============================================================================
             *          TODO 2 - Write our contract to control issuance!
             * ===========================================================================*/
            // We check our transaction is valid based on its contracts.
            return signSyncCollectAndFinalize(this.serviceProvider, transactionBuilder);
        }

    }




    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Action extends BaseFlow {
        private final UniqueIdentifier id;
        private final String action;

        public Action(UniqueIdentifier id, String action) {
            this.id = id;
            this.action = action;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return this.progressTracker_sync;
        }

        private StateMachine.StateTransition getTransition() {
            return ServiceStateMachine.StateTransition(this.action);
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            getProgressTracker().setCurrentStep(PREPARATION);
            // We get a reference to our own identity.
            Party me = getOurIdentity();

            /* ============================================================================
             *         TODO 1 - Create our object !
             * ===========================================================================*/

            StateAndRef<ServiceState> serviceRef = new FlowHelper<ServiceState>(this.getServiceHub()).getLastStateByLinearId(ServiceState.class, this.id);
            if (serviceRef == null) {
                throw new FlowException("service with id "+this.id+" not found");
            }
            ServiceState service = this.getStateByRef(serviceRef);

            // We create our new TokenState.
            ServiceState newService = service.withAction(this.getTransition());

            /* ============================================================================
             *      TODO 3 - Build our issuance transaction to update the ledger!
             * ===========================================================================*/
            // We build our transaction.
            getProgressTracker().setCurrentStep(BUILDING);
            CommandData command = null;
            if (newService.getStateObject().isLaterState(ServiceStateMachine.State("SHARED"))) {
                command = new ServiceContract.Commands.ActionAfterShare(this.action);
            } else if (!newService.getState().equals(ServiceStateMachine.State("SHARED"))) {
                command = new ServiceContract.Commands.ActionBeforeShare(this.action);
            } else {
                throw new FlowException("Sharing cannot be executed as action");
            }
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    newService,
                    command);
            transactionBuilder.addInputState(serviceRef);
            transactionBuilder.addOutputState(newService);
            return signSyncCollectAndFinalize(new Parties(service, newService), transactionBuilder);
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