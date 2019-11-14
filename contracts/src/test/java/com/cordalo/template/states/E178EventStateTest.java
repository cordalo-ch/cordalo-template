package com.cordalo.template.states;

import ch.cordalo.corda.common.test.CordaTestNetwork;
import com.cordalo.template.E178BaseTests;
import com.cordalo.template.contracts.E178StateMachine;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class E178EventStateTest extends E178BaseTests {

    @Before
    public void setup() {
        this.setup(false);
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void test_ctor_expectObjectBuildProperly() {
        // Arrange
        E178EventState e178EventState = new E178EventState(
                new UniqueIdentifier(),
                this.retailer.party,
                this.leasing.party,
                this.insurer.party,
                this.regulator.party,
                "ZH",
                E178StateMachine.State.REQUESTED
        );

        // Assert
        assertThat(e178EventState, is(notNullValue()));
        assertThat(e178EventState.getState(), is("ZH"));
        assertThat(e178EventState.getParticipants().size(), is(4));
        assertThat(e178EventState.getRegulator(), is(this.regulator.party));
        assertThat(e178EventState.getRetailer(), is(this.retailer.party));
        assertThat(e178EventState.getLeasing(), is(this.leasing.party));
        assertThat(e178EventState.getInsurer(), is(this.insurer.party));
        assertThat(e178EventState.getStatus(), is(E178StateMachine.State.REQUESTED));
    }

}
