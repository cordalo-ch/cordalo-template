package com.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.RpcConnection;
import ch.cordalo.corda.common.client.webserver.StateAndLinks;
import ch.cordalo.corda.common.client.webserver.StateBuilder;
import ch.cordalo.corda.common.contracts.JsonHelper;
import ch.cordalo.corda.common.contracts.StateVerifier;
import com.cordalo.template.contracts.StateMachine;
import com.cordalo.template.flows.ChatMessageFlow;
import com.cordalo.template.flows.ServiceFlow;
import com.cordalo.template.states.ChatMessageState;
import com.cordalo.template.states.ServiceState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
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
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;


/**
 * Define your API endpoints here.
 * supported by example for WebSockets
 * https://www.toptal.com/java/stomp-spring-boot-websocket
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/") // The paths for HTTP requests are relative to this base path.
public class Controller {

    public static final boolean DEBUG = true;

    private CordaRPCOps proxy;
    private CordaX500Name myLegalName;

    private final List<String> serviceNames = ImmutableList.of("Notary");

    private final static Logger logger = LoggerFactory.getLogger(Controller.class);

    private final static String MAPPING_PATH = "/api/v1/";
    private final static String BASE_PATH = "cordalo/template";

    public Controller(RpcConnection rpc) {
        StateMachine.State.values();
        StateMachine.StateTransition.values();
        if (DEBUG && rpc.getProxy() == null) {
            this.proxy = null;
            this.myLegalName = null;
            logger.error("NodeRPC connection + proxy is not initialized (null)");
            return;
        }
        this.proxy = rpc.getProxy();
        this.myLegalName = rpc.getProxy().nodeInfo().getLegalIdentities().get(0).getName();
    }

    private ResponseEntity<List<StateAndLinks<ServiceState>>> getResponse(HttpServletRequest request, List<ServiceState> list, HttpStatus status) throws URISyntaxException {
        return new StateBuilder<>(list, ResponseEntity.status(status))
                .stateMapping(MAPPING_PATH, BASE_PATH, request)
                .links( "services", x -> x.getState().getNextActions())
                .self("services")
                .buildList();
    }
    private ResponseEntity<StateAndLinks<ServiceState>> getResponse(HttpServletRequest request, ServiceState service, HttpStatus status) throws URISyntaxException {
        return new StateBuilder<>(service, ResponseEntity.status(HttpStatus.OK))
                .stateMapping(MAPPING_PATH, BASE_PATH, request)
                .self("services")
                .links("services", x -> x.getState().getNextActions())
                .build();
    }
    private ResponseEntity<StateAndLinks<ServiceState>> createUpdateActionResponse(HttpServletRequest request, ServiceState serviceState, HttpStatus status) throws URISyntaxException {
        ResponseEntity<StateAndLinks<ServiceState>> response = this.getResponse(request, serviceState, status);
        //this.messagingTemplate.convertAndSend("/topic/vaultChanged/cordalo/template/service", response.getBody());
        return response;
    }


    private ResponseEntity<StateAndLinks<ChatMessageState>> getResponse(HttpServletRequest request, ChatMessageState message, HttpStatus status) throws URISyntaxException {
        String[] actions = { "reply" };
        return new StateBuilder<>(message, ResponseEntity.status(HttpStatus.OK))
                .stateMapping(MAPPING_PATH, BASE_PATH, request)
                .self("messages")
                .links("messages", actions)
                .build();
    }
    private ResponseEntity<StateAndLinks<ChatMessageState>> createUpdateResponse(HttpServletRequest request, ChatMessageState message, HttpStatus status) throws URISyntaxException {
        ResponseEntity<StateAndLinks<ChatMessageState>> response = this.getResponse(request, message, status);
        //this.messagingTemplate.convertAndSend("/topic/vaultChanged/cordalo/template/chatMessage", response.getBody());
        return response;
    }

    /**
     * Returns the node's name.
     */
    @GetMapping(value = BASE_PATH + "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, CordaX500Name> whoami() {
        return ImmutableMap.of("me", myLegalName);
    }

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GetMapping(value = BASE_PATH + "/peers", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, List<CordaX500Name>> getPeers() {
        List<NodeInfo> nodeInfoSnapshot = proxy.networkMapSnapshot();
        return ImmutableMap.of("peers", nodeInfoSnapshot
                .stream()
                .map(node -> node.getLegalIdentities().get(0).getName())
                .filter(name -> !name.equals(myLegalName) && !serviceNames.contains(name.getOrganisation()))
                .collect(toList()));
    }

    /**
     * returns all unconsumed services that exist in the node's vault.
     */
    @GetMapping(value = BASE_PATH + "/services", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<StateAndLinks<ServiceState>>> getServices(
            HttpServletRequest request) throws URISyntaxException {
        List<ServiceState> list = proxy.vaultQuery(ServiceState.class).getStates()
                .stream().map(state -> state.getState().getData()).collect(toList());
        return this.getResponse(request, list, HttpStatus.OK);
    }

    /**
     * receives a unconsumed service with a given ID from the node's vault.
     * @param id unique identifier as UUID for service
     */
    @RequestMapping(
            value = BASE_PATH + "/services/{id}",
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
        List<ServiceState> states = proxy.vaultQueryByCriteria(queryCriteria, ServiceState.class)
                .getStates().stream().map(state -> state.getState().getData()).collect(toList());
        if (states.isEmpty()) {
            return null;
        } else {
            ServiceState service = states.get(states.size()-1);
            return this.getResponse(request, service, HttpStatus.OK);
        }
    }


    /**
     * deletes an unconsumed service with a given ID from the node's vault.
     * @param id unique identifier as UUID for service
     */
    @RequestMapping(
            value = BASE_PATH + "/services/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<StateAndLinks<ServiceState>> deleteServiceById(
            @PathVariable("id") String id) {
        UniqueIdentifier uid = new UniqueIdentifier(null, UUID.fromString(id));
        try {
            final SignedTransaction signedTx = proxy
                    .startTrackedFlowDynamic(ServiceFlow.Delete.class, uid)
                    .getReturnValue()
                    .get();
            //this.messagingTemplate.convertAndSend("/topic/vaultChanged/cordalo/template/service", "");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);


        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new StateAndLinks<ServiceState>().error(ex));
        }

    }

    /**
     * create a new service with given data
     * @param request is the original http request to calculate links in response
     * @param data string contains json data for the service
     * @param serviceName is the name of the service
     * @param price is a possible positiv price for the service
     */
    @RequestMapping(
            value = BASE_PATH + "/services",
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        try {
            final SignedTransaction signedTx = proxy
                    .startTrackedFlowDynamic(ServiceFlow.Create.class,
                            serviceName,
                            data,
                            price)
                    .getReturnValue()
                    .get();

            StateVerifier verifier = StateVerifier.fromTransaction(signedTx, null);
            ServiceState service = verifier.output().one(ServiceState.class).object();
            return this.createUpdateActionResponse(request, service, HttpStatus.CREATED);

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new StateAndLinks<ServiceState>().error(ex));
        }
    }


    /**
     * execute an action on the services give by id
     * @param id identifier of the service
     * @param action name of action to be executed
     */
    @RequestMapping(
            value = BASE_PATH + "/services/{id}/{action}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<StateAndLinks<ServiceState>> serviceAction(
            HttpServletRequest request,
            @RequestParam(value = "service-provider", required = false) String serviceProvider,
            @PathVariable("id") String id,
            @PathVariable("action") String action) {

        StateMachine.State state = StateMachine.StateTransition.valueOf(action).getNextState();
        if (state == null) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body(new StateAndLinks<ServiceState>().error("illegal action <"+action+">. Method not allowed"));
        }
        UniqueIdentifier uid = new UniqueIdentifier(null, UUID.fromString(id));
        try {
            if (state.equals(StateMachine.State.SHARED)) {
                if (serviceProvider == null || serviceProvider.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new StateAndLinks<ServiceState>().error("service-provider not specified in post"));
                }
                Party serviceProviderParty = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(serviceProvider));
                if (serviceProviderParty == null){
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new StateAndLinks<ServiceState>().error("service-provider not a valid peer."));
                }
                final SignedTransaction signedTx = proxy
                        .startTrackedFlowDynamic(ServiceFlow.Share.class,
                                uid,
                                serviceProviderParty)
                        .getReturnValue()
                        .get();

                StateVerifier verifier = StateVerifier.fromTransaction(signedTx, null);
                ServiceState service = verifier.output().one(ServiceState.class).object();
                return this.getResponse(request, service, HttpStatus.OK);

            } else {
                final SignedTransaction signedTx = proxy
                        .startTrackedFlowDynamic(ServiceFlow.Action.class,
                                uid,
                                action)
                        .getReturnValue()
                        .get();

                StateVerifier verifier = StateVerifier.fromTransaction(signedTx, null);
                ServiceState service = verifier.output().one(ServiceState.class).object();
                return this.getResponse(request, service, HttpStatus.OK);
            }
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new StateAndLinks<ServiceState>().error(ex));
        }
    }





    /**
     * create a new chat message with from sender to receiver
     * @param request is the original http request to calculate links in response
     * @param receiver string repesenting a party
     * @param message optional - string message of chat, if empty german joke is choosen
     */
    @RequestMapping(
            value = BASE_PATH + "/messages",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<StateAndLinks<ChatMessageState>> sendMessage(
            HttpServletRequest request,
            @RequestParam(value = "receiver", required = true) String receiver,
            @RequestParam(name = "message", required = false) String message) {
        Party receiverParty = proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(receiver));
        if (receiverParty == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new StateAndLinks<ChatMessageState>().error("receiver not a valid peer."));
        }
        try {
            SignedTransaction signedTx= null;
            if (message != null && !message.isEmpty()) {
                signedTx = proxy
                    .startTrackedFlowDynamic(ChatMessageFlow.Send.class,
                            receiverParty,
                            message)
                    .getReturnValue()
                    .get();
            } else {
                signedTx = proxy
                        .startTrackedFlowDynamic(ChatMessageFlow.Send.class,
                                receiverParty)
                        .getReturnValue()
                        .get();
            }

            StateVerifier verifier = StateVerifier.fromTransaction(signedTx, null);
            ChatMessageState chatMessage = verifier.output().one(ChatMessageState.class).object();
            return this.createUpdateResponse(request, chatMessage, HttpStatus.CREATED);

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                    .body(new StateAndLinks<ChatMessageState>().error(ex));
        }
    }




}