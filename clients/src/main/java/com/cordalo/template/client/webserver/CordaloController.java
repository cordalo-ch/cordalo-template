package com.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.CordaRpcProxy;
import ch.cordalo.corda.common.client.webserver.RpcConnection;
import ch.cordalo.corda.common.client.webserver.StateAndLinks;
import ch.cordalo.corda.ext.CordaProxy;
import net.corda.core.contracts.LinearState;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class CordaloController {

    private final CordaProxy rpc;

    @Autowired
    private RpcConnection rpcConnection;

    private final static Logger logger = LoggerFactory.getLogger(CordaloController.class);

    public CordaloController() {
        if (CordaProxy.getInstance() == null) {
            CordaProxy.register(new CordaRpcProxy(rpcConnection));
            this.rpc = CordaProxy.getInstance();
        } else {
            this.rpc = CordaProxy.getInstance();
        }
        if (rpc == null || !rpc.isValid()) {
            logger.error("NodeRPC connection + proxy is not initialized (null)");
            return;
        }
    }

    public CordaRPCOps getProxy() {
        return this.rpc.getProxy();
    }

    public Party getMe() { return this.rpc.getMe(); }

    public Party getNotary() {  return this.rpc.getNotary(); }

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