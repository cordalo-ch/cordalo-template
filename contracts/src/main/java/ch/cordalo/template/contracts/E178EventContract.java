package ch.cordalo.template.contracts;

import ch.cordalo.corda.common.contracts.CommandVerifier;
import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.template.states.E178EventState;
import kotlin.Pair;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class E178EventContract implements Contract {
    public static final String ID = "ch.cordalo.template.contracts.E178EventContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, E178EventContract.Commands.class);
        E178EventContract.Commands commandData = (E178EventContract.Commands) verifier.command();
        commandData.verify(tx, verifier);
    }

    public interface Commands extends CommandData {
        void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;

        // Retail --> provider retailer, leasing, state
        //          status = requested
        class Request implements E178EventContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().empty("input must be empty");
                    E178EventState e178 = verifier
                            .output()
                            .one()
                            .one(E178EventState.class)
                            .isNotEmpty(E178EventState::getLinearId, "id must be provided")
                            .isNotEmpty(E178EventState::getStammNr, "stammNr must be provided")
                            .isNotEmpty(E178EventState::getRetailer, "retailer must be provided")
                            .isNotEmpty(E178EventState::getLeasing, "leasing must be provided")
                            .isNotEmpty(E178EventState::getState, "state cannot be empty")
                            .isNotEmpty(E178EventState::getStatus, "status cannot be empty")
                            .isNotEqual(E178EventState::getRetailer, E178EventState::getLeasing, "retailer can not be same than leasing")
                            .isEqual(E178EventState::getStatusObject, x -> E178StateMachine.State("REQUESTED"), "state must be requested")
                            .object();
                    return null;
                });
            }
        }

        // Leasing  --> update same e178
        //      add Regulator, status must be updated to issued
        class Issue implements E178EventContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    E178EventState requestedE178 = verifier
                            .input()
                            .one()
                            .one(E178EventState.class)
                            .isEqual(E178EventState::getStatusObject, x -> E178StateMachine.State("REQUESTED"))
                            .object();
                    E178EventState e178 = verifier
                            .output()
                            .one()
                            .one(E178EventState.class)
                            .isNotEqual(E178EventState::getLeasing, E178EventState::getRegulator, "leasing can not be regulator")
                            .isNotEqual(E178EventState::getRetailer, E178EventState::getRegulator, "retailer can not be regulator")
                            .isEqual(E178EventState::getStatusObject, x -> E178StateMachine.State("ISSUED"))
                            .object();

                    // validate 1 input and 1 output - simple update for same or different values
                    CommandVerifier.Parameters<E178EventState> params = new CommandVerifier.Parameters<>();
                    params.notEmpty(
                            E178EventState::getLinearId,
                            E178EventState::getStammNr,
                            E178EventState::getLeasing,
                            E178EventState::getRetailer,
                            E178EventState::getState);
                    params.equal(
                            E178EventState::getLinearId,
                            E178EventState::getStammNr,
                            E178EventState::getLeasing,
                            E178EventState::getRetailer);
                    params.notEqual(
                            E178EventState::getStatus);

                    Pair<E178EventState, E178EventState> pair = new CommandVerifier(verifier)
                            .verify_update1(E178EventState.class, params);

                    return null;
                });
            }
        }


        // retail  --> update same e178 and requests insurance
        //      add insurance
        //      set status to insurance-requested
        class RequestInsurance implements E178EventContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    E178EventState requestedE178 = verifier
                            .input()
                            .one()
                            .one(E178EventState.class)
                            .isEqual(E178EventState::getStatusObject, x -> E178StateMachine.State("ISSUED"))
                            .object();
                    E178EventState e178 = verifier
                            .output()
                            .one()
                            .one(E178EventState.class)
                            .isNotEqual(E178EventState::getLeasing, E178EventState::getInsurer, "leasing can not be insurer")
                            .isNotEqual(E178EventState::getRetailer, E178EventState::getInsurer, "retailer can not be insurer")
                            .isNotEqual(E178EventState::getRegulator, E178EventState::getInsurer, "retailer can not be insurer")
                            .isEqual(E178EventState::getStatusObject, x -> E178StateMachine.State("INSURANCE_REQUESTED"))
                            .object();

                    // validate 1 input and 1 output - simple update for same or different values
                    CommandVerifier.Parameters<E178EventState> params = new CommandVerifier.Parameters<>();
                    params.notEmpty(
                            E178EventState::getLinearId,
                            E178EventState::getStammNr,
                            E178EventState::getLeasing,
                            E178EventState::getRetailer,
                            E178EventState::getRegulator,
                            E178EventState::getState);
                    params.equal(
                            E178EventState::getLinearId,
                            E178EventState::getStammNr,
                            E178EventState::getLeasing,
                            E178EventState::getRetailer,
                            E178EventState::getRegulator);
                    params.notEqual(
                            E178EventState::getStatus);

                    Pair<E178EventState, E178EventState> pair = new CommandVerifier(verifier)
                            .verify_update1(E178EventState.class, params);

                    return null;
                });
            }
        }


        // insurer  --> update same e178
        //      set status to insurance-requested
        class Insure implements E178EventContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    E178EventState requestedE178 = verifier
                            .input()
                            .one()
                            .one(E178EventState.class)
                            .isEqual(E178EventState::getStatusObject, x -> E178StateMachine.State("INSURANCE_REQUESTED"))
                            .object();
                    E178EventState e178 = verifier
                            .output()
                            .one()
                            .one(E178EventState.class)
                            .isEqual(E178EventState::getStatusObject, x -> E178StateMachine.State("INSURED"))
                            .object();

                    // validate 1 input and 1 output - simple update for same or different values
                    CommandVerifier.Parameters<E178EventState> params = new CommandVerifier.Parameters<>();
                    params.notEmpty(
                            E178EventState::getLinearId,
                            E178EventState::getStammNr,
                            E178EventState::getLeasing,
                            E178EventState::getRetailer,
                            E178EventState::getRegulator,
                            E178EventState::getInsurer,
                            E178EventState::getState);
                    params.equal(
                            E178EventState::getLinearId,
                            E178EventState::getStammNr,
                            E178EventState::getLeasing,
                            E178EventState::getRetailer,
                            E178EventState::getRegulator,
                            E178EventState::getInsurer);
                    params.notEqual(
                            E178EventState::getStatus);

                    Pair<E178EventState, E178EventState> pair = new CommandVerifier(verifier)
                            .verify_update1(E178EventState.class, params);

                    return null;
                });
            }
        }


        // regulator  --> update same e178
        //      set status to registered
        class Register implements E178EventContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    E178EventState requestedE178 = verifier
                            .input()
                            .one()
                            .one(E178EventState.class)
                            .isEqual(E178EventState::getStatusObject, x -> E178StateMachine.State("INSURED"))
                            .object();
                    E178EventState e178 = verifier
                            .output()
                            .one()
                            .one(E178EventState.class)
                            .isEqual(E178EventState::getStatusObject, x -> E178StateMachine.State("REGISTERED"))
                            .object();

                    // validate 1 input and 1 output - simple update for same or different values
                    CommandVerifier.Parameters<E178EventState> params = new CommandVerifier.Parameters<>();
                    params.notEmpty(
                            E178EventState::getLinearId,
                            E178EventState::getStammNr,
                            E178EventState::getLeasing,
                            E178EventState::getRetailer,
                            E178EventState::getRegulator,
                            E178EventState::getInsurer,
                            E178EventState::getState);
                    params.equal(
                            E178EventState::getLinearId,
                            E178EventState::getStammNr,
                            E178EventState::getLeasing,
                            E178EventState::getRetailer,
                            E178EventState::getRegulator,
                            E178EventState::getInsurer);
                    params.notEqual(
                            E178EventState::getStatus);

                    Pair<E178EventState, E178EventState> pair = new CommandVerifier(verifier)
                            .verify_update1(E178EventState.class, params);

                    return null;
                });
            }
        }


        // any  --> update same e178
        //      set status to canceled
        class Cancel implements E178EventContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    E178EventState requestedE178 = verifier
                            .input()
                            .one()
                            .one(E178EventState.class)
                            .isNotEqual(E178EventState::getStatusObject, x -> E178StateMachine.State("CANCELED"))
                            .object();
                    E178EventState e178 = verifier
                            .output()
                            .one()
                            .one(E178EventState.class)
                            .isEqual(E178EventState::getStatusObject, x -> E178StateMachine.State("CANCELED"))
                            .object();

                    // validate 1 input and 1 output - simple update for same or different values
                    CommandVerifier.Parameters<E178EventState> params = new CommandVerifier.Parameters<>();
                    params.notEmpty(
                            E178EventState::getLinearId,
                            E178EventState::getStammNr,
                            E178EventState::getLeasing,
                            E178EventState::getRetailer,
                            E178EventState::getState);
                    params.equal(
                            E178EventState::getLinearId,
                            E178EventState::getStammNr,
                            E178EventState::getLeasing,
                            E178EventState::getRetailer);
                    params.notEqual(
                            E178EventState::getStatus);

                    Pair<E178EventState, E178EventState> pair = new CommandVerifier(verifier)
                            .verify_update1(E178EventState.class, params);

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
                            .one(E178EventState.class)
                            .isNotEmpty(E178EventState::getLinearId, "id must be provided")
                            .object();
                    return null;
                });
            }
        }


    }
}
