/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
