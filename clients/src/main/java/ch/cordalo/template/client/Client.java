/*
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ch.cordalo.template.client;

import net.corda.client.rpc.CordaRPCClient;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.NodeInfo;
import net.corda.core.utilities.NetworkHostAndPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static net.corda.core.utilities.NetworkHostAndPort.parse;

/**
 * Connects to a Corda node via RPC and performs RPC operations on the node.
 * <p>
 * The RPC connection is configured using command line arguments.
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public static void main(String[] args) {
        // Create an RPC connection to the node.
        if (args.length != 3)
            throw new IllegalArgumentException("Usage: Client <node address> <rpc username> <rpc password>");
        final NetworkHostAndPort nodeAddress = parse(args[0]);
        final String rpcUsername = args[1];
        final String rpcPassword = args[2];
        final CordaRPCClient client = new CordaRPCClient(nodeAddress);
        final CordaRPCOps proxy = client.start(rpcUsername, rpcPassword).getProxy();

        // Interact with the node.
        // For example, here we print the nodes on the network.
        final List<NodeInfo> nodes = proxy.networkMapSnapshot();
        logger.info("{}", nodes);
    }
}