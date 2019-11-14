package com.cordalo.template;

import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.corda.common.test.CordaTestNetwork;
import ch.cordalo.corda.common.test.CordaloBaseTests;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.FlowLogic;

import java.util.List;

public class E178BaseTests extends CordaloBaseTests {

    protected CordaTestNetwork network;

    protected CordaNodeEnvironment retailer;
    protected CordaNodeEnvironment regulator;
    protected CordaNodeEnvironment leasing;
    protected CordaNodeEnvironment insurer1;
    protected CordaNodeEnvironment insurer2;
    protected CordaNodeEnvironment notary;

    public List<String> getCordappPackageNames() {
        return ImmutableList.of(
                "com.cordalo.template.contracts",
                "ch.cordalo.corda.common.contracts"
        );
    }

    public CordaTestNetwork setup(boolean withNodes, Class<? extends FlowLogic> ...responderClasses) {
        this.network = new CordaTestNetwork(
                withNodes,
            this.getCordappPackageNames(),
            responderClasses
        );
        this.notary = network.startNotaryEnv("Notary", "O=Notary,L=Zurich,ST=ZH,C=CH");
        this.retailer = network.startEnv("Company-A", "O=Company-A,L=Zurich,ST=ZH,C=CH");
        this.regulator = network.startEnv("Company-B", "O=Company-B,L=Winterthur,ST=ZH,C=CH");
        this.leasing = network.startEnv("Company-C", "O=Company-C,L=Zug,ST=ZG,C=CH");
        this.insurer1 = network.startEnv("Company-D", "O=Company-D,L=Zurich,ST=ZH,C=CH");
        this.insurer2 = network.startEnv("Company-E", "O=Company-E,L=Zurich,ST=ZH,C=CH");
        this.network.startNodes();
        return this.network;
    }

    @Override
    public CordaTestNetwork getNetwork() {
        return this.network;
    }

    public void tearDown() {
        if (network != null) network.stopNodes();
    };
}
