package com.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.StateAndLinks;
import ch.cordalo.corda.common.client.webserver.StateBuilder;
import ch.cordalo.corda.common.contracts.StateVerifier;
import com.cordalo.template.flows.E178EventFlow;
import com.cordalo.template.states.E178EventState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.messaging.FlowProgressHandle;
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
public class ControllerE178 extends CordaloController {

    private final static Logger logger = LoggerFactory.getLogger(ControllerE178.class);

    private final static String MAPPING_PATH = "/api/v1/cordalo/template";
    private final static String BASE_PATH = "/e178";

    public ControllerE178() {
        super();
    }


    private ResponseEntity<StateAndLinks<E178EventState>> getResponse(HttpServletRequest request, E178EventState e178, HttpStatus status) throws URISyntaxException {
        String[] actions = { "reply" };
        return new StateBuilder<>(e178, ResponseEntity.status(HttpStatus.OK))
                .stateMapping(MAPPING_PATH, BASE_PATH, request)
                .self()
                .links(actions)
                .build();
    }

    private ResponseEntity<List<StateAndLinks<E178EventState>>> getResponses(HttpServletRequest request, List<E178EventState> list, HttpStatus status) throws URISyntaxException {
        String[] actions = { "reply" };
        return new StateBuilder<>(list, ResponseEntity.status(HttpStatus.OK))
                .stateMapping(MAPPING_PATH, BASE_PATH, request)
                .self()
                .links(actions)
                .buildList();
    }
    /**
     * returns all unconsumed e178 that exist in the node's vault.
     */
    @RequestMapping(
            value = BASE_PATH,
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<List<StateAndLinks<E178EventState>>> getE178(
            HttpServletRequest request) throws URISyntaxException {
        List<E178EventState> list = this.getProxy().vaultQuery(E178EventState.class).getStates()
                .stream().map(state -> state.getState().getData()).collect(toList());
        return this.getResponses(request, list, HttpStatus.OK);
    }

    /**
     * create a new e178 with from retail to leasing
     * @param request is the original http request to calculate links in response
     * @param leasing leasing part to issue the e178
     * @param state state of car to be registered at
     */
    @RequestMapping(
            value = BASE_PATH,
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<StateAndLinks<E178EventState>> requestE178(
            HttpServletRequest request,
            @RequestParam(value = "leasing", required = true) String leasing,
            @RequestParam(name = "state", required = false) String state) {
        Party leasingParty = this.partyFromString(leasing);
        if (leasingParty == null){
            return this.buildResponseFromException(HttpStatus.BAD_REQUEST, "leasing not a valid peer.");
        }
        try {
            SignedTransaction signedTx= null;
            if (state != null && !state.isEmpty()) {
                signedTx = this.getProxy()
                        .startTrackedFlowDynamic(E178EventFlow.Request.class,
                                leasingParty, state)
                        .getReturnValue()
                        .get();
            } else {
                signedTx = this.getProxy()
                        .startTrackedFlowDynamic(E178EventFlow.Request.class,
                                leasingParty)
                        .getReturnValue()
                        .get();
            }

            StateVerifier verifier = StateVerifier.fromTransaction(signedTx, null);
            E178EventState e178 = verifier.output().one(E178EventState.class).object();
            return this.getResponse(request, e178, HttpStatus.CREATED);

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return this.buildResponseFromException(HttpStatus.EXPECTATION_FAILED, ex);
        }
    }

    /**
     * receives a unconsumed E178 with a given ID from the node's vault.
     * @param id unique identifier as UUID for service
     */
    @RequestMapping(
            value = BASE_PATH + "/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<StateAndLinks<E178EventState>> getE178(
            HttpServletRequest request,
            @PathVariable("id") String id) throws URISyntaxException {
        UniqueIdentifier uid = new UniqueIdentifier(null, UUID.fromString(id));
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(uid),
                Vault.StateStatus.UNCONSUMED,
                null);
        List<E178EventState> e178list = this.getProxy().vaultQueryByCriteria(queryCriteria, E178EventState.class)
                .getStates().stream().map(state -> state.getState().getData()).collect(toList());
        if (e178list.isEmpty()) {
            return null;
        } else {
            E178EventState e178 = e178list.get(e178list.size()-1);
            return this.getResponse(request, e178, HttpStatus.OK);
        }
    }

    /**
     * deletes an unconsumed e178 with a given ID from the node's vault.
     * @param id unique identifier as UUID for service
     */
    @RequestMapping(
            value = BASE_PATH + "/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<StateAndLinks<E178EventState>> deleteE178(
            @PathVariable("id") String id) {
        UniqueIdentifier uid = new UniqueIdentifier(null, UUID.fromString(id));
        try {
            final SignedTransaction signedTx = this.getProxy()
                    .startTrackedFlowDynamic(E178EventFlow.Delete.class, uid)
                    .getReturnValue()
                    .get();
            //this.messagingTemplate.convertAndSend("/topic/vaultChanged/cordalo/template/service", "");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return this.buildResponseFromException(HttpStatus.EXPECTATION_FAILED, ex);
        }
    }



}