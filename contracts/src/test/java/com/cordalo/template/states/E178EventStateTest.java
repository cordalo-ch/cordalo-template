package com.cordalo.template.states;

import com.cordalo.template.E178BaseTests;
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
                this.regulator.party,
                this.retailer.party,
                this.leasing.party,
                this.insurer.party,
                "ZH",
                E178EventState.E178StatusType.INITIAL
        );

        // Assert
        assertThat(e178EventState, is(notNullValue()));
        assertThat(e178EventState.getState(), is("ZH"));
        assertThat(e178EventState.getParticipants().size(), is(3));
        assertThat(e178EventState.getRegulator(), is(this.regulator.party));
        assertThat(e178EventState.getRetailer(), is(this.retailer.party));
        assertThat(e178EventState.getLeasing(), is(this.leasing.party));
        assertThat(e178EventState.getInsurer(), is(this.insurer.party));
        assertThat(e178EventState.getStatus(), is(E178EventState.E178StatusType.INITIAL));
    }

}
