package com.cordalo.template.client.webserver;

import com.cordalo.template.states.ChatMessageState;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ChatMessageScheduler extends TrackVaultChanges<ChatMessageState> {
    public ChatMessageScheduler(RpcConnection rpc) {
        super(rpc, ChatMessageState.class);
    }

    @PostConstruct
    public void installFeed() {
        this.installVaultFeedAndSubscribeToTopic("/topic/vaultChanged/chatMessage");
    }
}
