package ch.cordalo.template;

import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.corda.common.test.CordaTestNetwork;
import ch.cordalo.corda.common.test.CordaloBaseTests;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.FlowLogic;

import java.util.List;

public abstract class CordaloTemplateBaseTests extends CordaloBaseTests {

    public CordaloTemplateBaseTests() {
    }

    protected CordaTestNetwork network;
    protected CordaNodeEnvironment notary;
    protected CordaNodeEnvironment companyA;
    protected CordaNodeEnvironment companyB;
    protected CordaNodeEnvironment companyC;
    protected CordaNodeEnvironment companyD;
    protected CordaNodeEnvironment companyE;

    public List<String> getCordappPackageNames() {
        return ImmutableList.of(
                "ch.cordalo.template.contracts",
                "ch.cordalo.corda.common.contracts"
        );
    }

    @Override
    public CordaTestNetwork setup(boolean withNodes, List<Class<? extends FlowLogic>> responderClasses) {
        this.network = new CordaTestNetwork(
                withNodes,
            this.getCordappPackageNames(),
            responderClasses
        );
        this.notary = network.startNotaryEnv("Notary", "O=Notary,L=Zurich,ST=ZH,C=CH");
        this.companyA = network.startEnv("Company-A", "O=Company-A,L=Zurich,ST=ZH,C=CH");
        this.companyB = network.startEnv("Company-B", "O=Company-B,L=Winterthur,ST=ZH,C=CH");
        this.companyC = network.startEnv("Company-C", "O=Company-C,L=Zug,ST=ZG,C=CH");
        this.companyD = network.startEnv("Company-D", "O=Company-D,L=Zurich,ST=ZH,C=CH");
        this.companyE = network.startEnv("Company-E", "O=Company-E,L=Zurich,ST=ZH,C=CH");
        this.network.startNodes();
        return this.network;
    }

    @Override
    public CordaTestNetwork getNetwork() {
        return network;
    }

}
