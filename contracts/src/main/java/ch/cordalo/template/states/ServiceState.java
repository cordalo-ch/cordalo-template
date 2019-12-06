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

import ch.cordalo.corda.common.contracts.JsonHelper;
import ch.cordalo.corda.common.contracts.StateMachine;
import ch.cordalo.corda.common.states.CordaloLinearState;
import ch.cordalo.corda.common.states.Parties;
import ch.cordalo.template.contracts.ServiceContract;
import ch.cordalo.template.contracts.ServiceStateMachine;
import com.fasterxml.jackson.annotation.JsonIgnore;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.LinearPointer;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@BelongsToContract(ServiceContract.class)
public class ServiceState extends CordaloLinearState {

    @NotNull
    private final String state;
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

    private List<LinearPointer> documents;

    @ConstructorForDeserialization
    public ServiceState(@NotNull UniqueIdentifier linearId, @NotNull String serviceName, @NotNull Party initiator, @NotNull String state, Map<String, Object> serviceData, Party serviceProvider, Integer price, List<LinearPointer> documents) {
        super(linearId);
        this.state = state;
        this.serviceName = serviceName;
        this.initiator = initiator;
        this.serviceData = serviceData == null ? new LinkedHashMap<>() : serviceData;
        this.serviceProvider = serviceProvider;
        this.price = price;
        this.documents = documents;
    }

    public ServiceState(@NotNull UniqueIdentifier linearId, @NotNull String serviceName, @NotNull Party initiator, @NotNull String state, Map<String, Object> serviceData, Party serviceProvider, Integer price) {
        this(linearId, serviceName, initiator, state, serviceData, serviceProvider, price, new ArrayList<>());
    }

    public ServiceState(@NotNull UniqueIdentifier linearId, @NotNull String serviceName, @NotNull Party initiator, @NotNull StateMachine.State state, Map<String, Object> serviceData, Party serviceProvider, Integer price, List<LinearPointer> documents) {
        this(linearId, serviceName, initiator, state.getValue(), serviceData, serviceProvider, price, documents);
    }

    public ServiceState(@NotNull UniqueIdentifier linearId, @NotNull String serviceName, @NotNull Party initiator, @NotNull StateMachine.State state, Map<String, Object> serviceData, Party serviceProvider, Integer price) {
        this(linearId, serviceName, initiator, state.getValue(), serviceData, serviceProvider, price, new ArrayList<>());
    }

    @NotNull
    @JsonIgnore
    @Override
    protected Parties getParties() {
        return new Parties(this.initiator, this.serviceProvider);
    }

    @NotNull
    public String getServiceName() {
        return serviceName;
    }

    @NotNull
    public Party getInitiator() {
        return initiator;
    }

    public String getInitiatorX500() {
        return Parties.partyToX500(this.initiator);
    }

    @NotNull
    public String getState() {
        return this.state;
    }

    @JsonIgnore
    public StateMachine.State getStateObject() {
        return ServiceStateMachine.State(this.state);
    }

    public Party getServiceProvider() {
        return serviceProvider;
    }

    public String getServiceProviderX500() {
        return Parties.partyToX500(this.serviceProvider);
    }

    public List<String> getParticipantsX500() {
        return this.getParties().getPartiesX500();
    }

    public Integer getPrice() {
        return price;
    }

    public Map<String, Object> getServiceData() {
        return serviceData;
    }

    public List<LinearPointer> getDocuments() {
        return documents;
    }

    /* actions CREATE */
    public static ServiceState create(@NotNull UniqueIdentifier linearId, @NotNull String serviceName, @NotNull Party initiator, Map<String, Object> serviceData) {
        return new ServiceState(linearId, serviceName, initiator, ServiceStateMachine.StateTransition("CREATE").getInitialState(), serviceData, null, null);
    }

    /* actions UPDATE */
    public ServiceState update(Map<String, Object> newServiceData) {
        return this.update(newServiceData, this.price);
    }

    public ServiceState update(Map<String, Object> newServiceData, Integer newPrice) {
        StateMachine.State newState = ServiceStateMachine.StateTransition("UPDATE").getNextStateFrom(this.getStateObject());
        return new ServiceState(this.linearId, this.serviceName, this.initiator, newState, newServiceData, this.serviceProvider, newPrice);
    }

    /* actions SHARE */
    public ServiceState share(@NotNull Party newServiceProvider) {
        StateMachine.State newState = ServiceStateMachine.StateTransition("SHARE").getNextStateFrom(this.getStateObject());
        return new ServiceState(this.linearId, this.serviceName, this.initiator, newState, this.serviceData, newServiceProvider, this.price);
    }


    /* actions any */
    public ServiceState withAction(ServiceStateMachine.StateTransition transition) {
        StateMachine.State newState = transition.getNextStateFrom(this.getStateObject());
        return new ServiceState(this.linearId, this.serviceName, this.initiator, newState, this.serviceData, this.serviceProvider, this.price);
    }

    /* actions any */
    public ServiceState addDocument(SignedDocument document) {
        ArrayList<LinearPointer> newDocuments = new ArrayList<>(this.documents);
        newDocuments.add(new LinearPointer(document.getLinearId(), SignedDocument.class));
        return new ServiceState(this.linearId, this.serviceName, this.initiator, this.state, this.serviceData, this.serviceProvider, this.price, newDocuments);
    }

    public String getData(String keys) {
        return JsonHelper.getDataValue(this.getServiceData(), keys);
    }

}
