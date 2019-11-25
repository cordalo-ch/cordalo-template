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
import ch.cordalo.template.contracts.E178StateMachine;
import net.corda.core.contracts.UniqueIdentifier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class E178EventStateTest extends E178BaseTests {
    private final static String STAMM_NR = "123.456.786";

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
                STAMM_NR,
                this.retailer.party,
                this.leasing.party,
                this.insurer1.party,
                this.regulator.party,
                "ZH",
                E178StateMachine.State("REQUESTED")
        );

        // Assert
        assertThat(e178EventState, is(notNullValue()));
        assertThat(e178EventState.getState(), is("ZH"));
        assertThat(e178EventState.getParticipants().size(), is(4));
        assertThat(e178EventState.getRegulator(), is(this.regulator.party));
        assertThat(e178EventState.getRetailer(), is(this.retailer.party));
        assertThat(e178EventState.getLeasing(), is(this.leasing.party));
        assertThat(e178EventState.getInsurer(), is(this.insurer1.party));
        assertThat(e178EventState.getStatusObject(), is(E178StateMachine.State("REQUESTED")));
        assertThat(e178EventState.getStatus(), is("REQUESTED"));
    }

}
