package ch.cordalo.template.contracts;

import ch.cordalo.template.E178BaseTests;
import ch.cordalo.template.states.E178EventState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static net.corda.testing.node.NodeTestUtils.transaction;

public class E178EventContractTest extends E178BaseTests {

    private final static String STAMM_NR = "123.456.786";
    @Before
    public void setup() {
        this.setup(false);
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    private E178EventState newE178EventState(String stammNr, Party retailer, Party leasing, String state) {
        return E178EventState.request(stammNr, retailer, leasing, state);
    }

    @Test
    public void test_request_fail_retailer_is_leasing() {
        transaction(regulator.ledgerServices, tx -> {
            E178EventState e178EventState = newE178EventState(STAMM_NR, this.retailer.party, this.retailer.party, "ZH");
            tx.output(E178EventContract.ID, e178EventState);
            tx.command(e178EventState.getParticipantKeys(), new E178EventContract.Commands.Request());
            tx.fails();
            return null;
        });
    }

    @Test
    public void test_request_fail_empty_state() {
        transaction(regulator.ledgerServices, tx -> {
            E178EventState e178EventState = newE178EventState(STAMM_NR, this.retailer.party, this.leasing.party, "");
            tx.output(E178EventContract.ID, e178EventState);
            tx.command(e178EventState.getParticipantKeys(), new E178EventContract.Commands.Request());
            tx.fails();
            return null;
        });
    }

    @Test
    public void test_request_OK() {
        transaction(regulator.ledgerServices, tx -> {
            E178EventState e178EventState = newE178EventState(STAMM_NR, this.retailer.party, this.leasing.party, "SG");
            tx.output(E178EventContract.ID, e178EventState);
            tx.command(e178EventState.getParticipantKeys(), new E178EventContract.Commands.Request());
            tx.verifies();
            return null;
        });
    }


    @Test
    public void test_issue_correct() {
        transaction(regulator.ledgerServices, tx -> {
            E178EventState e178EventState = newE178EventState(STAMM_NR, this.retailer.party, this.leasing.party, "SG");
            E178EventState issuedE178 = e178EventState.issue("ZH", this.regulator.party);
            tx.input(E178EventContract.ID, e178EventState);
            tx.output(E178EventContract.ID, issuedE178);
            tx.command(e178EventState.getParticipantKeys(), new E178EventContract.Commands.Issue());
            tx.verifies();
            return null;
        });
    }

}