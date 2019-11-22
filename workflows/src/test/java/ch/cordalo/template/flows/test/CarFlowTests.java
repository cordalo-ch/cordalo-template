package ch.cordalo.template.flows.test;

import ch.cordalo.corda.common.test.CordaNodeEnvironment;
import ch.cordalo.template.flows.CarFlow;
import ch.cordalo.template.flows.ChatMessageFlow;
import ch.cordalo.template.states.CarState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.transactions.SignedTransaction;
import org.assertj.core.internal.cglib.asm.$Type;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

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


    protected CarState newCar(CordaNodeEnvironment from, String stammNr, String make, String model) throws  FlowException {
        FlowLogic<SignedTransaction> flow = new CarFlow.Create(new UniqueIdentifier(),make, model,"PW",stammNr, new ArrayList<>());
        return this.startFlowAndResult(from, flow, CarState.class);
    }
    protected CarState newUpdateCar(CordaNodeEnvironment from, CarState car, String make, String model, String type) throws  FlowException {
        FlowLogic<SignedTransaction> flow = new CarFlow.Update(car.getLinearId(), make, model, type);
        return this.startFlowAndResult(from, flow, CarState.class);
    }

    @Test
    public void create_car() throws Exception {
        CarState car = this.newCar(companyA, "123.456.789","Audi", "A8");
        Assert.assertEquals("Audi", "Audi", car.getMake());
    }


    @Test
    public void update_car() throws Exception {
        CarState car = this.newCar(companyA, "123.456.789","Audi", "A8");
        CarState updatedCar = this.newUpdateCar(companyA, car,"Audi", "A8-2019", "PW");
        Assert.assertEquals("A8", "A8", car.getModel());
        Assert.assertEquals("A8", "A8-2019", updatedCar.getModel());
    }

}
