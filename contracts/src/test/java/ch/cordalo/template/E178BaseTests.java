/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template;

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
    }

    ;
}
