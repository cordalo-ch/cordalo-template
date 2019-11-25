package ch.cordalo.template.contracts;

import ch.cordalo.corda.common.contracts.StateVerifier;
import ch.cordalo.template.states.ChatMessageState;
import ch.cordalo.template.states.ServiceState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class ChatMessageContract implements Contract {
    public static final String ID = "ch.cordalo.template.contracts.ChatMessageContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        StateVerifier verifier = StateVerifier.fromTransaction(tx, ChatMessageContract.Commands.class);
        ChatMessageContract.Commands commandData = (ChatMessageContract.Commands)verifier.command();
        commandData.verify(tx, verifier);
    }

    public interface Commands extends CommandData {
        public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException;

        /*
        @CordaSerializable
        public class Reference extends ReferenceContract.Commands.Reference<ChatMessageState> implements ChatMessageContract.Commands {
            public Reference(ChatMessageState myState) {
                super(myState);
            }

            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                this.verify(tx);
            }
        }
        */

        class Send implements ChatMessageContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.input().empty("input must be empty");
                    ChatMessageState message = verifier
                            .output()
                            .one()
                            .one(ChatMessageState.class)
                            .isNotEmpty(ChatMessageState::getLinearId, "id must be provided")
                            .isNotEmpty(ChatMessageState::getSender, "sender must be provided")
                            .isNotEmpty(ChatMessageState::getReceiver, "receiver must be provided")
                            .isNotEmpty(ChatMessageState::getMessage, "message cannot be empty")
                            .isNotEqual(ChatMessageState::getSender, ChatMessageState::getReceiver, "selfie-messages are not allowed")
                            .object();
                    return null;
                });
            }
        }

        class Reply implements ChatMessageContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier
                            .input()
                            .one()
                            .one(ChatMessageState.class);
                    verifier
                            .output()
                            .count(2);

                    ChatMessageState refMessage = verifier
                            .intersection(ChatMessageState.class)
                            .one("1 input and 1 output the same - used as a reference")
                            .one(ChatMessageState.class)
                            .object();

                    ChatMessageState reply = verifier
                            .output()
                            .count(2)
                            .newOutput(ChatMessageState.class)
                            .one()
                            .one(ChatMessageState.class)
                            .isNotEmpty(ChatMessageState::getLinearId, "id must be provided")
                            .isNotEmpty(ChatMessageState::getSender, "sender must be provided")
                            .isNotEmpty(ChatMessageState::getReceiver, "receiver must be provided")
                            .isNotEmpty(ChatMessageState::getMessage, "message cannot be empty")
                            .isNotEqual(ChatMessageState::getSender, ChatMessageState::getReceiver, "selfie-messages are not allowed")
                            .object();
                    req.using("receiver must be sender", reply.getReceiver().equals(refMessage.getSender()));
                    req.using("sender must be receiver", reply.getSender().equals(refMessage.getReceiver()));
                    req.using("reply base id must be id from input", reply.getBaseMessageId().equals(refMessage.getLinearId()));
                    return null;
                });
            }
        }


        class Delete implements ChatMessageContract.Commands {
            @Override
            public void verify(LedgerTransaction tx, StateVerifier verifier) throws IllegalArgumentException {
                requireThat(req -> {
                    verifier.output().empty("output must be empty");
                    ChatMessageState message = verifier
                            .input()
                            .one()
                            .one(ChatMessageState.class)
                            .isNotEmpty(ChatMessageState::getLinearId, "id must be provided")
                            .isNotEmpty(ChatMessageState::getSender, "sender must be provided")
                            .isNotEmpty(ChatMessageState::getReceiver, "receiver must be provided")
                            .isNotEmpty(ChatMessageState::getMessage, "message cannot be empty")
                            .object();
                    return null;
                });
            }
        }


    }
}
