package ch.cordalo.template.contracts;

import ch.cordalo.corda.common.contracts.CommandVerifier;
import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.template.states.CarState;
import ch.cordalo.template.states.E178EventState;
import kotlin.Pair;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CarContract implements Contract {
    public static final String ID = "ch.cordalo.template.contracts.CarContract";

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

        class Update implements CarContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    // validate 1 input and 1 output - simple update for same or different values
                    CommandVerifier.Parameters<CarState> params = new CommandVerifier.Parameters<>();
                    params.notEmpty(
                            CarState::getLinearId,
                            CarState::getMake,
                            CarState::getModel,
                            CarState::getType,
                            CarState::getStammNr,
                            CarState::getCreator,
                            CarState::getOwners
                    );
                    params.equal(
                            CarState::getLinearId,
                            CarState::getStammNr,
                            CarState::getCreator,
                            CarState::getOwners);

                    Pair<CarState, CarState> pair = new CommandVerifier(verifier)
                            .verify_update1(CarState.class, params);
                    return null;
                });
            }
        }


        class Share implements CarContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    // validate 1 input and 1 output - simple update for same or different values
                    CommandVerifier.Parameters<CarState> params = new CommandVerifier.Parameters<>();
                    params.notEmpty(
                            CarState::getLinearId,
                            CarState::getMake,
                            CarState::getModel,
                            CarState::getType,
                            CarState::getStammNr,
                            CarState::getCreator,
                            CarState::getOwners);
                    params.equal(
                            CarState::getLinearId,
                            CarState::getMake,
                            CarState::getModel,
                            CarState::getType,
                            CarState::getStammNr,
                            CarState::getCreator);
                    params.notEqual(
                            CarState::getOwners);

                    Pair<CarState, CarState> pair = new CommandVerifier(verifier)
                            .verify_update1(CarState.class, params);
                    return null;
                });
            }
        }
    }
}
