/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.CordaloController;
import ch.cordalo.corda.common.client.webserver.RpcConnection;
import ch.cordalo.corda.common.client.webserver.StateAndLinks;
import ch.cordalo.corda.common.contracts.JsonHelper;
import ch.cordalo.corda.common.contracts.StateMachine;
import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.template.contracts.ServiceStateMachine;
import ch.cordalo.template.flows.ServiceFlow;
import ch.cordalo.template.states.ServiceState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;


@RestController
@RequestMapping("/api/v1/cordalo/template") // The paths for HTTP requests are relative to this base path.
public class ControllerServices extends CordaloController<ServiceState> {

    private final static Logger logger = LoggerFactory.getLogger(ControllerServices.class);

    private final static String MAPPING_PATH = "/api/v1/cordalo/template";
    private final static String BASE_PATH = "/services";

    public ControllerServices(RpcConnection rpcConnection) {
        super(rpcConnection, MAPPING_PATH, BASE_PATH);
    }

    /**
     * returns all unconsumed services that exist in the node's vault.
     */
    @RequestMapping(
            value = BASE_PATH,
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<List<StateAndLinks<ServiceState>>> getServices(
            HttpServletRequest request) throws URISyntaxException {
        List<ServiceState> list = this.getProxy().vaultQuery(ServiceState.class).getStates()
                .stream().map(state -> state.getState().getData()).collect(toList());
        return this.buildResponseFromStates(request, list, HttpStatus.OK)
                .links(x -> x.getStateObject().getNextActions())
                .buildList();
    }

    /**
     * receives a unconsumed service with a given ID from the node's vault.
     *
     * @param id unique identifier as UUID for service
     */
    @RequestMapping(
            value = BASE_PATH + "/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<StateAndLinks<ServiceState>> getUnconsumedServiceById(
            HttpServletRequest request,
            @PathVariable("id") String id) throws URISyntaxException {
        UniqueIdentifier uid = new UniqueIdentifier(null, UUID.fromString(id));
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(uid),
                Vault.StateStatus.UNCONSUMED,
                null);
        List<ServiceState> states = this.getProxy().vaultQueryByCriteria(queryCriteria, ServiceState.class)
                .getStates().stream().map(state -> state.getState().getData()).collect(toList());
        if (states.isEmpty()) {
            return null;
        } else {
            ServiceState service = states.get(states.size() - 1);
            return this.buildResponseFromState(request, service, HttpStatus.OK)
                    .links(x -> x.getStateObject().getNextActions())
                    .build();
        }
    }


    /**
     * deletes an unconsumed service with a given ID from the node's vault.
     *
     * @param id unique identifier as UUID for service
     */
    @RequestMapping(
            value = BASE_PATH + "/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<StateAndLinks<ServiceState>> deleteServiceById(
            @PathVariable("id") String id) {
        UniqueIdentifier uid = new UniqueIdentifier(null, UUID.fromString(id));
        try {
            this.startFlow(ServiceFlow.Delete.class, uid);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return this.buildResponseFromException(HttpStatus.EXPECTATION_FAILED, ex);
        }

    }

    /**
     * create a new service with given data
     *
     * @param request     is the original http request to calculate links in response
     * @param data        string contains json data for the service
     * @param serviceName is the name of the service
     * @param price       is a possible positiv price for the service
     */
    @RequestMapping(
            value = BASE_PATH,
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<StateAndLinks<ServiceState>> createService(
            HttpServletRequest request,
            @RequestParam(name = "service-name") String serviceName,
            @RequestParam(name = "data", required = false) String data,
            @RequestParam(name = "price", required = false) Integer price) {
        try {
            if (data == null || data.isEmpty() || JsonHelper.convertStringToJson(data) == null) {
                data = "{}";
            }
        } catch (IllegalStateException ex) {
            logger.error(ex.getMessage());
            return this.buildResponseFromException(HttpStatus.BAD_REQUEST, ex);
        }
        try {
            final SignedTransaction signedTx = this.startFlow(
                    ServiceFlow.Create.class,
                    serviceName,
                    data,
                    price);

            StateVerifier verifier = StateVerifier.fromTransaction(signedTx, null);
            ServiceState service = verifier.output().one(ServiceState.class).object();
            return this.buildResponseFromState(request, service, HttpStatus.CREATED)
                    .links(x -> x.getStateObject().getNextActions())
                    .build();
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return this.buildResponseFromException(HttpStatus.EXPECTATION_FAILED, ex);
        }
    }


    /**
     * execute an action on the services give by id
     *
     * @param id     identifier of the service
     * @param action name of action to be executed
     */
    @RequestMapping(
            value = BASE_PATH + "/{id}/{action}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<StateAndLinks<ServiceState>> serviceAction(
            HttpServletRequest request,
            @RequestParam(value = "service-provider", required = false) String serviceProvider,
            @PathVariable("id") String id,
            @PathVariable("action") String action) {
        UniqueIdentifier uid = new UniqueIdentifier(null, UUID.fromString(id));
        StateMachine.State state = ServiceStateMachine.StateTransition(action).getNextState();
        if (state == null) {
            return this.buildResponseFromException(HttpStatus.METHOD_NOT_ALLOWED, "illegal action <" + action + ">. Method not allowed");
        }
        try {
            if (state.equals(ServiceStateMachine.State("SHARED"))) {
                if (serviceProvider == null || serviceProvider.isEmpty()) {
                    return this.buildResponseFromException(HttpStatus.BAD_REQUEST, "service-provider not specified in post");
                }
                Party serviceProviderParty = this.partyFromString(serviceProvider);
                if (serviceProviderParty == null) {
                    return this.buildResponseFromException(HttpStatus.BAD_REQUEST, "service-provider not a valid peer.");
                }
                final SignedTransaction signedTx = this.startFlow(
                        ServiceFlow.Share.class,
                        uid,
                        serviceProviderParty);

                StateVerifier verifier = StateVerifier.fromTransaction(signedTx, null);
                ServiceState service = verifier.output().one(ServiceState.class).object();
                return this.buildResponseFromState(request, service, HttpStatus.OK)
                        .links(x -> x.getStateObject().getNextActions())
                        .build();
            } else {
                final SignedTransaction signedTx = this.startFlow(
                        ServiceFlow.Action.class,
                        uid,
                        action);

                StateVerifier verifier = StateVerifier.fromTransaction(signedTx, null);
                ServiceState service = verifier.output().one(ServiceState.class).object();
                return this.buildResponseFromState(request, service, HttpStatus.OK)
                        .links(x -> x.getStateObject().getNextActions())
                        .build();
            }
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return this.buildResponseFromException(HttpStatus.EXPECTATION_FAILED, ex);
        }
    }


}