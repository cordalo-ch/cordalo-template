/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.client.webserver;

import ch.cordalo.corda.common.client.webserver.CordaloController;
import ch.cordalo.corda.common.client.webserver.RpcConnection;
import com.google.common.collect.ImmutableMap;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.node.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api/v1/network") // The paths for HTTP requests are relative to this base path.
public class ControllerMe extends CordaloController {

    private final static Logger logger = LoggerFactory.getLogger(ControllerMe.class);

    private final static String MAPPING_PATH = "/api/v1/network";
    private final static String BASE_PATH = "";

    public ControllerMe(RpcConnection rpcConnection) {
        super(rpcConnection);
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
                .filter(x500 -> !x500.equals(this.getMe()) && !x500.equals(this.getNotary()))
                .map(x500 -> x500.getName())
                .collect(toList()));
    }

}