package com.cordalo.template.contracts;

import com.cordalo.template.E178BaseTests;
import com.cordalo.template.states.E178EventState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class E178EventContractTest extends E178BaseTests {
    @Before
    public void setup() {
        this.setup(false);
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    private E178EventState newE178EventState(Party regulator, Party retailer, Party leasing, Party insurer) {
        return new E178EventState(
                new UniqueIdentifier(),
                regulator,
                retailer,
                leasing,
                insurer,
                "ZH",
                E178EventState.E178StatusType.INITIAL);
    }

    @Test
    public void test_request_fail_retailer_is_leasing() {
        transaction(regulator.ledgerServices, tx -> {
            Party leasingParty = this.retailer.party; // will fail

            E178EventState e178EventState = newE178EventState(this.regulator.party, this.retailer.party, leasingParty, this.insurer.party);
            tx.output(E178EventContract.ID, e178EventState);
            tx.command(e178EventState.getParticipantKeys(), new E178EventContract.Commands.Request());
            tx.fails();
            return null;
        });
    }

}