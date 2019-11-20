package ch.cordalo.template.contracts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;

import java.util.*;
import java.util.stream.Collectors;

@CordaSerializable
public abstract class StateMachine {

    public static final State[] EMPTY_STATES = new State[0];
    private final String name;
    private final Map<String, State> stateMap = new LinkedHashMap<>();
    private final Map<String, StateTransition> transitionMap = new LinkedHashMap<>();

    public abstract void initStates();
    public abstract void initTransitions();

    public StateMachine(String name) {
        this.name = name;
        this.initStates();
        this.initTransitions();
    }

    public StateMachine.State state(String name) {
        if (name != null && !name.isEmpty()) {
            State state = this.stateMap.get(name);
            if (state == null)
                throw new IllegalArgumentException(name + " is not a valid state in state machine " + this.name);
            return state;
        } else {
            return null;
        }
    }
    public StateMachine.StateTransition transition(String name) {
        StateTransition stateTransition = this.transitionMap.get(name);
        if (stateTransition == null) throw new IllegalArgumentException(name+" is not a valid state transition in state machine "+this.name);
        return stateTransition;
    }
    public StateMachine.State newState(String value, StateMachine.StateType type) {
        State state = new State(value, type);
        this.stateMap.put(value, state);
        return state;
    }
    public StateMachine.State newState(String value, String type) {
        return this.newState(value, StateType.valueOf(type));
    }
    public StateMachine.State newState(String value) {
        return newState(value, StateMachine.StateType.CONDITIONAL);
    }
    public StateMachine.StateTransition newTransition(String action, State next, String... previous) {
        if (previous != null) {
            StateMachine.State[] prevStates = new State[previous.length];
            StateTransition stateTransition = new StateTransition(
                    action, next,
                    Arrays.stream(previous).map(x -> this.state(x)).toArray(value -> prevStates));
            this.transitionMap.put(action, stateTransition);
            return stateTransition;
        } else {
            StateTransition stateTransition = new StateTransition(action, next);
            this.transitionMap.put(action, stateTransition);
            return stateTransition;
        }
    }

    public StateMachine.StateTransition newTransition(String... values) {
        String action = values[0];
        String next = values[1];
        if (values.length > 2) {
            String[] currentStates = Arrays.copyOfRange(values, 2, values.length);
            return newTransition(action, this.state(next), currentStates);
        } else {
            return newTransition(action, this.state(next));
        }
    }

    public StateMachine.StateTransition newTransitionSameState(String... values) {
        String action = values[0];
        if (values.length > 1) {
            String[] currentStates = Arrays.copyOfRange(values, 1, values.length - 1);
            return newTransition(action, null, currentStates);
        } else {
            return newTransition(action, (State)null);
        }
    }
    @CordaSerializable
    public enum StateType {
        INITIAL,
        CONDITIONAL,
        SHARE_STATE,
        FINAL;
    }

    @CordaSerializable
    public static class State {

        private final String value;
        private final StateType type;
        private final List<StateMachine.StateTransition> transitions;

        @ConstructorForDeserialization
        public State(String value, StateType type, List<StateMachine.StateTransition> transitions) {
            this.value = value;
            this.type = type;
            this.transitions = transitions;
        }
        public State(String value, StateType type) { this(value, type, new ArrayList<>()); }
        public State(String value) {
            this(value, StateType.CONDITIONAL, new ArrayList<>());
        }

        public StateType getType() { return this.type; }
        public String getValue() { return this.value; }
        public boolean isFinalState() { return this.type == StateType.FINAL; }
        @JsonIgnore
        public boolean isSharingState() { return this.type == StateType.SHARE_STATE; }
        public boolean isInitialState() { return this.type == StateType.INITIAL; }
        public void addTransition(StateMachine.StateTransition transition) {
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
            for (StateMachine.StateTransition transition : this.transitions) {
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof String) {
            return this.getValue().equals(o);
            };
            if (!(o instanceof State)) return false;
            State state = (State) o;
            return getValue().equals(state.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getValue());
        }
    }

    @CordaSerializable
    public static class StateTransition {

        private final String value;
        @JsonIgnore
        private final State nextState;
        @JsonIgnore
        private final State[] currentStates;

        @ConstructorForDeserialization
        StateTransition(String value, State nextState, State[] currentStates) {
            this.value = value;
            this.currentStates = currentStates;
            this.nextState = nextState;
            if (currentStates != null) {
                for (State states : currentStates) {
                    states.addTransition(this);
                }
            }
        }
        StateTransition(String value, State nextState) {
            this.value = value;
            this.currentStates = EMPTY_STATES;
            this.nextState = nextState;
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
                throw new IllegalStateException("state <"+from.getValue()+"> is final state and cannot be transitioned");
            }
            for(State s : this.currentStates) {
                if (s.equals(from)) {
                    return this.nextState != null ? this.nextState : from;
                }
            }
            throw new IllegalStateException("state <"+from.getValue()+"> is not allowed in this current transition");
        }
        @JsonIgnore
        public State getInitialState() throws IllegalStateException {
            if (this.currentStates.length == 0) {
                return this.nextState;
            }
            throw new IllegalStateException("transition has preconditions and is not an initial state");
        }
    }
}
