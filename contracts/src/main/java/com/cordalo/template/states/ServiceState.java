package com.cordalo.template.states;

import ch.cordalo.corda.common.contracts.JsonHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.cordalo.template.contracts.ServiceContract;
import com.cordalo.template.contracts.StateMachine;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.*;
import java.util.stream.Collectors;

@BelongsToContract(ServiceContract.class)
public class ServiceState implements LinearState {


    @NotNull
    private final UniqueIdentifier id;
    @NotNull
    private final StateMachine.State state;
    @NotNull
    private final String serviceName;

    @JsonIgnore
    @NotNull
    private final Party initiator;


    @NotNull
    private final Map<String, Object> serviceData;

    @JsonIgnore
    private final Party serviceProvider;
    private final Integer price;

    @ConstructorForDeserialization
    public ServiceState(@NotNull UniqueIdentifier id, @NotNull String serviceName, @NotNull Party initiator, @NotNull StateMachine.State state, Map<String, Object> serviceData, Party serviceProvider, Integer price) {
        this.id = id;
        this.state = state;
        this.serviceName = serviceName;
        this.initiator = initiator;
        this.serviceData = serviceData == null ? new LinkedHashMap<>() : serviceData;
        this.serviceProvider = serviceProvider;
        this.price = price;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.id;
    }

    @NotNull
    @JsonIgnore
    @Override
    public List<AbstractParty> getParticipants() {
        List<AbstractParty> list = new ArrayList<>();
        list.add(this.initiator);
        if (this.serviceProvider != null) list.add(this.serviceProvider);
        return list;
    }

    @NotNull
    @JsonIgnore
    public List<PublicKey> getParticipantKeys() {
        return getParticipants().stream().map(AbstractParty::getOwningKey).collect(Collectors.toList());
    }

    @NotNull
    public UniqueIdentifier getId() {
        return id;
    }
    @NotNull
    public String getServiceName() {
        return serviceName;
    }
    @NotNull
    public Party getInitiator() {
        return initiator;
    }
    @NotNull
    public StateMachine.State getState() { return state; }
    public Party getServiceProvider() {
        return serviceProvider;
    }


    @NotNull
    public String getInitiatorX500() {
        return initiator.getName().getX500Principal().getName();
    }
    public String getServiceProviderX500() {
        return serviceProvider != null ? serviceProvider.getName().getX500Principal().getName() : "";
    }
    public Integer getPrice() {
        return price;
    }
    public Map<String, Object> getServiceData() {
        return serviceData;
    }
    public List<AbstractParty> getCounterParties(Party me) {
        if (me != null) {
            if (this.getServiceProvider() != null) {
                if (this.getInitiator().equals(me)) return Arrays.asList(this.serviceProvider);
                if (this.getServiceProvider().equals(me)) return Arrays.asList(this.initiator);
            } else {
                if (this.getInitiator().equals(me)) return Collections.EMPTY_LIST;
            }
        }
        return Collections.EMPTY_LIST;
    }


    /* actions CREATE */
    public static ServiceState create(@NotNull UniqueIdentifier id, @NotNull String serviceName, @NotNull Party initiator, Map<String, Object> serviceData) {
        return new ServiceState(id, serviceName, initiator, StateMachine.StateTransition.CREATE.getInitialState(), serviceData, null, null);
    }
    /* actions UPDATE */
    public ServiceState update(Map<String, Object> newServiceData) {
        return this.update(newServiceData, this.price);
    }
    public ServiceState update(Map<String, Object> newServiceData, Integer newPrice) {
        StateMachine.State newState = StateMachine.StateTransition.UPDATE.getNextStateFrom(this.state);
        return new ServiceState(this.id, this.serviceName, this.initiator, newState, newServiceData, this.serviceProvider, newPrice);
    }

    /* actions SHARE */
    public ServiceState share(@NotNull Party newServiceProvider) {
        StateMachine.State newState = StateMachine.StateTransition.SHARE.getNextStateFrom(this.state);
        return new ServiceState(this.id, this.serviceName, this.initiator, newState, this.serviceData, newServiceProvider, this.price);
    }


    /* actions any */
    public ServiceState withAction(StateMachine.StateTransition transition) {
        StateMachine.State newState = transition.getNextStateFrom(this.state);
        return new ServiceState(this.id, this.serviceName, this.initiator, newState, this.serviceData, this.serviceProvider, this.price);
    }

    public String getData(String keys) {
        return JsonHelper.getDataValue(this.getServiceData(), keys);
    }

}
