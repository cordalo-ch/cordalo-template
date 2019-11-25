package ch.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.CordaloController;
import ch.cordalo.corda.common.client.webserver.RpcConnection;
import ch.cordalo.corda.common.client.webserver.StateAndLinks;
import ch.cordalo.corda.common.client.webserver.StateBuilder;
import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.template.flows.ChatMessageFlow;
import ch.cordalo.template.states.ChatMessageState;
import ch.cordalo.template.states.ServiceState;
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
public class ControllerMessages extends CordaloController {

    private final static Logger logger = LoggerFactory.getLogger(ControllerMessages.class);

    private final static String MAPPING_PATH = "/api/v1/cordalo/template";
    private final static String BASE_PATH = "/messages";

    public ControllerMessages(RpcConnection rpcConnection) {
        super(rpcConnection);
    }


    private ResponseEntity<StateAndLinks<ChatMessageState>> getResponse(HttpServletRequest request, ChatMessageState message, HttpStatus status) throws URISyntaxException {
        String[] actions = {"reply"};
        return new StateBuilder<>(message, ResponseEntity.status(HttpStatus.OK))
                .stateMapping(MAPPING_PATH, BASE_PATH, request)
                .self()
                .links(actions)
                .build();
    }

    private ResponseEntity<List<StateAndLinks<ChatMessageState>>> getResponses(HttpServletRequest request, List<ChatMessageState> list, HttpStatus status) throws URISyntaxException {
        String[] actions = {"reply"};
        return new StateBuilder<>(list, ResponseEntity.status(HttpStatus.OK))
                .stateMapping(MAPPING_PATH, BASE_PATH, request)
                .self()
                .links(actions)
                .buildList();
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
    public ResponseEntity<List<StateAndLinks<ChatMessageState>>> getMessages(
            HttpServletRequest request) throws URISyntaxException {
        List<ChatMessageState> list = this.getProxy().vaultQuery(ChatMessageState.class).getStates()
                .stream().map(state -> state.getState().getData()).collect(toList());
        return this.getResponses(request, list, HttpStatus.OK);
    }

    /**
     * create a new chat message with from sender to receiver
     *
     * @param request is the original http request to calculate links in response
     * @param to      string repesenting a party
     * @param message optional - string message of chat, if empty german joke is choosen
     */
    @RequestMapping(
            value = BASE_PATH,
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<StateAndLinks<ChatMessageState>> sendMessage(
            HttpServletRequest request,
            @RequestParam(value = "to", required = true) String to,
            @RequestParam(name = "message", required = false) String message) {
        Party receiverParty = this.partyFromString(to);
        if (receiverParty == null) {
            return this.buildResponseFromException(HttpStatus.BAD_REQUEST, "receiver not a valid peer.");
        }
        try {
            SignedTransaction signedTx = null;
            if (message != null && !message.isEmpty()) {
                FlowProgressHandle<SignedTransaction> tFlowProgressHandle = this.getProxy()
                        .startTrackedFlowDynamic(ChatMessageFlow.Send.class,
                                receiverParty,
                                message);
                signedTx = tFlowProgressHandle.getReturnValue().get();
            } else {
                signedTx = this.getProxy()
                        .startTrackedFlowDynamic(ChatMessageFlow.Send.class,
                                receiverParty)
                        .getReturnValue()
                        .get();
            }

            StateVerifier verifier = StateVerifier.fromTransaction(signedTx, null);
            ChatMessageState chatMessage = verifier.output().one(ChatMessageState.class).object();
            return this.getResponse(request, chatMessage, HttpStatus.CREATED);

        } catch (Throwable ex) {
            logger.error(ex.getMessage(), ex);
            return this.buildResponseFromException(HttpStatus.EXPECTATION_FAILED, ex);
        }
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
    public ResponseEntity<StateAndLinks<ChatMessageState>> getMessage(
            HttpServletRequest request,
            @PathVariable("id") String id) throws URISyntaxException {
        UniqueIdentifier uid = new UniqueIdentifier(null, UUID.fromString(id));
        QueryCriteria queryCriteria = new QueryCriteria.LinearStateQueryCriteria(
                null,
                Collections.singletonList(uid),
                Vault.StateStatus.UNCONSUMED,
                null);
        List<ChatMessageState> messages = this.getProxy().vaultQueryByCriteria(queryCriteria, ChatMessageState.class)
                .getStates().stream().map(state -> state.getState().getData()).collect(toList());
        if (messages.isEmpty()) {
            return null;
        } else {
            ChatMessageState message = messages.get(messages.size() - 1);
            return this.getResponse(request, message, HttpStatus.OK);
        }
    }


    /**
     * deletes an unconsumed message with a given ID from the node's vault.
     *
     * @param id unique identifier as UUID for service
     */
    @RequestMapping(
            value = BASE_PATH + "/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<StateAndLinks<ServiceState>> deleteMessageById(
            @PathVariable("id") String id) {
        UniqueIdentifier uid = new UniqueIdentifier(null, UUID.fromString(id));
        try {
            final SignedTransaction signedTx = this.getProxy()
                    .startTrackedFlowDynamic(ChatMessageFlow.Delete.class, uid)
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