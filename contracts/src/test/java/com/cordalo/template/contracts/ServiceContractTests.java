package com.cordalo.template.contracts;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.cordalo.template.CordaloTemplateBaseTests;
import com.cordalo.template.states.ServiceState;
import com.cordalo.template.states.ServiceStateTests;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class ServiceContractTests extends CordaloTemplateBaseTests {

    @Before
    public void setup() {
        super.setup(false);
    }

    private ServiceState newService() {
        return ServiceState.create(
                new UniqueIdentifier(), "insurance", this.insurance1.party,
                JsonHelper.convertStringToJson(ServiceStateTests.dataJSONString()));
    }
    private ServiceState updateService(ServiceState service) {
        return service.update(JsonHelper.convertStringToJson(ServiceStateTests.dataUpdateJSONString()));
    }
    private ServiceState shareService(ServiceState service, @NotNull Party serviceProvider) {
        return service.share(serviceProvider);
    }

    private ServiceState withAction(ServiceState service, StateMachine.StateTransition transition) {
        return service.withAction(transition);
    }
    private ServiceState withAction(ServiceState service, String transition) {
        return service.withAction(StateMachine.StateTransition.valueOf(transition));
    }

    private ServiceState updateAfterShareService(ServiceState service) {
        return service.update(JsonHelper.convertStringToJson(ServiceStateTests.dataUpdateAfterShareJSONString()));
    }

    private ServiceState setInvalidState(ServiceState serviceState, StateMachine.State newState) {
        return new ServiceState(
                serviceState.getId(),
                serviceState.getServiceName(),
                serviceState.getInitiator(),
                newState,
                serviceState.getServiceData(),
                serviceState.getServiceProvider(),
                serviceState.getPrice());
    }
    private ServiceState setInvalidStateProvider(ServiceState serviceState, StateMachine.State newState, Party newProvider) {
        return new ServiceState(
                serviceState.getId(),
                serviceState.getServiceName(),
                serviceState.getInitiator(),
                newState,
                serviceState.getServiceData(),
                newProvider,
                serviceState.getPrice());
    }


    @Test
    public void service_create_normal_no_initial_state() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = setInvalidState(service1, StateMachine.State.SHARED);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Create());
            tx.failsWith("state must be an initial state");
            return null;
        });
    }

    @Test
    public void service_create_normal_input_not_empty() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = newService();
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Create());
            tx.failsWith("input must be empty");
            return null;
        });
    }

    @Test
    public void service_create_normal_double_output() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = newService();
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Create());
            tx.failsWith("input must be empty");
            return null;
        });
    }

    @Test
    public void service_create_normal_double_input() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = newService();
            tx.output(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Create());
            tx.failsWith("List must contain only 1 entry");
            return null;
        });

    }

    @Test
    public void service_create_normal() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            tx.output(ServiceContract.ID, service1);
            tx.command(service1.getParticipantKeys(), new ServiceContract.Commands.Create());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void service_update_normal_no_input() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = updateService(service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Update());
            tx.failsWith("List should not be empty");
            return null;
        });

    }

    @Test
    public void service_update_normal_not_same_ID() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = updateService(newService());
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Update());
            tx.fails();
            return null;
        });
    }

    @Test
    public void service_update_normal_ACCEPTED_invalid_state() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service1a = setInvalidState(service1, StateMachine.State.ACCEPTED);
            ServiceState service2 = setInvalidState(updateService(service1), StateMachine.State.ACCEPTED);
            tx.input(ServiceContract.ID, service1a);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Update());
            tx.failsWith("state <ACCEPTED> is final state and cannot be transitioned");
            return null;
        });

    }

    @Test
    public void service_update_normal_WITHDRAWN_invalid_state() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service1a = setInvalidState(service1, StateMachine.State.NOT_SHARED);
            ServiceState service2 = setInvalidState(updateService(service1), StateMachine.State.NOT_SHARED);
            tx.input(ServiceContract.ID, service1a);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Update());
            tx.failsWith("state <NOT_SHARED> is final state and cannot be transitioned");
            return null;
        });

    }

    @Test
    public void service_update_normal_DECLINED_invalid_state() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service1a = setInvalidState(service1, StateMachine.State.DECLINED);
            ServiceState service2 = setInvalidState(updateService(service1), StateMachine.State.DECLINED);
            tx.input(ServiceContract.ID, service1a);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Update());
            tx.failsWith("state <DECLINED> is final state and cannot be transitioned");
            return null;
        });


    }

    @Test
    public void service_update_normal_double_output_same() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = updateService(service1);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Update());
            tx.failsWith("List must contain only 1 entry");
            return null;
        });
    }

    @Test
    public void service_update_normal_double_output_different() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = updateService(service1);
            ServiceState service3 = updateService(service2);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.output(ServiceContract.ID, service3);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Update());
            tx.failsWith("List must contain only 1 entry");
            return null;
        });


    }

    @Test
    public void service_update_normal() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = updateService(service1);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Update());
            tx.verifies();
            return null;
        });

    }



    @Test
    public void service_share_direct_failed() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = shareService(service1, this.insurance2.party);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Share());
            tx.failsWith("List should not be empty");
            return null;
        });
    }


    @Test
    public void service_share_direct_failed_same_parties() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = shareService(service1, this.insurance1.party);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Share());
            tx.failsWith("service provider must be different than initiator");
            return null;
        });
    }

    @Test
    public void service_share_direct_failed_sp_null() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = shareService(service1, null);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Share());
            tx.failsWith("service provider must be provided");
            return null;
        });
    }

    @Test
    public void service_share_direct() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = shareService(service1, this.insurance2.party);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Share());
            tx.verifies();
            return null;
        });
    }

    @Test
    public void service_share_updated() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service1a = updateService(service1);
            ServiceState service2 = shareService(service1a, this.insurance2.party);
            tx.input(ServiceContract.ID, service1a);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Share());
            tx.verifies();
            return null;
        });
    }


    @Test
    public void service_create_withdraw_failed_no_input() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = withAction(service1, "WITHDRAW");
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionBeforeShare("WITHDRAW"));
            tx.failsWith("List should not be empty");
            return null;
        });
    }

    @Test
    public void service_create_withdraw_wrong_state() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = withAction(service1, "WITHDRAW");
            ServiceState service2a = setInvalidState(service2, StateMachine.State.ACCEPTED);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2a);
            tx.command(service2a.getParticipantKeys(), new ServiceContract.Commands.ActionBeforeShare("WITHDRAW"));
            tx.failsWith("Failed requirement: state <ACCEPTED> is not valid next state from <CREATED>");
            return null;
        });
    }


    @Test
    public void service_create_withdraw() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = withAction(service1, "WITHDRAW");
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionBeforeShare("WITHDRAW"));
            tx.verifies();
            return null;
        });
    }

    @Test
    public void service_create_withdraw_updated() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = updateService(newService());
            ServiceState service2 = withAction(service1, "WITHDRAW");
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionBeforeShare("WITHDRAW"));
            tx.verifies();
            return null;
        });
    }



    @Test
    public void service_update_shared_updated() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = shareService(updateService(newService()), this.insurance2.party);
            ServiceState service2 = updateService(service1);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Update());
            tx.verifies();
            return null;
        });
    }


    @Test
    public void service_update2_shared_updated() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = updateService(shareService(updateService(newService()), this.insurance2.party));
            ServiceState service2 = updateService(service1);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.Update());
            tx.verifies();
            return null;
        });
    }



    @Test
    public void service_decline() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = shareService(newService(), this.insurance2.party);
            ServiceState service2 = withAction(service1, "DECLINE");
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionAfterShare("DECLINE"));
            tx.verifies();
            return null;
        });
    }



    @Test
    public void service_decline_after_update() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = updateService(shareService(newService(), this.insurance2.party));
            ServiceState service2 = withAction(service1, "DECLINE");
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionAfterShare("DECLINE"));
            tx.verifies();
            return null;
        });
    }


    @Test
    public void service_decline_invalid_state() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = shareService(newService(), this.insurance2.party);
            ServiceState service2 = setInvalidState(withAction(service1, "DECLINE"), StateMachine.State.ACCEPTED);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionAfterShare("DECLINE"));
            tx.failsWith("state <ACCEPTED> is not valid next state from <SHARED>");
            return null;
        });
    }


    @Test(expected = IllegalStateException.class)
    public void service_decline_invalid_pre_state_on_state_feature() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = withAction(service1, "DECLINE");
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionAfterShare("DECLINE"));
            tx.failsWith("any error");
            return null;
        });
    }


    @Test
    public void service_decline_invalid_pre_state() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = setInvalidStateProvider(service1, StateMachine.State.DECLINED, this.insurance2.party);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionAfterShare("DECLINE"));
            tx.failsWith("state <CREATED> is not allowed in this current transition");
            return null;
        });
    }



    @Test
    public void service_accept() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = shareService(newService(), this.insurance2.party);
            ServiceState service2 = withAction(service1, "ACCEPT");
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionAfterShare("ACCEPT"));
            tx.verifies();
            return null;
        });
    }



    @Test
    public void service_accept_after_update() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = updateService(shareService(newService(), this.insurance2.party));
            ServiceState service2 = withAction(service1, "ACCEPT");
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionAfterShare("ACCEPT"));
            tx.verifies();
            return null;
        });
    }


    @Test
    public void service_accept_invalid_state() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = shareService(newService(), this.insurance2.party);
            ServiceState service2 = setInvalidState(withAction(service1, "ACCEPT"), StateMachine.State.DECLINED);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionAfterShare("ACCEPT"));
            tx.failsWith("state <DECLINED> is not valid next state from <SHARED>");
            return null;
        });
    }


    @Test(expected = IllegalStateException.class)
    public void service_accept_invalid_pre_state_on_state_feature() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = withAction(service1, "ACCEPT");
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionAfterShare("ACCEPT"));
            tx.failsWith("any error");
            return null;
        });
    }


    @Test
    public void service_accept_invalid_pre_state() {
        transaction(insurance1.ledgerServices, tx -> {
            ServiceState service1 = newService();
            ServiceState service2 = setInvalidStateProvider(service1, StateMachine.State.ACCEPTED, this.insurance2.party);
            tx.input(ServiceContract.ID, service1);
            tx.output(ServiceContract.ID, service2);
            tx.command(service2.getParticipantKeys(), new ServiceContract.Commands.ActionAfterShare("ACCEPT"));
            tx.failsWith("state <CREATED> is not allowed in this current transition");
            return null;
        });
    }




}
