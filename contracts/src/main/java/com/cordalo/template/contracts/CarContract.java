package com.cordalo.template.contracts;

import ch.cordalo.corda.common.contracts.StateVerifier;
import com.cordalo.template.states.CarState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CarContract implements Contract {
    public static final String ID = "com.cordalo.template.contracts.CarContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, CarContract.Commands.class);
        CarContract.Commands commandData = (CarContract.Commands)verifier.command();
        commandData.verify(tx, verifier);
    }

    public interface Commands extends CommandData {
        void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;

        class Create implements CarContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().empty("input must be empty");
                    CarState car = verifier
                            .output()
                            .one()
                            .one(CarState.class)
                            .isNotEmpty(CarState::getLinearId, "linearId must be provided")
                            .isNotEmpty(CarState::getMake, "make must be provided")
                            .isNotEmpty(CarState::getModel, "model must be provided")
                            .isNotEmpty(CarState::getType, "type must be provided")
                            .isNotEmpty(CarState::getStammNr, "stammN must be provided")
                            .isNotEmpty(CarState::getCreator, "creator must be provided")
                            .isNotEmpty(CarState::getOwners, "owners must be provided")
                            .object();
                    return null;
                });
            }
        }

    }
}
