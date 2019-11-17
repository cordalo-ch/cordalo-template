package ch.cordalo.template.flows.test;

import ch.cordalo.template.CordaloTemplateBaseTests;
import ch.cordalo.template.flows.ServiceFlow;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowLogic;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.StartedMockNode;

import java.util.concurrent.ExecutionException;

public class CordaloTemplateBaseFlowTests extends CordaloTemplateBaseTests {


    protected SignedTransaction newServiceCreateFlow(String serviceName, String data, Integer price) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new ServiceFlow.Create(serviceName, data, price);
        CordaFuture<SignedTransaction> future = companyA.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }
    protected SignedTransaction newServiceShareFlow(UniqueIdentifier id, Party serviceProvider) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new ServiceFlow.Share(id, serviceProvider);
        CordaFuture<SignedTransaction> future = companyA.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }
    protected SignedTransaction newServiceDeleteFlow(UniqueIdentifier id) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new ServiceFlow.Delete(id);
        CordaFuture<SignedTransaction> future = companyA.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }
    protected SignedTransaction newServiceUpdateFlow(UniqueIdentifier id, String data, Integer price) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new ServiceFlow.Update(id, data, price);
        CordaFuture<SignedTransaction> future = companyA.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }
    protected SignedTransaction newServiceActionFlow(UniqueIdentifier id, String action) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new ServiceFlow.Action(id, action);
        CordaFuture<SignedTransaction> future = companyA.node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }
    protected SignedTransaction newServiceActionFlowBy(UniqueIdentifier id, String action, StartedMockNode node) throws ExecutionException, InterruptedException {
        FlowLogic<SignedTransaction> flow = new ServiceFlow.Action(id, action);
        CordaFuture<SignedTransaction> future = node.startFlow(flow);
        network.runNetwork();
        return future.get();
    }

}
