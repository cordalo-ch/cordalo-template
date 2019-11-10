package com.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.RpcConnection;
import ch.cordalo.corda.common.client.webserver.StateAndLinks;
import net.corda.core.contracts.LinearState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class CordaloController {

    private CordaRPCOps proxy;
    private Party me;

    private final List<Party> notaries;

    private final static Logger logger = LoggerFactory.getLogger(CordaloController.class);

    public CordaloController(RpcConnection rpc) {
        if (rpc.getProxy() == null) {
            this.proxy = null;
            this.me = null;
            this.notaries = Collections.emptyList();
            logger.error("NodeRPC connection + proxy is not initialized (null)");
            return;
        }
        this.proxy = rpc.getProxy();
        this.me = rpc.getProxy().nodeInfo().getLegalIdentities().get(0);
        this.notaries = rpc.getProxy().notaryIdentities();
    }

    public CordaRPCOps getProxy() {
        return proxy;
    }

    public Party getMe() {
        return me;
    }

    public List<Party> getNotaries() {
        return notaries;
    }

    public boolean isValid() {
        return this.getProxy() != null;
    }

    public <T extends LinearState> ResponseEntity<StateAndLinks<T>> buildResponseFromException(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new StateAndLinks<T>().error(message));
    }
    public <T extends LinearState> ResponseEntity<StateAndLinks<T>> buildResponseFromException(HttpStatus status, Throwable exception) {
        return ResponseEntity.status(status)
                .body(new StateAndLinks<T>().error(exception));
    }
    public <T extends LinearState> ResponseEntity<List<StateAndLinks<T>>> buildResponsesFromException(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(Arrays.asList(new StateAndLinks<T>().error(message)));
    }
    public <T extends LinearState> ResponseEntity<List<StateAndLinks<T>>> buildResponsesFromException(HttpStatus status, Throwable exception) {
        return ResponseEntity.status(status)
                .body(Arrays.asList(new StateAndLinks<T>().error(exception)));
    }

    public Party partyFromString(String partyString) {
        return this.getProxy().wellKnownPartyFromX500Name(CordaX500Name.parse(partyString));
    }


}