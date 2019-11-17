package ch.cordalo.template.flows;

import ch.cordalo.corda.common.flows.BaseFlow;
import ch.cordalo.corda.common.flows.ResponderBaseFlow;
import ch.cordalo.corda.common.flows.SimpleBaseFlow;
import ch.cordalo.corda.common.flows.SimpleFlow;
import co.paralleluniverse.fibers.Suspendable;
import ch.cordalo.template.contracts.E178EventContract;
import ch.cordalo.template.states.E178EventState;
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
    public static class Request extends SimpleBaseFlow implements SimpleFlow.Create<E178EventState> {

        private final String stammNr;
        private final Party leasing;
        private final String state;

        public Request(String stammNr, Party leasing, String state) {
            this.stammNr = stammNr;
            this.leasing = leasing;
            this.state = state;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Create(this, new E178EventContract.Commands.Request());
        }

        @Override
        @Suspendable
        public E178EventState create() throws FlowException {
            return E178EventState.request(this.stammNr, this.getOurIdentity(), this.leasing, this.state);
        }
    }

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Issue extends SimpleBaseFlow implements SimpleFlow.Update<E178EventState> {

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

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Update(
                    E178EventState.class, this.id, this, new E178EventContract.Commands.Issue());
        }

        @Override
        @Suspendable
        public E178EventState update(E178EventState state) throws FlowException {
            if (!state.getLeasing().equals(this.getOurIdentity())) {
                throw new FlowException("issue e178 must be done by leasing company");
            }
            return (this.state != null) ?
                    state.issue(this.state, this.regulator)  : state.issue(this.regulator);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class RequestInsurance extends SimpleBaseFlow implements SimpleFlow.Update<E178EventState> {

        private final UniqueIdentifier id;
        private final Party insurer;

        public RequestInsurance(UniqueIdentifier id, Party insurer) {
            this.id = id;
            this.insurer = insurer;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Update(E178EventState.class, this.id, this, new E178EventContract.Commands.RequestInsurance());
        }

        @Override
        @Suspendable
        public E178EventState update(E178EventState e178) throws FlowException {
            if (!e178.getRetailer().equals(this.getOurIdentity())) {
                throw new FlowException("request insurance of e178 must be done by a retailer company");
            }
            return e178.requestInsurance(this.insurer);
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Insure extends SimpleBaseFlow implements SimpleFlow.Update<E178EventState> {

        private final UniqueIdentifier id;

        public Insure(UniqueIdentifier id) {
            this.id = id;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Update(E178EventState.class, this.id, this, new E178EventContract.Commands.Insure());
        }

        @Override
        @Suspendable
        public E178EventState update(E178EventState e178) throws FlowException {
            if (!e178.getInsurer().equals(this.getOurIdentity())) {
                throw new FlowException("insure e178 must be done by an insurance company");
            }
            return e178.insure();
        }
    }

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Cancel extends SimpleBaseFlow implements SimpleFlow.Update<E178EventState> {

        private final UniqueIdentifier id;

        public Cancel(UniqueIdentifier id) {
            this.id = id;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Update(E178EventState.class, this.id, this, new E178EventContract.Commands.Cancel());
        }

        @Override
        @Suspendable
        public E178EventState update(E178EventState e178) throws FlowException {
            return e178.cancel();
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Register extends SimpleBaseFlow implements SimpleFlow.Update<E178EventState> {

        private final UniqueIdentifier id;

        public Register(UniqueIdentifier id) {
            this.id = id;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Update(E178EventState.class, this.id, this, new E178EventContract.Commands.Register());
        }

        @Override
        @Suspendable
        public E178EventState update(E178EventState e178) throws FlowException {
            if (!e178.getRegulator().equals(this.getOurIdentity())) {
                throw new FlowException("register e178 must be done by a regulator company");
            }
            return e178.registered();
        }
    }


    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class Delete extends SimpleBaseFlow implements SimpleFlow.Delete<E178EventState> {

        private final UniqueIdentifier id;

        public Delete(UniqueIdentifier id) {
            this.id = id;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            return this.simpleFlow_Delete(E178EventState.class, this.id, this, new E178EventContract.Commands.Delete());
        }

        @Override
        @Suspendable
        public void validateToDelete(E178EventState state) throws FlowException {

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

    @InitiatedBy(E178EventFlow.Cancel.class)
    public static class CancelResponder extends ResponderBaseFlow<E178EventState> {
        public CancelResponder(FlowSession otherFlow) {
            super(otherFlow);
        }
        @Suspendable
        @Override
        public Unit call() throws FlowException {
            return this.receiveIdentitiesCounterpartiesNoTxChecking();
        }
    }

}
