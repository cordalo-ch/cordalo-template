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
import ch.cordalo.template.flows.CarFlow;
import ch.cordalo.template.states.CarState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.transactions.SignedTransaction;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CarFlowTests extends CordaloTemplateBaseFlowTests {

    @Before
    public void setup() {
        this.setup(true, CarFlow.class);
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }


    protected CarState newCar(CordaNodeEnvironment from, String stammNr, String make, String model) throws FlowException {
        FlowLogic<SignedTransaction> flow = new CarFlow.Create(new UniqueIdentifier(), make, model, "PW", stammNr);
        return this.startFlowAndResult(from, flow, CarState.class);
    }

    protected CarState newUpdateCar(CordaNodeEnvironment from, CarState car, String make, String model, String type) throws FlowException {
        FlowLogic<SignedTransaction> flow = new CarFlow.Update(car.getLinearId(), make, model, type);
        return this.startFlowAndResult(from, flow, CarState.class);
    }

    protected CarState newShareCar(CordaNodeEnvironment from, CarState car, CordaNodeEnvironment to) throws FlowException {
        FlowLogic<SignedTransaction> flow = new CarFlow.Share(car.getLinearId(), to.party);
        return this.startFlowAndResult(from, flow, CarState.class);
    }

    @Test
    public void create_car() throws Exception {
        CarState car = this.newCar(companyA, "123.456.789", "Audi", "A8");
        Assert.assertEquals("Audi", "Audi", car.getMake());
    }


    @Test
    public void update_car() throws Exception {
        CarState car = this.newCar(companyA, "123.456.789", "Audi", "A8");
        CarState updatedCar = this.newUpdateCar(companyA, car, "Audi", "A8-2019", "PW");
        Assert.assertEquals("A8", "A8", car.getModel());
        Assert.assertEquals("A8", "A8-2019", updatedCar.getModel());
    }


    @Test
    public void share_car() throws Exception {
        CarState car = this.newCar(companyA, "123.456.789", "Audi", "A8");
        CarState sharedCar = this.newShareCar(companyA, car, companyB);
        Assert.assertEquals("A8", "A8", car.getModel());
        Assert.assertTrue("Company B is not an owners", !car.getOwners().contains(this.companyB.party));
        Assert.assertTrue("Company B must be part of owners", sharedCar.getOwners().contains(this.companyB.party));
    }


    @Test
    public void search_car() throws Exception {
        CarState car = this.newCar(companyC, "123.123.999", "Audi", "A8");
        Assert.assertTrue("old car owners does not contains companyA", !car.getOwners().contains(companyA.party));

        FlowLogic<CarState> flow = new CarFlow.Search("123.123.999", companyC.party);

        CarState copyCar = this.startFlowAndState(companyA, flow);
        Assert.assertEquals("cars identical: id", car.getLinearId(), copyCar.getLinearId());
        Assert.assertEquals("cars identical: model", car.getModel(), copyCar.getModel());
        Assert.assertEquals("cars identical: make", car.getMake(), copyCar.getMake());
        Assert.assertEquals("cars identical: type", car.getType(), copyCar.getType());
        Assert.assertEquals("cars identical: creator", car.getCreator(), copyCar.getCreator());
        Assert.assertTrue("new car owners contains companyA", copyCar.getOwners().contains(companyA.party));

    }

}
