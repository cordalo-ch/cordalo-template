package com.cordalo.template.contracts;

import ch.cordalo.corda.common.contracts.StateVerifier;
import com.cordalo.template.states.E178EventState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class E178EventContract implements Contract {
    public static final String ID = "com.cordalo.template.contracts.E178EventContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, E178EventContract.Commands.class);
        E178EventContract.Commands commandData = (E178EventContract.Commands)verifier.command();
        commandData.verify(tx, verifier);
    }

    public interface Commands extends CommandData {
        void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;

        // Retail --> +Leasing
        class Request implements E178EventContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().empty("input must be empty");
                    E178EventState message = verifier
                            .output()
                            .one()
                            .one(E178EventState.class)
                            .isNotEmpty(E178EventState::getLinearId, "id must be provided")
                            .isNotEmpty(E178EventState::getRetailer, "retailer must be provided")
                            .isNotEmpty(E178EventState::getLeasing, "leasing must be provided")
                            .isNotEmpty(E178EventState::getState, "state cannot be empty")
                            .isNotEmpty(E178EventState::getStatus, "status cannot be empty")
                            .isNotEqual(E178EventState::getRetailer, E178EventState::getLeasing, "retailer can not be same than leasing")
                            .object();
                    req.using("state must be REQUESTED", message.getStatus().equals(E178EventState.E178StatusType.REQUESTED));
                    return null;
                });
            }
        }

        // Leasing  --> +Regulator
        class Issue implements E178EventContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().notEmpty("input must not be empty");
                    E178EventState message = verifier
                            .output()
                            .one()
                            .one(E178EventState.class)
                            .isNotEmpty(E178EventState::getLinearId, "id must be provided")
                            .isNotEmpty(E178EventState::getLeasing, "leasing must be provided")
                            .isNotEmpty(E178EventState::getRegulator, "regulator must be provided")
                            .isNotEmpty(E178EventState::getState, "state cannot be empty")
                            .isNotEmpty(E178EventState::getStatus, "status cannot be empty")
                            // TODO check status
//                            .isEqual(E178EventState::getStatus, E178EventState.E178StatusType.I)
                            .isNotEqual(E178EventState::getLeasing, E178EventState::getRegulator, "leasing can not be regulator")
                            .object();
                    return null;
                });
            }
        }

        class Delete implements E178EventContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.output().empty("output must be empty");
                    E178EventState e178 = verifier
                            .input()
                            .one()
                            .one(E178EventState.class)
                            .isNotEmpty(E178EventState::getLinearId, "id must be provided")
                            .object();
                    return null;
                });
            }
        }


    }
}
