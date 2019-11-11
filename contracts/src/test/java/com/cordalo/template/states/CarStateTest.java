package com.cordalo.template.states;

import com.cordalo.template.E178BaseTests;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class CarStateTest extends E178BaseTests {
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
        CarState carState = new CarState(
                new UniqueIdentifier(),
                "Mercedes",
                "SLS",
                "v8 bi-turbo",
                "1234356789"
        );

        // Assert
        assertThat(carState, is(notNullValue()));
        assertThat(carState.getMake(), is("Mercedes"));
        assertThat(carState.getModel(), is("SLS"));
        assertThat(carState.getType(), is("v8 bi-turbo"));
        assertThat(carState.getStammNr(), is("1234356789"));
    }

}