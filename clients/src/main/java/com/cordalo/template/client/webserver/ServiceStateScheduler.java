package com.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.NodeRPCConnection;
import com.cordalo.template.states.ServiceState;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ServiceStateScheduler extends TrackVaultChanges<ServiceState> {
    public ServiceStateScheduler(NodeRPCConnection rpc) {
        super(rpc, ServiceState.class);
    }

    @PostConstruct
    public void installFeed() {
        this.installVaultFeedAndSubscribeToTopic("/topic/vaultChanged/cordalo/template/serviceState");
    }
}
