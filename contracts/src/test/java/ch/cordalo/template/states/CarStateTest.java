/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.states;

import ch.cordalo.template.E178BaseTests;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

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
                this.retailer.party,
                "Mercedes",
                "SLS",
                "v8 bi-turbo",
                "1234356789",
                new ArrayList<>());

        // Assert
        assertThat(carState, is(notNullValue()));
        assertThat(carState.getMake(), is("Mercedes"));
        assertThat(carState.getModel(), is("SLS"));
        assertThat(carState.getType(), is("v8 bi-turbo"));
        assertThat(carState.getStammNr(), is("1234356789"));
    }

}