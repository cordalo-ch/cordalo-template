package com.cordalo.template;

import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.corda.common.test.CordaTestNetwork;
import ch.cordalo.corda.common.test.CordaloBaseTests;
import com.google.common.collect.ImmutableList;
import net.corda.core.flows.FlowLogic;
import net.corda.testing.node.TestCordapp;

import java.util.List;

public abstract class CordaloTemplateBaseTests extends CordaloBaseTests {

    public CordaloTemplateBaseTests() {
    }

    protected CordaTestNetwork network;
    protected CordaNodeEnvironment insurance1;
    protected CordaNodeEnvironment insurance2;
    protected CordaNodeEnvironment companyD1;
    protected CordaNodeEnvironment companyD2;

    public List<String> getCordappPackageNames() {
        return ImmutableList.of(
                "com.cordalo.template.contracts",
                "ch.cordalo.corda.common.contracts"
        );
    }


    public void setup(boolean withNodes, Class<? extends FlowLogic> ...responderClasses) {
        this.network = new CordaTestNetwork(
                withNodes,
            this.getCordappPackageNames(),
            responderClasses
        );
        this.insurance1 = network.startEnv("Company-A", "O=Company-A,L=Zurich,ST=ZH,C=CH");
        this.insurance2 = network.startEnv("Company-B", "O=Company-B,L=Winterthur,ST=ZH,C=CH");
        this.companyD1 = network.startEnv("Company-C", "O=Company-C,L=Zug,ST=ZG,C=CH");
        this.companyD2 = network.startEnv("Company-D", "O=Company-D,L=Zurich,ST=ZH,C=CH");
        this.network.startNodes();
    }

    public void tearDown() {
        if (network != null) network.stopNodes();
    };
}
