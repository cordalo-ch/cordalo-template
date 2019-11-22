package ch.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.RpcConnection;
import ch.cordalo.corda.common.client.webserver.TrackVaultChanges;
import ch.cordalo.template.states.CarState;
import ch.cordalo.template.states.E178EventState;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TrackVaultChangesCar extends TrackVaultChanges<CarState> {
    public TrackVaultChangesCar(RpcConnection rpc) {
        super(rpc, CarState.class);
    }

    @PostConstruct
    public void installFeed() {
        this.installVaultFeedAndSubscribeToTopic("/topic/vaultChanged/car");
    }
}
