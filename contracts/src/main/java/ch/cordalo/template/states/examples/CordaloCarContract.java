package ch.cordalo.template.states.examples;

import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.template.contracts.CarContract;
import ch.cordalo.template.states.CarState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CordaloCarContract implements Contract {
    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, CarContract.Commands.class);
        CarContract.Commands commandData = (CarContract.Commands) verifier.command();
        commandData.verify(tx, verifier);
    }

    public interface Commands extends CommandData {
        void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;

        class Create implements CarContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().empty("No inputs should be consumed when create a car");
                    OriginalCarState outputState = verifier.output()
                            .one().one(OriginalCarState.class)
                            .isEmpty(CarState::getMake, "A newly created car must have a valid make and is not empty.")
                            .isNotEqual(OriginalCarState::getCreator, OriginalCarState::getRegulator, "The creator and regulator cannot have the same identity")
                            .participantsAreSigner("Both owner and regulator together only may sign car creation transaction")
                            .object();
                    return null;
                });
            }
        }

        class Update implements CarContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                //
            }
        }
    }
}
