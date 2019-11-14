package com.cordalo.template.contracts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class E178StateMachine {
    @CordaSerializable
    public enum StateType {
        INITIAL,
        CONDITIONAL,
        SHARE_STATE,
        FINAL;
    }

    @CordaSerializable
    public enum State {
        REQUESTED(StateType.INITIAL),
        ISSUED,
        INSURANCE_REQUESTED,
        INSURED,
        REGISTERED(StateType.FINAL),
        CANCELED(StateType.FINAL);

        @JsonIgnore
        private StateType type;
        private List<E178StateMachine.StateTransition> transitions;

        @ConstructorForDeserialization
        State(StateType type, List<E178StateMachine.StateTransition> transitions) {
            this.type = type;
            this.transitions = transitions;
        }
        State(StateType type) { this(type, new ArrayList<>()); }
        State() {
            this(StateType.CONDITIONAL, new ArrayList<>());
        }

        public StateType getType() { return this.type; }
        @JsonIgnore
        public boolean isFinalState() { return this.type == StateType.FINAL; }
        public boolean isSharingState() { return this.type == StateType.SHARE_STATE; }
        public boolean isInitialState() { return this.type == StateType.INITIAL; }
        public void addTransition(E178StateMachine.StateTransition transition) {
            this.transitions.add(transition);
        }
        public List<String> getNextActions() {
            if (this.isFinalState()) return Collections.EMPTY_LIST;
            return this.transitions.stream().map(x -> x.toString()).collect(Collectors.toList());
        }
        public boolean isValidAction(String action) {
            if (this.isFinalState()) return false;
            return this.transitions.stream().anyMatch(x -> x.toString().equals(action));
        }
        public boolean isLaterState(State state) {
            if (this.equals(state)) return false;
            return state.hasLaterState(this);
        }
        public boolean isEarlierState(State state) {
            if (this.equals(state)) return false;
            return state.hasEarlierState(this);
        }

        public boolean hasLaterState(State state) {
            if (this.equals(state)) return false;
            return hasLaterState(state, new HashSet<>());
        }
        private boolean hasLaterState(State state, Set<State> visited) {
            if (visited.contains(this)) return false;
            visited.add(this);
            if (this.equals(state)) return true;
            for (E178StateMachine.StateTransition transition : this.transitions) {
                if (transition.nextState != null && transition.nextState.hasLaterState(state, visited)) {
                    return true;
                }
            }
            return false;
        }
        public boolean hasEarlierState(State state) {
            if (this.equals(state)) return false;
            return state.hasLaterState(this);
        }

    }

    @CordaSerializable
    public enum StateTransition {
        REQUEST(State.REQUESTED),
        ISSUE(State.ISSUED,                     State.REQUESTED),
        REQUEST_INSURANCE
                (State.INSURANCE_REQUESTED,     State.ISSUED),
        INSURE(State.INSURED,                   State.INSURANCE_REQUESTED),
        REGISTER(State.REGISTERED,              State.INSURED),
        CANCEL(State.CANCELED,      State.REQUESTED, State.ISSUED, State.INSURANCE_REQUESTED, State.INSURED, State.REGISTERED);

        @JsonIgnore
        private State nextState;
        @JsonIgnore
        private State[] currentStates;
        StateTransition(State nextState, @NotNull State... currentStates) {
            Arrays.sort(currentStates);
            this.currentStates = currentStates;
            this.nextState = nextState;
            for (State states : currentStates) {
                states.addTransition(this);
            }
        }
        @JsonIgnore
        public boolean willBeInFinalState() {
            return this.nextState.isFinalState();
        }
        @JsonIgnore
        public State getNextState() {
            return this.nextState;
        }
        @JsonIgnore
        public boolean willBeSharingState() {
            return this.nextState.isSharingState();
        }
        public State getNextStateFrom(State from) throws IllegalStateException {
            if (from.isFinalState()) {
                throw new IllegalStateException("state <"+from+"> is final state and cannot be transitioned");
            }
            if (Arrays.binarySearch(this.currentStates, from) >= 0) {
                return this.nextState != null ? this.nextState : from;
            }
            throw new IllegalStateException("state <"+from+"> is not allowed in this current transition");
        }
        public State getInitialState() throws IllegalStateException {
            if (this.currentStates.length == 0) {
                return this.nextState;
            }
            throw new IllegalStateException("transition has preconditions and is not an initial state");
        }
    }
}
