package ch.cordalo.template.contracts;

import ch.cordalo.corda.common.contracts.CommandVerifier;
import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.template.states.ServiceState;
import kotlin.Pair;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class ServiceContract implements Contract {
    public static final String ID = "ch.cordalo.template.contracts.ServiceContract";

    public ServiceContract() {
    }

    public interface Commands extends CommandData {
        public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;

        class Common implements ServiceContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {

            }

            public Pair<ServiceState, ServiceState> verify1InOut(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                return requireThat(req -> {

                    CommandVerifier.Parameters<ServiceState> params = new CommandVerifier.Parameters<>();
                    params.notEmpty(
                            ServiceState::getLinearId, ServiceState::getState,
                            ServiceState::getInitiator, ServiceState::getServiceName);
                    params.equal(ServiceState::getLinearId, ServiceState::getInitiator);
                    new CommandVerifier(verifier).verify_update1(ServiceState.class, params);


                    ServiceState service1 = verifier.input().notEmpty().one().one(ServiceState.class).object();
                    ServiceState service2 = verifier
                            .output().notEmpty().one(ServiceState.class)
                            .object();
                    req.using("ID must be the same",
                            service1.getLinearId().equals(service2.getLinearId()));
                    req.using("initiator must be the same",
                            service1.getInitiator().equals(service2.getInitiator()));
                    return new Pair<>(service1, service2);
                });
            }

        }

        class Create extends Common implements ServiceContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().empty("input must be empty");
                    ServiceState service = verifier
                            .output()
                            .one().one(ServiceState.class)
                            .isEmpty(ServiceState::getServiceProvider, "service provider must be empty on creation")
                            .object();
                    req.using("state must be an initial state",
                            ServiceStateMachine.StateTransition("CREATE")
                              .getInitialState()
                                  .equals(service.getState()));
                    return null;
                });
            }
        }
        class Update extends Common implements ServiceContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    Pair<ServiceState, ServiceState> pair = verify1InOut(tx, verifier);
                    ServiceState service1 = pair.component1();
                    ServiceState service2 = pair.component2();
                    req.using("state must be the same",
                            service1.getState().equals(service2.getState()));
                    req.using("state <"+service2.getState()+"> is not valid next state from <"+service1.getState()+">",
                        ServiceStateMachine.StateTransition("UPDATE")
                                .getNextStateFrom(service1.getStateObject())
                                    .equals(service2.getState()));
                    if (service1.getServiceProvider() == null) {
                            req.using("service provider must be both null",
                                    service2.getServiceProvider() == null);
                    } else {
                       req.using("service provider must be the same",
                                service1.getServiceProvider().equals(service2.getServiceProvider()));
                        req.using("service provider must be different than initiator",
                                !service2.getInitiator().equals(service2.getServiceProvider()));
                    }
                    return null;
                });
            }
        }
        class Delete extends Common implements ServiceContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    ServiceState service = verifier
                            .input().one().one(ServiceState.class)
                            .object();
                    verifier.output().empty("output must be empty");
                    return null;
                });
            }
        }
        class Share extends Common implements ServiceContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    Pair<ServiceState, ServiceState> pair = verify1InOut(tx, verifier);
                    ServiceState service1 = pair.component1();
                    ServiceState service2 = pair.component2();
                    req.using("state must be different",
                            !service1.getState().equals(service2.getState()));
                    req.using("state <"+service2.getState()+"> is not valid next state from <"+service1.getState()+">",
                            ServiceStateMachine.StateTransition("SHARE")
                                    .getNextStateFrom(service1.getStateObject())
                                    .equals(service2.getState()));
                    if (service1.getServiceProvider() != null) {
                        req.using("service provider must be the same",
                                service1.getServiceProvider().equals(service2.getServiceProvider()));
                    }
                    req.using("service provider must be provided",
                            service2.getServiceProvider() != null);
                    req.using("service provider must be different than initiator",
                            !service2.getInitiator().equals(service2.getServiceProvider()));
                    return null;
                });
            }
        }

        class AnyAction extends Common implements ServiceContract.Commands {
            private final ServiceStateMachine.StateTransition transition;
            public AnyAction(String transition) {
                this.transition = ServiceStateMachine.StateTransition(transition);
            }
            @ConstructorForDeserialization
            public AnyAction(ServiceStateMachine.StateTransition transition) {
                this.transition = transition;
            }
            public ServiceStateMachine.StateTransition getTransition() {
                return this.transition;
            }

            public Pair<ServiceState, ServiceState> verifyAnyAction(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                return requireThat(req -> {
                    Pair<ServiceState, ServiceState> pair = verify1InOut(tx, verifier);
                    ServiceState service1 = pair.component1();
                    ServiceState service2 = pair.component2();
                    req.using("state must be different",
                            !service1.getState().equals(service2.getState()));
                    req.using("state <"+service2.getState()+"> is not valid next state from <"+service1.getState()+">",
                            this.getTransition()
                                    .getNextStateFrom(service1.getStateObject())
                                    .equals(service2.getState()));
                    return pair;
                });
            }
        }
        class ActionBeforeShare extends AnyAction implements ServiceContract.Commands {

            public ActionBeforeShare(String transition) {
                super(transition);
            }
            @ConstructorForDeserialization
            public ActionBeforeShare(ServiceStateMachine.StateTransition transition) {
                super(transition);
            }

            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    Pair<ServiceState, ServiceState> pair = verifyAnyAction(tx, verifier);
                    ServiceState service1 = pair.component1();
                    ServiceState service2 = pair.component2();
                    if (service1.getServiceProvider() == null) {
                        req.using("service provider must be both null",
                                service2.getServiceProvider() == null);
                    } else {
                        req.using("service provider must be the same",
                                service1.getServiceProvider().equals(service2.getServiceProvider()));
                        req.using("service provider must be different than initiator",
                                !service2.getInitiator().equals(service2.getServiceProvider()));
                    }
                    return null;
                });
            }
        }

        class ActionAfterShare extends AnyAction implements ServiceContract.Commands {

            public ActionAfterShare(String transition) {
                super(transition);
            }
            @ConstructorForDeserialization
            public ActionAfterShare(ServiceStateMachine.StateTransition transition) {
                super(transition);
            }

            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    Pair<ServiceState, ServiceState> pair = verifyAnyAction(tx, verifier);
                    ServiceState service1 = pair.component1();
                    ServiceState service2 = pair.component2();
                    req.using("service provider must be set",
                            service1.getServiceProvider() != null);
                    req.using("service provider must be set",
                            service2.getServiceProvider() != null);
                    req.using("service provider must be the same",
                            service1.getServiceProvider().equals(service2.getServiceProvider()));
                    req.using("service provider must be different than initiator",
                            !service2.getInitiator().equals(service2.getServiceProvider()));
                    return null;
                });
            }
        }
/*
        @CordaSerializable
        public class Reference extends ReferenceContract.Commands.Reference<ServiceState> implements ServiceContract.Commands {
            public Reference(ServiceState myState) {
                super(myState);
            }

            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                this.verify(tx);
            }
        }
 */
    }

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, ServiceContract.Commands.class);
        ServiceContract.Commands commandData = (ServiceContract.Commands)verifier.command();
        commandData.verify(tx, verifier);
    }

    private void verifyAllSigners(StateVerifier verifier) {
        requireThat(req -> {
            verifier
                    .output()
                    .participantsAreSigner("all participants must be signer");
            return null;
        });
    }


}
