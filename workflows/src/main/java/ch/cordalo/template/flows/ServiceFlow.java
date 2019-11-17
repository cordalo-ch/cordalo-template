package ch.cordalo.template.flows;

import ch.cordalo.corda.common.contracts.JsonHelper;
import ch.cordalo.corda.common.flows.BaseFlow;
import ch.cordalo.corda.common.flows.FlowHelper;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import co.paralleluniverse.fibers.Suspendable;
import ch.cordalo.template.contracts.ServiceContract;
import ch.cordalo.template.contracts.StateMachine;
import ch.cordalo.template.states.ServiceState;
import kotlin.Unit;
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
    public static class Create extends BaseFlow {
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

        @Override
        public ProgressTracker getProgressTracker() {
            return this.progressTracker_nosync;
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
            // We create our new TokenState.
            ServiceState serviceRecord = new ServiceState(
                    new UniqueIdentifier(),
                    this.serviceName,
                    me,
                    StateMachine.State.CREATED,
                    JsonHelper.convertStringToJson(this.data),
                    null, this.price);

            /* ============================================================================
             *      TODO 3 - Build our issuance transaction to update the ledger!
             * ===========================================================================*/
            // We build our transaction.
            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    serviceRecord,
                    new ServiceContract.Commands.Create());
            transactionBuilder.addOutputState(serviceRecord);

            /* ============================================================================
             *          TODO 2 - Write our contract to control issuance!
             * ===========================================================================*/
            // We check our transaction is valid based on its contracts.
            return signAndFinalize(transactionBuilder);
        }

    }



    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Update extends BaseFlow {
        private final UniqueIdentifier id;
        private final String data;
        private final Integer price;

        public Update(UniqueIdentifier id, String data, Integer price) {
            this.id = id;
            this.data = data;
            this.price = price;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return this.progressTracker_nosync;
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
            ServiceState sharedService = service.update(
                    JsonHelper.convertStringToJson(this.data),
                    this.price
            );

            /* ============================================================================
             *      TODO 3 - Build our issuance transaction to update the ledger!
             * ===========================================================================*/
            // We build our transaction.
            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    sharedService,
                    new ServiceContract.Commands.Update());
            transactionBuilder.addInputState(serviceRef);
            transactionBuilder.addOutputState(sharedService);

            /* ============================================================================
             *          TODO 2 - Write our contract to control issuance!
             * ===========================================================================*/
            // We check our transaction is valid based on its contracts.
            return signAndFinalize(transactionBuilder);
        }

    }

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Delete extends BaseFlow {
        private final UniqueIdentifier id;

        public Delete(UniqueIdentifier id) {
            this.id = id;
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

            /* ============================================================================
             *      TODO 3 - Build our issuance transaction to update the ledger!
             * ===========================================================================*/
            // We build our transaction.
            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    service,
                    new ServiceContract.Commands.Delete());
            transactionBuilder.addInputState(serviceRef);

            /* ============================================================================
             *          TODO 2 - Write our contract to control issuance!
             * ===========================================================================*/
            // We check our transaction is valid based on its contracts.
            if (service.getState().isLaterState(StateMachine.State.SHARED)) {
                // We check our transaction is valid based on its contracts.
                if (service.getServiceProvider() == null) {
                    return signAndFinalize(transactionBuilder);
                } else {
                    return signSyncCollectAndFinalize(service.getCounterParties(me), transactionBuilder);
                }
            } else if (!service.getState().equals(StateMachine.State.SHARED)) {
                return signAndFinalize(transactionBuilder);
            }
            return signSyncCollectAndFinalize(service.getCounterParties(me), transactionBuilder);
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
            return StateMachine.StateTransition.valueOf(this.action);
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
            if (newService.getState().isLaterState(StateMachine.State.SHARED)) {
                // new state is follow up state of SHARED
                TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                        newService,
                        new ServiceContract.Commands.ActionAfterShare(this.action));
                transactionBuilder.addInputState(serviceRef);
                transactionBuilder.addOutputState(newService);

                /* ============================================================================
                 *          TODO 2 - Write our contract to control issuance!
                 * ===========================================================================*/
                // We check our transaction is valid based on its contracts.
                if (newService.getServiceProvider() == null) {
                    return signAndFinalize(transactionBuilder);
                } else {
                    return signSyncCollectAndFinalize(newService.getCounterParties(me), transactionBuilder);
                }
            } else if (!newService.getState().equals(StateMachine.State.SHARED)) {
                // current state is predecessor of SHARED or parallel states of shared
                getProgressTracker().setCurrentStep(BUILDING);
                TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                        service,
                        new ServiceContract.Commands.ActionBeforeShare(this.action));
                transactionBuilder.addInputState(serviceRef);
                transactionBuilder.addOutputState(newService);

                /* ============================================================================
                 *          TODO 2 - Write our contract to control issuance!
                 * ===========================================================================*/
                return signAndFinalize(transactionBuilder);
            } else {
                throw new FlowException("Sharing cannot be executed as action");
            }
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