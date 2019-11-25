package ch.cordalo.template.flows.test;

import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.template.CordaloTemplateBaseTests;
import ch.cordalo.template.flows.ServiceFlow;
import ch.cordalo.template.states.ServiceState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.identity.Party;

public class CordaloTemplateBaseFlowTests extends CordaloTemplateBaseTests {


    protected ServiceState newServiceCreateFlow(CordaNodeEnvironment environment, String serviceName, String data, Integer price) throws FlowException {
        return this.startFlowAndResult(companyA, new ServiceFlow.Create(serviceName, data, price), ServiceState.class);
    }

    protected ServiceState newServiceShareFlow(CordaNodeEnvironment env, UniqueIdentifier id, Party serviceProvider) throws FlowException {
        return this.startFlowAndResult(env, new ServiceFlow.Share(id, serviceProvider), ServiceState.class);
    }

    protected void newServiceDeleteFlow(UniqueIdentifier id) throws FlowException {
        this.startFlow(companyA, new ServiceFlow.Delete(id));
    }

    protected ServiceState newServiceUpdateFlow(CordaNodeEnvironment env, UniqueIdentifier id, String data, Integer price) throws FlowException {
        return this.startFlowAndResult(env, new ServiceFlow.Update(id, data, price), ServiceState.class);
    }

    protected ServiceState newServiceActionFlow(CordaNodeEnvironment env, UniqueIdentifier id, String action) throws FlowException {
        return this.startFlowAndResult(env, new ServiceFlow.Action(id, action), ServiceState.class);
    }

}
