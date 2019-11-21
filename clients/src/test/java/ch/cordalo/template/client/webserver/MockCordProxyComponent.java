package ch.cordalo.template.client.webserver;

import ch.cordalo.corda.common.test.CordaTestNetwork;
import ch.cordalo.corda.common.test.MockCordaProxy;
import ch.cordalo.corda.ext.CordaProxy;
import org.junit.Test;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class MockCordProxyComponent extends CordaloControllerBaseTests {

    private CordaProxy mockProxy;

    public MockCordProxyComponent() {
    }
    @PostConstruct
    public void initialiseNodeRPCConnection() {
        CordaTestNetwork setup = this.setup(false);
        this.mockProxy = new MockCordaProxy(setup.getEnv("Company-A")).register();
    }

    @Test
    public void noTestJustInitialization() {

    }

}
