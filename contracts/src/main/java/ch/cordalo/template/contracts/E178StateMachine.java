package ch.cordalo.template.contracts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.serialization.ConstructorForDeserialization;
import net.corda.core.serialization.CordaSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class E178StateMachine extends StateMachine {

    private final static E178StateMachine INSTANCE = new E178StateMachine();

    public static E178StateMachine get() { return INSTANCE; }
    private E178StateMachine() {
        super("E178-services");
    }

    public static StateMachine.StateTransition StateTransition(String transition) {
        return INSTANCE.transition(transition);
    }
    public static StateMachine.State State(String state) {
        return INSTANCE.state(state);
    }

    @Override
    public void initStates() {
        newState("REQUESTED", StateMachine.StateType.INITIAL);
        newState("ISSUED");
        newState("INSURANCE_REQUESTED");
        newState("INSURED");
        newState("REGISTERED", StateMachine.StateType.FINAL);
        newState("CANCELED", StateMachine.StateType.FINAL);
    }

    @Override
    public void initTransitions() {

        newTransition("REQUEST"             , "REQUESTED");

        newTransition("ISSUE"               ,"ISSUED"                   ,"REQUESTED");
        newTransition("REQUEST_INSURANCE"   ,"INSURANCE_REQUESTED"      ,"ISSUED");
        newTransition("INSURE"              ,"INSURED"                  ,"INSURANCE_REQUESTED");
        newTransition("REGISTER"            ,"REGISTERED"               ,"INSURED");
        newTransition("CANCEL"              ,"CANCELED"                 ,"REQUESTED","ISSUED","INSURANCE_REQUESTED","INSURED", "REGISTERED");
    }
}
