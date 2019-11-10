package com.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.RpcConnection;
import com.cordalo.template.contracts.StateMachine;
import com.google.common.collect.ImmutableMap;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/network") // The paths for HTTP requests are relative to this base path.
public class ControllerMe extends CordaloController {

    private final static Logger logger = LoggerFactory.getLogger(ControllerMe.class);

    private final static String MAPPING_PATH = "/api/v1/network";
    private final static String BASE_PATH = "";

    public ControllerMe(RpcConnection rpc) {
        super(rpc);
        StateMachine.State.values();
        StateMachine.StateTransition.values();
    }

    /**
     * Returns the node's name.
     */
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, CordaX500Name> whoami() {
        return ImmutableMap.of("me", this.getMe().getName());
    }

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService] except me and except Notaries
     */
    @GetMapping(value = "/peers", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, List<CordaX500Name>> getPeers() {
        List<NodeInfo> nodeInfoSnapshot = this.getProxy().networkMapSnapshot();
        return ImmutableMap.of("peers", nodeInfoSnapshot
                .stream()
                .map(node -> node.getLegalIdentities().get(0))
                .filter(x500 -> !x500.equals(this.getMe()) && !this.getNotaries().contains(x500))
                .map(x500 -> x500.getName())
                .collect(toList()));
    }

}