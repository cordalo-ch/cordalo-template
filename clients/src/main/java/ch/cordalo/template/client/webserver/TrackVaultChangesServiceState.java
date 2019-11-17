package ch.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.RpcConnection;
import ch.cordalo.corda.common.client.webserver.TrackVaultChanges;
import ch.cordalo.template.states.ServiceState;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TrackVaultChangesServiceState extends TrackVaultChanges<ServiceState> {
    public TrackVaultChangesServiceState(RpcConnection rpc) {
        super(rpc, ServiceState.class);
    }

    @PostConstruct
    public void installFeed() {
        this.installVaultFeedAndSubscribeToTopic("/topic/vaultChanged/serviceState");
    }
}
