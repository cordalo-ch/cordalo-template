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

import ch.cordalo.corda.common.contracts.StateMachine.State;
import ch.cordalo.template.contracts.ServiceStateMachine;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ServiceStateMachineTests {

    @Before
    public void setup() {
    }

    @Test
    public void testInitial() {
        State state = ServiceStateMachine.State("CREATED");
        Assert.assertEquals("created is a valid state", ServiceStateMachine.State("CREATED"), state);
        Assert.assertEquals("created is initial", true, state.isInitialState());
        Assert.assertEquals("created is not final", false, state.isFinalState());
    }

    @Test
    public void testActions() {
        State state = ServiceStateMachine.State("CREATED");
        Assert.assertTrue("no valid next actions", state.getNextActions().size() > 0);
    }

    @Test
    public void testSharedAfterCreate() {
        State createdState = ServiceStateMachine.State("CREATED");
        State sharedState = ServiceStateMachine.State("SHARED");
        Assert.assertEquals("shared is follower of CREATED", true, createdState.hasLaterState(sharedState));
        Assert.assertEquals("created is NOT follower of SHARED", false, sharedState.hasLaterState(createdState));
    }


    @Test
    public void testSharedAfterBeforeShared() {
        State sharedState = ServiceStateMachine.State("SHARED");
        Assert.assertEquals("shared is never later than shared", false, sharedState.hasLaterState(sharedState));
        Assert.assertEquals("shared is never earlier than shared", false, sharedState.hasLaterState(sharedState));
    }

    @Test
    public void testCreateBeforeShared() {
        State createdState = ServiceStateMachine.State("CREATED");
        State sharedState = ServiceStateMachine.State("SHARED");
        Assert.assertEquals("shared is NOT before CREATED", false, createdState.hasEarlierState(sharedState));
        Assert.assertEquals("created is before of SHARED", true, sharedState.hasEarlierState(createdState));
    }


    @Test
    public void testAcceptAfterShared() {
        State sharedState = ServiceStateMachine.State("SHARED");
        State acceptedState = ServiceStateMachine.State("ACCEPTED");
        Assert.assertEquals("accept has earlier shared", true, acceptedState.hasEarlierState(sharedState));
        Assert.assertEquals("shared is later accept", true, sharedState.hasLaterState(acceptedState));
    }


    @Test
    public void testNoShare_is_not_shared() {
        State notShared = ServiceStateMachine.State("NOT_SHARED");
        State shared = ServiceStateMachine.State("SHARED");
        Assert.assertEquals("shared is not later than not-shared", false, shared.hasLaterState(notShared));
        Assert.assertEquals("shared is not earlier than not-shared", false, shared.hasEarlierState(notShared));
    }


}
