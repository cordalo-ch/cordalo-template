package com.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.NodeRPCConnection;
import com.cordalo.template.states.ChatMessageState;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ChatMessageScheduler extends TrackVaultChanges<ChatMessageState> {
    public ChatMessageScheduler(NodeRPCConnection rpc) {
        super(rpc, ChatMessageState.class);
    }

    @PostConstruct
    public void installFeed() {
        this.installVaultFeedAndSubscribeToTopic("/topic/vaultChanged/cordalo/template/chatMessage");
    }
}
