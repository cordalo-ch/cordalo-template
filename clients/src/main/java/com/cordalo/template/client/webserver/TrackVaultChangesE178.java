package com.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.RpcConnection;
import ch.cordalo.corda.common.client.webserver.TrackVaultChanges;
import com.cordalo.template.states.ChatMessageState;
import com.cordalo.template.states.E178EventState;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TrackVaultChangesE178 extends TrackVaultChanges<E178EventState> {
    public TrackVaultChangesE178(RpcConnection rpc) {
        super(rpc, E178EventState.class);
    }

    @PostConstruct
    public void installFeed() {
        this.installVaultFeedAndSubscribeToTopic("/topic/vaultChanged/e178");
    }
}