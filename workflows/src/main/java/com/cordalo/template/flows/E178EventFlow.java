package com.cordalo.template.flows;

import ch.cordalo.corda.common.flows.BaseFlow;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import co.paralleluniverse.fibers.Suspendable;
import com.cordalo.template.contracts.E178EventContract;
import com.cordalo.template.states.E178EventState;
import kotlin.Unit;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

public class E178EventFlow {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Request extends BaseFlow {

        private final Party leasing;
        private final String state;

        public Request(Party leasing, String state) {
            this.leasing = leasing;
            this.state = state;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return super.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            Party me = getOurIdentity();

            getProgressTracker().setCurrentStep(PREPARATION);
            E178EventState e178 = E178EventState.request(me, this.leasing, this.state);

            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    e178,
                    new E178EventContract.Commands.Request());
            transactionBuilder.addOutputState(e178);

            return signSyncCollectAndFinalize(e178.getParticipants(), transactionBuilder);
        }
    }

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Issue extends BaseFlow {

        private final UniqueIdentifier id;
        private final String state;
        private final Party regulator;

        public Issue(UniqueIdentifier id, String state, Party regulator) {
            this.id = id;
            this.state = state;
            this.regulator = regulator;
        }
        public Issue(UniqueIdentifier id, Party regulator) {
            this(id, null, regulator);
        }


        @Override
        public ProgressTracker getProgressTracker() {
            return super.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            Party me = getOurIdentity();

            getProgressTracker().setCurrentStep(PREPARATION);
            StateAndRef<E178EventState> e178StateRef = this.getLastStateByLinearId(E178EventState.class, this.id);
            E178EventState e178 = this.getStateByRef(e178StateRef);
            if (!e178.getLeasing().equals(me)) {
                throw new FlowException("issue e178 must be done by leasing company");
            }
            E178EventState issuedE178 = (this.state != null) ?
                e178.issue(this.state, this.regulator)  : e178.issue(this.regulator);

            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    issuedE178,
                    new E178EventContract.Commands.Issue());
            transactionBuilder.addInputState(e178StateRef);
            transactionBuilder.addOutputState(issuedE178);
            return signSyncCollectAndFinalize(issuedE178.getParticipants(), transactionBuilder);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class RequestInsurance extends BaseFlow {

        private final UniqueIdentifier id;
        private final Party insurer;

        public RequestInsurance(UniqueIdentifier id, Party insurer) {
            this.id = id;
            this.insurer = insurer;
        }


        @Override
        public ProgressTracker getProgressTracker() {
            return super.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            Party me = getOurIdentity();

            getProgressTracker().setCurrentStep(PREPARATION);
            StateAndRef<E178EventState> e178StateRef = this.getLastStateByLinearId(E178EventState.class, this.id);
            E178EventState e178 = this.getStateByRef(e178StateRef);
            if (!e178.getRetailer().equals(me)) {
                throw new FlowException("requestion insurance of e178 must be done by a retailer company");
            }
            E178EventState insuranceRequestedE178 = e178.requestInsurance(this.insurer);

            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    insuranceRequestedE178,
                    new E178EventContract.Commands.RequestInsurance());
            transactionBuilder.addInputState(e178StateRef);
            transactionBuilder.addOutputState(insuranceRequestedE178);
            return signSyncCollectAndFinalize(insuranceRequestedE178.getParticipants(), transactionBuilder);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Insure extends BaseFlow {

        private final UniqueIdentifier id;

        public Insure(UniqueIdentifier id) {
            this.id = id;
        }


        @Override
        public ProgressTracker getProgressTracker() {
            return super.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            Party me = getOurIdentity();

            getProgressTracker().setCurrentStep(PREPARATION);
            StateAndRef<E178EventState> e178StateRef = this.getLastStateByLinearId(E178EventState.class, this.id);
            E178EventState e178 = this.getStateByRef(e178StateRef);
            if (!e178.getInsurer().equals(me)) {
                throw new FlowException("insure e178 must be done by an insurance company");
            }
            E178EventState insuredE178 = e178.insure();

            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    insuredE178,
                    new E178EventContract.Commands.Insure());
            transactionBuilder.addInputState(e178StateRef);
            transactionBuilder.addOutputState(insuredE178);
            return signSyncCollectAndFinalize(insuredE178.getParticipants(), transactionBuilder);
        }
    }

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Cancel extends BaseFlow {

        private final UniqueIdentifier id;

        public Cancel(UniqueIdentifier id) {
            this.id = id;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return super.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            getProgressTracker().setCurrentStep(PREPARATION);
            StateAndRef<E178EventState> e178StateRef = this.getLastStateByLinearId(E178EventState.class, this.id);
            E178EventState e178 = this.getStateByRef(e178StateRef);

            E178EventState cancelledE178 = e178.cancel();

            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    cancelledE178,
                    new E178EventContract.Commands.Cancel());
            transactionBuilder.addInputState(e178StateRef);
            transactionBuilder.addOutputState(cancelledE178);
            return signSyncCollectAndFinalize(cancelledE178.getParticipants(), transactionBuilder);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Register extends BaseFlow {

        private final UniqueIdentifier id;

        public Register(UniqueIdentifier id) {
            this.id = id;
        }


        @Override
        public ProgressTracker getProgressTracker() {
            return super.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            Party me = getOurIdentity();

            getProgressTracker().setCurrentStep(PREPARATION);
            StateAndRef<E178EventState> e178StateRef = this.getLastStateByLinearId(E178EventState.class, this.id);
            E178EventState e178 = this.getStateByRef(e178StateRef);
            if (!e178.getRegulator().equals(me)) {
                throw new FlowException("register e178 must be done by a regulator company");
            }
            E178EventState registeredE178 = e178.registered();

            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    registeredE178,
                    new E178EventContract.Commands.Register());
            transactionBuilder.addInputState(e178StateRef);
            transactionBuilder.addOutputState(registeredE178);
            return signSyncCollectAndFinalize(registeredE178.getParticipants(), transactionBuilder);
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
            return super.progressTracker_sync;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            Party me = getOurIdentity();

            getProgressTracker().setCurrentStep(PREPARATION);
            StateAndRef<E178EventState> e178StateRef = this.getLastStateByLinearId(E178EventState.class, this.id);
            E178EventState e178State = this.getStateByRef(e178StateRef);

            getProgressTracker().setCurrentStep(BUILDING);
            TransactionBuilder transactionBuilder = getTransactionBuilderSignedByParticipants(
                    e178State,
                    new E178EventContract.Commands.Delete());
            transactionBuilder.addInputState(e178StateRef);

            return signSyncCollectAndFinalize(e178State.getParticipants(), transactionBuilder);
        }
    }



    @InitiatedBy(E178EventFlow.Request.class)
    public static class RequestResponder extends ResponderBaseFlow<E178EventState> {
        public RequestResponder(FlowSession otherFlow) {
            super(otherFlow);
        }
        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }


    @InitiatedBy(E178EventFlow.Issue.class)
    public static class IssueResponder extends ResponderBaseFlow<E178EventState> {
        public IssueResponder(FlowSession otherFlow) {
            super(otherFlow);
        }
        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }


    @InitiatedBy(E178EventFlow.RequestInsurance.class)
    public static class RequestInsuranceResponder extends ResponderBaseFlow<E178EventState> {
        public RequestInsuranceResponder(FlowSession otherFlow) {
            super(otherFlow);
        }
        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }


    @InitiatedBy(E178EventFlow.Insure.class)
    public static class InsureResponder extends ResponderBaseFlow<E178EventState> {
        public InsureResponder(FlowSession otherFlow) {
            super(otherFlow);
        }
        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }


    @InitiatedBy(E178EventFlow.Register.class)
    public static class RegisterResponder extends ResponderBaseFlow<E178EventState> {
        public RegisterResponder(FlowSession otherFlow) {
            super(otherFlow);
        }
        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

    @InitiatedBy(E178EventFlow.Delete.class)
    public static class DeleteResponder extends ResponderBaseFlow<E178EventState> {
        public DeleteResponder(FlowSession otherFlow) {
            super(otherFlow);
        }
        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }


}
